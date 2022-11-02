package org.observertc.observer.evaluators;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.Utils;
import org.observertc.observer.evaluators.eventreports.CallStartedReports;
import org.observertc.observer.repositories.Call;
import org.observertc.observer.repositories.CallsRepository;
import org.observertc.observer.repositories.Room;
import org.observertc.observer.repositories.RoomsRepository;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.samples.ServiceRoomId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
class ObservedCallsFetcherInMasterMode {

    private static final Logger logger = LoggerFactory.getLogger(ObservedCallsFetcherInMasterMode.class);

    @Inject
    RoomsRepository roomsRepository;

    @Inject
    CallsRepository callsRepository;

    @Inject
    CallStartedReports callStartedReports;

    Map<ServiceRoomId, Call> fetchFor(ObservedClientSamples observedClientSamples) {
        if (observedClientSamples == null || observedClientSamples.isEmpty()) {
            return Collections.emptyMap();
        }
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

    record CreateRoomsResult(
            Map<ServiceRoomId, Room> existingRooms,
            Map<ServiceRoomId, Room> createdRooms
    ) {

    }

    private CreateRoomsResult createRooms(ObservedClientSamples observedClientSamples) {
        var existingRooms = new HashMap<ServiceRoomId, Room>(this.roomsRepository.getAll(observedClientSamples.getServiceRoomIds()));
        var roomsToCreate = observedClientSamples.getServiceRoomIds().stream()
                .filter(serviceRoomId -> !existingRooms.containsKey(serviceRoomId))
                .collect(Collectors.toMap(
                        Function.identity(),
                        e -> UUID.randomUUID().toString()
                ));
        if (roomsToCreate.size() < 1) {
            return new CreateRoomsResult(
                    existingRooms,
                    Collections.emptyMap()
            );
        }
        var alreadyCreatedRooms = Utils.firstNotNull(this.roomsRepository.insertAll(roomsToCreate), Collections.<ServiceRoomId, Room>emptyMap());
        var createdRooms = new HashMap<ServiceRoomId, Room>();
        for (var room : this.roomsRepository.getAll(roomsToCreate.keySet()).values()) {
            if (alreadyCreatedRooms.containsKey(room.getServiceRoomId())) {
                existingRooms.put(room.getServiceRoomId(), room);
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

