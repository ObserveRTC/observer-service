package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Prototype
public class AddMediaTrackTask extends ChainedTask<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(AddMediaTrackTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FetchPeerConnectionsTask fetchPeerConnectionsTask;

    private Map<String, MediaTrackDTO> mediaTrackDTOs = new HashMap<>();


    @PostConstruct
    void setup() {
        new Builder<Boolean>(this)
                .<Map<String, MediaTrackDTO>>addConsumerEntry("Merge all inputs",
                        () -> {},
                        receivedPeerConnectionEntities -> {
                            if (Objects.nonNull(receivedPeerConnectionEntities)) {
                                this.mediaTrackDTOs.putAll(receivedPeerConnectionEntities);
                            }
                        }
                )
                .<Map<UUID, PeerConnectionDTO>> addBreakCondition((resultHolder) -> {
                    if (this.mediaTrackDTOs.size() < 1) {
                        resultHolder.set(true);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Add PeerConnection DTOs",
                        // action
                        () -> {
                            hazelcastMaps.getMediaTracks().putAll(this.mediaTrackDTOs);
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            for (String mediaTrackKey : this.mediaTrackDTOs.keySet()) {
                                hazelcastMaps.getMediaTracks().remove(mediaTrackKey);
                            }
                        })
                .addActionStage("Bind Inbound Media Tracks to Peer Connections",
                        // action
                        () -> {
                            this.mediaTrackDTOs.forEach(((mediaTrackKey, mediaTrackDTO) -> {
                                switch (mediaTrackDTO.direction) {
                                    case INBOUND:
                                        this.hazelcastMaps.getPeerConnectionToInboundTrackIds().put(mediaTrackDTO.peerConnectionId, mediaTrackKey);
                                        break;
                                    case OUTBOUND:
                                        this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().put(mediaTrackDTO.peerConnectionId, mediaTrackKey);
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
                .addTerminalSupplier("Completed", () -> true)
                .build();
    }


    public AddMediaTrackTask withMediaTrackDTOs(Map<String, MediaTrackDTO> mediaTrackDTOs) {
        if (Objects.isNull(mediaTrackDTOs) || mediaTrackDTOs.size() < 1) {
            this.getLogger().info("mediaTrackDTOs was not given");
            return this;
        }
        this.mediaTrackDTOs.putAll(mediaTrackDTOs);
        return this;
    }
}
