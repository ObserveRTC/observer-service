package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.dto.SfuDTO;
import org.observertc.observer.dto.SfuRtpPadDTO;
import org.observertc.observer.dto.SfuTransportDTO;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Prototype
public class RefreshSfusTask extends ChainedTask<RefreshSfusTask.Report> {

    private static final Logger logger = LoggerFactory.getLogger(RefreshSfusTask.class);

    public static class Report {
        public Set<UUID> foundSfuIds = new HashSet<>();
        public Set<UUID> foundSfuTransportIds = new HashSet<>();
        public Set<UUID> foundRtpPadIds = new HashSet<>();
    }


    private Set<UUID> sfuIds = new HashSet<>();
    private Set<UUID> transportIds = new HashSet<>();
    private Set<UUID> rtpPadIds = new HashSet<>();
    private final Report report = new Report();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ExposedMetrics exposedMetrics;

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<Report>(this)
                .addActionStage("Check Sfu Rtp Pads",
                        // action
                        () -> {
                            if (this.rtpPadIds.size() < 1) {
                                return;
                            }
                            Map<UUID, SfuRtpPadDTO> rtpPadDTOs = this.hazelcastMaps.getSFURtpPads().getAll(this.rtpPadIds);
                            this.report.foundRtpPadIds.addAll(rtpPadDTOs.keySet());
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
                .addActionStage("Refresh Sfu Transport timestamps", () -> {
                    var now = Instant.now().toEpochMilli();
                    var refreshedSfuTransports = this.transportIds.stream().filter(Objects::nonNull).collect(Collectors.toMap(
                            Function.identity(),
                            id -> now
                    ));
                    this.hazelcastMaps.getRefreshedSfuTransports().putAll(refreshedSfuTransports);
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
        sfuIdsArray.stream().filter(Utils::expensiveNonNullCheck).forEach(this.sfuIds::add);
        return this;
    }

    public RefreshSfusTask withSfuIds(Set<UUID> sfuIds) {
        if (Objects.isNull(sfuIds)) {
            return this;
        }
        sfuIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.sfuIds::add);
        return this;
    }

    public RefreshSfusTask withSfuTransportIds(UUID... sfuTransportIds) {
        if (Objects.isNull(sfuTransportIds)) {
            return this;
        }
        var sfuTransportIdsArray = Arrays.asList(sfuTransportIds);
        sfuTransportIdsArray.stream().filter(Utils::expensiveNonNullCheck).forEach(this.transportIds::add);
        return this;
    }

    public RefreshSfusTask withSfuTransportIds(Set<UUID> sfuTransportIds) {
        if (Objects.isNull(sfuTransportIds)) {
            return this;
        }
        sfuTransportIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.transportIds::add);
        return this;
    }

    public RefreshSfusTask withSfuRtpPadIds(UUID... rtpStreamIds) {
        if (Objects.isNull(rtpStreamIds)) {
            return this;
        }
        var rtpStreamIdsList = Arrays.asList(rtpStreamIds);
        rtpStreamIdsList.stream().filter(Utils::expensiveNonNullCheck).forEach(this.rtpPadIds::add);
        return this;
    }

    public RefreshSfusTask withSfuRtpPadIds(Set<UUID> rtpPadIds) {
        if (Objects.isNull(rtpPadIds)) {
            return this;
        }
        rtpPadIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.rtpPadIds::add);
        return this;
    }

}
