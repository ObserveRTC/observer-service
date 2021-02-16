package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

@Prototype
public class FindCallsByNameTask extends ChainedTask<Map<UUID, CallEntity>> {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FetchCallsTask fetchCallsTask;

    private Map<UUID, Set<String>> serviceCallNames = new HashMap<>();
    private boolean removeUnboundNames = false;


    @PostConstruct
    void setup() {

        new Builder<>(this)
            .<Map<UUID, Set<String>>> addConsumerEntry("Merge all provided inputs",
                    () -> {}, // no input was invoked
                    input -> { // input was invoked, so we may got some names through that
                    Iterator<Map.Entry<UUID, Set<String>>> it = input.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<UUID, Set<String>> entry = it.next();
                        UUID serviceUUID = entry.getKey();
                        Set<String> receivedNames = entry.getValue();
                        Set<String> storedNames = serviceCallNames.get(serviceUUID);
                        if (Objects.isNull(storedNames)) {
                            serviceCallNames.put(serviceUUID, receivedNames);
                        } else {
                            storedNames.addAll(receivedNames);
                        }
                    }
                    serviceCallNames.putAll(input);
            })
            .<Set<UUID>>addSupplierStage("Fetch Call UUIDs",
            // action
            () -> {
                Set<UUID> result = new HashSet<>();
                this.serviceCallNames.forEach((serviceUUID, callNames) -> {
                    callNames.forEach(callName -> {
                        Collection<UUID> callUUIDs = hazelcastMaps.getCallNames(serviceUUID).get(callName);
                        if (0 < callUUIDs.size()) {
                            result.addAll(callUUIDs);
                        }
                    });
                });
                return result;
            })
            .<Set<UUID>>addBreakCondition((callUUIDs, resultHolder) -> {
                if (this.removeUnboundNames) {
                    // if we have to remove unbound names, than this is not the end of the story here
                    return false;
                }
                // we do not want to remove unbound names, so we just fetch the result and go
                Map<UUID, CallEntity> result = this.fetchCallsTask.whereCallUUIDs(callUUIDs).execute().getResult();
                resultHolder.set(result);
                return true;
            })

            .<Set<UUID>, Carrier>addFunctionalStage("Map the call UUIDs to call DTOs and names", callUUIDs -> {
                Carrier resultCarrier = new Carrier();
                Iterator<Map.Entry<UUID, Set<String>>> it = serviceCallNames.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<UUID, Set<String>> entry = it.next();
                    UUID serviceUUID = entry.getKey();
                    Set<String> storedNames = entry.getValue();
                    for (String callName : storedNames) {
                        Collection<UUID> uuids = hazelcastMaps.getCallNames(serviceUUID).get(callName);
                        for (UUID callUUID : uuids) {
                            resultCarrier.callUUIDToNames.put(callUUID, new CallNameEntry(serviceUUID, callName));
                        }

                    }
                }

                resultCarrier.callUUIDTOCallDTOs = hazelcastMaps.getCallDTOs().getAll(callUUIDs);
                if (Objects.isNull(resultCarrier.callUUIDTOCallDTOs)) {
                    getLogger().warn("Result is null for {}", ObjectToString.toString(serviceCallNames));
                    resultCarrier.callUUIDTOCallDTOs = new HashMap<>();
                }
                return resultCarrier;
            })
            .<Carrier, Carrier>addFunctionalStage("Remove unbound names", resultCarrier -> {
                Set<UUID> unboundCallUUIDs = resultCarrier.callUUIDToNames.keySet();
                resultCarrier.callUUIDTOCallDTOs.keySet().forEach(unboundCallUUIDs::remove);
                if (unboundCallUUIDs.size() < 1) {
                    return resultCarrier;
                }

                for (UUID callUUID : unboundCallUUIDs) {
                    CallNameEntry callNameEntry = resultCarrier.callUUIDToNames.get(callUUID);
                    this.hazelcastMaps.getCallNames(callNameEntry.serviceUUID).remove(callNameEntry.name, callUUID);
                    this.getLogger().warn("Call DTO was not found for service: {}, call name: {}, extracted callUUID was {}. The relation is ceased by removing the name callUUID binding",
                            callNameEntry.serviceUUID, callNameEntry.name, callUUID);
                }
                return resultCarrier;
            })
            .<Carrier>addTerminalFunction("Fetch the result", result -> {
                Set<UUID> callUUIDs = result.callUUIDTOCallDTOs.keySet();
                return fetchCallsTask.whereCallUUIDs(callUUIDs).execute().getResult();
            })
        .build();
    }

    public FindCallsByNameTask whereCallName(UUID serviceUUID, String callName) {
        Set<String> names = this.serviceCallNames.get(serviceUUID);
        if (Objects.isNull(names)) {
            names = new HashSet<>();
            this.serviceCallNames.put(serviceUUID, names);
        }
        names.add(callName);
        return this;
    }

    public FindCallsByNameTask removeUnboundCallNameIsNotBound() {
        this.removeUnboundNames = true;
        return this;
    }

    @Override
    protected void validate() {

    }

    private class Carrier {
        Map<UUID, CallDTO> callUUIDTOCallDTOs = new HashMap<>();
        Map<UUID, CallNameEntry> callUUIDToNames = new HashMap<>();
    }

    private class CallNameEntry {
        final UUID serviceUUID;
        final String name;

        private CallNameEntry(UUID serviceUUID, String name) {
            this.serviceUUID = serviceUUID;
            this.name = name;
        }
    }
}
