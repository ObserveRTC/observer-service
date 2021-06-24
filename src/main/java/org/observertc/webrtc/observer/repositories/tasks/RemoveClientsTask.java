package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.entities.ClientEntity;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class RemoveClientsTask extends ChainedTask<Map<UUID, ClientEntity>> {

    private static final Logger logger = LoggerFactory.getLogger(RemoveClientsTask.class);

    private Set<UUID> clientIds = new HashSet<>();

    private Map<UUID, ClientDTO> removedClientDTOs = new HashMap<>();

    private Map<UUID, Collection<UUID>> removedClientPeerConnectionIds = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    RemovePeerConnectionsTask removePeerConnectionsTask;

    @Inject
    WeakLockProvider weakLockProvider;

    @PostConstruct
    void setup() {
        new Builder<Map<UUID, ClientEntity>>(this)
                .<Set<UUID>, Set<UUID>>addSupplierEntry("Merge Inputs",
                        () -> this.clientIds,
                        receivedClientIds -> {
                            this.clientIds.addAll(receivedClientIds);
                            return this.clientIds;
                        }
                )
                .<Set<UUID>>addBreakCondition((clientIds, resultHolder) -> {
                    if (Objects.isNull(clientIds)) {
                        this.getLogger().warn("No ClientId have been passed");
                        resultHolder.set(Collections.EMPTY_MAP);
                        return true;
                    }
                    if (clientIds.size() < 1) {
                        this.getLogger().warn("No ClientId have been passed");
                        resultHolder.set(Collections.EMPTY_MAP);
                        return true;
                    }
                    return false;
                })
                .<Set<UUID>, Map<UUID, ClientEntity.Builder>> addFunctionalStage("Remove Client DTOs and Call bindings",
                        clientIds -> {
                            Map<UUID, ClientEntity.Builder> clientEntityBuilders = new HashMap<>();
                            for (UUID clientId : clientIds) {
                                // only those, which has not been already removed
                                if (this.removedClientDTOs.containsKey(clientId)) {
                                    continue;
                                }
                                ClientDTO clientDTO = this.hazelcastMaps.getClients().remove(clientId);
                                if (Objects.isNull(clientDTO)) {
                                    logger.warn("Cannot retrieve clientDTO for clientId: {}", clientId);
                                    continue;
                                }
                                this.removedClientDTOs.put(clientId, clientDTO);
                                this.hazelcastMaps.getCallToClientIds().remove(clientDTO.callId, clientId);
                                var clientEntityBuilder = ClientEntity.builder().withClientDTO(clientDTO);
                                clientEntityBuilders.put(clientId, clientEntityBuilder);
                            }
                            return clientEntityBuilders;
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            if (this.removedClientDTOs.size() < 1) {
                                return;
                            }
                            this.hazelcastMaps.getClients().putAll(this.removedClientDTOs);
                            this.removedClientDTOs.forEach((clientId, clientDTO) -> {
                                this.hazelcastMaps.getCallToClientIds().put(clientDTO.callId, clientId);
                            });
                        })
                .<Map<UUID, ClientEntity.Builder>, Map<UUID, ClientEntity.Builder>> addFunctionalStage("Fetch Client Peer Connection Ids",
                        // action
                        clientEntityBuilders -> {
                            Set<UUID> peerConnectionIds = clientEntityBuilders.keySet();
                            clientEntityBuilders.forEach((clientId, clientEntityBuilder) -> {
                                Collection<UUID> clientPeerConnectionIds = this.hazelcastMaps.getClientToPeerConnectionIds().remove(clientId);
                                this.removedClientPeerConnectionIds.put(clientId, clientPeerConnectionIds);
                                peerConnectionIds.addAll(clientPeerConnectionIds);
                            });
                            return clientEntityBuilders;
                        },
                        // rollback
                        (clientEntitiesHolder, thrownException) -> {
                            if (Objects.isNull(clientEntitiesHolder) || Objects.isNull(clientEntitiesHolder.get())) {
                                this.getLogger().warn("Unexpected condition at rollback. callEntities are null");
                                return;
                            }
                            this.removedClientPeerConnectionIds.forEach((clientId, peerConnectionIds) -> {
                                peerConnectionIds.forEach(peerConnectionId -> {
                                    this.hazelcastMaps.getClientToPeerConnectionIds().put(clientId, peerConnectionId);
                                });
                            });
                        })
                .<Map<UUID, ClientEntity.Builder>, Map<UUID, ClientEntity.Builder>> addFunctionalStage("Remove Client Peer Connections",
                        // action
                        clientEntityBuilders -> {
                            Set<UUID> peerConnectionIds = this.removedClientPeerConnectionIds.values().stream().flatMap(c -> c.stream()).collect(Collectors.toSet());
                            this.removePeerConnectionsTask.wherePeerConnectionIds(peerConnectionIds);
                            if (!this.removePeerConnectionsTask.execute().succeeded()) {
                                throw new RuntimeException("Cannot execute action remove client, because remove peer connection has failed");
                            }
                            Map<UUID, PeerConnectionEntity> removedPeerConnectionEntities = this.removePeerConnectionsTask.getResult();
                            this.removedClientPeerConnectionIds.forEach((clientId, clientPeerConnectionIds) -> {
                                if (Objects.isNull(clientPeerConnectionIds)) {
                                    return;
                                }
                                for (UUID clientPeerConnectionId : clientPeerConnectionIds) {
                                    PeerConnectionEntity removedPeerConnectionEntity = removedPeerConnectionEntities.get(clientPeerConnectionId);
                                    if (Objects.isNull(removedPeerConnectionEntity)) {
                                        // TODO: notify inconsistency
                                        continue;
                                    }
                                    var clientEntityBuilder = clientEntityBuilders.get(clientId);
                                    if (Objects.isNull(clientEntityBuilder)) {
                                        // TODO: notify inconsistency
                                        continue;
                                    }
                                    clientEntityBuilder.withPeerConnectionEntity(removedPeerConnectionEntity);
                                }
                            });
                            return clientEntityBuilders;
                        }
                        // no need rollback, because the task roll back itself inside the action
                        )
                .<Map<UUID, ClientEntity.Builder>> addTerminalFunction("Creating Client Entities", clientEntityBuilders -> {
                    return clientEntityBuilders.entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> {
                                ClientEntity.Builder clientEntityBuilder = entry.getValue();
                                return clientEntityBuilder.build();
                            })
                    );
                })
                .build();
    }

    public RemoveClientsTask whereClientIds(UUID... clientIds) {
        if (Objects.isNull(clientIds) || clientIds.length < 1) {
            return this;
        }
        this.clientIds.addAll(Arrays.asList(clientIds));
        return this;
    }

    public RemoveClientsTask whereClientIds(Set<UUID> clientIds) {
        if (Objects.isNull(clientIds) || clientIds.size() < 1) {
            return this;
        }
        this.clientIds.addAll(clientIds);
        return this;
    }

    public RemoveClientsTask addRemovedClientDTO(ClientDTO clientDTO) {
        if (Objects.isNull(clientDTO)) {
            return this;
        }
        this.clientIds.add(clientDTO.clientId);
        this.removedClientDTOs.put(clientDTO.clientId, clientDTO);
        return this;
    }

    public RemoveClientsTask addRemovedClientDTOs(Map<UUID, ClientDTO> clientDTOs) {
        if (Objects.isNull(clientDTOs) || clientDTOs.size() < 1) {
            return this;
        }
        this.clientIds.addAll(clientDTOs.keySet());
        this.removedClientDTOs.putAll(clientDTOs);
        return this;
    }
}
