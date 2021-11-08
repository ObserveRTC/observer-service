package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.SfuRtpPadDTO;
import org.observertc.webrtc.observer.dto.SfuTransportDTO;
import org.observertc.webrtc.observer.dto.StreamDirection;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.repositories.StoredRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

@Prototype
public class AddSfuRtpPadsTask extends ChainedTask<Void> {

    private static final Logger logger = LoggerFactory.getLogger(AddSfuRtpPadsTask.class);


    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    StoredRequests storedRequests;

    private Map<UUID, SfuRtpPadDTO> sfuRtpPadDTOs = new HashMap<>();

    @PostConstruct
    void setup() {
        new Builder<Void>(this)
                .<Map<UUID, SfuRtpPadDTO>>addConsumerEntry("Merge all inputs",
                        () -> {},
                        receivedSfuRtpStreamPadDTOs -> {
                            if (Objects.nonNull(receivedSfuRtpStreamPadDTOs)) {
                                this.sfuRtpPadDTOs.putAll(receivedSfuRtpStreamPadDTOs);
                            }
                        }
                )
                .<Map<UUID, SfuTransportDTO>> addBreakCondition((resultHolder) -> {
                    if (this.sfuRtpPadDTOs.size() < 1) {
                        resultHolder.set(null);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Try Complete RtpPads", () -> {
                    Set<UUID> missingRtpStreamMediaTracks = new HashSet<>();
                    Map<UUID, List<SfuRtpPadDTO>> rtpStreamSfuRtpPads = this.sfuRtpPadDTOs.values().stream().collect(groupingBy(dto -> dto.rtpStreamId));
                    Set<UUID> rtpStreamIds = rtpStreamSfuRtpPads.keySet();
                    Map<UUID, UUID> rtpStreamOutboundTrackIds = this.hazelcastMaps.getRtpStreamIdsToOutboundTrackIds().getAll(rtpStreamIds);
                    Set<UUID> trackIds = new HashSet<>(rtpStreamOutboundTrackIds.values());
                    Map<UUID, MediaTrackDTO> mediaTracks = this.hazelcastMaps.getMediaTracks().getAll(trackIds);
                    rtpStreamSfuRtpPads.forEach((rtpStreamId, sfuRtpPads) -> {
                        UUID trackId = rtpStreamOutboundTrackIds.get(rtpStreamId);
                        if (Objects.isNull(trackId)) {
                            missingRtpStreamMediaTracks.add(rtpStreamId);
                            return;
                        }
                        MediaTrackDTO mediaTrackDTO = mediaTracks.get(trackId);
                        if (Objects.isNull(mediaTrackDTO)) {
                            missingRtpStreamMediaTracks.add(rtpStreamId);
                            return;
                        }
                        sfuRtpPads.forEach(sfuRtpPad -> {
                            sfuRtpPad.callId = mediaTrackDTO.callId;
                            if (!sfuRtpPad.internalPad && StreamDirection.INBOUND.equals(sfuRtpPad.streamDirection)) {
                                sfuRtpPad.clientId = mediaTrackDTO.clientId;
                                sfuRtpPad.trackId = mediaTrackDTO.trackId;
                            }
                        });
                    });
                    if (0 < missingRtpStreamMediaTracks.size()) {
                        this.storedRequests.addCompleteRtpStreamSfuRtpPadRequests(missingRtpStreamMediaTracks);
                    }
                })
                .addActionStage("Add SfuRtpPad DTOs",
                        // action
                        () -> {
                            hazelcastMaps.getSFURtpPads().putAll(this.sfuRtpPadDTOs);
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            for (UUID rtpSourceId : this.sfuRtpPadDTOs.keySet()) {
                                this.hazelcastMaps.getSFURtpPads().remove(rtpSourceId);
                            }
                        })
                .addActionStage("Bind RtpPads to rtp streamIds",
                        // action
                        () -> {
                            this.sfuRtpPadDTOs.forEach((padId, sfuRtpPadDTO) -> {
                                this.hazelcastMaps.getRtpStreamIdToSfuPadIds().put(sfuRtpPadDTO.rtpStreamId, padId);
                            });
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            this.sfuRtpPadDTOs.forEach((padId, sfuRtpPadDTO) -> {
                                this.hazelcastMaps.getRtpStreamIdToSfuPadIds().remove(sfuRtpPadDTO.rtpStreamId, padId);
                            });
                        })
                .addTerminalPassingStage("Completed")
                .build();
        this.withLogger(logger);
    }

    public AddSfuRtpPadsTask withSfuRtpPadDTOs(Map<UUID, SfuRtpPadDTO> sfuRtpPadDTO) {
        if (Objects.isNull(sfuRtpPadDTO) || sfuRtpPadDTO.size() < 1) {
            this.getLogger().info("sfu stream DTO was not given to be added");
            return this;
        }
        this.sfuRtpPadDTOs.putAll(sfuRtpPadDTO);
        return this;
    }
}
