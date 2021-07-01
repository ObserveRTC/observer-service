package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class FetchPeerConnectionsTask extends ChainedTask<Map<UUID, PeerConnectionEntity>> {

    private Set<UUID> peerConnectionIds = new HashSet<>();

    @Inject
    HazelcastMaps hazelcastMaps;


    @PostConstruct
    void setup() {
        Function<Set<UUID>, Map<UUID, PeerConnectionDTO>> fetchPcDTO = pcUUIDs -> {
            if (Objects.nonNull(pcUUIDs)) {
                this.peerConnectionIds.addAll(pcUUIDs);
            }
            if (this.peerConnectionIds.size() < 1) {
                return Collections.EMPTY_MAP;
            }
            return hazelcastMaps.getPeerConnections().getAll(this.peerConnectionIds);
        };
        new Builder<Map<UUID, PeerConnectionEntity>>(this)
            .<Set<UUID>, Map<UUID, PeerConnectionDTO>>addSupplierEntry("Find PC DTO By UUID",
                    () -> fetchPcDTO.apply(this.peerConnectionIds),
                    pcUUID -> fetchPcDTO.apply(pcUUID)
            )
            .<Map<UUID, PeerConnectionDTO>>addBreakCondition((pcDTOMap, resultHolder) -> {
                if (Objects.isNull(pcDTOMap) || pcDTOMap.size() < 1) {
                    resultHolder.set(Collections.EMPTY_MAP);
                    return true;
                }
                return false;
            })
            .<Map<UUID, PeerConnectionDTO>, Map<UUID, PeerConnectionEntity.Builder>> addFunctionalStage("Convert PeerConnectionDTO to PeerConnectionEntity builder", pcDTOMap -> {
                Map<UUID, PeerConnectionEntity.Builder> pcBuilders = pcDTOMap.entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            PeerConnectionEntity.Builder builder = new PeerConnectionEntity.Builder();
                            builder.withPeerConnectionDTO(entry.getValue());
                            return builder;
                        }
                ));
                return pcBuilders;
            })
            .<Map<UUID, PeerConnectionEntity.Builder>, Map<UUID, PeerConnectionEntity.Builder>> addFunctionalStage("Add Inbound Media Tracks",
                    pcEntityBuilders -> {
                        Set<UUID> peerConnectionIds = pcEntityBuilders.keySet();
                        Map<UUID, UUID> inboundTrackToPcIds = new HashMap<>();
                        for (UUID peerConnectionId : peerConnectionIds) {
                            Collection<UUID> mediaTrackIds = this.hazelcastMaps.getPeerConnectionToInboundTrackIds().get(peerConnectionId);
                            mediaTrackIds.forEach(mediaTrackId -> inboundTrackToPcIds.put(mediaTrackId, peerConnectionId));
                        }
                        Map<UUID, MediaTrackDTO> mediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(inboundTrackToPcIds.keySet());
                        inboundTrackToPcIds.forEach((mediaTrackId, peerConnectionId) -> {
                            MediaTrackDTO mediaTrackDTO = mediaTrackDTOs.get(mediaTrackId);
                            if (Objects.isNull(mediaTrackDTO)) {
                                // TODO: notify about a problem: inconsistent media track to peer connections
                                return;
                            }
                            PeerConnectionEntity.Builder builder = pcEntityBuilders.get(peerConnectionId);
                            if (Objects.isNull(builder)) {
                                // TODO: notify about a problem: inconsistent mapping between media track and pc
                                // should be imposible as we get pc id out from builders
                                return;
                            }
                            builder.withInboundMediaTrackDTO(mediaTrackDTO);
                        });
                        return pcEntityBuilders;
                    }
            )
            .<Map<UUID, PeerConnectionEntity.Builder>, Map<UUID, PeerConnectionEntity.Builder>> addFunctionalStage("Add Outbound Media Tracks",
                    pcEntityBuilders -> {
                        Set<UUID> peerConnectionIds = pcEntityBuilders.keySet();
                        Map<UUID, UUID> outboundTrackToPcIds = new HashMap<>();
                        for (UUID peerConnectionId : peerConnectionIds) {
                            Collection<UUID> mediaTrackIds = this.hazelcastMaps.getPeerConnectionToOutboundTrackIds().get(peerConnectionId);
                            mediaTrackIds.forEach(mediaTrackId -> outboundTrackToPcIds.put(mediaTrackId, peerConnectionId));
                        }
                        Map<UUID, MediaTrackDTO> mediaTrackDTOs = this.hazelcastMaps.getMediaTracks().getAll(outboundTrackToPcIds.keySet());
                        outboundTrackToPcIds.forEach((mediaTrackId, peerConnectionId) -> {
                            MediaTrackDTO mediaTrackDTO = mediaTrackDTOs.get(mediaTrackId);
                            if (Objects.isNull(mediaTrackDTO)) {
                                // TODO: notify about a problem: inconsistent media track to peer connections
                                return;
                            }
                            PeerConnectionEntity.Builder builder = pcEntityBuilders.get(peerConnectionId);
                            if (Objects.isNull(builder)) {
                                // TODO: notify about a problem: inconsistent mapping between media track and pc
                                // should be imposible as we get pc id out from builders
                                return;
                            }
                            builder.withOutboundMediaTrackDTO(mediaTrackDTO);
                        });
                        return pcEntityBuilders;
                    }
            )
            .<Map<UUID, PeerConnectionEntity.Builder>> addTerminalFunction("Creating Peer Connection Entities", dataCarriers -> {
                return dataCarriers.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    PeerConnectionEntity.Builder pcEntityBuilder = entry.getValue();
                    return pcEntityBuilder.build();
                }));
            })
        .build();
    }

    public FetchPeerConnectionsTask wherePeerConnectionIds(UUID... values) {
        if (Objects.isNull(values) || values.length < 1) {
            return this;
        }
        this.peerConnectionIds.addAll(Arrays.asList(values));
        return this;
    }

    public FetchPeerConnectionsTask wherePeerConnectionIds(Set<UUID> peerConnectionIds) {
        if (Objects.isNull(this.peerConnectionIds) || this.peerConnectionIds.size() < 1) {
            return this;
        }
        this.peerConnectionIds.addAll(this.peerConnectionIds);
        return this;
    }

    @Override
    protected void validate() {

    }


}
