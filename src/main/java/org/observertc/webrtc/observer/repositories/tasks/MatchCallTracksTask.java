package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.SfuPodRole;
import org.observertc.webrtc.observer.dto.SfuRtpStreamPodDTO;
import org.observertc.webrtc.observer.dto.StreamDirection;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class MatchCallTracksTask extends ChainedTask<List<MatchCallTracksTask.MatchedIds>> {

    private static final Logger logger = LoggerFactory.getLogger(MatchCallTracksTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    private List<MatchedIds> result = new LinkedList<>();

    private Set<UUID> inboundTrackIds = new HashSet<>();

    // task necessities.
    private Set<UUID> callIds = new HashSet<>();
    private Map<Long, List<MediaTrackDTO>> ssrcToInboundTracks = new HashMap<>();

    private Map<UUID, MediaTrackDTO> sfuRtpSinkIdsToInboundTrackDTOs = new HashMap<>();

    @PostConstruct
    void setup() {

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
                            this.result.add(new MatchedIds(
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
                    UUID sfuSinkId = inboundMediaTrackDTO.sfuPodId;
                    if (Objects.nonNull(sfuSinkId)) { // track is streamed to an SFU
                        this.sfuRtpSinkIdsToInboundTrackDTOs.put(sfuSinkId, inboundMediaTrackDTO);
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
            .addActionStage("Matching SFU Calls", () -> {
                if (this.sfuRtpSinkIdsToInboundTrackDTOs.size() < 1) {
                    return;
                }
                var sfuRtpStreamPods = this.hazelcastMaps.getSFURtpPods().getAll(this.sfuRtpSinkIdsToInboundTrackDTOs.keySet());
                if (sfuRtpStreamPods.size() < 1) {
                    return;
                }
                sfuRtpStreamPods.forEach((sfuStreamId, sfuSink) -> {
                    var inboundMediaTrack = this.sfuRtpSinkIdsToInboundTrackDTOs.get(sfuStreamId);
                    if (!SfuPodRole.SINK.equals(sfuSink.sfuPodRole)) {
                        logger.warn("Inconsistent in matchings! reported inbound track id does not belong to a SFU stream, which is outbound type. inboundTrack: {} sfuStream: {} ", inboundMediaTrack, sfuSink);
                        return;
                    }
                    SfuRtpStreamPodDTO sfuSource = null;
                    var relatedPodIds = this.hazelcastMaps.getSfuStreamToRtpPodIds().get(sfuSink.sfuStreamId);
                    for (Iterator<UUID> it = relatedPodIds.iterator(); it.hasNext(); ) {
                        UUID relatedPodId = it.next();
                        sfuSource = this.hazelcastMaps.getSFURtpPods().get(relatedPodId);
                        if (!SfuPodRole.SOURCE.equals(sfuSource.sfuPodRole) ||
                            !inboundMediaTrack.callId.equals(sfuSource.callId) ||
                            Objects.isNull(sfuSource.trackId)) {
                            sfuSource = null;
                            continue;
                        }
                    }
                    if (Objects.isNull(sfuSource)) {
                        logger.info("Cannot find matching sfuSource to inboundTrack: {} sfuStream: {} ", inboundMediaTrack, sfuSink);
                        return;
                    }
                    var outboundMediaTrack = this.hazelcastMaps.getMediaTracks().get(sfuSource.trackId);
                    if (Objects.isNull(outboundMediaTrack)) {
                        logger.warn("Outbound track does not exists referenced by sfuSource {} for inbound track {}, sfuSink {} ", sfuSource, inboundMediaTrack, sfuSink);
                        return;
                    }
                    if (!StreamDirection.OUTBOUND.equals(outboundMediaTrack.direction) || !inboundMediaTrack.callId.equals(outboundMediaTrack.callId)) {
                        logger.warn("Matched outbound Track {} is either not an outbound track, or the callId is different than the inbound Track {}", outboundMediaTrack, inboundMediaTrack);
                        return;
                    }
                    this.result.add(new MatchedIds(
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
                });

            })
            .addActionStage("Matching P2P calls",
                () -> {
                    if (this.callIds.size() < 1) {
                        return;
                    }
                    List<MatchedIds> result = new LinkedList<>();
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
                                        result.add(matchingIds);
                                        this.hazelcastMaps
                                                .getInboundTrackIdsToOutboundTrackIds()
                                                .put(inboundMediaTrackDTO.trackId, outboundMediaTrackDTO.trackId);
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

    public MatchCallTracksTask whereMediaTrackIds(Set<UUID> mediaTrackIds) {
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
