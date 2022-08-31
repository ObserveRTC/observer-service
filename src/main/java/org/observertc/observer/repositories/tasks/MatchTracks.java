package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.metrics.RepositoryMetrics;
import org.observertc.observer.repositories.OutboundAudioTracksRepository;
import org.observertc.observer.repositories.OutboundVideoTracksRepository;
import org.observertc.observer.samples.ServiceRoomId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;

@Prototype
public class MatchTracks extends ChainedTask<MatchTracks.Report> {

    private static final Logger logger = LoggerFactory.getLogger(MatchTracks.class);
    public static final Report EMPTY_REPORT = new Report();

    @Inject
    RepositoryMetrics exposedMetrics;

    @Inject
    OutboundAudioTracksRepository outboundAudioTracksRepository;

    @Inject
    OutboundVideoTracksRepository outboundVideoTracksRepository;

    public static class Report {
        public final Map<String, Match> inboundAudioMatches = new HashMap<>();
        public final Map<String, Match> inboundVideoMatches = new HashMap<>();
    }

    private Report result = new Report();

    private Set<String> outboundAudioTrackIds = new HashSet<>();
    private Set<String> outboundVideoTrackIds = new HashSet<>();

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<>(this)
            .<Set<ServiceRoomId>> addConsumerEntry("Merge all provided inputs",
                    () -> {}, // no input was invoked
                    input -> { // input was invoked, so we may got some names through that
                    if (Objects.isNull(input)) {
                        return;
                    }
            })
            .addActionStage("Fetch Inbound To Outbound Audio Tracks", () -> {
                if (this.outboundAudioTrackIds == null || this.outboundAudioTrackIds.size() < 1) {
                    return;
                }
                var outboundAudioTracks = this.outboundAudioTracksRepository.getAll(this.outboundAudioTrackIds);
                for (var entry : outboundAudioTracks.entrySet()) {
                    var outboundAudioTrackId = entry.getKey();
                    var outboundAudioTrack = entry.getValue();
                    var outboundPeerConnection = outboundAudioTrack.getPeerConnection();
                    var outboundClient = outboundPeerConnection.getClient();
                    var call = outboundClient.getCall();
                    var allClients = call.getClients();
                    for (var clientEntry : allClients.entrySet()) {
                        if (clientEntry.getKey() == outboundClient.getClientId()) {
                            continue;
                        }
                        var remoteClient = clientEntry.getValue();
                        for (var inboundPeerConnection : remoteClient.getPeerConnections().values()) {
                            for (var inboundAudioTrack : inboundPeerConnection.getInboundAudioTracks().values()) {
                                var matched = false;
                                if (outboundAudioTrack.getSfuStreamId() != null) {
                                    if (inboundAudioTrack.getSfuStreamId() == outboundAudioTrack.getSfuStreamId()) {
                                        matched = true;
                                    } else {
                                        continue;
                                    }
                                }
                                // match by ssrcs
                                for (var ssrc : outboundAudioTrack.getSSSRCs()) {
                                    if (inboundAudioTrack.hasSSRC(ssrc)) {
                                        matched = true;
                                        // that is a match
                                        break;
                                    }
                                }
                                if (matched) {
                                    // thats a match
                                    var match = new Match(
                                            inboundAudioTrack.getTrackId(),
                                            inboundPeerConnection.getPeerConnectionId(),
                                            remoteClient.getClientId(),
                                            remoteClient.getUserId(),
                                            call.getCallId(),
                                            call.getServiceRoomId(),
                                            outboundClient.getClientId(),
                                            outboundClient.getUserId(),
                                            outboundPeerConnection.getPeerConnectionId(),
                                            outboundAudioTrackId
                                    );
                                    this.result.inboundAudioMatches.put(match.inboundTrackId, match);
                                }
                            }
                        }
                    }
                }
            })

            .addActionStage("Fetch Inbound To Outbound Video Tracks", () -> {
                if (this.outboundVideoTrackIds == null || this.outboundVideoTrackIds.size() < 1) {
                    return;
                }
                var outboundVideoTracks = this.outboundVideoTracksRepository.getAll(this.outboundVideoTrackIds);
                for (var entry : outboundVideoTracks.entrySet()) {
                    var outboundVideoTrackId = entry.getKey();
                    var outboundVideoTrack = entry.getValue();
                    var outboundPeerConnection = outboundVideoTrack.getPeerConnection();
                    var outboundClient = outboundPeerConnection.getClient();
                    var call = outboundClient.getCall();
                    var allClients = call.getClients();
                    for (var clientEntry : allClients.entrySet()) {
                        if (clientEntry.getKey() == outboundClient.getClientId()) {
                            continue;
                        }
                        var remoteClient = clientEntry.getValue();
                        for (var inboundPeerConnection : remoteClient.getPeerConnections().values()) {
                            for (var inboundVideoTrack : inboundPeerConnection.getInboundVideoTracks().values()) {
                                var matched = false;
                                if (outboundVideoTrack.getSfuStreamId() != null) {
                                    if (inboundVideoTrack.getSfuStreamId() == outboundVideoTrack.getSfuStreamId()) {
                                        matched = true;
                                    } else {
                                        continue;
                                    }
                                }
                                // match by ssrcs
                                for (var ssrc : outboundVideoTrack.getSSSRCs()) {
                                    if (inboundVideoTrack.hasSSRC(ssrc)) {
                                        matched = true;
                                        // that is a match
                                        break;
                                    }
                                }
                                if (matched) {
                                    // thats a match
                                    var match = new Match(
                                            inboundVideoTrack.getTrackId(),
                                            inboundPeerConnection.getPeerConnectionId(),
                                            remoteClient.getClientId(),
                                            remoteClient.getUserId(),
                                            call.getCallId(),
                                            call.getServiceRoomId(),
                                            outboundClient.getClientId(),
                                            outboundClient.getUserId(),
                                            outboundPeerConnection.getPeerConnectionId(),
                                            outboundVideoTrackId
                                    );
                                    this.result.inboundVideoMatches.put(match.inboundTrackId, match);
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
