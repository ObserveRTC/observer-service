package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.entities.ClientEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Prototype
public class AddClientsTask extends ChainedTask<Map<UUID, ClientEntity>> {

    private static final Logger logger = LoggerFactory.getLogger(AddClientsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    AddPeerConnectionsTask addPeerConnectionsTask;

    @Inject
    RemovePeerConnectionsTask removePeerConnectionsTask;

    @Inject
    FetchClientsTask fetchClientsTask;

    private boolean fetchEntitiesBack = true;
    private Map<UUID, ClientEntity> clientEntities = new HashMap<>();


    @PostConstruct
    void setup() {
        new Builder<Map<UUID, ClientEntity>>(this)
                .<Map<UUID, ClientEntity>>addConsumerEntry("Merge all inputs",
                        () -> {},
                        receivedClientEntities -> {
                            if (Objects.nonNull(receivedClientEntities)) {
                                this.clientEntities.putAll(receivedClientEntities);
                            }
                        }
                )
                .<Map<UUID, ClientEntity>> addBreakCondition((resultHolder) -> {
                    if (this.clientEntities.size() < 1) {
                        resultHolder.set(Collections.EMPTY_MAP);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Add Client DTOs",
                // action
                () -> {
                    Map<UUID, ClientDTO> clientDTOs = new HashMap<>();
                    this.clientEntities.forEach((clientId, clientEntity) -> {
                        clientDTOs.put(clientId, clientEntity.getClientDTO());
                    });
                    hazelcastMaps.getClients().putAll(clientDTOs);
                },
                // rollback
                (inputHolder, thrownException) -> {
                    for (UUID clientId : this.clientEntities.keySet()) {
                        hazelcastMaps.getClients().remove(clientId);
                    }
                })
                .addActionStage("Add Peer Connections",
                // action
                () -> {

                    this.clientEntities.values()
                            .stream()
                            .flatMap(clientEntity -> clientEntity.getPeerConnections().values().stream())
                            .forEach(this.addPeerConnectionsTask::withPeerConnectionEntities);

                    if (!this.addPeerConnectionsTask.execute().succeeded()) {
                        throw new RuntimeException("Task cannot be executed due to problem while executing peer connection entities");
                    }
                },
                // rollback
                (inputHolder, thrownException) -> {
                    this.clientEntities.values()
                            .stream()
                            .flatMap(clientEntity -> clientEntity.getPeerConnections().values().stream())
                            .forEach(pcEntity -> {
                                var peerConnectionId = pcEntity.getPeerConnectionId();
                                this.removePeerConnectionsTask.wherePeerConnectionIds(peerConnectionId);
                            });
                    this.removePeerConnectionsTask.execute();
                })
                .addActionStage("Bind Clients to Peer Connections", () -> {
                    this.clientEntities.values()
                            .stream()
                            .forEach(clientEntity -> {
                                var clientId = clientEntity.getClientId();
                                var peerConnections = clientEntity.getPeerConnections().values();
                                peerConnections.forEach(peerConnectionEntity -> {
                                    var peerConnectionId = peerConnectionEntity.getPeerConnectionId();
                                    this.hazelcastMaps.getClientToPeerConnectionIds().put(clientId, peerConnectionId);
                                });
                            });
                },
                // rollback
                (inputHolder, thrownException) -> {
                    this.clientEntities.values()
                            .stream()
                            .forEach(clientEntity -> {
                                var clientId = clientEntity.getClientId();
                                var peerConnections = clientEntity.getPeerConnections().values();
                                peerConnections.forEach(peerConnectionEntity -> {
                                    var peerConnectionId = peerConnectionEntity.getPeerConnectionId();
                                    this.hazelcastMaps.getClientToPeerConnectionIds().remove(clientId, peerConnectionId);
                                });
                            });
                })
                .<Map<UUID, ClientEntity>>addSupplierStage("Fetch Result Entities", () -> {
                    if (!this.fetchEntitiesBack) {
                        return Collections.unmodifiableMap(this.clientEntities);
                    }
                    Set<UUID> clientIds = this.clientEntities.keySet();
                    if (!this.fetchClientsTask.whereClientIds(clientIds).execute().succeeded()) {
                        throw new RuntimeException("Error occurred during fetching entities back");
                    }
                    return this.fetchClientsTask.getResult();
                })
                .addTerminalPassingStage("Completed")
                .build();
    }

    public AddClientsTask withClientEntities(ClientEntity... entities) {
        if (Objects.isNull(entities) && entities.length < 1) {
            this.getLogger().info("call uuid was not given to be removed");
            return this;
        }
        Arrays.stream(entities).forEach(clientEntity -> {
            this.clientEntities.put(clientEntity.getClientId(), clientEntity);
        });
        return this;
    }

    public AddClientsTask withClientEntities(Map<UUID, ClientEntity> entities) {
        this.clientEntities.putAll(entities);
        return this;
    }

    public AddClientsTask withFetchingBackTheResult(boolean value) {
        this.fetchEntitiesBack = value;
        return this;
    }
}
