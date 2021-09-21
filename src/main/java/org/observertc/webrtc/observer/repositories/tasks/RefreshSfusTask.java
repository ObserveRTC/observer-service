package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.SfuDTO;
import org.observertc.webrtc.observer.dto.SfuRtpStreamPodDTO;
import org.observertc.webrtc.observer.dto.SfuTransportDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Prototype
public class RefreshSfusTask extends ChainedTask<RefreshSfusTask.Report> {

    private static final Logger logger = LoggerFactory.getLogger(RefreshSfusTask.class);

    public static class Report {
        public Set<UUID> foundSfuIds = new HashSet<>();
        public Set<UUID> foundSfuTransportIds = new HashSet<>();
        public Set<UUID> foundRtpPodIds = new HashSet<>();
    }


    private Set<UUID> sfuIds = new HashSet<>();
    private Set<UUID> transportIds = new HashSet<>();
    private Set<UUID> rtpPodIds = new HashSet<>();
    private final Report report = new Report();

    private Map<UUID, SfuRtpStreamPodDTO> incompleteSfuPodDTOs = new HashMap<>();
    private Map<UUID, SfuRtpStreamPodDTO> completedSfuPodDTOs = new HashMap<>();
    @Inject
    HazelcastMaps hazelcastMaps;

    @PostConstruct
    void setup() {
        new Builder<Report>(this)
                .addActionStage("Check Sfu Rtp Streams",
                        // action
                        () -> {
                            if (this.rtpPodIds.size() < 1) {
                                return;
                            }
                            Map<UUID, SfuRtpStreamPodDTO> rtpPodDTOs = this.hazelcastMaps.getSFURtpPods().getAll(this.rtpPodIds);
                            this.report.foundRtpPodIds.addAll(rtpPodDTOs.keySet());
                            rtpPodDTOs.values().stream()
                                    .filter(dto -> Objects.isNull(dto.callId))
                                    .forEach(dto -> incompleteSfuPodDTOs.put(dto.sfuPodId, dto));
                        })
                .addActionStage("Try complete SfuPods",
                // action
                () -> {
                    if (this.incompleteSfuPodDTOs.size() < 1) {
                        return;
                    }
                    Set<UUID> sfuPodIds = this.incompleteSfuPodDTOs.keySet();
                    logger.info("sfuPodIds: {}", sfuPodIds);
                    this.hazelcastMaps.getSfuPodToMediaTracks().entrySet().forEach(entry -> {
                        logger.info("{} -> {}", entry.getKey(), entry.getValue());
                    });
                    Map<UUID, UUID> podIdToTrackIds = this.hazelcastMaps.getSfuPodToMediaTracks().getAll(sfuPodIds);
                    if (podIdToTrackIds.size() < 1) {
                        return;
                    }
                    Set<UUID> trackIds = new HashSet<>(podIdToTrackIds.values());
                    Map<UUID, MediaTrackDTO> mediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(trackIds);
                    mediaTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
                        UUID rtpPodId = mediaTrackDTO.sfuPodId;
                        if (Objects.isNull(rtpPodId)) {
                            return;
                        }
                        SfuRtpStreamPodDTO rtpPodDTO = this.incompleteSfuPodDTOs.remove(rtpPodId);
                        if (Objects.isNull(rtpPodDTO)) {
                            return;
                        }

                        var completedStreamDTO = SfuRtpStreamPodDTO.builderFrom(rtpPodDTO)
                                .withTrackId(mediaTrackDTO.trackId)
                                .withClientId(mediaTrackDTO.clientId)
                                .withCallId(mediaTrackDTO.callId)
                                .build();
                        this.completedSfuPodDTOs.put(rtpPodId, completedStreamDTO);
                        logger.info("SFU Stream Pod ({}) in role {} is bound to track {} for client {} on call {}", rtpPodId, completedStreamDTO.sfuPodRole, completedStreamDTO.trackId, completedStreamDTO.clientId, completedStreamDTO.callId);
                    });
                    if (0 < this.completedSfuPodDTOs.size()) {
                        this.hazelcastMaps.getSFURtpPods().putAll(this.completedSfuPodDTOs);
                    }
                })
                .addActionStage("Complete related RTRP Pods", () -> {
                    List<SfuRtpStreamPodDTO> completedPods = this.completedSfuPodDTOs.values().stream().collect(Collectors.toList());
                    completedPods.forEach(completedRtpPod -> {
                        UUID streamId = completedRtpPod.sfuStreamId;
                        if (Objects.isNull(streamId)) {
                            return;
                        }
                        var podIds = hazelcastMaps.getSfuStreamToRtpPodIds().get(streamId).stream().collect(Collectors.toSet());
                        if (podIds.size() < 1) {
                            return;
                        }
                        var completedRelatedRtpPods = this.hazelcastMaps.getSFURtpPods()
                                .getAll(podIds)
                                .values()
                                .stream()
                                .filter(dto -> Objects.isNull(dto.callId))
                                .map(incompleteRelatedPodDTO -> SfuRtpStreamPodDTO.builderFrom(incompleteRelatedPodDTO)
                                        .withCallId(completedRtpPod.callId)
                                        .build()
                                ).collect(Collectors.toMap(
                                        dto -> dto.sfuPodId,
                                        Function.identity()
                                ));
                        if (0 < completedRelatedRtpPods.size()) {
                            hazelcastMaps.getSFURtpPods().putAll(completedRelatedRtpPods);
                        }
                    });
                })
                .addActionStage("Try complete Transports",
                // action
                () -> {
                    if (this.completedSfuPodDTOs.size() < 1) {
                        return;
                    }
                    Map<UUID, SfuTransportDTO> completedSfuTransports = new HashMap<>();
                    this.completedSfuPodDTOs.forEach((sfuPodId, sfuRtpStreamPodDTO) -> {
                        UUID transportId = sfuRtpStreamPodDTO.sfuTransportId;
                        if (Objects.isNull(transportId)) {
                            return;
                        }
                        SfuTransportDTO sfuTransportDTO = this.hazelcastMaps.getSFUTransports().get(transportId);
                        if (Objects.isNull(sfuTransportDTO) || Objects.nonNull(sfuRtpStreamPodDTO.callId)) {
                            return;
                        }
                        completedSfuTransports.put(transportId, SfuTransportDTO.builder()
                                .from(sfuTransportDTO)
                                .withCallId(sfuRtpStreamPodDTO.callId)
                                .build()
                        );
                    });
                },
                // rollback: yeah.... no hard feelings about the completed transports
                (inputHolder, thrown) -> {

                })
                .addActionStage("Check Sfu Transports",
                        // action
                        () -> {
                            if (this.transportIds.size() < 1) {
                                return;
                            }
                            Map<UUID, SfuTransportDTO> sfuTransportDTOs = this.hazelcastMaps.getSFUTransports().getAll(this.transportIds);
                            this.report.foundSfuTransportIds.addAll(sfuTransportDTOs.keySet());
                        })
                .addActionStage("Check Sfus",
                        // action
                        () -> {
                            if (this.sfuIds.size() < 1) {
                                return;
                            }
                            Map<UUID, SfuDTO> sfuDTOs = this.hazelcastMaps.getSFUs().getAll(this.sfuIds);
                            this.report.foundSfuIds.addAll(sfuDTOs.keySet());
                        })
                .<Report> addTerminalSupplier("Provide the composed report", () -> {
                    return this.report;
                })
                .build();
    }



    public RefreshSfusTask withSfuIds(UUID... sfuIds) {
        if (Objects.isNull(sfuIds)) {
            return this;
        }
        var sfuIdsArray = Arrays.asList(sfuIds);
        this.sfuIds.addAll(sfuIdsArray);
        return this;
    }

    public RefreshSfusTask withSfuIds(Set<UUID> sfuIds) {
        if (Objects.isNull(sfuIds)) {
            return this;
        }
        this.sfuIds.addAll(sfuIds);
        return this;
    }

    public RefreshSfusTask withSfuTransportIds(UUID... sfuTransportIds) {
        if (Objects.isNull(sfuTransportIds)) {
            return this;
        }
        var sfuTransportIdsArray = Arrays.asList(sfuTransportIds);
        this.transportIds.addAll(sfuTransportIdsArray);
        return this;
    }

    public RefreshSfusTask withSfuTransportIds(Set<UUID> sfuTransportIds) {
        if (Objects.isNull(sfuTransportIds)) {
            return this;
        }
        this.transportIds.addAll(sfuTransportIds);
        return this;
    }

    public RefreshSfusTask withSfuRtpPodIds(UUID... rtpStreamIds) {
        if (Objects.isNull(rtpStreamIds)) {
            return this;
        }
        var rtpStreamIdsList = Arrays.asList(rtpStreamIds);
        this.rtpPodIds.addAll(rtpStreamIdsList);
        return this;
    }

    public RefreshSfusTask withSfuRtpPodIds(Set<UUID> rtpPodIds) {
        if (Objects.isNull(rtpPodIds)) {
            return this;
        }
        this.rtpPodIds.addAll(rtpPodIds);
        return this;
    }

}
