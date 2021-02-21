package org.observertc.webrtc.observer.repositories.tasks;

import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.entities.CallEntity;
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
public class RemoveCallsTask extends ChainedTask<Map<UUID, CallEntity>> {

    private static final Logger logger = LoggerFactory.getLogger(CallsRepository.class);

    private static final String LOCK_NAME = "observertc-call-remover-lock";

    private Set<UUID> callUUIDs = new HashSet<>();
    private Map<UUID, CallEntity> callEntities = new HashMap<>();
    private boolean removePeerConnections = true;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FetchCallsTask fetchCallsTask;

    @Inject
    RemovePCsTask removePCsTask;

    @Inject
    WeakLockProvider weakLockProvider;

    @PostConstruct
    void setup() {
        Function<Set<UUID>, Map<UUID, CallEntity>> fetchCallEntities = callUUIDs -> {
            if (Objects.nonNull(callUUIDs)) {
                this.callUUIDs.addAll(callUUIDs);
            }
            this.callUUIDs.addAll(this.callEntities.keySet());
            Set<UUID> missingUUIDs = this.callUUIDs.stream()
                    .filter(callUUID -> !this.callEntities.containsKey(callUUID))
                    .collect(Collectors.toSet());
            if (0 < missingUUIDs.size()) {
                Map<UUID, CallEntity> map = this.fetchCallsTask.execute(missingUUIDs).getResult();
                this.callEntities.putAll(map);
            }
            return this.callEntities;
        };
        new ChainedTask.Builder<Map<UUID, CallEntity>>(this)
                .withLockProvider(() -> weakLockProvider.autoLock(LOCK_NAME))
                .<Set<UUID>, Map<UUID, CallEntity>>addSupplierEntry("Fetch CallEntity",
                        () -> fetchCallEntities.apply(this.callUUIDs),
                        fetchCallEntities
                )
                .addSupplierChainedTask("Fetch Entity", fetchCallsTask)
                .<Map<UUID, CallEntity>>addBreakCondition((callEntities, resultHolder) -> {
                    if (Objects.isNull(callEntities)) {
                        this.getLogger().warn("No Entities have been passed");
                        resultHolder.set(Collections.EMPTY_MAP);
                        return true;
                    }
                    if (callEntities.size() < 1) {
                        resultHolder.set(callEntities);
                        return true;
                    }
                    this.callUUIDs.stream().filter(c -> !callEntities.containsKey(c)).forEach(callUUID -> {
                        getLogger().warn("Cannot find CallEntity for callUUID {}, it cannot be removed", callUUID);
                    });
                    return false;
                })
                .<Map<UUID, CallEntity>, Map<UUID, CallEntity>> addFunctionalStage("Remove Call",
                        // action
                        callEntities -> {
                            callEntities.keySet().stream().forEach(hazelcastMaps.getCallDTOs()::remove);
                            return callEntities;
                        },
                        // rollback
                        (callEntitiesHolder, thrownException) -> {
                            if (Objects.isNull(callEntitiesHolder) || Objects.isNull(callEntitiesHolder.get())) {
                                this.getLogger().warn("Unexpected condition at rollback. callEntities are null");
                                return;
                            }
                            Map<UUID, CallEntity> callEntities = (Map<UUID, CallEntity>) callEntitiesHolder.get();
                            for (CallEntity callEntity : callEntities.values()) {
                                hazelcastMaps.getCallDTOs().put(callEntity.call.callUUID, callEntity.call);
                            }
                        })
                .<Map<UUID, CallEntity>, Map<UUID, CallEntity>> addFunctionalStage("Remove Call Name",
                        // action
                        callEntities -> {
                            for (CallEntity callEntity : callEntities.values()) {
                                if (Objects.nonNull(callEntity.call.callName)) {
                                    hazelcastMaps.getCallNames(callEntity.call.serviceUUID).remove(callEntity.call.callName, callEntity.call.callUUID);
                                }
                            }
                            return callEntities;
                        },
                        // rollback
                        (callEntitiesHolder, thrownException) -> {
                            if (Objects.isNull(callEntitiesHolder) || Objects.isNull(callEntitiesHolder.get())) {
                                this.getLogger().warn("Unexpected condition at rollback. callEntities are null");
                                return;
                            }
                            Map<UUID, CallEntity> callEntities = (Map<UUID, CallEntity>) callEntitiesHolder.get();
                            for (CallEntity callEntity : callEntities.values()) {
                                if (Objects.nonNull(callEntity.call.callName)) {
                                    hazelcastMaps.getCallNames(callEntity.call.serviceUUID).put(callEntity.call.callName, callEntity.call.callUUID);
                                }
                            }
                        })
                .<Map<UUID, CallEntity>, Map<UUID, CallEntity>> addFunctionalStage("Remove Peer Connections", callEntities -> {
                    if (!this.removePeerConnections) {
                        Optional<Integer> hangingPCHolder = callEntities.values().stream().map(e -> e.peerConnections.values().size()).reduce(Integer::sum);
                        if (hangingPCHolder.isPresent() && 0 < hangingPCHolder.get()) {
                            List<PeerConnectionEntity> hangingPcs = callEntities.values().stream().flatMap(e -> e.peerConnections.values().stream()).collect(Collectors.toList());
                            this.getLogger().warn("Peer Connections will be hanged, du to deleted calls {}", hangingPcs);
                        }
                        return callEntities;
                    }
                    Set<UUID> pcUUIDs = callEntities.values().stream().flatMap(callEntity -> callEntity.peerConnections.keySet().stream()).collect(Collectors.toSet());
                    this.removePCsTask
                            .withLogger(this.getLogger())
                            .withRethrowingExceptions(true)
                    ;
                    this.removePCsTask.execute(pcUUIDs);
                    return callEntities;
                })
                .addTerminalPassingStage("Completed")
                .build();
    }

    public RemoveCallsTask whereCallUUID(UUID... callUUIDs) {
        if (Objects.isNull(callUUIDs) && callUUIDs.length < 1) {
            this.getLogger().info("call uuid was not given to be removed");
            return this;
        }
        this.callUUIDs.addAll(Arrays.asList(callUUIDs));
        return this;
    }

    public RemoveCallsTask whereCallEntities(CallEntity... callEntities) {
        if (Objects.isNull(callEntities) && callEntities.length < 1) {
            this.getLogger().info("call uuid was not given to be removed");
            return this;
        }
        for (int i = 0; i < callEntities.length; ++i) {
            CallEntity callEntity = callEntities[i];
            UUID callUUID = callEntity.call.callUUID;
            this.callEntities.put(callUUID, callEntity);
        }
        return this;
    }

    public RemoveCallsTask dontRemovePeerConnections() {
        this.removePeerConnections = false;
        return this;
    }

}
