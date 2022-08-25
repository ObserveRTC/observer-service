package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.dto.SfuRtpPadDTO;
import org.observertc.observer.dto.SfuSinkDTO;
import org.observertc.observer.dto.SfuStreamDTO;
import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.metrics.RepositoryMetrics;
import org.observertc.observer.repositories.HamokStorages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Prototype
public class FetchSfuRelationsTask extends ChainedTask<FetchSfuRelationsTask.Report> {

    private static final Logger logger = LoggerFactory.getLogger(FetchSfuRelationsTask.class);

    public static final Report EMPTY_REPORT = new Report();

    @Inject
    HamokStorages hazelcastMaps;

    @Inject
    RepositoryMetrics exposedMetrics;

    public static class InternalRtpPadMatch {
        public UUID inboundRtpPadId;
        public UUID inboundStreamId;
        public UUID inboundTransportId;
        public UUID inboundSfuId;
        public UUID outboundSfuId;
        public UUID outboundTransportId;
        public UUID outboundSinkId;
        public UUID outboundRtpPadId;

    }

    public static class Report {
        public final Map<UUID, SfuStreamDTO> sfuStreams = new HashMap<>();
        public final Map<UUID, SfuSinkDTO> sfuSinks = new HashMap<>();
        public final Map<UUID, InternalRtpPadMatch> internalInboundRtpPadMatches = new HashMap<>();
    }

    private Report result = new Report();

    private Set<UUID> rtpPadIds = new HashSet<>();
    private Map<UUID, SfuRtpPadDTO> rtpPads = new HashMap<>();

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<>(this)
                .addActionStage("Collect Sfu Stream relations", () -> {
                    if (this.rtpPadIds.size() < 1) {
                        return;
                    }
                    this.rtpPads = this.hazelcastMaps.getSFURtpPads().getAll(this.rtpPadIds);
                    if (this.rtpPads.size() < 1) {
                        return;
                    }
                    var streamIds = this.rtpPads.values().stream()
                            .filter(dto -> Objects.nonNull(dto.streamId))
                            .map(dto -> dto.streamId)
                            .collect(Collectors.toSet());

                    if (0 < streamIds.size()) {
                        var sfuStreams = this.hazelcastMaps.getSfuStreams().getAll(streamIds);
                        if (0 < sfuStreams.size()) {
                            this.result.sfuStreams.putAll(sfuStreams);
                        }
                    }
                    var sinkIds = this.rtpPads.values().stream()
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
                .addActionStage("Match internal sfu Rtp pads", () -> {
                    if (this.rtpPadIds.size() < 1) {
                        return;
                    }
                    var internalInboundRtpPads = this.rtpPads.values().stream()
                            .filter(dto -> Boolean.TRUE.equals(dto.internal) && StreamDirection.INBOUND.equals(dto.streamDirection))
                            .collect(Collectors.toMap(
                                    dto -> dto.rtpPadId,
                                    Function.identity()
                            ));
                    if (internalInboundRtpPads.size() < 1) {
                        return;
                    }
                    var internalInboundRtpPadIds = internalInboundRtpPads.values().stream().map(dto -> dto.rtpPadId).collect(Collectors.toSet());
                    var internalInboundRtpPadIdToOutboundRtpPadIds = this.hazelcastMaps.getSfuInternalInboundRtpPadIdToOutboundRtpPadId().getAll(internalInboundRtpPadIds);
                    Map<UUID, SfuRtpPadDTO> matchedOutboundRtpPads;
                    if (0 < internalInboundRtpPadIdToOutboundRtpPadIds.size()) {
                        var outboundRtpPadIds = internalInboundRtpPadIdToOutboundRtpPadIds.values().stream().collect(Collectors.toSet());
                        matchedOutboundRtpPads = this.hazelcastMaps.getSFURtpPads().getAll(outboundRtpPadIds);
                    } else {
                        matchedOutboundRtpPads = Collections.EMPTY_MAP;
                    }

                    internalInboundRtpPads.forEach((rtpPadId, inboundRtpPad) -> {
                        if (inboundRtpPad.streamId == null) {
                            return;
                        }
                        var matchedOutboundRtpPadId = internalInboundRtpPadIdToOutboundRtpPadIds.get(inboundRtpPad.rtpPadId);
                        if (matchedOutboundRtpPadId != null) {
                            var matchedOutboundRtpPad = matchedOutboundRtpPads.get(matchedOutboundRtpPadId);
                            if (matchedOutboundRtpPad != null) {
                                var internalRtpPadMatch = createInternalInboundMatch(inboundRtpPad, matchedOutboundRtpPad);
                                this.result.internalInboundRtpPadMatches.put(inboundRtpPad.rtpPadId, internalRtpPadMatch);
                                return;
                            }
                        }
                        var outboundRtpPadIds = this.hazelcastMaps.getSfuStreamIdToInternalOutboundRtpPadIds().get(inboundRtpPad.streamId)
                                .stream().filter(Objects::nonNull).collect(Collectors.toSet());
                        if (outboundRtpPadIds.size() < 1) {
                            return;
                        }
                        var outboundRtpPads = this.hazelcastMaps.getSFURtpPads().getAll(outboundRtpPadIds);
                        var match = outboundRtpPads.values().stream().filter(outboundRtpPad -> {
                            var matched = outboundRtpPad.ssrc.equals(inboundRtpPad.ssrc);
                            return matched;
                        }).findFirst();
                        if (match.isEmpty()) {
                            return;
                        }
                        var matchedOutboundRtpPad = match.get();
                        var internalRtpPadMatch = this.createInternalInboundMatch(inboundRtpPad, matchedOutboundRtpPad);
                        this.result.internalInboundRtpPadMatches.put(inboundRtpPad.rtpPadId, internalRtpPadMatch);

                        logger.info("Internal Sfu Pad Matching outboundRtpPad: (rtpPadId: {}) InboundRtpPad:  (rtpPadId: {})", matchedOutboundRtpPad.rtpPadId, inboundRtpPad.rtpPadId);
                        this.hazelcastMaps.getSfuInternalInboundRtpPadIdToOutboundRtpPadId().put(inboundRtpPad.rtpPadId, matchedOutboundRtpPad.rtpPadId);
                    });
                })
                .addTerminalSupplier("Completed", () -> {
                    return this.result;
                })
        .build();
    }

    private InternalRtpPadMatch createInternalInboundMatch(SfuRtpPadDTO inboundRtpPad, SfuRtpPadDTO matchedOutboundRtpPad) {
        var internalRtpPadMatch = new InternalRtpPadMatch();
        internalRtpPadMatch.inboundRtpPadId = inboundRtpPad.rtpPadId;
        internalRtpPadMatch.inboundStreamId = inboundRtpPad.streamId;
        internalRtpPadMatch.inboundTransportId = inboundRtpPad.transportId;
        internalRtpPadMatch.inboundSfuId = inboundRtpPad.sfuId;
        internalRtpPadMatch.outboundSfuId = matchedOutboundRtpPad.sfuId;
        internalRtpPadMatch.outboundTransportId = matchedOutboundRtpPad.transportId;
        internalRtpPadMatch.outboundSinkId = matchedOutboundRtpPad.sinkId;
        internalRtpPadMatch.outboundRtpPadId = matchedOutboundRtpPad.rtpPadId;
        return internalRtpPadMatch;
    }

    public FetchSfuRelationsTask whereSfuRtpPadIds(Set<UUID> sfuRtpPadIds) {
        sfuRtpPadIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.rtpPadIds::add);
        return this;
    }

    @Override
    protected void validate() {

    }
}
