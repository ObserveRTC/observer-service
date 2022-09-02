package org.observertc.observer.repositories;

import io.github.balazskreith.hamok.ModifiedStorageEntry;
import io.github.balazskreith.hamok.memorystorages.MemoryStorageBuilder;
import io.github.balazskreith.hamok.storagegrid.ReplicatedStorage;
import io.reactivex.rxjava3.core.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.HamokService;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.mappings.Mapper;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class CallsRepository implements RepositoryStorageMetrics {

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

    Observable<List<ModifiedStorageEntry<ServiceRoomId, Models.Call>>> observableCreatedEntries() {
        return this.storage.collectedEvents().createdEntries();
    }

    public Map<ServiceRoomId, Call> fetchRecursively(Set<ServiceRoomId> serviceRoomIds) {
        var result = this.getAll(serviceRoomIds);
        var clientIds = result.values().stream()
                .map(Call::getClientIds)
                .flatMap(s -> s.stream())
                .collect(Collectors.toSet());
        this.clientsRepositoryRepo.fetchRecursively(clientIds);
        return result;
    }

    /**
     * Tries to insert calls, and returns with the NOT inserted one if it is already inserted
     * @param createCallInfo
     * @return
     */
    public Map<ServiceRoomId, Call> insertAll(Collection<CreateCallInfo> createCallInfo) {
        if (createCallInfo == null || createCallInfo.size() < 1) {
            return Collections.emptyMap();
        }
        var timestamp = Instant.now().toEpochMilli();
        var proposedModels = createCallInfo.stream().collect(Collectors.toMap(
                info -> info.serviceRoomId,
                info -> {
                    var proposedCallId = Utils.firstNotNull(info.providedCallId, UUID.randomUUID().toString());
                    var serviceRoomId = info.serviceRoomId();
                    return Models.Call.newBuilder()
                            .setServiceId(serviceRoomId.serviceId)
                            .setRoomId(serviceRoomId.roomId)
                            .setCallId(proposedCallId)
                            .setMarker(info.marker())
                            .setStarted(timestamp)
                            .build();
                }
        ));
        var notInsertedModels = this.storage.insertAll(proposedModels);
        if (notInsertedModels == null) {
            return Collections.emptyMap();
        }
        return notInsertedModels.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> this.wrapCall(entry.getValue(), Collections.emptySet())
        ));
    }

    public Set<ServiceRoomId> removeAll(Set<ServiceRoomId> serviceRoomIds) {
        var calls = this.getAll(serviceRoomIds);
        var clientIds = calls.values().stream().map(call -> call.getClientIds())
                .flatMap(s -> s.stream())
                .collect(Collectors.toSet());
        var result = this.storage.deleteAll(serviceRoomIds);
        this.callClientIds.deleteAll(clientIds);
        this.callClientIds.save();
        this.fetched.clear();
        return result;
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

    @Override
    public String storageId() {
        return this.storage.getId();
    }

    @Override
    public int localSize() {
        return this.storage.localSize();
    }

    public record CreateCallInfo(ServiceRoomId serviceRoomId, String marker, String providedCallId) {

    }
}
