package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.entities.ClientEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.*;

@Prototype
public class RemoveClientsTask extends ChainedTask<List<CallEventReport.Builder>> {

    private static final Logger logger = LoggerFactory.getLogger(RemoveClientsTask.class);

    private Set<UUID> clientIds = new HashSet<>();
    private Map<UUID, ClientDTO> removedClientDTOs = new HashMap<>();
    private Map<UUID, Collection<UUID>> removedClientPeerConnectionIds = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    Provider<RemovePeerConnectionsTask> removePeerConnectionsTaskProvider;

    @PostConstruct
    void setup() {
        new Builder<List<CallEventReport.Builder>>(this)
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
                        resultHolder.set(Collections.EMPTY_LIST);
                        return true;
                    }
                    if (clientIds.size() < 1) {
                        this.getLogger().warn("No ClientId have been passed");
                        resultHolder.set(Collections.EMPTY_LIST);
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
                .<List<CallEventReport.Builder>>addSupplierStage("Remove PeerConnection Entities",
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
                                return Collections.EMPTY_LIST;
                            }
                            var task = this.removePeerConnectionsTaskProvider.get();
                            task.wherePeerConnectionIds(peerConnectionIds);

                            if (!task.execute().succeeded()) {
                                logger.warn("Remove peer connections failed");
                                return Collections.EMPTY_LIST;
                            }

                            return task.getResult();
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
                .<List<CallEventReport.Builder>> addTerminalFunction("Creating Client Entities", callEventBuildersObj -> {
                    var callEventBuilders = (List<CallEventReport.Builder>) callEventBuildersObj;
                    List<CallEventReport.Builder> result = new LinkedList<>();
                    this.removedClientDTOs.values().stream()
                            .map(this::makeReportBuilder)
                            .filter(Objects::nonNull)
                            .forEach(result::add);
                    if (Objects.nonNull(callEventBuilders)) {
                        callEventBuilders.stream().forEach(result::add);
                    }
                    return result;
                })
                .build();
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

    private CallEventReport.Builder makeReportBuilder(ClientDTO clientDTO) {
        Long now = Instant.now().toEpochMilli();
        try {
            var builder = CallEventReport.newBuilder()
                    .setName(CallEventType.CLIENT_LEFT.name())
                    .setTimestamp(now);
            return setupBuilder(builder, clientDTO);
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for client DTO", ex);
            return null;
        }
    }

    private CallEventReport.Builder setupBuilder(CallEventReport.Builder builder, ClientDTO clientDTO) {
        try {
            return builder
                    .setMediaUnitId(clientDTO.mediaUnitId)
                    .setServiceId(clientDTO.serviceId)
                    .setRoomId(clientDTO.roomId)

                    .setUserId(clientDTO.userId)
                    .setCallId(clientDTO.callId.toString())
                    .setClientId(clientDTO.clientId.toString())
                    ;
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for client DTO", ex);
            return null;
        }
    }
}
