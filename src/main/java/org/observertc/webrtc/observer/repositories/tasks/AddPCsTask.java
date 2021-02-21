package org.observertc.webrtc.observer.repositories.tasks;

import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
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
public class AddPCsTask extends ChainedTask<Map<UUID, PeerConnectionEntity>> {

    private static final Logger logger = LoggerFactory.getLogger(AddPCsTask.class);

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FetchPCsTask fetchPCsTask;

    private boolean fetchPCEntities = true;
    private Map<UUID, PeerConnectionEntity> peerConnectionEntities = new HashMap<>();


    @PostConstruct
    void setup() {
        new Builder<Map<UUID, PeerConnectionEntity>>(this)
                .<Map<UUID, PeerConnectionEntity>>addConsumerEntry("Fetch Peer Connection DTOs",
                        () -> {},
                        inputPeerConnectionEntities -> {
                            if (Objects.nonNull(inputPeerConnectionEntities)) {
                                this.peerConnectionEntities.putAll(inputPeerConnectionEntities);
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
                .addActionStage("Add Peer Connection DTOs",
                // action
                () -> {
                    Map<UUID, PeerConnectionDTO> pcDTOs = this.peerConnectionEntities.entrySet()
                            .stream().map(entry -> Map.entry(entry.getKey(), entry.getValue().peerConnection))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    hazelcastMaps.getPcDTOs().putAll(pcDTOs);
                },
                // rollback
                (inputHolder, thrownException) -> {
                    for (UUID pcUUID : this.peerConnectionEntities.keySet()) {
                        hazelcastMaps.getPcDTOs().remove(pcUUID);
                    }
                })
                .addActionStage("Add Peer Connections to Calls",
                // action
                () -> {
                    Map<UUID, PeerConnectionDTO> pcDTOs = this.peerConnectionEntities.entrySet()
                            .stream().map(entry -> Map.entry(entry.getKey(), entry.getValue().peerConnection))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    for (PeerConnectionDTO pcDTO : pcDTOs.values()) {
                        hazelcastMaps.getCallToPCUUIDs().put(pcDTO.callUUID, pcDTO.peerConnectionUUID);
                    }
                },
                // rollback
                (inputHolder, thrownException) -> {
                    List<PeerConnectionDTO> pcDTOs = this.peerConnectionEntities.values()
                            .stream().map(pc -> pc.peerConnection)
                            .collect(Collectors.toList());
                    for (PeerConnectionDTO pcDTO : pcDTOs) {
                        hazelcastMaps.getCallToPCUUIDs().remove(pcDTO.callUUID, pcDTO.peerConnectionUUID);
                    }
                })
                .addActionStage("Add Peer Connections SSRCs",
                // action
                () -> {
                    addSSRCs(hazelcastMaps, peerConnectionEntities);
                },
                // rollback
                (inputHolder, thrownException) -> {
                    RemovePCsTask.removeSSRCs(this.getLogger(), this.hazelcastMaps, this.peerConnectionEntities);
                })
                .<Map<UUID, PeerConnectionEntity>>addSupplierStage("Fetch Result Entities", () -> {
                    return Collections.unmodifiableMap(this.peerConnectionEntities);
                })
                .addTerminalPassingStage("Completed")
                .build();
    }

    public AddPCsTask withPeerConnection(PeerConnectionEntity... entities) {
        if (Objects.isNull(entities) && entities.length < 1) {
            this.getLogger().info("call uuid was not given to be removed");
            return this;
        }
        Arrays.stream(entities).forEach(pc -> {
            this.peerConnectionEntities.put(pc.pcUUID, pc);
        });
        return this;
    }

    static void addSSRCs(HazelcastMaps hazelcastMaps, Map<UUID, PeerConnectionEntity> pcEntities) {
        for (PeerConnectionEntity pcEntity : pcEntities.values()) {
            for (Long SSRC : pcEntity.SSRCs) {
                hazelcastMaps.getSSRCsToPCMap(pcEntity.serviceUUID).put(SSRC, pcEntity.pcUUID);
                hazelcastMaps.getPCsToSSRCMap(pcEntity.serviceUUID).put(pcEntity.pcUUID, SSRC);
            }
        }
    }
}
