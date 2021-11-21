package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.dto.SfuRtpPadDTO;
import org.observertc.webrtc.observer.micrometer.ExposedMetrics;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.repositories.StoredRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

@Prototype
public class AddMediaTracksTasks extends ChainedTask<Void> {

    private static final Logger logger = LoggerFactory.getLogger(AddMediaTracksTasks.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    StoredRequests storedRequests;

    @Inject
    ExposedMetrics exposedMetrics;

    private Map<UUID, MediaTrackDTO> mediaTrackDTOs = new HashMap<>();


    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<Void>(this)
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
                        resultHolder.set(null);
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
                .addActionStage("Bind Media Tracks to Peer Connections and Rtp Streams",
                        // action
                        () -> {
                            this.mediaTrackDTOs.forEach(((mediaTrackId, mediaTrackDTO) -> {
                                switch (mediaTrackDTO.direction) {
                                    case INBOUND:
                                        this.hazelcastMaps.getPeerConnectionToInboundTrackIds().put(mediaTrackDTO.peerConnectionId, mediaTrackId);
                                        break;
                                    case OUTBOUND:
                                        this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().put(mediaTrackDTO.peerConnectionId, mediaTrackId);
                                        if (Objects.nonNull(mediaTrackDTO.rtpStreamId)) {
                                            this.hazelcastMaps.getRtpStreamIdsToOutboundTrackIds().put(mediaTrackDTO.rtpStreamId, mediaTrackId);
                                        }
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
                                        if (Objects.nonNull(mediaTrackDTO.rtpStreamId)) {
                                            this.hazelcastMaps.getRtpStreamIdsToOutboundTrackIds().remove(mediaTrackDTO.rtpStreamId);
                                        }
                                        break;
                                }
                            }));
                        })
                .addActionStage("Try complete SfuRtpPads if has requests", () -> {
                    Map<UUID, List<MediaTrackDTO>> rtpStreamMediaTracks = this.mediaTrackDTOs.values().stream().filter(dto -> Objects.nonNull(dto.rtpStreamId)).collect(groupingBy(dto -> dto.rtpStreamId));
                    Set<UUID> incompleteSfuRtpPadRtpStreamIds = this.storedRequests.removeCompleteRtpStreamSfuRtpPadsRequests(rtpStreamMediaTracks.keySet());
                    if (incompleteSfuRtpPadRtpStreamIds.size() < 1) {
                        return;
                    }
                    Set<UUID> incompleteSfuRtpPads = new HashSet<>();
                    rtpStreamMediaTracks.keySet().stream().forEach(rtpStreamId -> {
                        Collection<UUID> sfuRtpPadIds = this.hazelcastMaps.getRtpStreamIdToSfuPadIds().get(rtpStreamId);
                        incompleteSfuRtpPads.addAll(sfuRtpPadIds);
                    });
                    Map<UUID, SfuRtpPadDTO> loadedSfuRtpPadDTOs = this.hazelcastMaps.getSFURtpPads().getAll(incompleteSfuRtpPads);
                    Map<UUID, SfuRtpPadDTO> completedSfuRtpPadDTOs = new HashMap<>();
                    loadedSfuRtpPadDTOs.forEach((rtpPadId, loadedSfuRtpPad) -> {
                        List<MediaTrackDTO> mediaTrackDTOs = rtpStreamMediaTracks.get(loadedSfuRtpPad.rtpStreamId);
                        if (Objects.isNull(mediaTrackDTOs) || mediaTrackDTOs.size() < 1) {
                            return;
                        }
                        // TODO: this can be refined a bit, or perform a check here
                        UUID callId = mediaTrackDTOs.get(0).callId;
                        var builder = SfuRtpPadDTO.builderFrom(loadedSfuRtpPad).withCallId(callId);
                        var completedSfuRtpPad = builder.build();
                        completedSfuRtpPadDTOs.put(completedSfuRtpPad.sfuPadId, completedSfuRtpPad);
                        logger.info("SfuRtpPad {} for streamId {} is assigned with CallId {}", completedSfuRtpPad.sfuPadId, completedSfuRtpPad.rtpStreamId, callId);
                    });
                    if (0 < completedSfuRtpPadDTOs.size()) {
                        this.hazelcastMaps.getSFURtpPads().putAll(completedSfuRtpPadDTOs);
                    }
                })
                .addTerminalPassingStage("Completed")
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
}
