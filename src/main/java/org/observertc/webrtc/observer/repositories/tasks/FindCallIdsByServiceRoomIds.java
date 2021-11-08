package org.observertc.webrtc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ChainedTask;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.samples.ServiceRoomId;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class FindCallIdsByServiceRoomIds extends ChainedTask<Map<ServiceRoomId, UUID>> {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    FetchCallsTask fetchCallsTask;

    private Set<ServiceRoomId> serviceRoomIds = new HashSet<>();
    private boolean unmodifiableResult = false;


    @PostConstruct
    void setup() {

        new Builder<>(this)
            .<Set<ServiceRoomId>> addConsumerEntry("Merge all provided inputs",
                    () -> {}, // no input was invoked
                    input -> { // input was invoked, so we may got some names through that
                    if (Objects.isNull(input)) {
                        return;
                    }
                    this.serviceRoomIds.addAll(input);
            })
            .addTerminalSupplier("Fetch Call Ids", () -> {
                Map<ServiceRoomId, UUID> result = new HashMap<>();
                Set<String> serviceRoomKeys = this.serviceRoomIds.stream().map(ServiceRoomId::createKey).collect(Collectors.toSet());
                Map<String, UUID> foundCallIds = this.hazelcastMaps.getServiceRoomToCallIds().getAll(serviceRoomKeys);
                foundCallIds.forEach((serviceRoomKey, foundCallId) -> {
                    var serviceRoomId = ServiceRoomId.fromKey(serviceRoomKey);
                    result.put(serviceRoomId, foundCallId);
                });
                if (this.unmodifiableResult) {
                    return Collections.unmodifiableMap(result);
                } else {
                    return result;
                }
            })
        .build();
    }

    public FindCallIdsByServiceRoomIds whereServiceRoomId(String serviceId, String roomId) {
        Objects.requireNonNull(serviceId);
        Objects.requireNonNull(roomId);
        var serviceRoomId = ServiceRoomId.make(serviceId, roomId);
        return this.whereServiceRoomId(serviceRoomId);
    }

    public FindCallIdsByServiceRoomIds whereServiceRoomId(ServiceRoomId serviceRoomId) {
        Objects.requireNonNull(serviceRoomId);
        if (Objects.isNull(serviceRoomId)) {
            return this;
        }
        this.serviceRoomIds.add(serviceRoomId);
        return this;
    }

    public FindCallIdsByServiceRoomIds whereServiceRoomIds(Set<ServiceRoomId> serviceRoomIds) {
        if (Objects.isNull(serviceRoomIds)) {
            return this;
        }
        this.serviceRoomIds.addAll(serviceRoomIds);
        return this;
    }

    public FindCallIdsByServiceRoomIds withUnmodifiableResult(boolean value) {
        this.unmodifiableResult = value;
        return this;
    }

    @Override
    protected void validate() {

    }
}
