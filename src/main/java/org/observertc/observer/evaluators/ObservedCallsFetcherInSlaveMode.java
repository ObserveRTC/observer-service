package org.observertc.observer.evaluators;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
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

@Singleton
class ObservedCallsFetcherInSlaveMode {

    private static final Logger logger = LoggerFactory.getLogger(ObservedCallsFetcherInSlaveMode.class);

    @Inject
    RoomsRepository roomsRepository;

    @Inject
    CallsRepository callsRepository;

    @Inject
    ClientsRepository clientsRepository;

    @Inject
    CallStartedReports callStartedReports;

    Map<ServiceRoomId, Call> fetchFor(ObservedClientSamples observedClientSamples) {
        if (observedClientSamples == null || observedClientSamples.isEmpty()) {
            return Collections.emptyMap();
        }
        var clientRelations = this.fetchObservedClientRelations(observedClientSamples);
        if (clientRelations.clientToServiceRoomIds.size() < 1 || clientRelations.clientToCallIds.size() < 1) {
            return Collections.emptyMap();
        }

        var existingClients = this.clientsRepository.fetchRecursivelyUpwards(clientRelations.clientToCallIds.keySet());
        var missingRooms = clientRelations.




        var createRoomsResult = this.createRooms(observedClientSamples);
        var callIds = new HashSet<String>();
        if (0 < createRoomsResult.existingRooms.size()) {
            callIds.addAll(createRoomsResult.existingRooms.values().stream()
                    .map(Room::getCallId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet())
            );
        }

        if (createRoomsResult.createdRooms.size() < 1) {
            return this.createResult(callIds, observedClientSamples);
        }
        var callsToCreate = new LinkedList<CallsRepository.CreateCallInfo>();
        for (var observedRoom : observedClientSamples.observedRooms()) {
            var createdRoom = createRoomsResult.createdRooms.get(observedRoom.getServiceRoomId());
            if (createdRoom == null) {
                continue;
            }
            callsToCreate.add(new CallsRepository.CreateCallInfo(
                    createdRoom.getServiceRoomId(),
                    observedRoom.getMarker(),
                    createdRoom.getCallId(),
                    observedRoom.getMinTimestamp()
            ));
        }
        if (callsToCreate.size() < 1) {
            return this.createResult(callIds, observedClientSamples);
        }

        var createCallsResult = this.createCalls(callsToCreate);
        if (createCallsResult.existingCalls != null && 0 < createCallsResult.existingCalls.size()) {
            callIds.addAll(createCallsResult.existingCalls
                    .values().stream()
                    .map(Call::getCallId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet())
            );
        }
        if (createCallsResult.createdCalls != null && 0 < createCallsResult.createdCalls.size()) {
            callIds.addAll(createCallsResult.createdCalls
                    .values().stream()
                    .map(Call::getCallId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet())
            );
        }

        return this.createResult(callIds, observedClientSamples);
    }

