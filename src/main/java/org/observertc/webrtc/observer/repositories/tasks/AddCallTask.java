//package org.observertc.webrtc.observer.repositories.tasks;
//
//import io.micronaut.context.annotation.Prototype;
//import io.reactivex.rxjava3.functions.BiFunction;
//import org.observertc.webrtc.observer.common.ChainedTask;
//import org.observertc.webrtc.observer.common.Utils;
//import org.observertc.webrtc.observer.dto.CallDTO;
//import org.observertc.webrtc.observer.dto.ClientDTO;
//import org.observertc.webrtc.observer.entities.CallEntity;
//import org.observertc.webrtc.observer.entities.ClientEntity;
//import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
//import org.observertc.webrtc.observer.repositories.HazelcastMaps;
//import org.observertc.webrtc.observer.samples.ServiceRoomId;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.annotation.PostConstruct;
//import javax.inject.Inject;
//import javax.inject.Provider;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//@Prototype
//public class AddCallTask extends ChainedTask<CallEntity> {
//
//    private static final Logger logger = LoggerFactory.getLogger(AddCallTask.class);
//
//    private static final String LOCK_NAME = "observertc-call-adder-lock";
//
//    private final CallDTO.Builder callDTOBuilder = CallDTO.builder();
//
//    private final Map<UUID, ClientEntity> clientEntities = new HashMap<>();
//
//    private boolean fetchBackClientEntities = true;
//
//    @Inject
//    HazelcastMaps hazelcastMaps;
//
//    @Inject
//    FindCallIdsByServiceRoomIds findCallIds;
//
//    @Inject
//    FetchCallsTask fetchCallsTask;
//
//    @Inject
//    AddClientsTask addClientsTask;
//
//    @Inject
//    Provider<RemoveClientsTask> removeClientsTaskProvider;
//
//    @Inject
//    WeakLockProvider weakLockProvider;
//
//
//    @PostConstruct
//    void setup() {
//        BiFunction<AtomicReference, AtomicReference<CallDTO>, Boolean> foundCallChecker = (lastInput, resultHolder) -> {
//            if (Objects.isNull(lastInput) || Objects.isNull(lastInput.get())) {
//                return false;
//            }
//            CallDTO foundCallDTO = (CallDTO) lastInput.get();
//            resultHolder.set(foundCallDTO);
//            return true;
//        };
//        new Builder<>(this)
//            .withLockProvider(() -> weakLockProvider.autoLock(LOCK_NAME))
//            .<CallDTO.Builder, CallDTO> addSupplierEntry("Merge All Inputs",
//                () -> {
//                    UUID callId = UUID.randomUUID();
//                    return this.callDTOBuilder.withCallId(callId).build();
//                },
//                callDTO -> {
//                    UUID callId = UUID.randomUUID();
//                    if (Objects.isNull(callDTOBuilder)) {
//                        return this.callDTOBuilder.withCallId(callId).build();
//                    }
//                    return this.callDTOBuilder.copyFrom(callDTOBuilder).withCallId(callId).build();
//                }
//            )
//            // Check if call already exists
//            .<CallDTO>addBreakCondition((callDTO, resultHolder) -> {
//                Objects.requireNonNull(callDTO);
//                ServiceRoomId serviceRoomId = ServiceRoomId.make(callDTO.serviceId, callDTO.roomId);
//                this.findCallIds.whereServiceRoomIds(Set.of(serviceRoomId));
//                if (!this.findCallIds.execute().succeeded()) {
//                    throw new RuntimeException("Cannot execute FindCall task, there is no way to check if the call exists already");
//                }
//                Map<ServiceRoomId, UUID> foundCallIds = this.findCallIds.getResult();
//                if (0 < foundCallIds.size()) {
//                    UUID foundCallId = foundCallIds.get(serviceRoomId);
//                    if (Objects.isNull(foundCallId)) {
//                        logger.warn("There was a call found for serviceRoomId ({}), but cannot be retrieved", serviceRoomId.toString());
//                        return false;
//                    }
//                    if (!this.fetchCallsTask.whereCallUUID(foundCallId).execute().succeeded()) {
//                        throw new RuntimeException("Cannot fetch calls for serviceRoomId: " + serviceRoomId.toString());
//                    }
//                    Map<UUID, CallEntity> foundCallEntities = this.fetchCallsTask.getResult();
//                    if (foundCallEntities.size() < 1) {
//                        logger.warn("There was no call fetched for serviceRoomId ({}), but FoundCalls indicate there are!", serviceRoomId.toString());
//                        return false;
//                    }
//                    CallEntity result = foundCallEntities.get(foundCallId);
//                    if (Objects.isNull(result)) {
//                        throw new RuntimeException("Fetched calls for callId (" + serviceRoomId.toString() + ") cannot be retrieved from fetched calls!");
//                    }
//                    resultHolder.set(result);
//                    return true;
//                }
//                return false;
//            })
//            .<CallDTO, CallEntity.Builder>addFunctionalStage("Add CallDTO to hazelcast",
//                callDTO -> {
//                    this.hazelcastMaps.getCalls().put(callDTO.callId, callDTO);
//                    var builder = CallEntity.builder().withCallDTO(callDTO);
//                    return builder;
//                },
//                // rollback
//                (inputHolder, thrownException) -> {
//                    CallDTO callDTO = (CallDTO) inputHolder.get();
//                    if (Objects.isNull(callDTO)) {
//                        return;
//                    }
//                    this.hazelcastMaps.getCalls().remove(callDTO.callId);
//                }
//            )
//            .<CallEntity.Builder, CallEntity.Builder>addFunctionalStage("Add Client Entities",
//                callEntityBuilder -> {
//                    this.addClientsTask.withClientEntities(this.clientEntities).withFetchingBackTheResult(this.fetchBackClientEntities);
//                    if (!this.addClientsTask.execute().succeeded()) {
//                        throw new RuntimeException("Cannot add client");
//                    }
//                    if (!this.fetchBackClientEntities) {
//                        return callEntityBuilder.withClientEntities(this.clientEntities);
//                    }
//                    Map<UUID, ClientEntity> fetchedClientEntities = this.addClientsTask.getResult();
//                    return callEntityBuilder.withClientEntities(fetchedClientEntities);
//                },
//                // rollback
//                (inputHolder, throwException) -> {
//                    CallEntity.Builder callEntityBuilder = (CallEntity.Builder) inputHolder.get();
//                    if (Objects.isNull(callEntityBuilder)) {
//                        return;
//                    }
//                    RemoveClientsTask removeClientsTask = removeClientsTaskProvider.get();
//                    Set<UUID> clientIds = this.clientEntities.keySet();
//                    removeClientsTask.whereClientIds(clientIds);
//                    if (!removeClientsTask.execute().succeeded()) {
//                        logger.error("Cannot remove client ids in rollback stage to rollback adding calls");
//                        return;
//                    }
//                })
//            .<CallEntity.Builder>addTerminalFunction("Build result", callEntityBuilder -> {
//                if (Objects.isNull(callEntityBuilder)) {
//                    return null;
//                }
//                return callEntityBuilder.build();
//            })
//            .build();
//    }
//
//    public AddCallTask withFetchBackClientEntities(boolean value) {
//        this.fetchBackClientEntities = value;
//        return this;
//    }
//
//    public AddCallTask withClientEntities(ClientEntity... entities) {
//        if (Objects.isNull(entities)) {
//            return this;
//        }
//        Map<UUID, ClientEntity> entityMap = Arrays.stream(entities).collect(Collectors.toMap(entity -> entity.getClientId(), Function.identity()));
//        this.clientEntities.putAll(entityMap);
//        return this;
//    }
//
//    public AddCallTask withClientEntities(Map<UUID, ClientEntity> clientEntities) {
//        if (Objects.isNull(clientEntities)) {
//            return this;
//        }
//        this.clientEntities.putAll(clientEntities);
//        return this;
//    }
//
//    public AddCallTask withServiceId(String value) {
//        this.callDTOBuilder.withServiceId(value);
//        return this;
//    }
//
//    public AddCallTask withRoomId(String value) {
//        this.callDTOBuilder.withRoomId(value);
//        return this;
//    }
//
//
//    public AddCallTask withStartedTimestamp(Long value) {
//        this.callDTOBuilder.withStartedTimestamp(value);
//        return this;
//    }
//}
