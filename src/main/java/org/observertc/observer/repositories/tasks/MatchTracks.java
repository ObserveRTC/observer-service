package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.metrics.RepositoryMetrics;
import org.observertc.observer.repositories.InboundTracksRepository;
import org.observertc.observer.repositories.OutboundTracksRepository;
import org.observertc.observer.samples.ServiceRoomId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;

@Prototype
public class MatchTracks extends ChainedTask<MatchTracks.Report> {

    private static final Logger logger = LoggerFactory.getLogger(MatchTracks.class);

    @Inject
    RepositoryMetrics exposedMetrics;

    @Inject
    OutboundTracksRepository outboundTracksRepository;

    @Inject
    InboundTracksRepository inboundTracksRepository;

    public static class Report {
        public final Map<String, Match> inboundMatches = new HashMap<>();
    }

    private Report result = new Report();

    private Set<String> outboundAudioTrackIds = new HashSet<>();
    private Set<String> outboundVideoTrackIds = new HashSet<>();

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
                if (this.outboundAudioTrackIds == null || this.outboundAudioTrackIds.size() < 1) {
                    return;
                }
                var outboundAudioTracks = this.outboundTracksRepository.getAll(this.outboundAudioTrackIds);
                for (var entry : outboundAudioTracks.entrySet()) {
                    var outboundTrackId = entry.getKey();
                    var outboundTrack = entry.getValue();
                    var outboundPeerConnection = outboundTrack.getPeerConnection();
                    var outboundClient = outboundPeerConnection.getClient();
                    var call = outboundClient.getCall();
                    var allClients = call.getClients();
                    for (var clientEntry : allClients.entrySet()) {
                        if (clientEntry.getKey() == outboundClient.getClientId()) {
                            continue;
                        }
                        var remoteClient = clientEntry.getValue();
                        for (var inboundPeerConnection : remoteClient.getPeerConnections().values()) {
                            for (var inboundTrack : inboundPeerConnection.getInboundTracks().values()) {
                                var matched = false;
                                if (outboundTrack.getSfuStreamId() != null) {
                                    if (inboundTrack.getSfuStreamId() == outboundTrack.getSfuStreamId()) {
                                        matched = true;
                                    } else {
                                        continue;
                                    }
                                }
                                // match by ssrcs
                                for (var ssrc : outboundTrack.getSSSRCs()) {
                                    if (inboundTrack.hasSSRC(ssrc)) {
                                        matched = true;
                                        // that is a match
                                        break;
                                    }
                                }
                                if (matched) {
                                    var match = new Match(
                                            inboundTrack.getTrackId(),
                                            inboundPeerConnection.getPeerConnectionId(),
                                            remoteClient.getClientId(),
                                            remoteClient.getUserId(),
                                            call.getCallId(),
                                            call.getServiceRoomId(),
                                            outboundClient.getClientId(),
                                            outboundClient.getUserId(),
                                            outboundPeerConnection.getPeerConnectionId(),
                                            outboundTrackId
                                    );
                                    this.result.inboundMatches.put(match.inboundTrackId, match);
                                }
                            }
                        }
                    }
                }
            })
            .addTerminalSupplier("Completed", () -> {
                return this.result;
            })
        .build();
    }

    public MatchTracks whereOutboundAudioTrackIds(Set<String> mediaTrackIds) {
        if (mediaTrackIds == null) return this;
        mediaTrackIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.outboundAudioTrackIds::add);
        return this;
    }

    public MatchTracks whereOutboundVideoTrackIds(Set<String> mediaTrackIds) {
        if (mediaTrackIds == null) return this;
        mediaTrackIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.outboundAudioTrackIds::add);
        return this;
    }


    @Override
    protected void validate() {

    }

    public record Match(
            String inboundTrackId,
            String inboundPeerConnectionId,
            String inboundClientId,
            String inboundUserId,
            String callId,
            ServiceRoomId serviceRoomId,
            String outboundClientId,
            String outboundUserId,
            String outboundPeerConnectionId,
            String outboundTrackId
    ) {

    }
}
