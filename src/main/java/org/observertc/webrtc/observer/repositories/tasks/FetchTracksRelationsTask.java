package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.StreamDirection;
import org.observertc.webrtc.observer.micrometer.ExposedMetrics;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

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
    private Map<UUID, List<MediaTrackDTO>> rtpStreamIdToInboundTrackDTOs = new HashMap<>();

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
                                    inboundMediaTrackDTO.callId,
                                    outboundMediaTrackDTO.clientId,
                                    outboundMediaTrackDTO.userId,
                                    outboundMediaTrackDTO.peerConnectionId,
                                    outboundMediaTrackDTO.trackId
                            ));
                            return;
                        }
                    }

                    UUID rtpStreamId = inboundMediaTrackDTO.rtpStreamId;
                    if (Objects.nonNull(rtpStreamId)) { // track is streamed to an SFU
                        List<MediaTrackDTO> rtpStreamInboundTrackDTOs = this.rtpStreamIdToInboundTrackDTOs.get(rtpStreamId);
                        if (Objects.isNull(rtpStreamInboundTrackDTOs)) {
                            rtpStreamInboundTrackDTOs = new LinkedList<>();
                            this.rtpStreamIdToInboundTrackDTOs.put(rtpStreamId, rtpStreamInboundTrackDTOs);
                        }
                        rtpStreamInboundTrackDTOs.add(inboundMediaTrackDTO);
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
            .addActionStage("Matching Tracks by RtpStream Ids", () -> {
                if (this.rtpStreamIdToInboundTrackDTOs.size() < 1) {
                    return;
                }
                var outboundTrackIds = this.hazelcastMaps.getRtpStreamIdsToOutboundTrackIds().getAll(this.rtpStreamIdToInboundTrackDTOs.keySet());
                outboundTrackIds.forEach((rtpStreamId, outboundTrackId) -> {
                    var outboundMediaTrack = this.hazelcastMaps.getMediaTracks().get(outboundTrackId);
                    if (Objects.isNull(outboundMediaTrack)) {
                        logger.warn("Outbound track does not exists referenced by rtp stream id {} (not existing) outbound track id {} ", rtpStreamId, outboundTrackId);
                        return;
                    }
                    if (!StreamDirection.OUTBOUND.equals(outboundMediaTrack.direction)) {
                        logger.warn("Matched Track {} for Rtp Stream Id {} is not an outbound track", outboundMediaTrack, rtpStreamId);
                        return;
                    }
                    var inboundTrackDTOs = rtpStreamIdToInboundTrackDTOs.get(rtpStreamId);
                    if (Objects.isNull((inboundTrackDTOs))) {
                        // WTF?!
                        logger.warn("Inbound Track DTOs, which supposed to be a list for RtpStream Id {} is null...", inboundTrackDTOs, rtpStreamId);
                        return;
                    }
                    inboundTrackDTOs.forEach(inboundMediaTrack -> {
                        if (Objects.isNull(inboundMediaTrack.callId) || !inboundMediaTrack.callId.equals(outboundMediaTrack.callId)) {
                            logger.warn("CallId for in-, and outbound media tracks ({}, {}) are null or does not match", inboundMediaTrack.callId, outboundMediaTrack.callId);
                            return;
                        }
                        this.result.inboundTrackMatchIds.put(inboundMediaTrack.trackId, new MatchedIds(
                                inboundMediaTrack.trackId,
                                inboundMediaTrack.peerConnectionId,
                                inboundMediaTrack.clientId,
                                inboundMediaTrack.callId,
                                outboundMediaTrack.clientId,
                                outboundMediaTrack.userId,
                                outboundMediaTrack.peerConnectionId,
                                outboundMediaTrack.trackId
                        ));
                        this.hazelcastMaps
                                .getInboundTrackIdsToOutboundTrackIds()
                                .put(inboundMediaTrack.trackId, outboundMediaTrack.trackId);
                        logger.info("RtpStreamId Matching: Inbound Track {} from client {} is matched to outbound track {} from client {}", inboundMediaTrack.trackId, inboundMediaTrack.clientId, outboundMediaTrack.trackId, outboundMediaTrack.clientId);
                    });
                });

            })
            .addActionStage("Matching calls by SSRCs",
                () -> {
                    if (this.callIds.size() < 1) {
                        return;
                    }
                    this.callIds.forEach(callId -> {
                        Collection<UUID> clientIds = this.hazelcastMaps.getCallToClientIds().get(callId);
                        clientIds.forEach(clientId -> {
                            Collection<UUID> peerConnectionIds = this.hazelcastMaps.getClientToPeerConnectionIds().get(clientId);
                            peerConnectionIds.forEach(peerConnectionId -> {
                                Set<UUID> trackIds = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().get(peerConnectionId).stream().collect(Collectors.toSet());
                                var outboundMediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(trackIds);
                                outboundMediaTrackDTOs.forEach((trackId, outboundMediaTrackDTO) -> {
                                    List<MediaTrackDTO> inboundMediaTrackDTOs = this.ssrcToInboundTracks.get(outboundMediaTrackDTO.ssrc);
                                    if (Objects.isNull(inboundMediaTrackDTOs)) {
                                        return;
                                    }
                                    inboundMediaTrackDTOs.forEach(inboundMediaTrackDTO -> {
                                        if (!inboundMediaTrackDTO.callId.equals(outboundMediaTrackDTO.callId)) {
                                            return;
                                        }
                                        var matchingIds = new MatchedIds(
                                                inboundMediaTrackDTO.trackId,
                                                inboundMediaTrackDTO.peerConnectionId,
                                                inboundMediaTrackDTO.clientId,
                                                inboundMediaTrackDTO.callId,
                                                outboundMediaTrackDTO.clientId,
                                                outboundMediaTrackDTO.userId,
                                                outboundMediaTrackDTO.peerConnectionId,
                                                outboundMediaTrackDTO.trackId
                                        );
                                        this.result.inboundTrackMatchIds.put(inboundMediaTrackDTO.trackId, matchingIds);
                                        this.hazelcastMaps
                                                .getInboundTrackIdsToOutboundTrackIds()
                                                .put(inboundMediaTrackDTO.trackId, outboundMediaTrackDTO.trackId);
                                        logger.info("SSRC Matching: Inbound Track {} from client {} is matched to outbound track {} from client {}", inboundMediaTrackDTO.trackId, inboundMediaTrackDTO.clientId, outboundMediaTrackDTO.trackId, outboundMediaTrackDTO.clientId);
                                    });
                                });
                            });
                        });
                    });
                })
                .addTerminalSupplier("Completed", () -> {
                    return this.result;
                })
        .build();
    }

    public FetchTracksRelationsTask whereMediaTrackIds(Set<UUID> mediaTrackIds) {
        this.inboundTrackIds.addAll(mediaTrackIds);
        return this;
    }


    @Override
    protected void validate() {

    }

    public static class MatchedIds {
        public final UUID inboundTrackId;
        public final UUID inboundPeerConnectionId;
        public final UUID inboundClientId;
        public final UUID callId;
        public final UUID outboundClientId;
        public final String outboundUserId;
        public final UUID outboundPeerConnectionId;
        public final UUID outboundTrackId;

        private MatchedIds(UUID inboundTrackId, UUID inboundPeerConnectionId, UUID inboundClientId, UUID callId, UUID outboundClientId, String outboundUserId, UUID outboundPeerConnectionId, UUID outboundTrackId) {
            this.inboundTrackId = inboundTrackId;
            this.inboundPeerConnectionId = inboundPeerConnectionId;
            this.inboundClientId = inboundClientId;
            this.callId = callId;
            this.outboundClientId = outboundClientId;
            this.outboundUserId = outboundUserId;
            this.outboundPeerConnectionId = outboundPeerConnectionId;
            this.outboundTrackId = outboundTrackId;
        }
    }
}
