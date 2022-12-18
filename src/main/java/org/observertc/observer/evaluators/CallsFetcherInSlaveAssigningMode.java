package org.observertc.observer.evaluators;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.ServerTimestamps;
import org.observertc.observer.common.Utils;
import org.observertc.observer.evaluators.eventreports.CallStartedReports;
import org.observertc.observer.repositories.*;
import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.samples.ServiceRoomId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Fetch calls for serviceRoom, create calls and rooms based on provided callIds.
 *
 */
@Singleton
class CallsFetcherInSlaveAssigningMode implements CallsFetcher {

    private static final Logger logger = LoggerFactory.getLogger(CallsFetcherInSlaveAssigningMode.class);

    @Inject
    RoomsRepository roomsRepository;

    @Inject
    CallsRepository callsRepository;

    @Inject
    ClientsRepository clientsRepository;

    @Inject
    CallStartedReports callStartedReports;

    @Inject
    ServerTimestamps serverTimestamps;

    public CallsFetcherResult fetchFor(ObservedClientSamples observedClientSamples) {
        if (observedClientSamples == null || observedClientSamples.isEmpty()) {
            return EMPTY_RESULT;
        }
        var clientRelations = this.fetchObservedClientRelations(observedClientSamples);
        if (clientRelations.clientToServiceRoomIds.size() < 1 || clientRelations.clientToCallIds.size() < 1) {
            return EMPTY_RESULT;
        }

        var clientToCallIds = clientRelations.clientToCallIds;
        var clientToServiceRoomIds = clientRelations.clientToServiceRoomIds;
        var existingCalls = this.callsRepository.getAll(clientToCallIds.values());
        var existingRooms = this.roomsRepository.getAll(clientToServiceRoomIds.values());

        var roomsToCreate = new HashMap<ServiceRoomId, String>();
        var roomsToAlter = new HashMap<ServiceRoomId, String>();
        var activeCallIds = new HashSet<String>();
        var remedyClientIds = new HashSet<String>();
        for (var entry : clientToServiceRoomIds.entrySet()) {
            var clientId = entry.getKey();
            var serviceRoomId = entry.getValue();
            var callId = clientToCallIds.get(clientId);
            if (callId == null) {
                logger.warn("No CallId is assigned to client {} in room: {}, service {}", clientId, serviceRoomId.roomId, serviceRoomId.serviceId);
                continue;
            }
            var existingCall = existingCalls.get(callId);
            var existingRoom = existingRooms.get(serviceRoomId);
            if (existingRoom == null) {
                // we should create a new room with the callId. btw we need to check if there is only one callId for the room
                var assignedCallIdToCreate = roomsToCreate.put(serviceRoomId, callId);
                if (assignedCallIdToCreate != null && !assignedCallIdToCreate.equals(callId)) {
                    logger.warn("Ambiguous call room relation observed in the reported measurements. Service: {}, Room: {}. colliding callIds for newly created room: {}, {}",
                            serviceRoomId.serviceId,
                            serviceRoomId.roomId,
                            assignedCallIdToCreate,
                            callId);
                }
                continue;
            }

            if (callId.equals(existingRoom.getCallId())) {
                // everything is okay, we don't need to change or create anything, but make sure we fetch it
                activeCallIds.add(callId);
                continue;
            }
            if (existingCall != null) {
                // thats a remedy client in a remedy call
                logger.info("Found remedy client. clientId: {}, prev callId: {}, room: {}, service: {}",
                        clientId,
                        existingCall.getCallId(),
                        existingRoom.getServiceRoomId().roomId,
                        existingRoom.getServiceRoomId().serviceId
                );
                remedyClientIds.add(clientId);
                continue;
            }
            // this is probably a new call (or maybe add a deletedCalls set, and we need to check if there is only one callId for this room)
            var assignedCallIdToAlter = roomsToAlter.put(serviceRoomId, callId);
            if (assignedCallIdToAlter != null && !assignedCallIdToAlter.equals(callId)) {
                logger.warn("Ambiguous call room relation observed in the reported measurements. Service: {}, Room: {}. colliding callIds for altering an existing room: {}, {}",
                        serviceRoomId.serviceId,
                        serviceRoomId.roomId,
                        assignedCallIdToAlter,
                        callId);
            }

        }
        // the result is correct, we should drop all client samples, which is not for the "actual" call.

        if (0 < roomsToCreate.size()) {
            var createRoomsResult = this.createRooms(roomsToCreate);
            var callsToCreate = new LinkedList<CallsRepository.CreateCallInfo>();
            for (var entry : Utils.firstNotNull(createRoomsResult.createdRoomToCallIds, Collections.<ServiceRoomId, String>emptyMap()).entrySet()) {
                var serviceRoomId = entry.getKey();
                var assignedCallId = entry.getValue();
                var observedRoom = observedClientSamples.getRoom(serviceRoomId);
                if (observedRoom == null) {
                    logger.warn("Observed room cannot be found for Room {}, in service {}", serviceRoomId.roomId, serviceRoomId.serviceId);
                    continue;
                }
                callsToCreate.add(new CallsRepository.CreateCallInfo(
                        serviceRoomId,
                        observedRoom.getMarker(),
                        assignedCallId,
                        observedRoom.getMinTimestamp()
                ));
            }
//            logger.info("Calls to create: {}", JsonUtils.objectToString(callsToCreate));
            if (0 < callsToCreate.size()) {
                var createdCallsResult = this.createCalls(callsToCreate);
//                logger.info("createdCallsResult: {}", JsonUtils.objectToString(createdCallsResult));
                activeCallIds.addAll(createdCallsResult.createdCallIds);
                activeCallIds.addAll(createdCallsResult.existingCallIds);

            }
        }
        var serverNow = this.serverTimestamps.instant().toEpochMilli();
        if (0 < roomsToAlter.size()) {
            var callsToCreate = new LinkedList<CallsRepository.CreateCallInfo>();
            var oldRoomToCallIds = this.roomsRepository.setCallIds(roomsToAlter);
            for (var oldRoomToCallEntry : Utils.firstNotNull(oldRoomToCallIds, Collections.<ServiceRoomId, String>emptyMap()).entrySet()) {
                var serviceRoomId = oldRoomToCallEntry.getKey();
                var oldCallId = oldRoomToCallEntry.getValue();
                var newCallId = roomsToAlter.get(serviceRoomId);
                if (oldCallId != null && oldCallId.equals(newCallId)) {
                    activeCallIds.add(newCallId);
                    continue;
                }
                logger.info("CallId for room {} in service {} is changed from {} to {}",
                    serviceRoomId.roomId,
                    serviceRoomId.serviceId,
                    oldCallId,
                    newCallId
                );
                var observedRoom = observedClientSamples.getRoom(serviceRoomId);
                if (observedRoom == null) {
                    logger.warn("Observed room cannot be found for Room {}, in service {}", serviceRoomId.roomId, serviceRoomId.serviceId);
                    continue;
                }
                callsToCreate.add(new CallsRepository.CreateCallInfo(
                        serviceRoomId,
                        observedRoom.getMarker(),
                        newCallId,
                        serverNow // it was observedRoom.getMinTimestamp(), but browser epoch timestamp

                ));
                for (var observedClient : observedRoom) {
                    var observedClientCallId = observedClient.streamObservedClientSamples()
                            .map(oc -> oc.getClientSample().callId)
                            .filter(Objects::nonNull)
                            .findFirst().orElse(null);
                    if (observedClientCallId == null || observedClientCallId.equals(newCallId)) {
                        continue;
                    }
                    if (activeCallIds.remove(observedClientCallId)) {
                        clientToCallIds.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(observedClientCallId))
                                .forEach(entry -> remedyClientIds.add(entry.getKey()));
                    }
                }
                activeCallIds.add(newCallId);
            }

            if (0 < callsToCreate.size()) {
                var createdCallsResult = this.createCalls(callsToCreate);
                activeCallIds.addAll(createdCallsResult.createdCallIds);
                activeCallIds.addAll(createdCallsResult.existingCallIds);
            }
        }
        this.roomsRepository.save();
        var actualCalls = this.callsRepository.fetchRecursively(activeCallIds).values()
                .stream()
                .collect(Collectors.toMap(
                        Call::getServiceRoomId,
                        Function.identity(),
                        (c1, c2) -> {
                            logger.warn("Colliding calls for serviceRoomId {}. callIds: {}, {}", c1.getServiceRoomId(), c1.getCallId(), c2.getCallId());
                            return c1;
                        }
                ));
        var existingRemedyClients = Utils.firstNotNull(this.clientsRepository.fetchRecursivelyUpwards(remedyClientIds), Collections.<String, Client>emptyMap());
        Set<String> unregisteredRemedyClientIds;
        if (existingRemedyClients.size() < remedyClientIds.size()) {
            unregisteredRemedyClientIds = remedyClientIds.stream()
                    .filter(clientId -> !existingRemedyClients.containsKey(clientId))
                    .collect(Collectors.toSet());
        } else {
            unregisteredRemedyClientIds = Collections.emptySet();
        }
//        logger.info("roomsToCreate: {}\nroomsToAlter: {}\nactiveCallIds: {}\nremedyClientIds: {}\nunregisteredRemedyClientIds: {}",
//                JsonUtils.objectToString(roomsToCreate),
//                JsonUtils.objectToString(roomsToAlter),
//                JsonUtils.objectToString(activeCallIds),
//                JsonUtils.objectToString(remedyClientIds),
//                JsonUtils.objectToString(unregisteredRemedyClientIds)
//        );
        return new CallsFetcherResult(
                actualCalls,
                existingRemedyClients,
                unregisteredRemedyClientIds
        );
    }


    record ObservedClientRelations(
            Map<String, ServiceRoomId> clientToServiceRoomIds,
            Map<String, String> clientToCallIds
    ) {

    }

    private ObservedClientRelations fetchObservedClientRelations(ObservedClientSamples observedClientSamples) {
        var clientToServiceRoomIds = new HashMap<String, ServiceRoomId>();
        var clientToCallIds = new HashMap<String, String>();
        for (var observedRoom : observedClientSamples.observedRooms()) {
            for (var observedClient : observedRoom) {
                var callId = observedClient
                        .streamObservedClientSamples()
                        .map(ObservedClientSample::getClientSample)
                        .map(clientSample -> clientSample.callId)
                        .filter(Objects::nonNull)
                        .findFirst().orElse(null);
                if (callId == null) {
                    logger.warn("CallId for a client {} must not be null when the callIdAssigning is in SLAVE mode. It leads to potential inconsistency", observedClient.getClientId());
                    continue;
                }
                clientToCallIds.put(observedClient.getClientId(), callId);
                clientToServiceRoomIds.put(observedClient.getClientId(), observedRoom.getServiceRoomId());
            }
        }
        return new ObservedClientRelations(
                clientToServiceRoomIds,
                clientToCallIds
        );
    }

    record CreateRoomsResult(
            Map<ServiceRoomId, String> createdRoomToCallIds,
            Map<ServiceRoomId, String> existingRoomToCallIds
    ) {

    }

    private CreateRoomsResult createRooms(Map<ServiceRoomId, String> roomsToCreate) {
        var createdRoomToCallIds = new HashMap<ServiceRoomId, String>();
        var existingRoomToCallIds = new HashMap<ServiceRoomId, String>();
        if (roomsToCreate == null || roomsToCreate.size() < 1) {
            return new CreateRoomsResult(
                    createdRoomToCallIds,
                    existingRoomToCallIds
            );
        }
        var alreadyInsertedRooms = this.roomsRepository.insertAll(roomsToCreate);

        for (var roomToCreateEntry : roomsToCreate.entrySet()) {
            var serviceRoomId = roomToCreateEntry.getKey();
            var requestedCallId = roomToCreateEntry.getValue();
            var alreadyCreatedRoom = alreadyInsertedRooms.get(serviceRoomId);
            if (alreadyCreatedRoom != null) {
                if (alreadyCreatedRoom.getCallId() == null) {
                    logger.error("CallId in room {} for service {} is NULL", serviceRoomId.roomId, serviceRoomId.serviceId);
                    continue;
                }
                if (requestedCallId != null && !requestedCallId.equals(alreadyCreatedRoom.getCallId())) {
                    logger.warn("Requested to create room {} for service {}, and the callId was {}, but the room was created for callId: {}",
                            alreadyCreatedRoom.getRoomId(),
                            alreadyCreatedRoom.getServiceId(),
                            requestedCallId,
                            alreadyCreatedRoom.getCallId()
                    );
                }
                existingRoomToCallIds.put(serviceRoomId, alreadyCreatedRoom.getCallId());
                continue;
            }
            createdRoomToCallIds.put(serviceRoomId, requestedCallId);
        }
        return new CreateRoomsResult(
                createdRoomToCallIds,
                existingRoomToCallIds
        );
    }



    record CreateCallsResult(
            Set<String> createdCallIds,
            Set<String> existingCallIds
    ) {

    }

    private CreateCallsResult createCalls(Collection<CallsRepository.CreateCallInfo> callsToCreate) {
        if (callsToCreate == null || callsToCreate.size() < 1) {
            return new CreateCallsResult(
                    Collections.emptySet(),
                    Collections.emptySet()
            );
        }
        var existingCalls = Utils.firstNotNull(this.callsRepository.insertAll(callsToCreate), Collections.<String, Call>emptyMap())
                .values().stream().collect(Collectors.toMap(
                        call -> call.getServiceRoomId(),
                        Function.identity()
                ));
        var createdCallIds = new HashSet<String>();
        var existingCallIds = new HashSet<String>();
        for (var callToCreate : callsToCreate) {
            if (existingCalls.containsKey(callToCreate.serviceRoomId())) {
                existingCallIds.add(callToCreate.callId());
            } else {
                createdCallIds.add(callToCreate.callId());

            }
        }

        if (0 < createdCallIds.size()) {
            var createdCallModels = this.callsRepository.getAll(createdCallIds).values().stream().map(Call::getModel).collect(Collectors.toList());
            this.callStartedReports.accept(createdCallModels);
        }
        return new CreateCallsResult(
                createdCallIds,
                existingCallIds
        );
    }
}