    private Map<ServiceRoomId, Call> createResult(Set<String> callIds, ObservedClientSamples observedClientSamples) {
        if (callIds == null || callIds.size() < 1) {
            return Collections.emptyMap();
        }
        var result = this.callsRepository.getAll(callIds).values().stream()
                .collect(Collectors.toMap(
                        call -> call.getServiceRoomId(),
                        Function.identity()
                ));
        for (var observedRoom : observedClientSamples.observedRooms()) {
            var call = result.get(observedRoom.getServiceRoomId());
            if (call == null) {
                continue;
            }
            var callId = call.getCallId();
            for (var observedClientSample : observedRoom.observedClientSamples()) {
                var clientSample = observedClientSample.getClientSample();
                clientSample.callId = callId;
            }
        }
        return result;
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
                    logger.warn("CallId for a client {} must not be null when the callIdAssigning is in SLAVE mode. I leads to potential inconsistency");
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

    private Map<String, Call> fetchExistingCalls(ObservedClientSamples observedClientSamples, ObservedClientRelations observedClientRelations) {


        if (observedClientRelations.clientToCallIds.size())
        var missingClientToCallIds = new HashMap<String, String>();
//        var missingClientToServiceRooms = new HashMap<String, >()

    }

    record UpdatedRoomInfo(String oldCallId, String newCallId) {

    }

    record CreateRoomsResult(
            Map<ServiceRoomId, Room> existingRooms,
            Map<ServiceRoomId, Room> createdRooms,
            Map<ServiceRoomId, UpdatedRoomInfo> updatedRooms
    ) {

    }

    private CreateRoomsResult fetchRooms(ObservedClientSamples observedClientSamples, ObservedClientRelations relations) {
        var existingRooms = new HashMap<ServiceRoomId, Room>(this.roomsRepository.getAll(relations.clientToServiceRoomIds.values()));
        var roomsToCreate = new HashMap<ServiceRoomId, String>();
        var callsToCheck = new HashMap<String, ServiceRoomId>();
        for (var observedRoom : observedClientSamples.observedRooms()) {
            var serviceRoomId = observedRoom.getServiceRoomId();
            var existingRoom = existingRooms.get(serviceRoomId);
            for (var observedClient : observedRoom) {
                var callId = relations.clientToCallIds.get(observedClient.getClientId());
                if (callId == null) {
                    continue;
                }
                if (existingRoom != null) {
                    var removedCallId = roomsToCreate.put(serviceRoomId, callId);
                    if (removedCallId != null && !removedCallId.equals(callId)) {
                        logger.warn("Ambigous callId for room {} to create. The registered callId is {}, the removedCallId {}",
                                serviceRoomId,
                                callId,
                                removedCallId
                        );
                    }
                    continue;
                }
                if (!callId.equals(existingRoom.getCallId())) {
                    // here we have calls not pointed by an existing rooms. they are either previous calls, or next calls we need to create
                    // but we don't know at this point.
                    callsToCheck.put(callId, existingRoom.getServiceRoomId());
                }
                // client joined to the call already created and the room is pointing to
            }
        }

        var alreadyCreatedRooms = Utils.firstNotNull(this.roomsRepository.insertAll(roomsToCreate), Collections.<ServiceRoomId, Room>emptyMap());
        for (var alreadyCreatedRoom : alreadyCreatedRooms.values()) {
            var expectedCallId = roomsToCreate.get(alreadyCreatedRoom.getServiceRoomId());
            if (expectedCallId == null) {
                // wtf?
                logger.warn("Room {} is created based on collected callIds, but th expected callId is not found in the list for roomsToCreate",
                        alreadyCreatedRoom.getServiceRoomId()
                );
                continue;
            }
            if (alreadyCreatedRoom.getCallId().equals(expectedCallId)) {
                existingRooms.put(alreadyCreatedRoom.getServiceRoomId(), alreadyCreatedRoom);
                continue;
            }
            callsToCheck.put(expectedCallId, alreadyCreatedRoom.getServiceRoomId());
            logger.warn("Room {} is concurrently created for two callIds: {}, {}. That might cause issues as only one will be the active call for the room", expectedCallId, alreadyCreatedRoom.getCallId());
        }





        var createdRooms = new HashMap<ServiceRoomId, Room>();
        for (var room : this.roomsRepository.getAll(roomsToCreate.keySet()).values()) {
            var alreadyCreatedRoom = alreadyCreatedRooms.get(room.getServiceRoomId());
            if (alreadyCreatedRoom != null) {
                existingRooms.put(room.getServiceRoomId(), alreadyCreatedRoom);

                continue;
            }
            createdRooms.put(room.getServiceRoomId(), room);
        }

        return new CreateRoomsResult(
                existingRooms,
                createdRooms
        );
    }

    record CreateCallsResult(
            Map<ServiceRoomId, Call> existingCalls,
            Map<ServiceRoomId, Call> createdCalls
    ) {

    }

    private CreateCallsResult createCalls(Collection<CallsRepository.CreateCallInfo> callsToCreate) {
        if (callsToCreate.size() < 1) {
            return new CreateCallsResult(
                    Collections.emptyMap(),
                    Collections.emptyMap()
            );
        }
        var existingCalls = Utils.firstNotNull(this.callsRepository.insertAll(callsToCreate), Collections.<String, Call>emptyMap())
                .values().stream().collect(Collectors.toMap(
                        call -> call.getServiceRoomId(),
                        Function.identity()
                ));
        var callIds = callsToCreate.stream().map(CallsRepository.CreateCallInfo::callId).collect(Collectors.toSet());
        var createdCalls = Utils.firstNotNull(this.callsRepository.getAll(callIds), Collections.<String, Call>emptyMap())
                .values().stream().collect(Collectors.toMap(
                        call -> call.getServiceRoomId(),
                        Function.identity()
                ));
        return new CreateCallsResult(
                existingCalls,
                createdCalls
        );
    }

}
