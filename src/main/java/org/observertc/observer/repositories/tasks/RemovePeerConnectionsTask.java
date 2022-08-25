package org.observertc.observer.repositories.tasks;

import io.micronaut.context.BeanProvider;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Utils;
import org.observertc.observer.dto.PeerConnectionDTO;
import org.observertc.observer.metrics.RepositoryMetrics;
import org.observertc.observer.repositories.HamokStorages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;

@Prototype
public class RemovePeerConnectionsTask extends ChainedTask<Map<UUID, PeerConnectionDTO>> {

    private static final Logger logger = LoggerFactory.getLogger(RemovePeerConnectionsTask.class);
    private Set<UUID> peerConnectionIds = new HashSet<>();
    private Map<UUID, PeerConnectionDTO> removedPeerConnectionDTOs = new HashMap<>();
    private Map<UUID, Collection<UUID>> removedPeerConnectionMediaTrackIds = new HashMap<>();
    private boolean unmodifiableResult = false;

    @Inject
    HamokStorages hamokStorages;

    @Inject
    BeanProvider<RemoveMediaTracksTask> removeMediaTracksTaskProvider;

    @Inject
    RepositoryMetrics exposedMetrics;

    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
        new Builder<Map<UUID, PeerConnectionDTO>>(this)
                .<Set<UUID>, Set<UUID>>addSupplierEntry("Merge Inputs",
                        () -> this.peerConnectionIds,
                        receivedClientIds -> {
                            this.peerConnectionIds.addAll(receivedClientIds);
                            return this.peerConnectionIds;
                        }
                )
                .<Set<UUID>> addConsumerStage("Remove PeerConnection DTOs",
                        peerConnectionIds -> {
                            for (UUID peerConnectionId : peerConnectionIds) {
                                if (this.removedPeerConnectionDTOs.containsKey(peerConnectionId)) {
                                    continue;
                                }
                                PeerConnectionDTO peerConnectionDTO = this.hamokStorages.getPeerConnections().remove(peerConnectionId);
                                if (Objects.isNull(peerConnectionDTO)) {
                                    logger.debug("Not found PeerConnectionDTO for peerConnectionId: {}. Perhaps it was ejected before it was ordered to be removed.", peerConnectionId);
                                    continue;
                                }
                                this.removedPeerConnectionDTOs.put(peerConnectionId, peerConnectionDTO);
                            }
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            if (this.removedPeerConnectionDTOs.size() < 1) {
                                return;
                            }
                            this.hamokStorages.getPeerConnections().putAll(this.removedPeerConnectionDTOs);
                        })
                .addActionStage("Remove Client Bindings", () -> {
                            this.removedPeerConnectionDTOs.forEach((peerConnectionId, removedPeerConnectionDTO) -> {
                                this.hamokStorages.getClientToPeerConnectionIds().remove(removedPeerConnectionDTO.clientId, peerConnectionId);
                            });
                        },
                        (inputHolder, thrownException) -> {
                            this.removedPeerConnectionDTOs.forEach((peerConnectionId, removedPeerConnectionDTO) -> {
                                this.hamokStorages.getClientToPeerConnectionIds().put(removedPeerConnectionDTO.clientId, peerConnectionId);
                            });
                        })
                . addActionStage("Remove Media Tracks", () -> {
                    Set<UUID> trackIds = new HashSet<>();
                    this.removedPeerConnectionDTOs.keySet().forEach(peerConnectionId -> {
                        Set<UUID> peerConnectionIdMediaTrackIds = new HashSet<>();
                        Collection<UUID> inboundTrackIds = this.hamokStorages.getPeerConnectionToInboundTrackIds().remove(peerConnectionId);
                        inboundTrackIds.forEach(peerConnectionIdMediaTrackIds::add);
                        Collection<UUID> outboundTrackIds = this.hamokStorages.getPeerConnectionToOutboundTrackIds().remove(peerConnectionId);
                        outboundTrackIds.forEach(peerConnectionIdMediaTrackIds::add);
                        this.removedPeerConnectionMediaTrackIds.put(peerConnectionId, peerConnectionIdMediaTrackIds);
                        peerConnectionIdMediaTrackIds.forEach(trackIds::add);
                    });
                    if (trackIds.size() < 1) {
                        return;
                    }
                    var task = this.removeMediaTracksTaskProvider.get()
                            .whereMediaTrackIds(trackIds);

                    if (!task.execute().succeeded()) {
                        logger.warn("Media Track removal has been failed");
                        return;
                    }
//                    return task.getResult();
                })
                .<Map<UUID, PeerConnectionDTO>> addTerminalSupplier("Return Removed PeerConnections", () -> {
                    if (this.unmodifiableResult) {
                        return Collections.unmodifiableMap(this.removedPeerConnectionDTOs);
                    } else {
                        return this.removedPeerConnectionDTOs;
                    }
                })
                .build();
    }

    public RemovePeerConnectionsTask wherePeerConnectionIds(Set<UUID> peerConnectionIds) {
        if (Objects.isNull(peerConnectionIds) || peerConnectionIds.size() < 1) {
            return this;
        }
        peerConnectionIds.stream().filter(Utils::expensiveNonNullCheck).forEach(this.peerConnectionIds::add);
        return this;
    }

    public RemovePeerConnectionsTask addRemovedPeerConnectionDTO(PeerConnectionDTO peerConnectionDTO) {
        if (Objects.isNull(peerConnectionDTO)) {
            return this;
        }
        this.removedPeerConnectionDTOs.put(peerConnectionDTO.peerConnectionId, peerConnectionDTO);
        return this;
    }

    public RemovePeerConnectionsTask withUnmodifiableResult(boolean value) {
        this.unmodifiableResult = value;
        return this;
    }
}
