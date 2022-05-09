package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.dto.ClientDTO;
import org.observertc.observer.entities.ClientEntity;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;

@Prototype
public class RemoveClientsTask extends ChainedTask<Map<UUID, ClientDTO>> {

    private static final Logger logger = LoggerFactory.getLogger(RemoveClientsTask.class);

    private Set<UUID> clientIds = new HashSet<>();
    private Map<UUID, ClientDTO> removedClientDTOs = new HashMap<>();
    private Map<UUID, Collection<UUID>> removedClientPeerConnectionIds = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    BeanProvider<RemovePeerConnectionsTask> removePeerConnectionsTaskProvider;
    private boolean unmodifiableResult = false;

    @Inject
    ExposedMetrics exposedMetrics;

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<Map<UUID, ClientDTO>>(this)
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
                .<Set<UUID>> addConsumerStage("Remove Client DTOs",
                        clientIds -> {
                            Map<UUID, ClientEntity.Builder> clientEntityBuilders = new HashMap<>();
                            for (UUID clientId : clientIds) {
                                var clientDTO = this.removedClientDTOs.get(clientId);
                                if (Objects.isNull(clientDTO)) {
                                    clientDTO = this.hazelcastMaps.getClients().remove(clientId);
                                    if (Objects.isNull(clientDTO)) {
                                        logger.warn("Not found ClientDTO for clientId {}", clientId);
                                        continue;
                                    }
                                    this.removedClientDTOs.put(clientId, clientDTO);
                                }
                                if (Objects.isNull(clientDTO)) {
                                    logger.warn("Cannot retrieve clientDTO for clientId: {}", clientId);
                                    continue;
                                }
                                this.hazelcastMaps.getCallToClientIds().remove(clientDTO.callId, clientId);
                                var clientEntityBuilder = ClientEntity.builder().withClientDTO(clientDTO);
                                clientEntityBuilders.put(clientId, clientEntityBuilder);
                            }
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            if (this.removedClientDTOs.size() < 1) {
                                return;
                            }
                            this.hazelcastMaps.getClients().putAll(this.removedClientDTOs);
                        })
                .addActionStage("Remove Client Binding to Calls", () -> {
                        this.removedClientDTOs.forEach((clientId, clientDTO) -> {
                            this.hazelcastMaps.getCallToClientIds().remove(clientDTO.callId, clientId);
                        });
                    },
                    (inputHolder, thrownException) -> {
                        this.removedClientDTOs.forEach((clientId, clientDTO) -> {
                            this.hazelcastMaps.getCallToClientIds().put(clientDTO.callId, clientId);
                        });
                    })
                .addActionStage("Remove PeerConnection Entities",
                        () -> {
                            Set<UUID> peerConnectionIds = new HashSet<>();
                            this.removedClientDTOs.keySet().forEach(clientId -> {
                                Collection<UUID> clientPeerConnectionIds = this.hazelcastMaps.getClientToPeerConnectionIds().get(clientId);
                                if (Objects.nonNull(clientPeerConnectionIds)) {
                                    clientPeerConnectionIds.forEach(peerConnectionId -> {
                                        peerConnectionIds.add(peerConnectionId);
                                    });
                                }
                            });
                            if (peerConnectionIds.size() < 1) {
                                return;
                            }
                            var task = this.removePeerConnectionsTaskProvider.get();
                            task.wherePeerConnectionIds(peerConnectionIds);

                            if (!task.execute().succeeded()) {
                                logger.warn("Remove peer connections failed");
                                return;
                            }
                        },
                        (inputHolder, thrownException) -> {
                            if (this.removedClientPeerConnectionIds.size() < 1) {
                                return;
                            }
                            this.removedClientPeerConnectionIds.forEach((clientId, peerConnectionIds) -> {
                                peerConnectionIds.forEach(peerConnectionId -> {
                                    this.hazelcastMaps.getCallToClientIds().put(clientId, peerConnectionId);
                                });
                            });
                        })
                .addSupplierStage("Returning with removed Client DTOs", () -> {
                    if (this.unmodifiableResult) {
                        return Collections.unmodifiableMap(this.removedClientDTOs);
                    } else {
                        return this.removedClientDTOs;
                    }

                })
                .build();
    }

    public RemoveClientsTask whereClientIds(Set<UUID> clientIds) {
        if (Objects.isNull(clientIds) || clientIds.size() < 1) {
            return this;
        }
        clientIds.stream().filter(Utils::nonNull).forEach(this.clientIds::add);
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

    public RemoveClientsTask withUnmodifiableResult(boolean value) {
        this.unmodifiableResult = value;
        return this;
    }
}
