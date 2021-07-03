package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;

@Prototype
public class RemovePeerConnectionsTask extends ChainedTask<List<CallEventReport.Builder>> {

    private static final Logger logger = LoggerFactory.getLogger(RemovePeerConnectionsTask.class);
    private Set<UUID> peerConnectionIds = new HashSet<>();
    private Map<UUID, PeerConnectionDTO> removedPeerConnectionDTOs = new HashMap<>();
    private Map<UUID, Collection<UUID>> removedPeerConnectionMediaTrackIds = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    Provider<RemoveMediaTracksTask> removeMediaTracksTaskProvider;

    @PostConstruct
    void setup() {
        new Builder<List<CallEventReport.Builder>>(this)
                .<Set<UUID>, Set<UUID>>addSupplierEntry("Merge Inputs",
                        () -> this.peerConnectionIds,
                        receivedClientIds -> {
                            this.peerConnectionIds.addAll(receivedClientIds);
                            return this.peerConnectionIds;
                        }
                )
                .<Set<UUID>> addConsumerStage("Remove PeerConnection DTOs",
                        peerConnectionIds -> {
                            for (UUID peerConnectionId : peerConnectionIds) {
                                if (this.removedPeerConnectionDTOs.containsKey(peerConnectionId)) {
                                    continue;
                                }
                                PeerConnectionDTO peerConnectionDTO = this.hazelcastMaps.getPeerConnections().remove(peerConnectionId);
                                if (Objects.isNull(peerConnectionDTO)) {
                                    logger.debug("Cannot retrieve PeerConnectionDTO for peerConnectionId: {}.", peerConnectionId);
                                    continue;
                                }
                                this.removedPeerConnectionDTOs.put(peerConnectionId, peerConnectionDTO);
                            }
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            if (this.removedPeerConnectionDTOs.size() < 1) {
                                return;
                            }
                            this.hazelcastMaps.getPeerConnections().putAll(this.removedPeerConnectionDTOs);
                        })
                .addActionStage("Remove Client Bindings", () -> {
                            this.removedPeerConnectionDTOs.forEach((peerConnectionId, removedPeerConnectionDTO) -> {
                                this.hazelcastMaps.getClientToPeerConnectionIds().remove(removedPeerConnectionDTO.clientId, peerConnectionId);
                            });
                        },
                        (inputHolder, thrownException) -> {
                            this.removedPeerConnectionDTOs.forEach((peerConnectionId, removedPeerConnectionDTO) -> {
                                this.hazelcastMaps.getClientToPeerConnectionIds().put(removedPeerConnectionDTO.clientId, peerConnectionId);
                            });
                        })
                .<List<CallEventReport.Builder>> addSupplierStage("Remove Media Tracks", () -> {
                    Set<UUID> trackIds = new HashSet<>();
                    this.removedPeerConnectionDTOs.keySet().forEach(peerConnectionId -> {
                        Set<UUID> peerConnectionIdMediaTrackIds = new HashSet<>();
                        Collection<UUID> inboundTrackIds = this.hazelcastMaps.getPeerConnectionToInboundTrackIds().remove(peerConnectionId);
                        inboundTrackIds.forEach(peerConnectionIdMediaTrackIds::add);
                        Collection<UUID> outboundTrackIds = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().remove(peerConnectionId);
                        outboundTrackIds.forEach(peerConnectionIdMediaTrackIds::add);
                        this.removedPeerConnectionMediaTrackIds.put(peerConnectionId, peerConnectionIdMediaTrackIds);
                        peerConnectionIdMediaTrackIds.forEach(trackIds::add);
                    });
                    if (trackIds.size() < 1) {
                        return Collections.EMPTY_LIST;
                    }
                    var task = this.removeMediaTracksTaskProvider.get()
                            .whereMediaTrackIds(trackIds);

                    if (!task.execute().succeeded()) {
                        logger.warn("Media Track removal has been failed");
                        return Collections.EMPTY_LIST;
                    }
                    return task.getResult();
                })
                .<List<CallEventReport.Builder>> addTerminalFunction("Creating PeerConnection Entities", callEventBuildersObj -> {
                    var callEventBuilders = (List<CallEventReport.Builder>) callEventBuildersObj;
                    List<CallEventReport.Builder> result = new LinkedList<>();
                    this.removedPeerConnectionDTOs.values().stream()
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

    public RemovePeerConnectionsTask wherePeerConnectionIds(Set<UUID> peerConnectionIds) {
        if (Objects.isNull(peerConnectionIds) || peerConnectionIds.size() < 1) {
            return this;
        }
        this.peerConnectionIds.addAll(peerConnectionIds);
        return this;
    }

    public RemovePeerConnectionsTask addRemovedPeerConnectionDTO(PeerConnectionDTO peerConnectionDTO) {
        if (Objects.isNull(peerConnectionDTO)) {
            return this;
        }
        this.removedPeerConnectionDTOs.put(peerConnectionDTO.peerConnectionId, peerConnectionDTO);
        return this;
    }

    private CallEventReport.Builder makeReportBuilder(PeerConnectionDTO peerConnectionDTO) {
        try {
            return CallEventReport.newBuilder()
                    .setName(CallEventType.PEER_CONNECTION_CLOSED.name())
                    .setCallId(peerConnectionDTO.callId.toString())
                    .setServiceId(peerConnectionDTO.serviceId)
                    .setRoomId(peerConnectionDTO.roomId)

                    .setMediaUnitId(peerConnectionDTO.mediaUnitId)
                    .setUserId(peerConnectionDTO.userId)

                    .setPeerConnectionId(peerConnectionDTO.peerConnectionId.toString())
                    .setClientId(peerConnectionDTO.clientId.toString())
                    .setTimestamp(peerConnectionDTO.created);
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for client DTO", ex);
            return null;
        }
    }
}
