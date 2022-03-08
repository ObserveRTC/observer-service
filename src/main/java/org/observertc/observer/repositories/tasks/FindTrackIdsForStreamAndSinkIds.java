package org.observertc.observer.repositories.tasks;

import com.hazelcast.multimap.MultiMap;
import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Prototype
public class FindTrackIdsForStreamAndSinkIds extends ChainedTask<FindTrackIdsForStreamAndSinkIds.Report> {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ExposedMetrics exposedMetrics;

    private Set<UUID> streamIds = new HashSet<>();
    private Set<UUID> sinkIds = new HashSet<>();

    public static class Report {
        public Map<UUID, UUID> rtpPadIdToOutboundTrackIds = new HashMap<>();
        public Map<UUID, UUID> rtpPadIdToInboundTrackIds = new HashMap<>();
    }

    private Report result = new Report();

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<>(this)
            .addActionStage("Collect Stream related rtp pads", () -> {
                if (this.streamIds.size() < 1) {
                    return;
                }
                Map<UUID, UUID> streamIdsToOutboundTrackIds = this.hazelcastMaps.getSfuStreamIdToOutboundTrackId().getAll(this.streamIds);
                if (streamIdsToOutboundTrackIds.size() < 1) {
                    return;
                }
                MultiMap<UUID, UUID> sfuStreamIdsToRtpPadIds = this.hazelcastMaps.getSfuStreamIdsToRtpPadIds();
                if (sfuStreamIdsToRtpPadIds.size() < 1) {
                    return;
                }
                this.streamIds.forEach(streamId -> {
                    var sfuRtpPadIds = sfuStreamIdsToRtpPadIds.get(streamId);
                    if (sfuRtpPadIds.size() < 1) {
                        return;
                    }
                    var outboundTrackId = streamIdsToOutboundTrackIds.get(streamId);
                    if (Objects.isNull(outboundTrackId)) {
                        return;
                    }
                    sfuRtpPadIds.forEach(sfuRtpPadId -> {
                        this.result.rtpPadIdToOutboundTrackIds.put(sfuRtpPadId, outboundTrackId);
                    });
                });
            })
            .addActionStage("Collect Sink related rtp pads", () -> {
                if (this.sinkIds.size() < 1) {
                    return;
                }
                Map<UUID, UUID> sinkIdsToInboundTrackIds = this.hazelcastMaps.getSfuSinkIdToInboundTrackId().getAll(this.streamIds);
                if (sinkIdsToInboundTrackIds.size() < 1) {
                    return;
                }
                MultiMap<UUID, UUID> sfuSinkIdsToRtpPadIds = this.hazelcastMaps.getSfuSinkIdsToRtpPadIds();
                if (sfuSinkIdsToRtpPadIds.size() < 1) {
                    return;
                }
                this.streamIds.forEach(streamId -> {
                    var sfuRtpPadIds = sfuSinkIdsToRtpPadIds.get(streamId);
                    if (sfuRtpPadIds.size() < 1) {
                        return;
                    }
                    var inboundTrackId = sinkIdsToInboundTrackIds.get(streamId);
                    if (Objects.isNull(inboundTrackId)) {
                        return;
                    }
                    sfuRtpPadIds.forEach(sfuRtpPadId -> {
                        this.result.rtpPadIdToOutboundTrackIds.put(sfuRtpPadId, inboundTrackId);
                    });
                });
            })
            .addTerminalSupplier("Fetch Call Ids", () -> {
                return this.result;
            })
            .build();
    }

    public FindTrackIdsForStreamAndSinkIds whereStreamIds(Set<UUID> streamIds) {
        Objects.requireNonNull(streamIds);
        this.streamIds.addAll(streamIds);
        return this;
    }

    public FindTrackIdsForStreamAndSinkIds whereSinkIds(Set<UUID> sinkIds) {
        Objects.requireNonNull(sinkIds);
        this.sinkIds.addAll(sinkIds);
        return this;
    }

    @Override
    protected void validate() {

    }
}
