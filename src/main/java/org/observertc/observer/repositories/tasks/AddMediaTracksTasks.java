package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.dto.MediaTrackDTO;
import org.observertc.observer.dto.PeerConnectionDTO;
import org.observertc.observer.micrometer.ExposedMetrics;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.repositories.StoredRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Prototype
public class AddMediaTracksTasks extends ChainedTask<Void> {

    private static final Logger logger = LoggerFactory.getLogger(AddMediaTracksTasks.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    StoredRequests storedRequests;

    @Inject
    ExposedMetrics exposedMetrics;

    private Map<UUID, MediaTrackDTO> mediaTrackDTOs = new HashMap<>();


    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<Void>(this)
                .<Map<UUID, MediaTrackDTO>>addConsumerEntry("Merge all inputs",
                        () -> {},
                        receivedPeerConnectionEntities -> {
                            if (Objects.nonNull(receivedPeerConnectionEntities)) {
                                this.mediaTrackDTOs.putAll(receivedPeerConnectionEntities);
                            }
                        }
                )
                .<Map<UUID, PeerConnectionDTO>> addBreakCondition((resultHolder) -> {
                    if (this.mediaTrackDTOs.size() < 1) {
                        resultHolder.set(null);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Add MediaTrack DTOs",
                        // action
                        () -> {
                            hazelcastMaps.getMediaTracks().putAll(this.mediaTrackDTOs);
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            for (UUID mediaTrackId : this.mediaTrackDTOs.keySet()) {
                                hazelcastMaps.getMediaTracks().remove(mediaTrackId);
                            }
                        })
                .addActionStage("Bind Media Tracks to Peer Connections, sfuStreams and sfuSinks",
                        // action
                        () -> {
                            this.mediaTrackDTOs.forEach(((mediaTrackId, mediaTrackDTO) -> {
                                switch (mediaTrackDTO.direction) {
                                    case INBOUND:
                                        this.hazelcastMaps.getPeerConnectionToInboundTrackIds().put(mediaTrackDTO.peerConnectionId, mediaTrackId);
                                        break;
                                    case OUTBOUND:
                                        this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().put(mediaTrackDTO.peerConnectionId, mediaTrackId);
                                        break;
                                }
                            }));
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            this.mediaTrackDTOs.forEach(((mediaTrackKey, mediaTrackDTO) -> {
                                switch (mediaTrackDTO.direction) {
                                    case INBOUND:
                                        this.hazelcastMaps.getPeerConnectionToInboundTrackIds().remove(mediaTrackDTO.peerConnectionId, mediaTrackKey);
                                        break;
                                    case OUTBOUND:
                                        this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().remove(mediaTrackDTO.peerConnectionId, mediaTrackKey);
                                        break;
                                }
                            }));
                        })
                .addTerminalPassingStage("Completed")
                .build();
    }


    public AddMediaTracksTasks withMediaTrackDTOs(Map<UUID, MediaTrackDTO> mediaTrackDTOs) {
        if (Objects.isNull(mediaTrackDTOs) || mediaTrackDTOs.size() < 1) {
            this.getLogger().info("mediaTrackDTOs was not given");
            return this;
        }
        this.mediaTrackDTOs.putAll(mediaTrackDTOs);
        return this;
    }
}
