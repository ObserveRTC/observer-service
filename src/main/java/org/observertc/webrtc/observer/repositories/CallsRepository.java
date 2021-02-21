package org.observertc.webrtc.observer.repositories;

import io.micrometer.core.instrument.MeterRegistry;
import org.observertc.webrtc.observer.entities.CallEntity;
import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
import org.observertc.webrtc.observer.repositories.tasks.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class CallsRepository {
    private static final Logger logger = LoggerFactory.getLogger(CallsRepository.class);


    @Inject
    Provider<AddCallsTask> addCallsTaskProvider;

    @Inject
    Provider<AddPCsTask> addPCsTaskProvider;

    @Inject
    Provider<RemovePCsTask> removePCsTaskProvider;

    @Inject
    Provider<RemoveCallsTask> removeCallsTaskProvider;

    @Inject
    Provider<FetchCallsTask> fetchCallsTaskProvider;

    @Inject
    Provider<FetchPCsTask> fetchPCsTaskProvider;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    Provider<FindCallTask> findCallTaskProvider;

    @Inject
    MeterRegistry meterRegistry;

    public <U extends CallEntity> CallEntity addCall(@NotNull U entity) {
        var task = this.addCallsTaskProvider.get()
                .withCallEntity(entity)
                .withMeterRegistry(meterRegistry)
                ;

        if (!task.execute().succeeded()) {
            return null;
        }

        var first = task.getResult().values().stream().findFirst();
        if (!first.isPresent()) {
            return null;
        }
        return first.get();
    }

    public <U extends PeerConnectionEntity> PeerConnectionEntity addPeerConnection(@NotNull U entity) {
        var task = this.addPCsTaskProvider.get()
                .withPeerConnection(entity)
                .withMeterRegistry(meterRegistry)
                ;

        if (!task.execute().succeeded()) {
            return null;
        }

        var first = task.getResult().values().stream().findFirst();
        if (!first.isPresent()) {
            return null;
        }
        return first.get();
    }

    public <U extends UUID> Optional<CallEntity> findCall(@NotNull U callUUID) {
        var task = this.fetchCallsTaskProvider.get()
                .whereCallUUID(callUUID)
                ;

        if (!task.execute().succeeded()) {
            return Optional.empty();
        }

        return task.getResult().values().stream().findFirst();
    }

    public <U extends UUID> Optional<PeerConnectionEntity> findPeerConnection(@NotNull U pcUUID) {
        var task = this.fetchPCsTaskProvider.get()
                .wherePCUuid(pcUUID)
                ;

        if (!task.execute().succeeded()) {
            return Optional.empty();
        }

        return task.getResult().values().stream().findFirst();
    }

    public <U extends UUID> Map<UUID, CallEntity> findCalls(@NotNull U... callUUIDs) {
        var task = this.fetchCallsTaskProvider.get()
                .whereCallUUID(callUUIDs)
                ;

        if (!task.execute().succeeded()) {
            return Collections.EMPTY_MAP;
        }

        return task.getResult();
    }

    public <U extends PeerConnectionEntity> Optional<CallEntity> findCall(@NotNull UUID serviceUUID, String callName, Set<Long> SSRCs) {
        var task = this.findCallTaskProvider.get();
        boolean toExecute = false;
        if (Objects.nonNull(callName)) {
            task.whereCallName(serviceUUID, callName);
            toExecute = true;
        }
        if (Objects.nonNull(SSRCs) && 0 < SSRCs.size()) {
            task.whereSSRC(serviceUUID, SSRCs);
            toExecute = true;
        }

        if (!toExecute || !task.execute().succeeded()) {
            return Optional.empty();
        }

        return task.getResult().values().stream().findFirst();
    }

    public <U extends UUID> Map<UUID, PeerConnectionEntity> findPeerConnections(@NotNull U... pcUUIDs) {
        var task = this.fetchPCsTaskProvider.get()
                .wherePCUuid(pcUUIDs)
                ;

        if (!task.execute().succeeded()) {
            return Collections.EMPTY_MAP;
        }

        return task.getResult();
    }

    public <U extends UUID> Map<UUID, PeerConnectionEntity> findPeerConnections(@NotNull Set<U> pcUUIDs) {
        var task = this.fetchPCsTaskProvider.get()
                .wherePCUuid(pcUUIDs.stream().collect(Collectors.toSet()))
                ;

        if (!task.execute().succeeded()) {
            return Collections.EMPTY_MAP;
        }

        return task.getResult();
    }

    public <R extends UUID> CallEntity removeCall(@NotNull R callUUID) {
        var task = this.removeCallsTaskProvider.get()
                .whereCallUUID(callUUID)
                .withMeterRegistry(meterRegistry)
                ;

        if (!task.execute().succeeded()) {
            return null;
        }

        var first = task.getResult().values().stream().findFirst();
        if (!first.isPresent()) {
            return null;
        }
        return first.get();
    }

    public <R extends CallEntity> CallEntity removeCall(@NotNull R callEntity) {
        var task = this.removeCallsTaskProvider.get()
                .whereCallEntities(callEntity)
                .withMeterRegistry(meterRegistry)
                ;

        if (!task.execute().succeeded()) {
            return null;
        }

        var first = task.getResult().values().stream().findFirst();
        if (!first.isPresent()) {
            return null;
        }
        return first.get();
    }

    public <U extends UUID> PeerConnectionEntity removePeerConnection(@NotNull U pcUUID) {
        var task = this.removePCsTaskProvider.get()
                .wherePCUUIDs(pcUUID)
                .withMeterRegistry(meterRegistry)
        ;

        if (!task.execute().succeeded()) {
            return null;
        }

        var first = task.getResult().values().stream().findFirst();
        if (!first.isPresent()) {
            return null;
        }
        return first.get();
    }

    public Set<UUID> filterExistingPeerConnectionUUIDs(Set<UUID> pcUUIDs) {
        Set<UUID> result = this.hazelcastMaps.getPcDTOs().getAll(pcUUIDs).keySet();
        if (Objects.isNull(result)) {
            return Collections.EMPTY_SET;
        }
        return result;
    }

    /**
     * Gets all call entities, for which calls DTO is stored in this instance!
     * @return
     */
    public Map<UUID, CallEntity> fetchLocallyStoredCalls() {
        Set<UUID> keys = this.hazelcastMaps.getCallDTOs().localKeySet();
        var task = this.fetchCallsTaskProvider.get()
                .whereCallUUIDs(keys)
                .withMeterRegistry(meterRegistry)
                ;

        if (!task.execute().succeeded()) {
            return Collections.EMPTY_MAP;
        }
        Map<UUID, CallEntity> callEntities = task.getResult();
        if (Objects.isNull(callEntities)) {
            return Collections.EMPTY_MAP;
        }
        return callEntities;
    }
}
