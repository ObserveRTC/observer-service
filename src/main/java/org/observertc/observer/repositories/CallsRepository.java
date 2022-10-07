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
    private static final int MAX_KEYS = 1000;
    private static final int MAX_VALUES = 100;

    private static final String STORAGE_ID = "observertc-calls";

    private ReplicatedStorage<ServiceRoomId, Models.Call> storage;
    private Map<ServiceRoomId, Models.Call> updated;
    private CachedFetches<ServiceRoomId, Call> fetched;

    @Inject
    private HamokService service;

    @Inject
    private ClientsRepository clientsRepositoryRepo;

    @Inject
    private ObserverConfig.InternalBuffersConfig bufferConfig;

    @PostConstruct
    void setup() {
        var baseStorage = new MemoryStorageBuilder<ServiceRoomId, Models.Call>()
                .setConcurrency(true)
                .setId(STORAGE_ID)
                .setMergeOp((oldValue, newValue) -> {
                    var clientLogs_1 = oldValue.getClientLogsList();
                    var clientLogs_2 = newValue.getClientLogsList();
                    var clientLogs = new HashMap<String, Models.Call.ClientLog>();
                    for (var clientLog : clientLogs_1) {
                        clientLogs.put(clientLog.getClientId(), clientLog);
                    }
                    for (var clientLog_2 : clientLogs_2) {
                        var clientLog_1 = clientLogs.get(clientLog_2.getClientId());
                        if (clientLog_1 == null) {
                            clientLogs.put(clientLog_2.getClientId(), clientLog_2);
                            continue;
                        }
                        if (clientLog_2.getTimestamp() <= clientLog_1.getTimestamp()) {
                            continue;
                        }
                        clientLogs.put(clientLog_2.getClientId(), clientLog_2);
                    }
                    var builder = Models.Call.newBuilder(newValue)
                            .clearClientLogs()
                            .addAllClientLogs(clientLogs.values())
                            ;
                    return builder.build();
                })
                .build();
        this.storage = this.service.getStorageGrid().replicatedStorage(baseStorage)
                .setKeyCodec(ServiceRoomId::toBytes, ServiceRoomId::fromBytes)
                .setValueCodec(
                        Mapper.create(Models.Call::toByteArray, logger)::map,
                        Mapper.<byte[], Models.Call>create(bytes -> Models.Call.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(bufferConfig.debouncers.maxItems)
                .setMaxCollectedStorageTimeInMs(bufferConfig.debouncers.maxTimeInMs)
                .setMaxMessageKeys(MAX_KEYS)
                .setMaxMessageValues(MAX_VALUES)
                .build();


        this.fetched = CachedFetches.<ServiceRoomId, Call>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
        this.updated = new HashMap<>();
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
        var proposedModels = createCallInfo.stream().collect(Collectors.toMap(
                info -> info.serviceRoomId,
                info -> {
                    var proposedCallId = Utils.firstNotNull(info.providedCallId, UUID.randomUUID().toString());
                    var serviceRoomId = info.serviceRoomId();
                    var builder = Models.Call.newBuilder()
                            .setServiceId(serviceRoomId.serviceId)
                            .setRoomId(serviceRoomId.roomId)
                            .setCallId(proposedCallId)
                            .setStarted(info.timestamp)
                            ;
                    if (info.marker != null) {
                        builder.setMarker(info.marker);
                    }
                    return builder.build();
                }
        ));
        var notInsertedModels = this.storage.insertAll(proposedModels);
        if (notInsertedModels == null) {
            return Collections.emptyMap();
        }
        return notInsertedModels.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> this.wrapCall(entry.getValue())
        ));
    }

    public Set<ServiceRoomId> removeAll(Set<ServiceRoomId> serviceRoomIds) {
        var calls = this.getAll(serviceRoomIds);
        var clientIds = calls.values().stream().map(call -> call.getClientIds())
                .flatMap(s -> s.stream())
                .collect(Collectors.toSet());
        var result = this.storage.deleteAll(serviceRoomIds);
        synchronized (this) {
            serviceRoomIds.forEach(this.updated::remove);
        }
        this.clientsRepositoryRepo.deleteAll(clientIds);
        this.clientsRepositoryRepo.save();
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
        synchronized (this) {
            if (0 < this.updated.size()) {
                this.storage.setAll(this.updated);
            }
            this.updated.clear();
        }
        this.clientsRepositoryRepo.save();
        this.fetched.clear();
    }

    private Call wrapCall(Models.Call model) {
        var result = new Call(
                model,
                this,
                this.clientsRepositoryRepo
        );
        this.fetched.add(result.getServiceRoomId(), result);
        return result;
    }

    synchronized void update(Models.Call call) {
        var serviceRoomId = ServiceRoomId.make(call.getServiceId(), call.getRoomId());
        this.updated.put(serviceRoomId, call);
    }

    private Call fetchOne(ServiceRoomId serviceRoomId) {
        var model = this.storage.get(serviceRoomId);
        if (model == null) {
            return null;
        }
        return this.wrapCall(model);
    }

    private Map<ServiceRoomId, Call> fetchAll(Set<ServiceRoomId> serviceRoomIds) {
        var models = this.storage.getAll(serviceRoomIds);

        if (models == null || models.isEmpty()) {
            return Collections.emptyMap();
        }
        return models.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    var model = entry.getValue();
                    return this.wrapCall(model);
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

    public Map<ServiceRoomId, Models.Call> removeExpiredCalls() {
        var threshold = Instant.now().toEpochMilli() - 5 * 60 * 1000;
        var toRemove = new HashMap<ServiceRoomId, Models.Call>();
        for (var it = this.storage.iterator(); it.hasNext(); ) {
            var entry = it.next();
            var call = entry.getValue();
            if (call == null) {
                continue;
            }
            if (call.getClientLogsCount() < 1) {
                toRemove.put(entry.getKey(), call);
                continue;
            }
            var logList = call.getClientLogsList();
            var hasActiveClient = logList.stream().anyMatch(log -> Call.CLIENT_JOINED_EVENT_NAME.equals(log.getEvent()));
            if (hasActiveClient) {
                continue;
            }
            var allExpired = logList.stream().allMatch(log -> log.getTimestamp() < threshold);
            if (allExpired) {
                toRemove.put(entry.getKey(), call);
            }
        }
        if (toRemove.size() < 1) {
            return Collections.emptyMap();
        }
        synchronized (this) {
            toRemove.keySet().stream().forEach(this.updated::remove);
            var removed = this.storage.deleteAll(toRemove.keySet());
            if (removed == null || removed.size() < 1) {
                return Collections.emptyMap();
            }
            return toRemove.entrySet().stream().filter(entry -> removed.contains(entry.getKey())).collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue
            ));
        }
    }

    public record CreateCallInfo(ServiceRoomId serviceRoomId, String marker, String providedCallId, Long timestamp) {

    }
}
