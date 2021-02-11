package org.observertc.webrtc.observer.repositories.tasks;

import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import io.reactivex.rxjava3.functions.BiFunction;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.common.TaskStage;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.repositories.CallsRepository;
import org.observertc.webrtc.observer.repositories.Repositories;
import org.observertc.webrtc.observer.tasks.WeakLockProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class CallsRepositoryTasks {

    private static final Logger logger = LoggerFactory.getLogger(CallsRepository.class);

    private static final String CALL_MODIFICATION_LOCK_NAME = "observertc-call-modification-lock";

    @Inject
    Repositories repositories;

    @Inject
    Provider<FindCallByNameTask> findCallByNameTaskProvider;

    @Inject
    Provider<FindCallBySSRCTask> findCallBySSRCTaskProvider;

    @Inject
    WeakLockProvider weakLockProvider;


    public ChainedTask<CallDTO> makeCallAdderTask(CallDTO newCallDTO, Set<Long> SSRCs) {
        BiFunction<AtomicReference, AtomicReference<CallDTO>, Boolean> foundCallChecker = (lastInput, resultHolder) -> {
            if (Objects.isNull(lastInput) || Objects.isNull(lastInput.get())) {
                return false;
            }
            CallDTO foundCallDTO = (CallDTO) lastInput.get();
            resultHolder.set(foundCallDTO);
            return true;
        };

        ChainedTask<CallDTO> result = ChainedTask.<CallDTO>builder()
                .withLockProvider(() -> weakLockProvider.autoLock(CALL_MODIFICATION_LOCK_NAME))
                .addSupplierTask("Try Find Call By Name", findCallByNameTaskProvider.get().forServiceAndCall(newCallDTO.serviceUUID, newCallDTO.callName))
                .addSupplierTaskIfInput("Try Find Call By SSRCs", Objects::isNull, findCallBySSRCTaskProvider.get().forServiceAndSSRCs(newCallDTO.serviceUUID, SSRCs))
                .addTerminalConditionAndAbsorbInput(foundCallChecker)
                .addActionStage("Add new Call",
                        // action
                        () -> {
                            repositories.getCallDTOs().put(newCallDTO.callUUID, newCallDTO);
                        },
                        // rollback
                        (actionInputHolder, thrownException) -> {
                            repositories.getCallDTOs().remove(newCallDTO.callUUID);
                        })
                .addActionStage("Add Call Name",
                        // action
                        () -> {
                            if (Objects.nonNull(newCallDTO.callName)) {
                                repositories.getCallNames(newCallDTO.serviceUUID).put(newCallDTO.callName, newCallDTO.callUUID);
                            }
                        },
                        // rollback
                        (actionInputHolder, thrownException) -> {
                            if (Objects.nonNull(newCallDTO.callName)) {
                                repositories.getCallNames(newCallDTO.serviceUUID).remove(newCallDTO.callName, newCallDTO.callUUID);
                            }
                        })
                .addActionStage("Add Call SSRCs",
                        // action
                        () -> {
                            if (Objects.nonNull(SSRCs) && 0 < SSRCs.size()) {
                                Map<Long, UUID> ssrmap = SSRCs.stream().collect(Collectors.toMap(Function.identity(), s -> newCallDTO.callUUID));
                                repositories.getSSRCToCallMap(newCallDTO.serviceUUID).putAll(ssrmap);
                                MultiMap<UUID, Long> callsToSSRCs = repositories.getCallToSSRCMap(newCallDTO.serviceUUID);
                                SSRCs.forEach(ssrc -> callsToSSRCs.put(newCallDTO.callUUID, ssrc));
                            }
                        },
                        // rollback
                        (actionInputHolder, thrownException) -> {
                            if (Objects.nonNull(SSRCs) && 0 < SSRCs.size()) {
                                IMap<Long, UUID> ssrcMap = repositories.getSSRCToCallMap(newCallDTO.serviceUUID);
                                SSRCs.forEach(ssrcMap::remove);
                                repositories.getCallToSSRCMap(newCallDTO.serviceUUID).remove(newCallDTO.callUUID);
                            }
                        })
                .build();
        return result;
    }

    public ChainedTask<CallEntity> makeCallRemoverTask(UUID callUUID) {

        ChainedTask<CallDTO> result = ChainedTask.<CallDTO>builder()
                .withLockProvider(() -> weakLockProvider.autoLock(CALL_MODIFICATION_LOCK_NAME))
                .addSupplierStage("Try to find Call By UUID", ()-> {

                })
                .addTerminalCondition((lastInput, resultHolder) -> {
                    if (Objects.isNull(lastInput)) {
                        logger.warn("Not found callDTO for Call UUID {}", callUUID);
                        return true;
                    }
                    return false;
                })
                .addFunctionalStage("Remove Call",
                        // action
                        callDTOObj -> {
                            CallDTO callDTO = repositories.getCallDTOs().remove(callUUID);
                            CallEntity callEntity = new CallEntity();
                            callEntity.call = callDTO;
                            return callEntity;
                        },
                        // rollback
                        (callDTOHolder, thrownException) -> {
                            if (Objects.nonNull(callDTOHolder.get())) {
                                repositories.getCallDTOs().put(callUUID, (CallDTO) callDTOHolder.get());
                            }
                        })
                .addFunctionalStage("Remove Call Name",
                        // action
                        callEntityObj -> {
                            CallEntity callEntity = (CallEntity) callEntityObj;
                            CallDTO callDTO = callEntity.call;
                            if (Objects.nonNull(callDTO.callName)) {
                                repositories.getCallNames(callDTO.serviceUUID).remove(callDTO.callName, callDTO.callUUID);
                            }
                            return callEntity;
                        },
                        // rollback
                        (callDTOHolder, thrownException) -> {
                            if (Objects.nonNull(callDTOHolder.get())) {
                                CallDTO callDTO = (CallDTO) callDTOHolder.get();
                                repositories.getCallNames(callDTO.serviceUUID).put(callDTO.callName, callDTO.callUUID);
                            }
                        })
                .addFunctionalStage("Remove Call SSRCs",
                        // action
                        callEntityObj -> {
                            CallEntity callEntity = (CallEntity) callEntityObj;
                            CallDTO callDTO = callEntity.call;
                            Collection<Long> SSRCs = repositories.getCallToSSRCMap(callDTO.serviceUUID).remove(callDTO.callUUID);
                            callEntity.SSRCs.addAll(SSRCs);
                            IMap<Long, UUID> ssrcToCall = repositories.getSSRCToCallMap(callDTO.serviceUUID);
                            SSRCs.forEach(ssrcToCall::remove);
                            return callEntity;
                        },
                        // rollback
                        (callEntityObjHolder, thrownException) -> {
                            if (Objects.isNull(callEntityObjHolder.get())) {
                                return;
                            }
                            CallEntity callEntity = (CallEntity) callEntityObjHolder.get();
                            CallDTO callDTO = callEntity.call;
                            if (callEntity.SSRCs.size() < 1) {
                                return;
                            }
                            IMap<Long, UUID> ssrcToCall = repositories.getSSRCToCallMap(callDTO.serviceUUID);
                            callEntity.SSRCs.forEach(ssrc -> ssrcToCall.put(ssrc, callDTO.callUUID));
                            MultiMap<UUID, Long> callToSSRC = repositories.getCallToSSRCMap(callDTO.serviceUUID);
                            callEntity.SSRCs.forEach(ssrc -> callToSSRC.put(callDTO.callUUID, ssrc));
                        })
                .addTerminalStage("Remove Peer Connections",
                        // action
                        callEntityObj -> {
                            CallEntity callEntity = (CallEntity) callEntityObj;
                            CallDTO callDTO = callEntity.call;
                            return callEntity;
                        },
                        // rollback
                        (callEntityObjHolder, thrownException) -> {
                            if (Objects.isNull(callEntityObjHolder.get())) {
                                return;
                            }
                            CallEntity callEntity = (CallEntity) callEntityObjHolder.get();
                        })
                .build();
        return result;
    }



}
