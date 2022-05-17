package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.common.Utils;
import org.observertc.observer.dto.MediaTrackDTO;
import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Prototype
public class FetchTracksRelationsTask extends ChainedTask<FetchTracksRelationsTask.Report> {

    private static final Logger logger = LoggerFactory.getLogger(FetchTracksRelationsTask.class);
    public static final Report EMPTY_REPORT = new Report();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ExposedMetrics exposedMetrics;

    public static class Report {
        public final Map<UUID, MatchedIds> inboundTrackMatchIds = new HashMap<>();
    }

    private Report result = new Report();

    private Set<UUID> inboundTrackIds = new HashSet<>();

    // match it by rtp stream ids
    private Map<UUID, List<MediaTrackDTO>> sfuSinkIdToInboundTrackDTOs = new HashMap<>();

    // match it by ssrcs
    private Set<UUID> callIds = new HashSet<>();
    private Map<Long, List<MediaTrackDTO>> ssrcToInboundTracks = new HashMap<>();

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<>(this)
            .<Set<UUID>> addConsumerEntry("Merge all provided inputs",
                    () -> {}, // no input was invoked
                    input -> { // input was invoked, so we may got some names through that
                    if (Objects.isNull(input)) {
                        return;
                    }
                    this.inboundTrackIds.addAll(input);

            })
            .addActionStage("Collecting already identified tracks", () -> {
                var mappings = this.hazelcastMaps.getInboundTrackIdsToOutboundTrackIds().getAll(this.inboundTrackIds);
                var existingOutboundIds = mappings.values().stream().collect(Collectors.toSet());
                Map<UUID, MediaTrackDTO> outboundMediaTrackDTOs = 0 < mappings.size() ? this.hazelcastMaps.getMediaTracks().getAll(existingOutboundIds) : Collections.EMPTY_MAP;
                var inboundMediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(this.inboundTrackIds);
                inboundMediaTrackDTOs.forEach((trackId, inboundMediaTrackDTO) -> {
                    UUID outboundTrackId = mappings.get(trackId);
                    if (Objects.nonNull(outboundTrackId)) {
                        MediaTrackDTO outboundMediaTrackDTO = outboundMediaTrackDTOs.get(outboundTrackId);
                        if (Objects.nonNull(outboundMediaTrackDTO)) {
                            if (!outboundMediaTrackDTO.callId.equals(inboundMediaTrackDTO.callId)) {
                                logger.warn("Two tracks matched already supposed to have the same callId, {}, {}", inboundMediaTrackDTO, outboundMediaTrackDTO);
                            }
                            this.result.inboundTrackMatchIds.put(inboundMediaTrackDTO.trackId, new MatchedIds(
                                    inboundMediaTrackDTO.trackId,
                                    inboundMediaTrackDTO.peerConnectionId,
                                    inboundMediaTrackDTO.clientId,
                                    inboundMediaTrackDTO.userId,
                                    inboundMediaTrackDTO.callId,
                                    outboundMediaTrackDTO.clientId,
                                    outboundMediaTrackDTO.userId,
                                    outboundMediaTrackDTO.peerConnectionId,
                                    outboundMediaTrackDTO.trackId
                            ));
                            return;
                        }
                    }

                    UUID sfuSinkId = inboundMediaTrackDTO.sfuSinkId;
                    if (Objects.nonNull(sfuSinkId)) { // track is streamed to an SFU
                        List<MediaTrackDTO> sfuSinkIdToInboundTrackDTOs = this.sfuSinkIdToInboundTrackDTOs.get(sfuSinkId);
                        if (Objects.isNull(sfuSinkIdToInboundTrackDTOs)) {
                            sfuSinkIdToInboundTrackDTOs = new LinkedList<>();
                            this.sfuSinkIdToInboundTrackDTOs.put(sfuSinkId, sfuSinkIdToInboundTrackDTOs);
                        }
                        sfuSinkIdToInboundTrackDTOs.add(inboundMediaTrackDTO);
                        return;
                    }
                    // track belongs to a p2p call
                    List<MediaTrackDTO> mappingIdsList = this.ssrcToInboundTracks.get(inboundMediaTrackDTO.ssrc);
                    if (Objects.isNull(mappingIdsList)) {
                        mappingIdsList = new LinkedList<>();
                        this.ssrcToInboundTracks.put(inboundMediaTrackDTO.ssrc, mappingIdsList);
                    }
                    mappingIdsList.add(inboundMediaTrackDTO);
                    this.callIds.add(inboundMediaTrackDTO.callId);
                });
            })
            .addActionStage("Matching Tracks by SfuStreams", () -> {
                if (this.sfuSinkIdToInboundTrackDTOs.size() < 1) {
                    return;
                }
                var sfuSinks = this.hazelcastMaps.getSfuSinks().getAll(this.sfuSinkIdToInboundTrackDTOs.keySet());
                var sfuStreamIds = sfuSinks.values()
                        .stream()
                        .filter(dto -> Objects.nonNull(dto.sfuStreamId))
                        .map(dto -> dto.sfuStreamId)
                        .collect(Collectors.toSet());
                var sfuStreams = this.hazelcastMaps.getSfuStreams().getAll(sfuStreamIds);
                sfuSinks.forEach((sfuSinkId, sfuSink) -> {
                    if (Objects.isNull(sfuSink.sfuStreamId)) {
                        return;
                    }
                    var sfuStream = sfuStreams.get(sfuSink.sfuStreamId);
                    if (Objects.isNull(sfuStream) || Objects.isNull(sfuStream.trackId)) {
                        return;
                    }
                    var outboundMediaTrack = this.hazelcastMaps.getMediaTracks().get(sfuStream.trackId);
                    if (Objects.isNull(outboundMediaTrack)) {
                        logger.warn("Outbound track does not exists referenced by rtp stream id {} (not existing) outbound track id {} ", sfuSinkId, sfuSink);
                        return;
                    }
                    if (!StreamDirection.OUTBOUND.equals(outboundMediaTrack.direction)) {
                        logger.warn("Matched Track {} for Sfu Stream Id {} is not an outbound track", outboundMediaTrack, sfuSink.sfuStreamId);
                        return;
                    }
                    var inboundTrackDTOs = this.sfuSinkIdToInboundTrackDTOs.get(sfuSinkId);
                    if (Objects.isNull((inboundTrackDTOs))) {
                        // WTF?!
                        logger.warn("Inbound Track DTOs, which supposed to be a list for sfu sink Id {} is null...", inboundTrackDTOs, sfuSinkId);
                        return;
                    }
                    inboundTrackDTOs.forEach(inboundMediaTrack -> {
                        if (Objects.isNull(inboundMediaTrack.callId) || !inboundMediaTrack.callId.equals(outboundMediaTrack.callId)) {
                            logger.warn("CallId for in-, and outbound media tracks are null or does not match. {}, {}", JsonUtils.objectToString(inboundMediaTrack), JsonUtils.objectToString(outboundMediaTrack));
                            return;
                        }
                        this.result.inboundTrackMatchIds.put(inboundMediaTrack.trackId, new MatchedIds(
                                inboundMediaTrack.trackId,
                                inboundMediaTrack.peerConnectionId,
                                inboundMediaTrack.clientId,
                                inboundMediaTrack.userId,
                                inboundMediaTrack.callId,
                                outboundMediaTrack.clientId,
                                outboundMediaTrack.userId,
                                outboundMediaTrack.peerConnectionId,
                                outboundMediaTrack.trackId
                        ));
                        this.hazelcastMaps
                                .getInboundTrackIdsToOutboundTrackIds()
                                .put(inboundMediaTrack.trackId, outboundMediaTrack.trackId);
                        logger.info("Sfu - MediaTrack matching: Inbound Track {} from client {} is matched to outbound track {} from client {}", inboundMediaTrack.trackId, inboundMediaTrack.clientId, outboundMediaTrack.trackId, outboundMediaTrack.clientId);
                    });
                });

            })
            .addActionStage("Matching calls by SSRCs",
                () -> {
                    if (this.callIds.size() < 1) {
                        return;
                    }
                    var ssrcToInboundTrackIds = this.hazelcastMaps.getMediaTracks().getAll(inboundTrackIds)
                            .values().stream().collect(groupingBy(track -> track.ssrc));

                    this.callIds.forEach(callId -> {
                        Collection<UUID> clientIds = this.hazelcastMaps.getCallToClientIds().get(callId);
                        var peerConnectionIds = clientIds.stream().map(this.hazelcastMaps.getClientToPeerConnectionIds()::get)
                                        .flatMap(Collection::stream)
                                        .collect(Collectors.toSet());
                        var peerConnections = this.hazelcastMaps.getPeerConnections().getAll(peerConnectionIds);
                        var outboundMediaTrackIds = peerConnections.keySet().stream().map(this.hazelcastMaps.getPeerConnectionToOutboundTrackIds()::get)
                                        .flatMap(Collection::stream)
                                        .collect(Collectors.toSet());
                        var outboundMediaTracks = this.hazelcastMaps.getMediaTracks().getAll(outboundMediaTrackIds);
                        for (var outboundMediaTrack : outboundMediaTracks.values()) {
                            var ssrcMatchedInboundMediaTracks = ssrcToInboundTrackIds.get(outboundMediaTrack.ssrc);
                            if (Objects.isNull(ssrcMatchedInboundMediaTracks)) {
                                continue;
                            }
                            if (!outboundMediaTrack.callId.equals(callId)) {
                                // should not happen
                                continue;
                            }
                            for (var inboundMediaTrack : ssrcMatchedInboundMediaTracks) {
                                // already found
                                if (this.result.inboundTrackMatchIds.containsKey(inboundMediaTrack.trackId)) {
                                    continue;
                                }
                                // not in this call
                                if (!inboundMediaTrack.callId.equals(callId)) {
                                    continue;
                                }
                                if (!inboundMediaTrack.direction.equals(StreamDirection.INBOUND)) {
                                    continue;
                                }
                                var matchingIds = new MatchedIds(
                                        inboundMediaTrack.trackId,
                                        inboundMediaTrack.peerConnectionId,
                                        inboundMediaTrack.clientId,
                                        inboundMediaTrack.userId,
                                        inboundMediaTrack.callId,
                                        outboundMediaTrack.clientId,
                                        outboundMediaTrack.userId,
                                        outboundMediaTrack.peerConnectionId,
                                        outboundMediaTrack.trackId
                                );
                                this.result.inboundTrackMatchIds.put(inboundMediaTrack.trackId, matchingIds);
                                this.hazelcastMaps
                                        .getInboundTrackIdsToOutboundTrackIds()
                                        .put(inboundMediaTrack.trackId, outboundMediaTrack.trackId);
                            }
                        }
                    });
                })
                .addTerminalSupplier("Completed", () -> {
                    return this.result;
                })
        .build();
    }

    public FetchTracksRelationsTask whereInboundMediaTrackIds(Set<UUID> mediaTrackIds) {
        if (mediaTrackIds == null) return this;
        mediaTrackIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.inboundTrackIds::add);
        return this;
    }


    @Override
    protected void validate() {

    }

    public static class MatchedIds {
        public final UUID inboundTrackId;
        public final UUID inboundPeerConnectionId;
        public final String inboundUserId;
        public final UUID inboundClientId;
        public final UUID callId;
        public final UUID outboundClientId;
        public final String outboundUserId;
        public final UUID outboundPeerConnectionId;
        public final UUID outboundTrackId;

        private MatchedIds(UUID inboundTrackId, UUID inboundPeerConnectionId, UUID inboundClientId, String inboundUserId,
                           UUID callId, UUID outboundClientId, String outboundUserId, UUID outboundPeerConnectionId, UUID outboundTrackId) {
            this.inboundTrackId = inboundTrackId;
            this.inboundPeerConnectionId = inboundPeerConnectionId;
            this.inboundClientId = inboundClientId;
            this.inboundUserId = inboundUserId;
            this.callId = callId;
            this.outboundClientId = outboundClientId;
            this.outboundUserId = outboundUserId;
            this.outboundPeerConnectionId = outboundPeerConnectionId;
            this.outboundTrackId = outboundTrackId;
        }
    }
}
