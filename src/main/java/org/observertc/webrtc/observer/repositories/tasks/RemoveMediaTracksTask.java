package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.StreamDirection;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class RemoveMediaTracksTask extends ChainedTask<List<CallEventReport.Builder>> {

    private static final Logger logger = LoggerFactory.getLogger(RemoveMediaTracksTask.class);
    private Set<UUID> mediaTrackIds = new HashSet<>();
    private Map<UUID, MediaTrackDTO> removedTrackDTOs = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @PostConstruct
    void setup() {
        new Builder<List<CallEventReport.Builder>>(this)
                .<Set<UUID>, Set<UUID>>addSupplierEntry("Merge Inputs",
                        () -> this.mediaTrackIds,
                        receivedTrackIds -> {
                            this.mediaTrackIds.addAll(receivedTrackIds);
                            return this.mediaTrackIds;
                        }
                )
                .<Set<UUID>> addConsumerStage("Remove MediaTrack DTOs",
                        mediaTrackIds -> {
                            for (UUID trackId : this.mediaTrackIds) {
                                if (this.removedTrackDTOs.containsKey(trackId)) {
                                    continue;
                                }
                                MediaTrackDTO mediaTrackDTO = this.hazelcastMaps.getMediaTracks().remove(trackId);
                                if (Objects.isNull(mediaTrackDTO)) {
                                    logger.warn("Cannot retrieve MediaTrackDTO for trackId: {}", trackId);
                                    continue;
                                }
                                this.removedTrackDTOs.put(mediaTrackDTO.trackId, mediaTrackDTO);
                            }
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            if (this.removedTrackDTOs.size() < 1) {
                                return;
                            }
                            this.hazelcastMaps.getMediaTracks().putAll(this.removedTrackDTOs);
                        })
                .addActionStage("Remove Inbound Media Tracks",
                        () -> {
                            this.removedTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
                                if (mediaTrackDTO.direction != StreamDirection.INBOUND) {
                                    return;
                                }
                                this.hazelcastMaps.getPeerConnectionToInboundTrackIds().remove(mediaTrackDTO.peerConnectionId, trackId);
                            });
                        },
                        (inputHolder, thrownException) -> {
                            this.removedTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
                                if (mediaTrackDTO.direction != StreamDirection.INBOUND) {
                                    return;
                                }
                                this.hazelcastMaps.getPeerConnectionToInboundTrackIds().put(mediaTrackDTO.peerConnectionId, trackId);
                            });
                        })
                .addActionStage("Remove Outbound Media Tracks",
                        () -> {
                            this.removedTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
                                if (mediaTrackDTO.direction != StreamDirection.OUTBOUND) {
                                    return;
                                }
                                this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().remove(mediaTrackDTO.peerConnectionId, trackId);
                            });
                        },
                        (inputHolder, thrownException) -> {
                            this.removedTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
                                if (mediaTrackDTO.direction != StreamDirection.OUTBOUND) {
                                    return;
                                }
                                this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().put(mediaTrackDTO.peerConnectionId, trackId);
                            });
                        })
                . addTerminalSupplier("Creating PeerConnection Entities", () -> {
                    List<CallEventReport.Builder> result = this.removedTrackDTOs.values().stream()
                            .map(this::makeReportBuilder)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    return result;
                })
                .build();
    }

    public RemoveMediaTracksTask whereMediaTrackIds(Set<UUID> mediaTrackIds) {
        if (Objects.isNull(mediaTrackIds) || mediaTrackIds.size() < 1) {
            return this;
        }
        this.mediaTrackIds.addAll(mediaTrackIds);
        return this;
    }

    public RemoveMediaTracksTask addremovedMediaTrackDTO(MediaTrackDTO mediaTrackDTO) {
        if (Objects.isNull(mediaTrackDTO)) {
            return this;
        }
        this.removedTrackDTOs.put(mediaTrackDTO.trackId, mediaTrackDTO);
        return this;
    }

    private CallEventReport.Builder makeReportBuilder(MediaTrackDTO mediaTrackDTO) {
        Long now = Instant.now().toEpochMilli();
        try {
            return CallEventReport.newBuilder()
                    .setName(CallEventType.MEDIA_TRACK_REMOVED.name())
                    .setCallId(mediaTrackDTO.callId.toString())
                    .setServiceId(mediaTrackDTO.serviceId)
                    .setRoomId(mediaTrackDTO.roomId)

                    .setClientId(mediaTrackDTO.clientId.toString())
                    .setMediaUnitId(mediaTrackDTO.mediaUnitId)
                    .setUserId(mediaTrackDTO.userId)

                    .setMediaTrackId(mediaTrackDTO.trackId.toString())
                    .setPeerConnectionId(mediaTrackDTO.peerConnectionId.toString())
                    .setAttachments("Direction of media track" + mediaTrackDTO.direction.name())
                    .setTimestamp(now);
        } catch (Exception ex) {
            this.getLogger().error("Cannot make report for client DTO", ex);
            return null;
        }
    }
}
