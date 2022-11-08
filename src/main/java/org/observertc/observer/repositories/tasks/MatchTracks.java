package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.metrics.RepositoryMetrics;
import org.observertc.observer.repositories.*;
import org.observertc.observer.samples.ServiceRoomId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class MatchTracks extends ChainedTask<MatchTracks.Report> {

    private static final Logger logger = LoggerFactory.getLogger(MatchTracks.class);

    @Inject
    RepositoryMetrics exposedMetrics;

    @Inject
    OutboundTracksRepository outboundTracksRepository;

    @Inject
    InboundTracksRepository inboundTracksRepository;

    @Inject
    ClientsRepository clientsRepository;

    @Inject
    CallsRepository callsRepository;


    public static class Report {
        public final Map<String, Match> inboundMatches = new HashMap<>();
    }

    private Report result = new Report();

    private Set<String> outboundTrackIds = new HashSet<>();
    private Set<String> inboundTrackIds = new HashSet<>();

    private Set<String> matchedOutboundTrackIds = new HashSet<>();

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
            .addActionStage("Match By Sfu StreamId", () -> {
                var localOutboundTracks = this.outboundTracksRepository.getAll(this.outboundTrackIds);
                if (localOutboundTracks == null || localOutboundTracks.size() < 1) {
                    return;
                }
                var callIds = localOutboundTracks.values().stream().map(t -> t.getCallId()).collect(Collectors.toSet());
                var calls = this.callsRepository.fetchRecursively(callIds);
                var clients = calls.values().stream().flatMap(call -> call.getClients().entrySet().stream())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (oldV, newV) -> newV
                        ));
                var peerConnections = clients.values().stream()
                        .flatMap(c -> c.getPeerConnections().entrySet().stream())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (oldV, newV) -> newV
                        ));
                var outboundTracks = peerConnections.values().stream()
                        .flatMap(p -> p.getOutboundTracks().entrySet().stream())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (oldV, newV) -> newV
                        ));

                var outboundTracksBySfuStreamIds = this.mapOutboundTracksBySfuStreamIds(outboundTracks.values());
                if (outboundTracksBySfuStreamIds == null || outboundTracksBySfuStreamIds.size() < 1) {
                    return;
                }
                var inboundTracks = this.inboundTracksRepository.getAll(this.inboundTrackIds);
                if (inboundTracks == null || inboundTracks.size() < 1) {
                    return;
                }
                for (var inboundTrack : inboundTracks.values()) {
                    var sfuStreamId = inboundTrack.getSfuStreamId();
                    if (sfuStreamId == null) {
                        continue;
                    }
                    var outboundTrack = outboundTracksBySfuStreamIds.get(sfuStreamId);
                    if (outboundTrack == null) {
                        return;
                    }
                    if (!outboundTrack.getCallId().equals(inboundTrack.getCallId())) {
                        logger.warn("CallId must be equal to match outbound tracks to inbound tracks. InboundTrack: {}, OutboundTrack: {}", inboundTrack, outboundTrack);
                        continue;
                    }
                    // match
                    var match = new Match(
                            inboundTrack.getTrackId(),
                            inboundTrack.getPeerConnectionId(),
                            inboundTrack.getClientId(),
                            inboundTrack.getUserId(),
                            inboundTrack.getCallId(),
                            inboundTrack.getServiceRoomId(),
                            outboundTrack.getClientId(),
                            outboundTrack.getUserId(),
                            outboundTrack.getPeerConnectionId(),
                            outboundTrack.getTrackId()
                    );
                    this.result.inboundMatches.put(match.inboundTrackId, match);
                    this.matchedOutboundTrackIds.add(outboundTrack.getTrackId());
                }
            })
            .addActionStage("Match By SSRC", () -> {
                var unmatchedOutboundTrackIds = this.outboundTrackIds.stream()
                        .filter(trackId -> this.matchedOutboundTrackIds.contains(trackId) == false)
                        .collect(Collectors.toSet());
                var outboundTracks = this.outboundTracksRepository.getAll(unmatchedOutboundTrackIds);
                var callIds = outboundTracks.values().stream()
                        .map(OutboundTrack::getCallId)
                        .collect(Collectors.toSet());
                this.callsRepository.fetchRecursively(callIds);
                for (var entry : outboundTracks.entrySet()) {
                    var outboundTrackId = entry.getKey();
                    var outboundTrack = entry.getValue();
                    var outboundTrackSfuStreamId = outboundTrack.getSfuStreamId();
                    if (outboundTrackSfuStreamId == null || !outboundTrackSfuStreamId.isBlank()) {
                        // these outbound tracks were not matched by sfu stream before
                        continue;
                    }
                    var outboundPeerConnection = outboundTrack.getPeerConnection();
                    if (outboundPeerConnection == null) {
                        logger.warn("Peer Connection is null for outbound track {}", outboundTrack);
                        continue;
                    }
                    var outboundClient = outboundPeerConnection.getClient();
                    if (outboundClient == null) {
                        logger.warn("Client is null for peer connection {}", outboundPeerConnection);
                        continue;
                    }
                    var call = outboundClient.getCall();

                    var allClients = call.getClients();
                    for (var clientEntry : allClients.entrySet()) {
                        if (clientEntry.getKey() == outboundClient.getClientId()) {
                            continue;
                        }
                        var remoteClient = clientEntry.getValue();
                        for (var remotePeerConnection : remoteClient.getPeerConnections().values()) {
                            for (var inboundTrack : remotePeerConnection.getInboundTracks().values()) {
                                var matched = false;
                                // match by sfuStreamId
                                for (var ssrc : outboundTrack.getSSSRCs()) {
                                    if (inboundTrack.hasSSRC(ssrc)) {
                                        matched = true;
                                        // that is a match
                                        break;
                                    }
                                }
                                if (!matched) {
                                    continue;
                                }
                                var match = new Match(
                                        inboundTrack.getTrackId(),
                                        remotePeerConnection.getPeerConnectionId(),
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
            })
            .addTerminalSupplier("Completed", () -> {
                return this.result;
            })
        .build();
    }

    public MatchTracks whereOutboundTrackIds(Set<String> mediaTrackIds) {
        if (mediaTrackIds == null) return this;
        mediaTrackIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.outboundTrackIds::add);
        return this;
    }

    public MatchTracks whereInboundTrackIds(Set<String> mediaTrackIds) {
        if (mediaTrackIds == null) return this;
        mediaTrackIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.inboundTrackIds::add);
        return this;
    }

    private Map<String, OutboundTrack> mapOutboundTracksBySfuStreamIds(Collection<OutboundTrack> outboundTracks) {
        if (outboundTracks == null || outboundTracks.size() < 1) {
            return Collections.emptyMap();
        }
        var result = new HashMap<String, OutboundTrack>();
        for (var outboundTrack : outboundTracks) {
            var sfuStreamId = outboundTrack.getSfuStreamId();
            if (sfuStreamId == null || sfuStreamId.isBlank()) {
                continue;
            }
            if (result.containsKey(sfuStreamId)) {
                var alreadyExistingOutboundTrack = result.get(sfuStreamId);
                logger.warn("Duplicated sfu stream id for outbound tracks: {}, {}", alreadyExistingOutboundTrack, outboundTrack);
                continue;
            }
            result.put(sfuStreamId, outboundTrack);
        }
        return Collections.unmodifiableMap(result);
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