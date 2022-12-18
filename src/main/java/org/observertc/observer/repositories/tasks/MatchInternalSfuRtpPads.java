package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.metrics.RepositoryMetrics;
import org.observertc.observer.repositories.OutboundTracksRepository;
import org.observertc.observer.repositories.SfuInboundRtpPadsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;

@Prototype
public class MatchInternalSfuRtpPads extends ChainedTask<MatchInternalSfuRtpPads.Report> {

    private static final Logger logger = LoggerFactory.getLogger(MatchInternalSfuRtpPads.class);

    @Inject
    RepositoryMetrics exposedMetrics;

    @Inject
    SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository;

    @Inject
    OutboundTracksRepository outboundTracksRepository;


    public static class Report {

        public Map<String, Match> internalInboundRtpPadMatches = new HashMap();
    }

    private Report result = new Report();

    private Set<String> sfuInboundRtpPadIds = new HashSet<>();



    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<>(this)
            .<Set<String>> addConsumerEntry("Merge all provided inputs",
                    () -> {}, // no input was invoked
                    input -> {
                        if (Objects.isNull(input)) {
                            return;
                        }
                        logger.error("This Task cannot be chained!");
            })
            .addActionStage("Fetch Inbound To Outbound Tracks", () -> {
                if (this.sfuInboundRtpPadIds == null || this.sfuInboundRtpPadIds.size() < 1) {
                    return;
                }
                // TODO: implement later
//                var inboundRtpPads = this.sfuInboundRtpPadsRepository.getAll(this.sfuInboundRtpPadIds);
//                var sfuStreamIds = inboundRtpPads.values()
//                        .stream().map(SfuInboundRtpPad::getSfuStream)
//                        .filter(Objects::nonNull).collect(Collectors.toSet());
//
//                var sfuStreamToCallIds = new HashMap<String, String>();
//                for (var it = this.outboundTracksRepository.iterator(); it.hasNext(); ) {
//                    var outboundTrack = it.next();
//                    var sfuStreamId = outboundTrack.getSfuStreamId();
//                    if (sfuStreamId == null) {
//                        continue;
//                    }
//                    if (!sfuStreamIds.contains(sfuStreamId)) {
//                        continue;
//                    }
//                    sfuStreamToCallIds.put(sfuStreamId, outboundTrack.getCallId());
//                }


            })
            .addTerminalSupplier("Completed", () -> {
                return this.result;
            })
        .build();
    }

    public MatchInternalSfuRtpPads whereSfuInboundRtpPadIds(Set<String> sfuInboundRtpPadIds) {
        if (sfuInboundRtpPadIds == null) return this;
        sfuInboundRtpPadIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.sfuInboundRtpPadIds::add);
        return this;
    }



    @Override
    protected void validate() {

    }

    public record Match(
            String remoteOutboundRtpPadId,
            String remoteSinkId,
            String remoteSfuTransportId,
            String remoteSfuId


    ) {

    }

}
