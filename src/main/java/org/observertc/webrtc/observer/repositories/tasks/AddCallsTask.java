package org.observertc.webrtc.observer.repositories.tasks;

import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.functions.BiFunction;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Prototype
public class AddCallsTask extends ChainedTask<Map<UUID, CallEntity>> {

    private static final Logger logger = LoggerFactory.getLogger(AddCallsTask.class);

    private static final String LOCK_NAME = "observertc-call-adder-lock";

    private Map<UUID, CallEntity> newCallEntities = new HashMap<>();
    private Map<UUID, CallEntity> foundCallEntities = new HashMap<>();
    private boolean checkExistingEntitiesByName = true;
    private boolean checkExistingEntitiesBySSRC = true;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FindCallsByNameTask findCallsByNameTask;

    @Inject
    FindPCsBySSRCTask findPCsBySSRCTask;

    @Inject
    FetchCallsTask fetchCallsTask;

    @Inject
    AddPCsTask addPCsTask;

    @Inject
    WeakLockProvider weakLockProvider;


    @PostConstruct
    void setup() {
        BiFunction<AtomicReference, AtomicReference<CallDTO>, Boolean> foundCallChecker = (lastInput, resultHolder) -> {
            if (Objects.isNull(lastInput) || Objects.isNull(lastInput.get())) {
                return false;
            }
            CallDTO foundCallDTO = (CallDTO) lastInput.get();
            resultHolder.set(foundCallDTO);
            return true;
        };
        new Builder<>(this)
            .withLockProvider(() -> weakLockProvider.autoLock(LOCK_NAME))
            .<Map<UUID, CallEntity>> addConsumerEntry("Merge All Inputs",
                () -> {},
                callEntities -> {
                    if (Objects.isNull(callEntities)) {
                        return;
                    }
                    callEntities.forEach((callUUID, callEntity) -> {
                        CallEntity existingEntity = this.newCallEntities.get(callUUID);
                        if (Objects.nonNull(existingEntity) && !existingEntity.equals(callEntity)) {
                            this.getLogger().warn("Different call entities are given for the same calluuid. callEntity will override the existingCallEntityy. callEntity: {}, existingCallEntity: {}", callEntity, existingEntity);
                        }
                        this.newCallEntities.put(callUUID, callEntity);
                    });
                }
            )
            .addActionStage("Find Calls By Name",
                () -> {
                    if (!this.checkExistingEntitiesByName) {
                        return;
                    }
                    Map<String, UUID> map = new HashMap<>();
                    for (CallEntity callEntity : this.newCallEntities.values()) {
                        if (Objects.isNull(callEntity.call.callName)) {
                            continue;
                        }
                        UUID callUUID = map.get(callEntity.call.callName);
                        if (Objects.nonNull(callUUID)) {
                            this.getLogger().warn("Multiple call has been search for the same callName. This is not allowed in the adding operation, " +
                                    "so the search part will not include the name search for those calls callname: {} callUUIDs: {}, {}", callEntity.call.callName, callUUID, callEntity.call.callUUID);
                            map.remove(callEntity.call.callName);
                            continue;
                        }
                        map.put(callEntity.call.callName, callEntity.call.callUUID);
                    }
                    if (map.size() < 1) {
                        return;
                    }
                    map.forEach((callName, callUUID) -> {
                        CallEntity callEntity = this.newCallEntities.get(callUUID);
                        findCallsByNameTask.whereCallName(callEntity.call.serviceUUID, callEntity.call.callName);
                    });
                    Map<UUID, CallEntity> foundCalls = findCallsByNameTask.execute().getResult();
                    foundCalls.values().forEach(foundEntity -> {
                        UUID callUUIDToRemove = map.get(foundEntity.call.callName);
                        if (Objects.isNull(callUUIDToRemove)) {
                            getLogger().warn("There is no call uuid for name {} after results are found", foundEntity.call.callName);
                            return;
                        }
                        this.newCallEntities.remove(callUUIDToRemove);
                        this.foundCallEntities.put(foundEntity.call.callUUID, foundEntity);
                    });
                }
            )
            // if we have found all calls we wanted to add, there is no point of adding
            .addBreakCondition(resultHolder -> {
                if (this.newCallEntities.size() < 1) {
                    resultHolder.set(Collections.unmodifiableMap(this.foundCallEntities));
                    return true;
                }
                return false;
            })
            .addActionStage("Find Calls by SSRCs", () -> {
                if (!this.checkExistingEntitiesBySSRC) {
                    return;
                }
                Map<String, UUID> SSRCToCall = new HashMap<>();
                BiFunction<UUID, Long, String> keyMaker = (serviceUUID, SSRC) -> String.format("%s-%d", serviceUUID.toString(), SSRC);
                for (CallEntity callEntity : this.newCallEntities.values()) {
                    for (Long SSRC : callEntity.SSRCs) {
                        findPCsBySSRCTask.whereServiceAndSSRC(callEntity.call.serviceUUID, SSRC);
                        String key = keyMaker.apply(callEntity.call.serviceUUID, SSRC);
                        SSRCToCall.put(key, callEntity.call.callUUID);
                    }
                }

                if (!findPCsBySSRCTask.execute().succeeded()) {
                    getLogger().warn("Find By SSRC task was failed, AddCall task might not be able to find calls already exists.");
                    return;
                }
                Map<UUID, PeerConnectionEntity> foundPCs = findPCsBySSRCTask.getResult();
                if (foundPCs.size() < 1) {
                    return;
                }
                boolean executeFetchCalls = false;
                for (PeerConnectionEntity pcEntity : foundPCs.values()) {
                    UUID callUUID = null;
                    for (Long SSRC : pcEntity.SSRCs) {
                        String key = keyMaker.apply(pcEntity.serviceUUID, SSRC);
                        callUUID = SSRCToCall.get(key);
                        if (Objects.nonNull(callUUID)) {
                            break;
                        }
                    }
                    if (Objects.isNull(callUUID)) {
                        getLogger().warn("Cannot find PC Entity SSRCs amongst call entities' SSRC");
                        continue;
                    }
                    newCallEntities.remove(callUUID);
                    fetchCallsTask.whereCallUUID(pcEntity.callUUID);
                    executeFetchCalls = true;
                }
                if (!executeFetchCalls || !fetchCallsTask.execute().succeeded()) {
                    return;
                }
                Map<UUID, CallEntity> newlyFoundCalls = fetchCallsTask.getResult();
                foundCallEntities.putAll(newlyFoundCalls);
            })
            // if we have found all calls we wanted to add, there is no point of adding
            .addBreakCondition(resultHolder -> {
                if (this.newCallEntities.size() < 1) {
                    resultHolder.set(Collections.unmodifiableMap(this.foundCallEntities));
                    return true;
                }
                return false;
            })
            .addActionStage("Add Call DTOs",
                // action
                () -> {
                    Map<UUID, CallDTO> callDTOs = this.newCallEntities.entrySet().stream()
                            .map(entry -> Map.entry(entry.getKey(), entry.getValue().call))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    hazelcastMaps.getCallDTOs().putAll(callDTOs);
                },
                // rollback
                (inputHolder, thronException) -> {
                    for (CallEntity callEntity : this.foundCallEntities.values()) {
                        hazelcastMaps.getCallDTOs().remove(callEntity.call.callUUID, callEntity.call);
                    }
                }
            )
            .addActionStage("Add Peer Connections", () -> {
                for (CallEntity callEntity : this.newCallEntities.values()) {
                    addPCsTask.withPeerConnection(callEntity.peerConnections.values().toArray(new PeerConnectionEntity[0]));
                }
                addPCsTask.execute();
            })
            .addActionStage("Bind calls by names",
                // action
                () -> {
                    for (CallEntity callEntity : this.newCallEntities.values()) {
                        if (Objects.isNull(callEntity.call.callName)) {
                            continue;
                        }
                        hazelcastMaps.getCallNames(callEntity.call.serviceUUID).put(callEntity.call.callName, callEntity.call.callUUID);
                    }
                },
                // rollback
                (inputHolder, thronException) -> {
                    for (CallEntity callEntity : this.newCallEntities.values()) {
                        if (Objects.isNull(callEntity.call.callName)) {
                            continue;
                        }
                        hazelcastMaps.getCallNames(callEntity.call.serviceUUID).remove(callEntity.call.callName, callEntity.call.callUUID);
                    }
                }
            )
            .addTerminalSupplier("Provide Results", () -> {
                Map<UUID, CallEntity> result = this.newCallEntities;
                if (0 < this.foundCallEntities.size()) {
                    result.putAll(this.foundCallEntities);
                }
                return Collections.unmodifiableMap(result);
            })
            .build();
    }

    public AddCallsTask withCallEntity(CallEntity callEntity) {
        if (Objects.isNull(callEntity)) {
            return this;
        }
        this.newCallEntities.put(callEntity.call.callUUID, callEntity);
        return this;
    }

    public AddCallsTask doNotCheckExistingCallsByNameOrSSRC() {
        this.checkExistingEntitiesByName = false;
        this.checkExistingEntitiesBySSRC = false;
        return this;
    }
}
