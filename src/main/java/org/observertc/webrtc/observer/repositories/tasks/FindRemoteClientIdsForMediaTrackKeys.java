package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class FindRemoteClientIdsForMediaTrackKeys extends ChainedTask<List<FindRemoteClientIdsForMediaTrackKeys.MatchedIds>> {

    private static final Logger logger = LoggerFactory.getLogger(FindRemoteClientIdsForMediaTrackKeys.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    private Set<UUID> inboundTrackIds = new HashSet<>();

    // Stage climbing the ids up
    private Map<Long, List<MappingIds>> inboundMappings = new HashMap<>();
    private Map<Long, List<MappingIds>> outboundMappings = new HashMap<>();

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
                Map<UUID, MediaTrackDTO> mediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(this.inboundTrackIds);
                Set<UUID> peerConnectionIds = mediaTrackDTOs.values().stream().map(t -> t.peerConnectionId).collect(Collectors.toSet());
                Map<UUID, PeerConnectionDTO> peerConnectionDTOs = this.hazelcastMaps.getPeerConnections().getAll(peerConnectionIds);
                Set<UUID> clientIds = peerConnectionDTOs.values().stream().map(t -> t.clientId).collect(Collectors.toSet());
                Map<UUID, ClientDTO> clientDTOs = this.hazelcastMaps.getClients().getAll(clientIds);
                Set<UUID> callIds = new HashSet<>();
                mediaTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
                    var peerConnectionDTO = peerConnectionDTOs.get(mediaTrackDTO.peerConnectionId);
                    if (Objects.isNull(peerConnectionDTO)) return;
                    var clientDTO = clientDTOs.get(peerConnectionDTO.clientId);
                    if (Objects.isNull(clientDTO)) return;
                    var mappingIds = new MappingIds(trackId,
                            mediaTrackDTO.peerConnectionId,
                            peerConnectionDTO.clientId,
                            clientDTO.callId);
                    List<MappingIds> mappingIdsList = this.inboundMappings.getOrDefault(mediaTrackDTO.ssrc, new LinkedList<>());
                    mappingIdsList.add(mappingIds);
                    callIds.add(clientDTO.callId);
                });
                return callIds;
            })
            .<Set<UUID>>addConsumerStage("Gathering outbound track parental ids (Climbing down for Ids)",
                callIds -> {
                    callIds.forEach(callId -> {
                        Collection<UUID> clientIds = this.hazelcastMaps.getCallToClientIds().get(callId);
                        clientIds.forEach(clientId -> {
                            Collection<UUID> peerConnectionIds = this.hazelcastMaps.getClientToPeerConnectionIds().get(clientId);
                            peerConnectionIds.forEach(peerConnectionId -> {
                                Collection<UUID> trackIds = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().get(peerConnectionId);
                                trackIds.forEach(trackId -> {
                                    MediaTrackDTO mediaTrackDTO = this.hazelcastMaps.getMediaTracks().get(trackId);
                                    var outboundMapping = new MappingIds(
                                            trackId,
                                            peerConnectionId,
                                            clientId,
                                            callId
                                    );
                                    var outboundMappingsList = this.outboundMappings.getOrDefault(mediaTrackDTO.ssrc, new LinkedList<>());
                                    outboundMappingsList.add(outboundMapping);

                                });
                            });
                        });
                    });
                })
            .addTerminalSupplier("Matching Ids", () -> {
                List<MatchedIds> result = new LinkedList<>();
                this.inboundMappings.forEach((ssrc, inboundMappingsList) -> {
                    inboundMappingsList.forEach(inboundMappings -> {
                        List<MappingIds> outboundMappingsList = this.outboundMappings.getOrDefault(ssrc, Collections.EMPTY_LIST);
                        outboundMappingsList.stream()
                                .filter(outbMapping -> outbMapping.callId == inboundMappings.callId)
                                .forEach(outboundMappings -> {
                                    var matchedIts = new MatchedIds(
                                            inboundMappings.trackId,
                                            inboundMappings.peerConnectionId,
                                            inboundMappings.clientId,
                                            inboundMappings.callId,
                                            ssrc,
                                            outboundMappings.clientId,
                                            outboundMappings.peerConnectionId,
                                            outboundMappings.trackId
                                    );
                                    result.add(matchedIts);
                        });
                    });
                });
                return result;
            })
        .build();
    }

    public FindRemoteClientIdsForMediaTrackKeys whereMediaTrackIds(Set<UUID> mediaTrackIds) {
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
        public final UUID outboundPeerConnectionId;
        public final UUID outboundTrackId;

        private MatchedIds(UUID inboundTrackId, UUID inboundPeerConnectionId, UUID inboundClientId, UUID callId, Long ssrc, UUID outboundClientId, UUID outboundPeerConnectionId, UUID outboundTrackId) {
            this.inboundTrackId = inboundTrackId;
            this.inboundPeerConnectionId = inboundPeerConnectionId;
            this.inboundClientId = inboundClientId;
            this.callId = callId;
            this.SSRC = ssrc;
            this.outboundClientId = outboundClientId;
            this.outboundPeerConnectionId = outboundPeerConnectionId;
            this.outboundTrackId = outboundTrackId;
        }
    }
}
