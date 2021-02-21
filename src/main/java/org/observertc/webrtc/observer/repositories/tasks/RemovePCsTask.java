package org.observertc.webrtc.observer.repositories.tasks;

import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.CallsRepository;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class RemovePCsTask extends ChainedTask<Map<UUID, PeerConnectionEntity>> {

    private static final Logger logger = LoggerFactory.getLogger(CallsRepository.class);

    private Set<UUID> pcUUIDs = new HashSet<>();
    private Map<UUID, PeerConnectionEntity> pcEntities = new HashMap<>();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FetchPCsTask fetchPCsTask;


    @PostConstruct
    void setup() {
        Function<Set<UUID>, Map<UUID, PeerConnectionEntity>> fetchCallEntities = pcUUIDs -> {
            if (Objects.nonNull(pcUUIDs)) {
                this.pcUUIDs.addAll(pcUUIDs);
            }
            this.pcUUIDs.addAll(this.pcEntities.keySet());
            Set<UUID> missingUUIDs = this.pcUUIDs.stream()
                    .filter(callUUID -> !this.pcEntities.containsKey(callUUID))
                    .collect(Collectors.toSet());
            if (0 < missingUUIDs.size()) {
                Map<UUID, PeerConnectionEntity> map = this.fetchPCsTask.execute(missingUUIDs).getResult();
                this.pcEntities.putAll(map);
            }
            return this.pcEntities;
        };
        new Builder<Map<UUID, PeerConnectionEntity>>(this)
                .<Set<UUID>, Map<UUID, PeerConnectionEntity>>addSupplierEntry("Fetch Peer Connection Entities",
                        () -> fetchCallEntities.apply(this.pcUUIDs),
                        fetchCallEntities
                )
                .<Map<UUID, PeerConnectionEntity>>addBreakCondition((pcEntities, resultHolder) -> {
                    if (Objects.isNull(pcEntities)) {
                        this.getLogger().warn("No Entities have been passed");
                        resultHolder.set(new HashMap<>());
                        return true;
                    }
                    if (pcEntities.size() < 1) {
                        resultHolder.set(pcEntities);
                        return true;
                    }
                    this.pcUUIDs.stream().filter(c -> !pcEntities.containsKey(c)).forEach(pcUUID -> {
                        getLogger().warn("Cannot find Peer Connection Entity for pcUUID {}, it cannot be removed", pcUUID);
                    });
                    return false;
                })
                .<Map<UUID, PeerConnectionEntity>, Map<UUID, PeerConnectionEntity>> addFunctionalStage("Remove PcDTO",
                        // action
                        pcEntities -> {
                            pcEntities.keySet().stream().forEach(hazelcastMaps.getPcDTOs()::remove);
                            return pcEntities;
                        },
                        // rollback
                        (pcEntitiesHolder, thrownException) -> {
                            if (Objects.isNull(pcEntitiesHolder) || Objects.isNull(pcEntitiesHolder.get())) {
                                this.getLogger().warn("Unexpected condition at rollback. callEntities are null");
                                return;
                            }
                            Map<UUID, PeerConnectionEntity> pcEntities = (Map<UUID, PeerConnectionEntity>) pcEntitiesHolder.get();
                            for (PeerConnectionEntity pcEntity : pcEntities.values()) {
                                UUID pcUUID = pcEntity.peerConnection.peerConnectionUUID;
                                hazelcastMaps.getPcDTOs().put(pcUUID, pcEntity.peerConnection);
                            }
                        })
                .<Map<UUID, PeerConnectionEntity>, Map<UUID, PeerConnectionEntity>> addFunctionalStage("Remove SSRC links",
                        // action
                        pcEntities -> {
                            removeSSRCs(this.getLogger(), hazelcastMaps, pcEntities);
                            return pcEntities;
                        },
                        // rollback
                        (pcEntitiesHolder, thrownException) -> {
                            if (Objects.isNull(pcEntitiesHolder) || Objects.isNull(pcEntitiesHolder.get())) {
                                this.getLogger().warn("Unexpected condition at rollback. callEntities are null");
                                return;
                            }
                            Map<UUID, PeerConnectionEntity> pcEntities = (Map<UUID, PeerConnectionEntity>) pcEntitiesHolder.get();
                            for (PeerConnectionEntity pcEntity : pcEntities.values()) {
                                pcEntity.SSRCs.forEach(SSRC -> {
                                    hazelcastMaps.getSSRCsToPCMap(pcEntity.serviceUUID).put(SSRC, pcEntity.pcUUID);
                                    hazelcastMaps.getPCsToSSRCMap(pcEntity.serviceUUID).put(pcEntity.pcUUID, SSRC);
                                });
                            }
                        })
                .<Map<UUID, PeerConnectionEntity>, Map<UUID, PeerConnectionEntity>> addFunctionalStage("Remove Links to calls",
                        // action
                        pcEntities -> {
                            for (PeerConnectionEntity pcEntity : pcEntities.values()) {
                                hazelcastMaps.getCallToPCUUIDs().remove(pcEntity.callUUID, pcEntity.pcUUID);
                            }
                            return pcEntities;
                        },
                        // rollback
                        (pcEntitiesHolder, thrownException) -> {
                            if (Objects.isNull(pcEntitiesHolder) || Objects.isNull(pcEntitiesHolder.get())) {
                                this.getLogger().warn("Unexpected condition at rollback. callEntities are null");
                                return;
                            }
                            Map<UUID, PeerConnectionEntity> pcEntities = (Map<UUID, PeerConnectionEntity>) pcEntitiesHolder.get();
                            for (PeerConnectionEntity pcEntity : pcEntities.values()) {
                                UUID pcUUID = pcEntity.peerConnection.peerConnectionUUID;
                                hazelcastMaps.getCallToPCUUIDs().put(pcEntity.callUUID, pcUUID);
                            }
                        })
                .addTerminalPassingStage("Completed")
                .build();
    }

    public RemovePCsTask wherePCUUIDs(UUID... pcUUIDs) {
        if (Objects.isNull(pcUUIDs) && pcUUIDs.length < 1) {
            this.getLogger().info("call uuid was not given to be removed");
            return this;
        }
        this.pcUUIDs.addAll(Arrays.asList(pcUUIDs));
        return this;
    }

    public RemovePCsTask wherePCEntities(PeerConnectionEntity... pcEntities) {
        if (Objects.isNull(pcEntities) && pcEntities.length < 1) {
            this.getLogger().info("call uuid was not given to be removed");
            return this;
        }
        for (int i = 0; i < pcEntities.length; ++i) {
            PeerConnectionEntity pcEntity = pcEntities[i];
            UUID pcUUID = pcEntity.peerConnection.peerConnectionUUID;
            this.pcEntities.put(pcUUID, pcEntity);
        }
        return this;
    }

    static void removeSSRCs(Logger logger, HazelcastMaps hazelcastMaps, Map<UUID, PeerConnectionEntity> pcEntities) {
        for (PeerConnectionEntity pcEntity : pcEntities.values()) {
            Collection<Long> removedSSRCs = hazelcastMaps.getPCsToSSRCMap(pcEntity.serviceUUID).remove(pcEntity.pcUUID);
            pcEntity.SSRCs.forEach(SSRC -> {
                        hazelcastMaps.getSSRCsToPCMap(pcEntity.serviceUUID).remove(SSRC, pcEntity.pcUUID);
                        removedSSRCs.remove(SSRC);
                    }
            );

            if (0 < removedSSRCs.size()) {
                logger.warn("There were SSRCs {}, which was part of the peer connection entity, but not presented in the repositories", removedSSRCs);
            }
        }
    }

}
