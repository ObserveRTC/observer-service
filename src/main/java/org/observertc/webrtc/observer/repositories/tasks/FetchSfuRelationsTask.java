package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.micrometer.ExposedMetrics;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Prototype
public class FetchSfuRelationsTask extends ChainedTask<FetchSfuRelationsTask.Report> {

    private static final Logger logger = LoggerFactory.getLogger(FetchSfuRelationsTask.class);

    public static final Report EMPTY_REPORT = new Report();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ExposedMetrics exposedMetrics;

    public static class Report {
        public final Map<UUID, UUID> rtpStreamIdToCallIds = new HashMap<>();
    }

    private Report result = new Report();

    private Set<UUID> rtpStreamIds = new HashSet<>();
    private Set<UUID> sfuTransportIds = new HashSet<>();

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<>(this)
                .addActionStage("Collect Sfu Stream relations", () -> {
                    if (this.rtpStreamIds.size() < 1) {
                        return;
                    }
                    Map<UUID, UUID> rtpStreamIdToTrackIds = this.hazelcastMaps.getRtpStreamIdsToOutboundTrackIds().getAll(this.rtpStreamIds);
                    if (rtpStreamIdToTrackIds.size() < 1) {
                        return;
                    }
                    Set<UUID> trackIds = new HashSet<>(rtpStreamIdToTrackIds.values());
                    Map<UUID, MediaTrackDTO> mediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(trackIds);
                    mediaTrackDTOs.values().forEach(mediaTrackDTO -> {
                        this.result.rtpStreamIdToCallIds.put(mediaTrackDTO.rtpStreamId, mediaTrackDTO.callId);
                    });
                })
                .addTerminalSupplier("Completed", () -> {
                    return this.result;
                })
        .build();
    }

    public FetchSfuRelationsTask whereSfuRtpPadIds(Set<UUID> sfuRtpPadIds) {
        this.rtpStreamIds.addAll(sfuRtpPadIds);
        return this;
    }

    @Override
    protected void validate() {

    }
}
