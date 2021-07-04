package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class FindRemoteClientIdsForMediaTrackIds extends ChainedTask<List<FindRemoteClientIdsForMediaTrackIds.MatchedIds>> {

    private static final Logger logger = LoggerFactory.getLogger(FindRemoteClientIdsForMediaTrackIds.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    private Set<UUID> inboundTrackIds = new HashSet<>();

    // Stage climbing the ids up
    private Map<Long, List<MediaTrackDTO>> inboundMediaTrackDTOs = new HashMap<>();
    private Map<Long, List<MediaTrackDTO>> outboundMappings = new HashMap<>();

    private class MappingIds {
        private final UUID trackId;
        private final UUID peerConnectionId;
        private final UUID clientId;
        private final UUID callId;

        private MappingIds(UUID trackId, UUID peerConnectionId, UUID clientId, UUID callId) {
            this.trackId = trackId;
            this.peerConnectionId = peerConnectionId;
            this.clientId = clientId;
            this.callId = callId;
        }
    }

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
            .<Set<UUID>>addSupplierStage("Gathering Media Track parental Ids (Climbing up for Ids)", () -> {
                var mediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(this.inboundTrackIds);
                Set<UUID> callIds = new HashSet<>();
                mediaTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
                    List<MediaTrackDTO> mappingIdsList = this.inboundMediaTrackDTOs.get(mediaTrackDTO.ssrc);
                    if (Objects.isNull(mappingIdsList)) {
                        mappingIdsList = new LinkedList<>();
                        this.inboundMediaTrackDTOs.put(mediaTrackDTO.ssrc, mappingIdsList);
                    }
                    mappingIdsList.add(mediaTrackDTO);
                    callIds.add(mediaTrackDTO.callId);
                });
                // TODO: sometime when we are in the beta, remove this.
//                this.hazelcastMaps.getMediaTracks().getAll(this.hazelcastMaps.getMediaTracks().keySet()).forEach((trackId, mediaTrackDTO) -> {
//                    logger.info("trackId {}, direction: {} SSRC: {}", trackId, mediaTrackDTO.direction, mediaTrackDTO.ssrc);
//                });
//                this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().keySet().forEach(pcId -> {
//                    var trackIds = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().get(pcId);
//                    logger.info("pcId: {} outbtrackids: {}", pcId, ObjectToString.toString(trackIds));
//                });
//                this.hazelcastMaps.getPeerConnectionToInboundTrackIds().keySet().forEach(pcId -> {
//                    var trackIds = this.hazelcastMaps.getPeerConnectionToInboundTrackIds().get(pcId);
//                    logger.info("pcId: {} inbtrackids: {}", pcId, ObjectToString.toString(trackIds));
//                });

                return callIds;
            })
            .<Set<UUID>>addTerminalFunction("Gathering outbound track parental ids (Climbing down for Ids)",
                callIds -> {
                    List<MatchedIds> result = new LinkedList<>();
                    callIds.forEach(callId -> {
                        Collection<UUID> clientIds = this.hazelcastMaps.getCallToClientIds().get(callId);
                        clientIds.forEach(clientId -> {
                            Collection<UUID> peerConnectionIds = this.hazelcastMaps.getClientToPeerConnectionIds().get(clientId);
                            peerConnectionIds.forEach(peerConnectionId -> {
                                Set<UUID> trackIds = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().get(peerConnectionId).stream().collect(Collectors.toSet());
                                var outboundMediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(trackIds);
                                outboundMediaTrackDTOs.forEach((trackId, outboundMediaTrackDTO) -> {
                                    List<MediaTrackDTO> inboundMediaTrackDTOs = this.inboundMediaTrackDTOs.get(outboundMediaTrackDTO.ssrc);
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
                                                inboundMediaTrackDTO.ssrc,
                                                outboundMediaTrackDTO.clientId,
                                                outboundMediaTrackDTO.userId,
                                                outboundMediaTrackDTO.peerConnectionId,
                                                outboundMediaTrackDTO.trackId
                                        );
                                        result.add(matchingIds);
                                    });
                                });
                            });
                        });
                    });
                    return result;
                })
        .build();
    }

    public FindRemoteClientIdsForMediaTrackIds whereMediaTrackIds(Set<UUID> mediaTrackIds) {
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
        public final Long SSRC;
        public final UUID outboundClientId;
        public final String outboundUserId;
        public final UUID outboundPeerConnectionId;
        public final UUID outboundTrackId;

        private MatchedIds(UUID inboundTrackId, UUID inboundPeerConnectionId, UUID inboundClientId, UUID callId, Long ssrc, UUID outboundClientId, String outboundUserId, UUID outboundPeerConnectionId, UUID outboundTrackId) {
            this.inboundTrackId = inboundTrackId;
            this.inboundPeerConnectionId = inboundPeerConnectionId;
            this.inboundClientId = inboundClientId;
            this.callId = callId;
            this.SSRC = ssrc;
            this.outboundClientId = outboundClientId;
            this.outboundUserId = outboundUserId;
            this.outboundPeerConnectionId = outboundPeerConnectionId;
            this.outboundTrackId = outboundTrackId;
        }
    }
}
