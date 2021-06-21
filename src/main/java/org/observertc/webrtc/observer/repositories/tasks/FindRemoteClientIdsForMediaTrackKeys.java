package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.samples.MediaTrackId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class FindRemoteClientIdsForMediaTrackKeys extends ChainedTask<Map<MediaTrackId, UUID>> {

    private static final Logger logger = LoggerFactory.getLogger(FindRemoteClientIdsForMediaTrackKeys.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    private Set<MediaTrackId> inboundMediaTrackIds = new HashSet<>();
    private boolean unmodifiableResult = false;

    // Stage climbing the ids up
    private Set<UUID> inboundPeerConnectionIds = Collections.EMPTY_SET; // must be replaced by a stage action
    private Set<UUID> inboundClientIds = Collections.EMPTY_SET; // must be replaced by a stage action
    private Set<UUID> callIds = new HashSet<>(); // must be filled with a stage action
    private Map<Long, List<MediaTrackMappedIds>> inboundMediaTrackIdLists = new HashMap<>(); // result of an action stage

    // Stage climbing the ids down
    private Map<UUID, UUID> clientIdToCallIds = Collections.EMPTY_MAP; // must be replaced by a stage action
    private Map<UUID, UUID> peerConnectionIdToClientIds = Collections.EMPTY_MAP; // must be replaced by a stage action
    private Map<String, UUID> outboundMediaTrackIdToPeerConnectionIds = Collections.EMPTY_MAP; // must be replaced by a stage action

    // Stage flattening the ids
    private Map<Long, List<MediaTrackMappedIds>> outboundMediaTrackIdLists = new HashMap<>(); // result of an action stage

    private class MediaTrackMappedIds {
        private final String mediaTrackKey;
        private final UUID peerConnectionId;
        private final UUID clientId;
        private final UUID callId;

        private MediaTrackMappedIds(String mediaTrackKey, UUID peerConnectionId, UUID clientId, UUID callId) {
            this.mediaTrackKey = mediaTrackKey;
            this.peerConnectionId = peerConnectionId;
            this.clientId = clientId;
            this.callId = callId;
        }
    }
    @PostConstruct
    void setup() {

        new Builder<>(this)
            .<Set<MediaTrackId>> addConsumerEntry("Merge all provided inputs",
                    () -> {}, // no input was invoked
                    input -> { // input was invoked, so we may got some names through that
                    if (Objects.isNull(input)) {
                        return;
                    }
                    this.inboundMediaTrackIds.addAll(input);

            })
            .addActionStage("Gathering Media Track parental Ids (Climbing up for Ids)", () -> {
                Set<String> inboundMediaTrackKeys = this.inboundMediaTrackIds.stream().map(mediaTrackId -> mediaTrackId.getKey()).collect(Collectors.toSet());
                Map<String, MediaTrackDTO> inboundMediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(inboundMediaTrackKeys);
                this.inboundPeerConnectionIds = inboundMediaTrackDTOs.values().stream().map(dto -> dto.peerConnectionId).collect(Collectors.toSet());
                Map<UUID, PeerConnectionDTO> inboundPeerConnectionDTOs = this.hazelcastMaps.getPeerConnections().getAll(inboundPeerConnectionIds);
                this.inboundClientIds = inboundPeerConnectionDTOs.values().stream().map(dto -> dto.clientId).collect(Collectors.toSet());
                Map<UUID, ClientDTO> inboundPeerConnectionClientDTOs = this.hazelcastMaps.getClients().getAll(inboundClientIds);
                inboundPeerConnectionClientDTOs.values().stream().map(clientDTO -> clientDTO.callId).collect(Collectors.toSet());

                inboundMediaTrackDTOs.forEach((mediaTrackKey, mediaTrackDTO) -> {
                    PeerConnectionDTO peerConnectionDTO = inboundPeerConnectionDTOs.get(mediaTrackDTO.peerConnectionId);
                    if (Objects.isNull(peerConnectionDTO)) {
                        logger.warn("Cannot find peer connection id {}, although media track {} refers to it", mediaTrackDTO.peerConnectionId, mediaTrackDTO);
                        return;
                    }
                    ClientDTO clientDTO = inboundPeerConnectionClientDTOs.get(peerConnectionDTO.clientId);
                    if (Objects.isNull(clientDTO)) {
                        logger.warn("Cannot find client id {}, although peer connection DTO {} refers to it", peerConnectionDTO.clientId, peerConnectionDTO);
                        return;
                    }

                    Long SSRC = mediaTrackDTO.ssrc;
                    List<MediaTrackMappedIds> mediaTrackMappedIdsList = this.inboundMediaTrackIdLists.get(SSRC);
                    if (Objects.isNull(mediaTrackMappedIdsList)) {
                        mediaTrackMappedIdsList = new LinkedList<>();
                        this.inboundMediaTrackIdLists.put(SSRC, mediaTrackMappedIdsList);
                    }
                    var callId = clientDTO.callId;
                    this.callIds.add(callId);
                    var mediaTrackIds = new MediaTrackMappedIds(mediaTrackKey, mediaTrackDTO.peerConnectionId, peerConnectionDTO.clientId, callId);
                    mediaTrackMappedIdsList.add(mediaTrackIds);
                });
            })
            .addActionStage("Gathering outbound track parental ids (Climbing down for Ids)",
                    () -> {
                        // map clients to calls
                        this.clientIdToCallIds = new HashMap<>();
                        this.callIds.forEach(callId -> {
                            Collection<UUID> clientIds = this.hazelcastMaps.getCallToClientIds().get(callId);
                            if (Objects.isNull(clientIds)) {
                                logger.warn("No client id has been found for callId {}", callId);
                                return;
                            }
                            clientIds
                                    .forEach(clientId -> this.clientIdToCallIds.put(clientId, callId));
                        });

                        // map peer connections to clients
                        this.peerConnectionIdToClientIds = new HashMap<>();
                        Set<UUID> clientIds = this.clientIdToCallIds.keySet();
                        clientIds.stream().forEach(clientId -> {
                            Collection<UUID> peerConnectionIds = this.hazelcastMaps.getClientToPeerConnectionIds().get(clientId);
                            if (Objects.isNull(peerConnectionIds)) {
                                logger.warn("No peer connection id has been found for clientId {}", clientId);
                                return;
                            }
                            peerConnectionIds
                                    .stream()
                                    .forEach(peerConnectionId -> this.peerConnectionIdToClientIds.put(peerConnectionId, clientId));
                        });

                        // map the outbound media track to peer connection
                        this.outboundMediaTrackIdToPeerConnectionIds = new HashMap<>();
                        Set<UUID> peerConnectionIds = this.peerConnectionIdToClientIds.keySet();
                        peerConnectionIds.stream().forEach(peerConnectionId -> {
                            Collection<String> outboundMediaTrackKeys = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().get(peerConnectionId);
                            if (Objects.isNull(outboundMediaTrackKeys)) {
                                logger.warn("No outbound media track keys has been found for peer connection id {}", peerConnectionId);
                                return;
                            }
                            outboundMediaTrackKeys
                                    .stream()
                                    .forEach(mediaTrackKey -> this.outboundMediaTrackIdToPeerConnectionIds.put(mediaTrackKey, peerConnectionId));
                        });
                    })
                .addActionStage("Creating outboundTrackIds map", () -> {
                    this.outboundMediaTrackIdToPeerConnectionIds.forEach((mediaTrackKey, peerConnectionId) -> {
                        MediaTrackId mediaTrackId = MediaTrackId.fromKey(mediaTrackKey);
                        var SSRC = mediaTrackId.ssrc;
                        var clientId = this.peerConnectionIdToClientIds.get(peerConnectionId);
                        if (Objects.isNull(clientId)) {
                            logger.warn("Cannot find client id for peer connections {}", peerConnectionId);
                            return;
                        }
                        var callId = this.clientIdToCallIds.get(clientId);
                        if (Objects.isNull(callId)) {
                            logger.warn("Cannot find call id for client id {}", clientId);
                            return;
                        }
                        List<MediaTrackMappedIds> mediaTrackMappedIdsList = this.outboundMediaTrackIdLists.get(SSRC);
                        if (Objects.isNull(mediaTrackMappedIdsList)) {
                            mediaTrackMappedIdsList = new LinkedList<>();
                            this.outboundMediaTrackIdLists.put(SSRC, mediaTrackMappedIdsList);
                        }

                        MediaTrackMappedIds mediaTrackMappedIds = new MediaTrackMappedIds(
                                mediaTrackKey,
                                peerConnectionId,
                                clientId,
                                callId
                        );
                        mediaTrackMappedIdsList.add(mediaTrackMappedIds);
                    });
                })
                .addTerminalSupplier("Pairing in-, and outbound tracks by ssrc and callId", () -> {
                    Map<MediaTrackId, UUID> result = new HashMap<>();
                    this.inboundMediaTrackIdLists.forEach((SSRC, inboundMediaTrackIdsList) -> {
                        List<MediaTrackMappedIds> outboundMediaTrackMappedIdsList = this.outboundMediaTrackIdLists.get(SSRC);
                        if (Objects.isNull(outboundMediaTrackMappedIdsList)) {
                            return;
                        }
                        inboundMediaTrackIdsList.forEach(inboundMediaTrackMappedIds -> {
                            Optional<MediaTrackMappedIds> foundOutboundMediaTrackIds = outboundMediaTrackMappedIdsList.stream().filter(outbTrackIds -> outbTrackIds.callId == inboundMediaTrackMappedIds.callId).findFirst();
                            if (foundOutboundMediaTrackIds.isEmpty()) {
                                return;
                            }
                            var outboundMediaTrackIds = foundOutboundMediaTrackIds.get();
                            MediaTrackId inboundMediaTrackId = MediaTrackId.fromKey(inboundMediaTrackMappedIds.mediaTrackKey);
                            result.put(inboundMediaTrackId, outboundMediaTrackIds.clientId);
                        });
                    });
                    if (this.unmodifiableResult) {
                        return Collections.unmodifiableMap(result);
                    } else {
                        return result;
                    }
                })
        .build();
    }

    public FindRemoteClientIdsForMediaTrackKeys whereMediaTrackIds(Set<MediaTrackId> mediaTrackIds) {
        this.inboundMediaTrackIds.addAll(mediaTrackIds);
        return this;
    }


    public FindRemoteClientIdsForMediaTrackKeys withUnmodifiableResult(boolean value) {
        this.unmodifiableResult = value;
        return this;
    }

    @Override
    protected void validate() {

    }
}
