package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class RemovePeerConnectionsTask extends ChainedTask<Map<UUID, PeerConnectionEntity>> {

    private static final Logger logger = LoggerFactory.getLogger(RemovePeerConnectionsTask.class);
    private Set<UUID> peerConnectionIds = new HashSet<>();

    private Map<UUID, PeerConnectionDTO> removedPeerConnectionDTOs = new HashMap<>();
    private Map<String, MediaTrackDTO> removedInboundMediaTrackDTOs = new HashMap<>();
    private Map<String, MediaTrackDTO> removedOutboundMediaTrackDTOs = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    RemovePeerConnectionsTask removePeerConnectionsTask;

    @Inject
    WeakLockProvider weakLockProvider;

    @PostConstruct
    void setup() {
        new Builder<Map<UUID, PeerConnectionEntity>>(this)
                .<Set<UUID>, Set<UUID>>addSupplierEntry("Merge Inputs",
                        () -> this.peerConnectionIds,
                        receivedClientIds -> {
                            this.peerConnectionIds.addAll(receivedClientIds);
                            return this.peerConnectionIds;
                        }
                )
                .<Set<UUID>>addBreakCondition((peerConnectionIds, resultHolder) -> {
                    if (Objects.isNull(peerConnectionIds)) {
                        this.getLogger().warn("No PeerConnectionId have been passed");
                        resultHolder.set(Collections.EMPTY_MAP);
                        return true;
                    }
                    if (peerConnectionIds.size() < 1) {
                        this.getLogger().warn("No PeerConnectionId have been passed");
                        resultHolder.set(Collections.EMPTY_MAP);
                        return true;
                    }
                    return false;
                })
                .<Set<UUID>, Map<UUID, PeerConnectionEntity.Builder>> addFunctionalStage("Remove PeerConnection DTOs",
                        peerConnectionIds -> {
                            Map<UUID, PeerConnectionEntity.Builder> peerConnectionEntityBuilders = new HashMap<>();
                            for (UUID peerConnectionId : peerConnectionIds) {
                                if (this.removedPeerConnectionDTOs.containsKey(peerConnectionId)) {
                                    continue;
                                }
                                PeerConnectionDTO peerConnectionDTO = this.hazelcastMaps.getPeerConnections().remove(peerConnectionId);
                                if (Objects.isNull(peerConnectionDTO)) {
                                    logger.warn("Cannot retrieve PeerConnectionDTO for peerConnectionId: {}", peerConnectionId);
                                    continue;
                                }
                                this.removedPeerConnectionDTOs.put(peerConnectionId, peerConnectionDTO);
                                var peerConnectionEntityBuilder = PeerConnectionEntity.builder().withPeerConnectionDTO(peerConnectionDTO);
                                peerConnectionEntityBuilders.put(peerConnectionId, peerConnectionEntityBuilder);
                            }
                            return peerConnectionEntityBuilders;
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            if (this.removedPeerConnectionDTOs.size() < 1) {
                                return;
                            }
                            this.hazelcastMaps.getPeerConnections().putAll(this.removedPeerConnectionDTOs);
                        })
                .<Map<UUID, PeerConnectionEntity.Builder>, Map<UUID, PeerConnectionEntity.Builder>>addFunctionalStage("Remove Inbound Media Tracks",
                        peerConnectionEntityBuilders -> {
                            peerConnectionEntityBuilders.forEach((peerConnectionId, peerConnectionEntityBuilder) -> {
                                Collection<String> mediaTrackKeys = this.hazelcastMaps.getPeerConnectionToInboundTrackIds().remove(peerConnectionId);
                                for (String mediaTractKey : mediaTrackKeys) {
                                    MediaTrackDTO mediaTrackDTO = this.hazelcastMaps.getMediaTracks().remove(mediaTractKey);
                                    peerConnectionEntityBuilder.withInboundMediaTrackDTO(mediaTrackDTO);
                                    this.removedInboundMediaTrackDTOs.put(mediaTractKey, mediaTrackDTO);
                                }
                            });
                            return peerConnectionEntityBuilders;
                        },
                        (inputHolder, thrownException) -> {
                            if (this.removedInboundMediaTrackDTOs.size() < 1) {
                                return;
                            }
                            this.removedInboundMediaTrackDTOs.forEach((mediaTrackKey, mediaTrackDTO) -> {
                                this.hazelcastMaps.getMediaTracks().put(mediaTrackKey, mediaTrackDTO);
                                this.hazelcastMaps.getPeerConnectionToInboundTrackIds().put(mediaTrackDTO.peerConnectionId, mediaTrackKey);
                            });
                        })
                .<Map<UUID, PeerConnectionEntity.Builder>, Map<UUID, PeerConnectionEntity.Builder>>addFunctionalStage("Remove Outbound Media Tracks",
                        peerConnectionEntityBuilders -> {
                            peerConnectionEntityBuilders.forEach((peerConnectionId, peerConnectionEntityBuilder) -> {
                                Collection<String> mediaTrackKeys = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().remove(peerConnectionId);
                                for (String mediaTractKey : mediaTrackKeys) {
                                    MediaTrackDTO mediaTrackDTO = this.hazelcastMaps.getMediaTracks().remove(mediaTractKey);
                                    peerConnectionEntityBuilder.withOutboundMediaTrackDTO(mediaTrackDTO);
                                    this.removedOutboundMediaTrackDTOs.put(mediaTractKey, mediaTrackDTO);
                                }
                            });
                            return peerConnectionEntityBuilders;
                        },
                        (inputHolder, thrownException) -> {
                            if (this.removedOutboundMediaTrackDTOs.size() < 1) {
                                return;
                            }
                            this.removedOutboundMediaTrackDTOs.forEach((mediaTrackKey, mediaTrackDTO) -> {
                                this.hazelcastMaps.getMediaTracks().put(mediaTrackKey, mediaTrackDTO);
                                this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().put(mediaTrackDTO.peerConnectionId, mediaTrackKey);
                            });
                        })
                .<Map<UUID, PeerConnectionEntity.Builder>> addTerminalFunction("Creating PeerConnection Entities", peerConnectionEntityBuilders -> {
                    return peerConnectionEntityBuilders.entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> {
                                PeerConnectionEntity.Builder peerConnectionEntityBuilder = entry.getValue();
                                return peerConnectionEntityBuilder.build();
                            })
                    );
                })
                .build();
    }

    public RemovePeerConnectionsTask wherePeerConnectionIds(UUID... peerConnectionIds) {
        if (Objects.isNull(peerConnectionIds) || peerConnectionIds.length < 1) {
            return this;
        }
        this.peerConnectionIds.addAll(Arrays.asList(peerConnectionIds));
        return this;
    }

    public RemovePeerConnectionsTask wherePeerConnectionIds(Set<UUID> peerConnectionIds) {
        if (Objects.isNull(peerConnectionIds) || peerConnectionIds.size() < 1) {
            return this;
        }
        this.peerConnectionIds.addAll(peerConnectionIds);
        return this;
    }

    public RemovePeerConnectionsTask withRemovedPeerConnectionDTO(Map<UUID, PeerConnectionDTO> peerConnectionDTOs) {
        if (Objects.isNull(peerConnectionDTOs) || peerConnectionDTOs.size() < 1) {
            return this;
        }
        this.removedPeerConnectionDTOs.putAll(peerConnectionDTOs);
        return this;
    }

}
