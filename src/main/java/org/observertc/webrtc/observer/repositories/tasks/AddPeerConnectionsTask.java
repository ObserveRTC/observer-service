package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

@Prototype
public class AddPeerConnectionsTask extends ChainedTask<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(AddPeerConnectionsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    private Map<UUID, PeerConnectionDTO> peerConnectionDTOs = new HashMap<>();


    @PostConstruct
    void setup() {
        new Builder<Boolean>(this)
                .<Map<UUID, PeerConnectionDTO>>addConsumerEntry("Merge all inputs",
                        () -> {},
                        receivedPeerConnectionEntities -> {
                            if (Objects.nonNull(receivedPeerConnectionEntities)) {
                                this.peerConnectionDTOs.putAll(receivedPeerConnectionEntities);
                            }
                        }
                )
                .<Map<UUID, PeerConnectionDTO>> addBreakCondition((resultHolder) -> {
                    if (this.peerConnectionDTOs.size() < 1) {
                        resultHolder.set(true);
                        return true;
                    }
                    return false;
                })
                .addActionStage("Add PeerConnection DTOs",
                        // action
                        () -> {
                            hazelcastMaps.getPeerConnections().putAll(this.peerConnectionDTOs);
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            for (UUID peerConnectionId : this.peerConnectionDTOs.keySet()) {
                                hazelcastMaps.getPeerConnections().remove(peerConnectionId);
                            }
                        })
                .addActionStage("Bind PeerConnection Ids to Client Ids",
                        // action
                        () -> {
                            Map<UUID, List<PeerConnectionDTO>> clientsToPeerConnectionDTOs =
                                    this.peerConnectionDTOs.values().stream()
                                            .collect(groupingBy(peerConnectionDTO -> peerConnectionDTO.clientId));

                            clientsToPeerConnectionDTOs.forEach((clientId, peerConnectionDTOs) -> {
                                peerConnectionDTOs.forEach(peerConnectionDTO -> {
                                    this.hazelcastMaps.getClientToPeerConnectionIds().put(clientId, peerConnectionDTO.peerConnectionId);
                                });
                            });
                        },
                        // rollback
                        (inputHolder, thrownException) -> {
                            this.peerConnectionDTOs.forEach((peerConnectionId, peerConnectionDTO) -> {
                                this.hazelcastMaps.getClientToPeerConnectionIds().remove(peerConnectionDTO.clientId, peerConnectionId);
                            });
                        })
                .addTerminalSupplier("Completed", () -> true)
                .build();
    }


    public AddPeerConnectionsTask withPeerConnectionDTOs(Map<UUID, PeerConnectionDTO> peerConnectionDTOs) {
        if (Objects.isNull(peerConnectionDTOs) || peerConnectionDTOs.size() < 1) {
            this.getLogger().info("peerConnectionDTOs was not given to be removed");
            return this;
        }
        this.peerConnectionDTOs.putAll(peerConnectionDTOs);
        return this;
    }
}
