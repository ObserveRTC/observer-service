package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.dto.SfuRtpPadDTO;
import org.observertc.observer.dto.SfuSinkDTO;
import org.observertc.observer.dto.SfuStreamDTO;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class FetchSfuRelationsTask extends ChainedTask<FetchSfuRelationsTask.Report> {

    private static final Logger logger = LoggerFactory.getLogger(FetchSfuRelationsTask.class);

    public static final Report EMPTY_REPORT = new Report();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ExposedMetrics exposedMetrics;

    public static class Report {
        public final Map<UUID, SfuStreamDTO> sfuStreams = new HashMap<>();
        public final Map<UUID, SfuSinkDTO> sfuSinks = new HashMap<>();
    }

    private Report result = new Report();

    private Set<UUID> rtpPadIds = new HashSet<>();

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<>(this)
                .addActionStage("Collect Sfu Stream relations", () -> {
                    if (this.rtpPadIds.size() < 1) {
                        return;
                    }
                    Map<UUID, SfuRtpPadDTO> rtpPads = this.hazelcastMaps.getSFURtpPads().getAll(this.rtpPadIds);
                    if (rtpPads.size() < 1) {
                        return;
                    }
                    var streamIds = rtpPads.values().stream()
                            .filter(dto -> Objects.nonNull(dto.streamId))
                            .map(dto -> dto.streamId)
                            .collect(Collectors.toSet());
                    if (0 < streamIds.size()) {
                        var sfuStreams = this.hazelcastMaps.getSfuStreams().getAll(streamIds);
                        if (0 < sfuStreams.size()) {
                            this.result.sfuStreams.putAll(sfuStreams);
                        }
                    }
                    var sinkIds = rtpPads.values().stream()
                            .filter(dto -> Objects.nonNull(dto.sinkId))
                            .map(dto -> dto.sinkId)
                            .collect(Collectors.toSet());
                    if (0 < sinkIds.size()) {
                        var sfuSinks = this.hazelcastMaps.getSfuSinks().getAll(sinkIds);
                        if (0 < sfuSinks.size()) {
                            this.result.sfuSinks.putAll(sfuSinks);
                        }
                    }
                })
                .addTerminalSupplier("Completed", () -> {
                    return this.result;
                })
        .build();
    }

    public FetchSfuRelationsTask whereSfuRtpPadIds(Set<UUID> sfuRtpPadIds) {
        this.rtpPadIds.addAll(sfuRtpPadIds);
        return this;
    }

    @Override
    protected void validate() {

    }
}
