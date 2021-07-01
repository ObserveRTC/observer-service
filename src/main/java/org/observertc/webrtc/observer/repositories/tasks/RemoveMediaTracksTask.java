package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.StreamDirection;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Prototype
public class RemoveMediaTracksTask extends ChainedTask<Map<UUID, MediaTrackDTO>> {

    private static final Logger logger = LoggerFactory.getLogger(RemoveMediaTracksTask.class);
    private Set<UUID> mediaTrackIds = new HashSet<>();
    private Map<UUID, MediaTrackDTO> removedTrackDTOs = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @PostConstruct
    void setup() {
        new Builder<Map<UUID, MediaTrackDTO>>(this)
                .<Set<UUID>, Set<UUID>>addSupplierEntry("Merge Inputs",
                        () -> this.mediaTrackIds,
                        receivedTrackIds -> {
                            this.mediaTrackIds.addAll(receivedTrackIds);
                            return this.mediaTrackIds;
                        }
                )
                .<Set<UUID>> addConsumerStage("Remove MediaTrack DTOs",
                        mediaTrackIds -> {
                            for (UUID trackId : this.mediaTrackIds) {
                                if (this.removedTrackDTOs.containsKey(trackId)) {
                                    continue;
                                }
                                MediaTrackDTO mediaTrackDTO = this.hazelcastMaps.getMediaTracks().remove(trackId);
                                if (Objects.isNull(mediaTrackDTO)) {
                                    logger.warn("Cannot retrieve MediaTrackDTO for trackId: {}", trackId);
                                    continue;
                                }
                                this.removedTrackDTOs.put(mediaTrackDTO.trackId, mediaTrackDTO);
                            }
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            if (this.removedTrackDTOs.size() < 1) {
                                return;
                            }
                            this.hazelcastMaps.getMediaTracks().putAll(this.removedTrackDTOs);
                        })
                .addActionStage("Remove Inbound Media Tracks",
                        () -> {
                            this.removedTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
                                if (mediaTrackDTO.direction != StreamDirection.INBOUND) {
                                    return;
                                }
                                this.hazelcastMaps.getPeerConnectionToInboundTrackIds().remove(mediaTrackDTO.peerConnectionId, trackId);
                            });
                        },
                        (inputHolder, thrownException) -> {
                            this.removedTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
                                if (mediaTrackDTO.direction != StreamDirection.INBOUND) {
                                    return;
                                }
                                this.hazelcastMaps.getPeerConnectionToInboundTrackIds().put(mediaTrackDTO.peerConnectionId, trackId);
                            });
                        })
                .addActionStage("Remove Outbound Media Tracks",
                        () -> {
                            this.removedTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
                                if (mediaTrackDTO.direction != StreamDirection.OUTBOUND) {
                                    return;
                                }
                                this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().remove(mediaTrackDTO.peerConnectionId, trackId);
                            });
                        },
                        (inputHolder, thrownException) -> {
                            this.removedTrackDTOs.forEach((trackId, mediaTrackDTO) -> {
                                if (mediaTrackDTO.direction != StreamDirection.OUTBOUND) {
                                    return;
                                }
                                this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().put(mediaTrackDTO.peerConnectionId, trackId);
                            });
                        })
                . addTerminalSupplier("Creating PeerConnection Entities", () -> {
                    return this.removedTrackDTOs;
                })
                .build();
    }

    public RemoveMediaTracksTask whereMediaTrackIds(Set<UUID> mediaTrackIds) {
        if (Objects.isNull(mediaTrackIds) || mediaTrackIds.size() < 1) {
            return this;
        }
        this.mediaTrackIds.addAll(mediaTrackIds);
        return this;
    }

    public RemoveMediaTracksTask addremovedMediaTrackDTO(MediaTrackDTO mediaTrackDTO) {
        if (Objects.isNull(mediaTrackDTO)) {
            return this;
        }
        this.removedTrackDTOs.put(mediaTrackDTO.trackId, mediaTrackDTO);
        return this;
    }
}
