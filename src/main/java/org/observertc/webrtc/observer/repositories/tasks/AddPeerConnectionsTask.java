package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.entities.ClientEntity;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.samples.MediaTrackId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Prototype
public class AddPeerConnectionsTask extends ChainedTask<Map<UUID, PeerConnectionEntity>> {

    private static final Logger logger = LoggerFactory.getLogger(AddPeerConnectionsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FetchPeerConnectionsTask fetchPeerConnectionsTask;

    private boolean fetchEntitiesBack = false;
    private Map<UUID, PeerConnectionEntity> peerConnectionEntities = new HashMap<>();


    @PostConstruct
    void setup() {
        new Builder<Map<UUID, PeerConnectionEntity>>(this)
                .<Map<UUID, PeerConnectionEntity>>addConsumerEntry("Merge all inputs",
                        () -> {},
                        receivedPeerConnectionEntities -> {
                            if (Objects.nonNull(receivedPeerConnectionEntities)) {
                                this.peerConnectionEntities.putAll(receivedPeerConnectionEntities);
                            }
                        }
                )
                .<Map<UUID, PeerConnectionEntity>> addBreakCondition((resultHolder) -> {
                    if (this.peerConnectionEntities.size() < 1) {
                        resultHolder.set(Collections.EMPTY_MAP);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Add PeerConnection DTOs",
                        // action
                        () -> {
                            Map<UUID, PeerConnectionDTO> peerConnectionDTOs = new HashMap<>();
                            this.peerConnectionEntities.forEach((clientId, peerConnectionEntity) -> {
                                peerConnectionDTOs.put(clientId, peerConnectionEntity.getPeerConnectionDTO());
                            });
                            hazelcastMaps.getPeerConnections().putAll(peerConnectionDTOs);
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            for (UUID peerConnectionId : this.peerConnectionEntities.keySet()) {
                                hazelcastMaps.getPeerConnections().remove(peerConnectionId);
                            }
                        })
                .addActionStage("Add Inbound Media Tracks",
                        // action
                        () -> {
                            Map<String, MediaTrackDTO> mediaTrackDTOMap = new HashMap<>();
                            this.peerConnectionEntities.forEach((peerConnectionId, peerConnectionEntity) -> {
                                Map<Long, MediaTrackDTO> mediaTrackDTOs = peerConnectionEntity.getInboundMediaTrackDTOs();
                                mediaTrackDTOs.forEach((SSRC, mediaTrackDTO) -> {
                                    MediaTrackId mediaTrackId = MediaTrackId.make(mediaTrackDTO.peerConnectionId, SSRC);
                                    String mediaTrackKey = MediaTrackId.getKey(mediaTrackId);
                                    mediaTrackDTOMap.put(mediaTrackKey, mediaTrackDTO);
                                    this.hazelcastMaps.getPeerConnectionToInboundTrackIds().put(peerConnectionId, mediaTrackKey);
                                });
                            });
                            this.hazelcastMaps.getMediaTracks().putAll(mediaTrackDTOMap);
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            this.peerConnectionEntities.forEach((peerConnectionId, peerConnectionEntity) -> {
                                Map<Long, MediaTrackDTO> mediaTrackDTOs = peerConnectionEntity.getInboundMediaTrackDTOs();
                                mediaTrackDTOs.forEach((SSRC, mediaTrackDTO) -> {
                                    MediaTrackId mediaTrackId = MediaTrackId.make(mediaTrackDTO.peerConnectionId, SSRC);
                                    String mediaTrackKey = MediaTrackId.getKey(mediaTrackId);
                                    this.hazelcastMaps.getPeerConnectionToInboundTrackIds().remove(peerConnectionId, mediaTrackKey);
                                    this.hazelcastMaps.getMediaTracks().remove(mediaTrackKey);
                                });
                            });
                        })
                .addActionStage("Add Outbound Media Tracks",
                        // action
                        () -> {
                            Map<String, MediaTrackDTO> mediaTrackDTOMap = new HashMap<>();
                            this.peerConnectionEntities.forEach((peerConnectionId, peerConnectionEntity) -> {
                                Map<Long, MediaTrackDTO> mediaTrackDTOs = peerConnectionEntity.getOutboundMediaTrackDTOs();
                                mediaTrackDTOs.forEach((SSRC, mediaTrackDTO) -> {
                                    MediaTrackId mediaTrackId = MediaTrackId.make(mediaTrackDTO.peerConnectionId, SSRC);
                                    String mediaTrackKey = MediaTrackId.getKey(mediaTrackId);
                                    mediaTrackDTOMap.put(mediaTrackKey, mediaTrackDTO);
                                    this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().put(peerConnectionId, mediaTrackKey);
                                });
                            });
                            this.hazelcastMaps.getMediaTracks().putAll(mediaTrackDTOMap);
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            this.peerConnectionEntities.forEach((peerConnectionId, peerConnectionEntity) -> {
                                Map<Long, MediaTrackDTO> mediaTrackDTOs = peerConnectionEntity.getOutboundMediaTrackDTOs();
                                mediaTrackDTOs.forEach((SSRC, mediaTrackDTO) -> {
                                    MediaTrackId mediaTrackId = MediaTrackId.make(mediaTrackDTO.peerConnectionId, SSRC);
                                    String mediaTrackKey = MediaTrackId.getKey(mediaTrackId);
                                    this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().remove(peerConnectionId, mediaTrackKey);
                                    this.hazelcastMaps.getMediaTracks().remove(mediaTrackKey);
                                });
                            });
                        })
                .<Map<UUID, PeerConnectionEntity>>addSupplierStage("Fetch Result Entities", () -> {
                    if (!this.fetchEntitiesBack) {
                        return Collections.unmodifiableMap(this.peerConnectionEntities);
                    }
                    Set<UUID> peerConnectionIds = this.peerConnectionEntities.keySet();
                    if (!this.fetchPeerConnectionsTask.wherePeerConnectionIds(peerConnectionIds).execute().succeeded()) {
                        throw new RuntimeException("Error occurred during fetching entities back");
                    }
                    return this.fetchPeerConnectionsTask.getResult();
                })
                .addTerminalPassingStage("Completed")
                .build();
    }

    public AddPeerConnectionsTask withPeerConnectionEntities(PeerConnectionEntity... entities) {
        if (Objects.isNull(entities) && entities.length < 1) {
            this.getLogger().info("call uuid was not given to be removed");
            return this;
        }
        Arrays.stream(entities).forEach(peerConnectionEntity -> {
            this.peerConnectionEntities.put(peerConnectionEntity.getPeerConnectionId(), peerConnectionEntity);
        });
        return this;
    }

    public AddPeerConnectionsTask withClientEntities(Map<UUID, PeerConnectionEntity> entities) {
        this.peerConnectionEntities.putAll(entities);
        return this;
    }

    public AddPeerConnectionsTask withFetchingBackTheResult(boolean value) {
        this.fetchEntitiesBack = value;
        return this;
    }
}
