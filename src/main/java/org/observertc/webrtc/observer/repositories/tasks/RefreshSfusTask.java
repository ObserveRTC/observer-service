package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.SfuDTO;
import org.observertc.webrtc.observer.dto.SfuRtpStreamDTO;
import org.observertc.webrtc.observer.dto.SfuTransportDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class RefreshSfusTask extends ChainedTask<RefreshSfusTask.Report> {

    private static final Logger logger = LoggerFactory.getLogger(RefreshSfusTask.class);

    public static class Report {
        public Set<UUID> foundSfuIds = new HashSet<>();
        public Set<UUID> foundSfuTransportIds = new HashSet<>();
        public Set<UUID> foundRtpStreamIds = new HashSet<>();
    }


    private Set<UUID> sfuIds = new HashSet<>();
    private Set<UUID> transportIds = new HashSet<>();
    private Set<UUID> rtpStreamIds = new HashSet<>();
    private final Report report = new Report();

    private Map<UUID, SfuRtpStreamDTO> incompleteSfuStreamDTOs = new HashMap<>();
    private Map<UUID, SfuRtpStreamDTO> completedSfuStreamDTOs = new HashMap<>();
    @Inject
    HazelcastMaps hazelcastMaps;

    @PostConstruct
    void setup() {
        new Builder<Report>(this)
                .addActionStage("Check Sfu Rtp Streams",
                        // action
                        () -> {
                            if (this.rtpStreamIds.size() < 1) {
                                return;
                            }
                            Map<UUID, SfuRtpStreamDTO> rtpStreamDTOs = this.hazelcastMaps.getSFURtpStreams().getAll(this.rtpStreamIds);
                            this.report.foundRtpStreamIds.addAll(rtpStreamDTOs.keySet());
                            rtpStreamDTOs.values().stream()
                                    .filter(dto -> Objects.isNull(dto.callId))
                                    .forEach(dto -> incompleteSfuStreamDTOs.put(dto.streamId, dto));
                        })
                .addActionStage("Try complete SfuStreams",
                // action
                () -> {
                    if (this.incompleteSfuStreamDTOs.size() < 1) {
                        return;
                    }
                    Set<UUID> sfuStreamIds = this.incompleteSfuStreamDTOs.keySet();
                    Map<UUID, UUID> streamToTracks = this.hazelcastMaps.getSfuStreamsToMediaTracks().getAll(sfuStreamIds);
                    if (streamToTracks.size() < 1) {
                        return;
                    }
                    Set<UUID> trackIds = new HashSet<>(streamToTracks.values());
                    Map<UUID, MediaTrackDTO> mediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(trackIds);
                    mediaTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
                        UUID streamId = mediaTrackDTO.sfuStreamId;
                        if (Objects.isNull(streamId)) {
                            return;
                        }
                        SfuRtpStreamDTO streamDTO = this.incompleteSfuStreamDTOs.remove(streamId);
                        if (Objects.isNull(streamDTO)) {
                            return;
                        }

                        this.completedSfuStreamDTOs.put(streamId,
                                SfuRtpStreamDTO.builderFrom(streamDTO)
                                        .withTrackId(mediaTrackDTO.trackId)
                                        .withClientId(mediaTrackDTO.clientId)
                                        .withCallId(mediaTrackDTO.callId)
                                        .build()
                        );
                    });
                },
                // rollback: yeah.... no hard feelings about the completed StreamDTOs
                (inputHolder, thrown) -> {

                })
                .addActionStage("Traverse completed sfu streams they are piped", () -> {
                    Queue<SfuRtpStreamDTO> sfuStreams = this.completedSfuStreamDTOs.values().stream()
                            .collect(Collectors.toCollection(LinkedList::new));
                    while(!sfuStreams.isEmpty()) {
                        SfuRtpStreamDTO sfuRtpStreamDTO = sfuStreams.poll();
                        UUID pipedStreamId = sfuRtpStreamDTO.pipedStreamId;
                        if (Objects.isNull(pipedStreamId)) {
                            continue;
                        }
                        SfuRtpStreamDTO pipedSfuStreamDTO = this.hazelcastMaps.getSFURtpStreams().get(pipedStreamId);
                        if (Objects.isNull(pipedSfuStreamDTO)) {
                            continue;
                        }
                        var newPipedStreamDTO = SfuRtpStreamDTO.builderFrom(pipedSfuStreamDTO)
                                .withCallId(sfuRtpStreamDTO.callId)
                                .build();
                        if (!this.completedSfuStreamDTOs.containsKey(pipedStreamId)) {
                            this.completedSfuStreamDTOs.put(pipedSfuStreamDTO.streamId, newPipedStreamDTO);
                            sfuStreams.add(newPipedStreamDTO);
                        }
                    }
                    if (0 < this.completedSfuStreamDTOs.size()) {
                        this.hazelcastMaps.getSFURtpStreams().putAll(this.completedSfuStreamDTOs);
                    }
                })
                .addActionStage("Try complete Transports",
                // action
                () -> {
                    if (this.completedSfuStreamDTOs.size() < 1) {
                        return;
                    }
                    Map<UUID, SfuTransportDTO> completedSfuTransports = new HashMap<>();
                    this.completedSfuStreamDTOs.forEach((sfuStreamId, sfuStreamDTO) -> {
                        UUID transportId = sfuStreamDTO.transportId;
                        if (Objects.isNull(transportId)) {
                            return;
                        }
                        SfuTransportDTO sfuTransportDTO = this.hazelcastMaps.getSFUTransports().get(transportId);
                        if (Objects.isNull(sfuTransportDTO) || Objects.nonNull(sfuStreamDTO.callId)) {
                            return;
                        }
                        completedSfuTransports.put(transportId, SfuTransportDTO
                                .builderFrom(sfuTransportDTO)
                                .withCallId(sfuStreamDTO.callId)
                                .build()
                        );
                    });
                    if (0 < completedSfuStreamDTOs.size()) {
                        this.hazelcastMaps.getSFUTransports().putAll(completedSfuTransports);
                    }
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

                .<Report> addTerminalSupplier("Provide the composed report", () -> {
                    return this.report;
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

    public RefreshSfusTask withSfuRtpStreamIds(UUID... rtpStreamIds) {
        if (Objects.isNull(rtpStreamIds)) {
            return this;
        }
        var rtpStreamIdsList = Arrays.asList(rtpStreamIds);
        this.rtpStreamIds.addAll(rtpStreamIdsList);
        return this;
    }

    public RefreshSfusTask withSfuRtpStreamIds(Set<UUID> rtpStreamIds) {
        if (Objects.isNull(rtpStreamIds)) {
            return this;
        }
        this.rtpStreamIds.addAll(rtpStreamIds);
        return this;
    }

}
