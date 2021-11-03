package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.SfuDTO;
import org.observertc.webrtc.observer.dto.SfuRtpPadDTO;
import org.observertc.webrtc.observer.dto.SfuTransportDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Prototype
public class RefreshSfusTask extends ChainedTask<RefreshSfusTask.Report> {

    private static final Logger logger = LoggerFactory.getLogger(RefreshSfusTask.class);

    public static class Report {
        public Set<UUID> foundSfuIds = new HashSet<>();
        public Set<UUID> foundSfuTransportIds = new HashSet<>();
        public Set<UUID> foundRtpPadIds = new HashSet<>();
        public Map<UUID, SfuRtpPadDTO> completedSfuRtpPadDTOs = new HashMap<>();
    }


    private Set<UUID> sfuIds = new HashSet<>();
    private Set<UUID> transportIds = new HashSet<>();
    private Set<UUID> rtpPodIds = new HashSet<>();
    private final Report report = new Report();

    private Map<UUID, List<SfuRtpPadDTO>> incompleteSfuPadDTOByRtpStreamIds = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FindCallIdsByRtpStreamIdsTask findCallIdsByRtpStreamIdsTask;

    @PostConstruct
    void setup() {
        new Builder<Report>(this)
                .addActionStage("Check Sfu Rtp Pads",
                        // action
                        () -> {
                            if (this.rtpPodIds.size() < 1) {
                                return;
                            }
                            Map<UUID, SfuRtpPadDTO> rtpPadDTOs = this.hazelcastMaps.getSFURtpPads().getAll(this.rtpPodIds);
                            this.report.foundRtpPadIds.addAll(rtpPadDTOs.keySet());
                            rtpPadDTOs.values().stream()
                                    .filter(dto -> Objects.isNull(dto.callId) && Objects.nonNull(dto.rtpStreamId))
                                    .forEach(dto -> {
                                        List<SfuRtpPadDTO> sfuRtpPadDTOs = incompleteSfuPadDTOByRtpStreamIds.get(dto.rtpStreamId);
                                        if (Objects.isNull(sfuRtpPadDTOs)) {
                                            sfuRtpPadDTOs = new LinkedList<>();
                                            incompleteSfuPadDTOByRtpStreamIds.put(dto.rtpStreamId, sfuRtpPadDTOs);
                                        }
                                        sfuRtpPadDTOs.add(dto);
                                    });
                        })
                .addActionStage("Try complete SfuPads based on sfuStreamIds",
                // action
                () -> {
                    if (this.incompleteSfuPadDTOByRtpStreamIds.size() < 1) {
                        return;
                    }
                    Set<UUID> rtpStreamIds = this.incompleteSfuPadDTOByRtpStreamIds.keySet();
                    var task = findCallIdsByRtpStreamIdsTask.withRtpStreamIds(rtpStreamIds);
                    if (!task.execute().succeeded()) {
                        return;
                    }
                    Map<UUID, UUID> rtpStreamToCallIds = task.getResult();
                    if (rtpStreamToCallIds.size() < 1) {
                        return;
                    }
                    rtpStreamToCallIds.forEach((rtpStreamId, callId) -> {
                        var sfuPadDTOs = this.incompleteSfuPadDTOByRtpStreamIds.get(rtpStreamId);
                        if (Objects.isNull(sfuPadDTOs) || sfuPadDTOs.size() < 1) {
                            return;
                        }
                        sfuPadDTOs.forEach(sfuPadDTO -> {
                            var completedStreamDTO = SfuRtpPadDTO.builderFrom(sfuPadDTO)
//                                    .withTrackId(mediaTrackDTO.trackId)
//                                    .withClientId(mediaTrackDTO.clientId)
                                    .withCallId(callId)
                                    .build();
                            this.report.completedSfuRtpPadDTOs.put(sfuPadDTO.sfuPadId, completedStreamDTO);
                        });
                    });
                })
                .addActionStage("Complete related RTP Pads", () -> {
                    if (0 < this.report.completedSfuRtpPadDTOs.size()) {
                        hazelcastMaps.getSFURtpPads().putAll(this.report.completedSfuRtpPadDTOs);
                    }
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
