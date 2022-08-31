package org.observertc.observer.repositories;

import io.github.balazskreith.hamok.ModifiedStorageEntry;
import io.github.balazskreith.hamok.memorystorages.MemoryStorageBuilder;
import io.github.balazskreith.hamok.storagegrid.ReplicatedStorage;
import io.reactivex.rxjava3.core.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.mappings.Mapper;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class CallsRepository {

    private static final Logger logger = LoggerFactory.getLogger(CallsRepository.class);

    private static final String STORAGE_ID = "observertc-calls";

    private ReplicatedStorage<ServiceRoomId, Models.Call> storage;
    private CachedFetches<ServiceRoomId, Call> fetched;

    @Inject
    private HamokService service;

    @Inject
    private CallClientIdsRepository callClientIds;

    @Inject
    private ClientsRepository clientsRepositoryRepo;

    @Inject
    private ObserverConfig.InternalBuffersConfig bufferConfig;


    @PostConstruct
    void setup() {
        var baseStorage = new MemoryStorageBuilder<ServiceRoomId, Models.Call>()
                .setConcurrency(true)
                .setId(STORAGE_ID)
                .build();
        this.storage = this.service.getStorageGrid().replicatedStorage(baseStorage)
                .setKeyCodec(ServiceRoomId::toBytes, ServiceRoomId::fromBytes)
                .setValueCodec(
                        Mapper.create(Models.Call::toByteArray, logger)::map,
                        Mapper.<byte[], Models.Call>create(bytes -> Models.Call.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(bufferConfig.debouncers.maxItems)
                .setMaxCollectedStorageTimeInMs(bufferConfig.debouncers.maxTimeInMs)
                .setMaxMessageValues(1000)
                .build();

        this.fetched = CachedFetches.<ServiceRoomId, Call>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
    }

    Observable<List<ModifiedStorageEntry<ServiceRoomId, Models.Call>>> observableDeletedEntries() {
        return this.storage.collectedEvents().deletedEntries();
    }

    Observable<List<ModifiedStorageEntry<ServiceRoomId, Models.Call>>> observableExpiredEntries() {
        return this.storage.collectedEvents().expiredEntries();
    }

    Observable<List<ModifiedStorageEntry<ServiceRoomId, Models.Call>>> observableCreatedEntries() {
        return this.storage.collectedEvents().createdEntries();
    }

    public Call add(ServiceRoomId serviceRoomId, Long timestamp, String marker, String providedCallId) {
        var callId = providedCallId != null ? providedCallId : UUID.randomUUID().toString();
        this.storage.insert(serviceRoomId, Models.Call.newBuilder()
                .setServiceId(serviceRoomId.serviceId)
                .setRoomId(serviceRoomId.roomId)
                .setCallId(callId)
                .setMarker(marker)
                .setStarted(timestamp)
                .build());
        var model = this.storage.get(serviceRoomId);
        return this.wrapCall(model, Collections.emptySet());
    }

    public boolean remove(ServiceRoomId serviceRoomId) {
        return this.storage.delete(serviceRoomId);
    }

    public Call get(ServiceRoomId serviceRoomId) {
        return this.fetched.get(serviceRoomId);
    }

    public Map<ServiceRoomId, Call> getAll(Collection<ServiceRoomId> serviceRoomIds) {
        if (serviceRoomIds == null || serviceRoomIds.size() < 1) {
            return Collections.emptyMap();
        }
        return this.fetched.getAll(Set.copyOf(serviceRoomIds));
    }

    public Map<String, Call> getAllMappedByCallIds(Collection<ServiceRoomId> serviceRoomIds) {
        return this.getAll(serviceRoomIds).values().stream().collect(Collectors.toMap(
                call -> call.getCallId(),
                Function.identity()
        ));
    }

    public void save() {
        this.callClientIds.save();
        this.clientsRepositoryRepo.save();
        this.fetched.clear();
    }

    private Call wrapCall(Models.Call model, Set<String> prefetchedClientIds) {
        var result = new Call(
                model,
                this,
                this.clientsRepositoryRepo,
                this.callClientIds,
                prefetchedClientIds
        );
        this.fetched.add(result.getServiceRoomId(), result);
        return result;
    }

    private Call fetchOne(ServiceRoomId serviceRoomId) {
        var model = this.storage.get(serviceRoomId);
        if (model == null) {
            return null;
        }
        Set<String> fetchedClientIds = Utils.firstNotNull(this.callClientIds.get(model.getCallId()), Collections.emptySet());
        return this.wrapCall(model, fetchedClientIds);
    }

    private Map<ServiceRoomId, Call> fetchAll(Set<ServiceRoomId> serviceRoomIds) {
        var models = this.storage.getAll(serviceRoomIds);

        if (models == null || models.isEmpty()) {
            return Collections.emptyMap();
        }
        var callIds = models.values().stream().map(model -> model.getCallId()).collect(Collectors.toSet());
        var fetchedClientIds = this.callClientIds.getAll(callIds);
        return models.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    var model = entry.getValue();
                    var clientIds = fetchedClientIds.getOrDefault(model.getCallId(), Collections.emptySet());
                    return this.wrapCall(model, clientIds);
                }
        ));
    }
}
