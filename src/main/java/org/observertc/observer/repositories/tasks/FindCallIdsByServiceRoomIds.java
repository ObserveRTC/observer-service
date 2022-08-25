package org.observertc.observer.repositories.tasks;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.metrics.RepositoryMetrics;
import org.observertc.observer.repositories.HamokStorages;
import org.observertc.observer.samples.ServiceRoomId;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Prototype
public class FindCallIdsByServiceRoomIds extends ChainedTask<Map<ServiceRoomId, UUID>> {

    @Inject
    HamokStorages hazelcastMaps;

    @Inject
    RepositoryMetrics exposedMetrics;

    private Set<ServiceRoomId> serviceRoomIds = new HashSet<>();
    private boolean unmodifiableResult = false;


    @PostConstruct
    void setup() {
        this.withStatsConsumer(this.exposedMetrics::processTaskStats);
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
        if (serviceId == null || roomId == null) {
            return this;
        }
        var serviceRoomId = ServiceRoomId.make(serviceId, roomId);
        return this.whereServiceRoomId(serviceRoomId);
    }

    public FindCallIdsByServiceRoomIds whereServiceRoomId(ServiceRoomId serviceRoomId) {
        Objects.requireNonNull(serviceRoomId);
        if (Objects.isNull(serviceRoomId) || serviceRoomId.serviceId == null || serviceRoomId.roomId == null) {
            return this;
        }
        this.serviceRoomIds.add(serviceRoomId);
        return this;
    }

    public FindCallIdsByServiceRoomIds whereServiceRoomIds(Set<ServiceRoomId> serviceRoomIds) {
        if (Objects.isNull(serviceRoomIds)) {
            return this;
        }
        serviceRoomIds.stream().filter(serviceRoomId -> {
            return serviceRoomId != null && serviceRoomId.roomId != null && serviceRoomId.serviceId != null;
        }).forEach(this.serviceRoomIds::add);
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
