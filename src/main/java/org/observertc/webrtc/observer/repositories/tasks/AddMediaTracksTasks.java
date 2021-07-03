package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class AddMediaTracksTasks extends ChainedTask<List<CallEventReport.Builder>> {

    private static final Logger logger = LoggerFactory.getLogger(AddMediaTracksTasks.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    private Map<UUID, MediaTrackDTO> mediaTrackDTOs = new HashMap<>();


    @PostConstruct
    void setup() {
        new Builder<List<CallEventReport.Builder>>(this)
                .<Map<UUID, MediaTrackDTO>>addConsumerEntry("Merge all inputs",
                        () -> {},
                        receivedPeerConnectionEntities -> {
                            if (Objects.nonNull(receivedPeerConnectionEntities)) {
                                this.mediaTrackDTOs.putAll(receivedPeerConnectionEntities);
                            }
                        }
                )
                .<Map<UUID, PeerConnectionDTO>> addBreakCondition((resultHolder) -> {
                    if (this.mediaTrackDTOs.size() < 1) {
                        resultHolder.set(Collections.EMPTY_LIST);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Add PeerConnection DTOs",
                        // action
                        () -> {
                            hazelcastMaps.getMediaTracks().putAll(this.mediaTrackDTOs);
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            for (UUID mediaTrackId : this.mediaTrackDTOs.keySet()) {
                                hazelcastMaps.getMediaTracks().remove(mediaTrackId);
                            }
                        })
                .addActionStage("Bind Inbound Media Tracks to Peer Connections",
                        // action
                        () -> {
                            this.mediaTrackDTOs.forEach(((mediaTrackKey, mediaTrackDTO) -> {
                                switch (mediaTrackDTO.direction) {
                                    case INBOUND:
                                        this.hazelcastMaps.getPeerConnectionToInboundTrackIds().put(mediaTrackDTO.peerConnectionId, mediaTrackKey);
                                        break;
                                    case OUTBOUND:
                                        this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().put(mediaTrackDTO.peerConnectionId, mediaTrackKey);
                                        break;
                                }
                            }));
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            this.mediaTrackDTOs.forEach(((mediaTrackKey, mediaTrackDTO) -> {
                                switch (mediaTrackDTO.direction) {
                                    case INBOUND:
                                        this.hazelcastMaps.getPeerConnectionToInboundTrackIds().remove(mediaTrackDTO.peerConnectionId, mediaTrackKey);
                                        break;
                                    case OUTBOUND:
                                        this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().remove(mediaTrackDTO.peerConnectionId, mediaTrackKey);
                                        break;
                                }
                            }));
                        })
                .addTerminalSupplier("Completed", () -> {
                    List<CallEventReport.Builder> result = this.mediaTrackDTOs.values().stream()
                            .map(this::makeReportBuilder)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    return result;
                })
                .build();
    }


    public AddMediaTracksTasks withMediaTrackDTOs(Map<UUID, MediaTrackDTO> mediaTrackDTOs) {
        if (Objects.isNull(mediaTrackDTOs) || mediaTrackDTOs.size() < 1) {
            this.getLogger().info("mediaTrackDTOs was not given");
            return this;
        }
        this.mediaTrackDTOs.putAll(mediaTrackDTOs);
        return this;
    }

    private CallEventReport.Builder makeReportBuilder(MediaTrackDTO mediaTrackDTO) {
        try {
            return CallEventReport.newBuilder()
                    .setName(CallEventType.MEDIA_TRACK_ADDED.name())

                    .setCallId(mediaTrackDTO.callId.toString())
                    .setServiceId(mediaTrackDTO.serviceId)
                    .setRoomId(mediaTrackDTO.roomId)

                    .setClientId(mediaTrackDTO.clientId.toString())
                    .setMediaUnitId(mediaTrackDTO.mediaUnitId)
                    .setUserId(mediaTrackDTO.userId)

                    .setMediaTrackId(mediaTrackDTO.trackId.toString())
                    .setPeerConnectionId(mediaTrackDTO.peerConnectionId.toString())
                    .setAttachments("Direction of media track" + mediaTrackDTO.direction.name())
                    .setTimestamp(mediaTrackDTO.added);
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for client DTO", ex);
            return null;
        }
    }
}
