package org.observertc.observer.repositories;

import io.github.balazskreith.hamok.ModifiedStorageEntry;
import io.github.balazskreith.hamok.memorystorages.MemoryStorageBuilder;
import io.github.balazskreith.hamok.storagegrid.SeparatedStorage;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.BackgroundTasksExecutor;
import org.observertc.observer.HamokService;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.common.Try;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.mappings.Mapper;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class CallsRepository implements RepositoryStorageMetrics {

    private static final Logger logger = LoggerFactory.getLogger(CallsRepository.class);
    private static final int MAX_KEYS = 1000;
    private static final int MAX_VALUES = 100;

    private static final String STORAGE_ID = "observertc-calls";

    private SeparatedStorage<String, Models.Call> storage;
    private Map<String, Models.Call> updated;
    private CachedFetches<String, Call> fetched;

    @Inject
    private ObserverConfig observerConfig;

    @Inject
    private Backups backups;

    @Inject
    private HamokService service;

    @Inject
    private ClientsRepository clientsRepositoryRepo;

    @Inject
    private BackgroundTasksExecutor backgroundTasksExecutor;

    @PostConstruct
    void setup() {
        BiFunction<Models.Call, Models.Call, Long> getMinSampleTouched = (call_1, call_2) -> {
            return Utils.getMin(
                    () -> call_1.hasSampleTouched() ? call_1.getSampleTouched() : null,
                    () -> call_2.hasSampleTouched() ? call_2.getSampleTouched() : null
            );
        };
        BiFunction<Models.Call, Models.Call, Long> getMaxServerTouched = (call_1, call_2) -> {
            return Utils.getMax(
                    () -> call_1.hasServerTouched() ? call_1.getServerTouched() : null,
                    () -> call_2.hasServerTouched() ? call_2.getServerTouched() : null
            );
        };

        var baseStorage = new MemoryStorageBuilder<String, Models.Call>()
                .setConcurrency(true)
                .setId(STORAGE_ID)
                .setMergeOp((call_1, call_2) -> {
                    var clientIds = Stream.concat(
                            call_1.getClientIdsCount() < 1 ? Stream.empty() : call_1.getClientIdsList().stream(),
                            call_2.getClientIdsCount() < 1 ? Stream.empty() : call_2.getClientIdsList().stream()
                    ).collect(Collectors.toSet());
                    var sampleTouch = getMinSampleTouched.apply(call_1, call_2);
                    var serverTouch =  getMaxServerTouched.apply(call_1, call_2);
                    var builder = Models.Call.newBuilder(call_1)
                            .clearClientIds()
                            .addAllClientIds(clientIds);
                    if (sampleTouch != null) {
                        builder.setSampleTouched(sampleTouch);
                    }
                    if (serverTouch != null) {
                        builder.setServerTouched(serverTouch);
                    }
                    return builder.build();
                })
//                .setExpiration(5 * 60 * 60 * 1000)
                .build();
        var storageBuilder = this.service.getStorageGrid().separatedStorage(baseStorage)
                .setKeyCodec(String::getBytes, String::new)
                .setValueCodec(
                        Mapper.create(Models.Call::toByteArray, logger)::map,
                        Mapper.<byte[], Models.Call>create(bytes -> Models.Call.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(this.observerConfig.buffers.debouncers.maxItems)
                .setMaxCollectedStorageTimeInMs(this.observerConfig.buffers.debouncers.maxTimeInMs)
                .setMaxMessageKeys(MAX_KEYS)
                .setMaxMessageValues(MAX_VALUES);

        if (this.observerConfig.repository.useBackups) {
            storageBuilder.setDistributedBackups(this.backups);
        }

        this.storage = storageBuilder.build();

        var checkCollision = new AtomicBoolean(false);
        this.storage.detectedEntryCollisions().subscribe(detectedCollision -> {
            logger.warn("Detected colliding items in {} for key {}", STORAGE_ID, detectedCollision.key());
            if (checkCollision.compareAndSet(false, true)) {
                this.backgroundTasksExecutor.addTask(ChainedTask.<Void>builder()
                        .withName("Remove collisions for storage: " + STORAGE_ID)
                        .withLogger(logger)
                        .addActionStage("Check collision for " + STORAGE_ID, this.storage::checkCollidingEntries)
                        .setFinalAction(() -> checkCollision.set(false))
                        .build()
                );
            }
        });

        this.fetched = CachedFetches.<String, Call>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
        this.updated = new HashMap<>();

        Schedulers.newThread().schedulePeriodicallyDirect(() -> {
            logger.warn("THERE IS A SCHEDULER IN THE CODE FOR DEBUGGING PURPOSES");
            Try.wrap(() -> this.storage.getAll(Set.of("key")), null);
        }, 5000, 5000, TimeUnit.MILLISECONDS);
    }


    Observable<List<ModifiedStorageEntry<String, Models.Call>>> observableDeletedEntries() {
        return this.storage.collectedEvents().deletedEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.Call>>> observableCreatedEntries() {
        return this.storage.collectedEvents().createdEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.Call>>> observableExpiredEntries() {
        return this.storage.collectedEvents().expiredEntries();
    }

    public Map<String, Call> fetchRecursively(Set<String> callIds) {
        if (callIds == null || callIds.size() < 1) {
            return Collections.emptyMap();
        }
        var result = this.getAll(callIds);
        var clientIds = result.values().stream()
                .map(Call::getClientIds)
                .flatMap(s -> s.stream())
                .collect(Collectors.toSet());
        this.clientsRepositoryRepo.fetchRecursively(clientIds);
        return result;
    }

    public Map<String, Call> fetchRecursivelyUpwards(Collection<String> callIds) {
        if (callIds == null || callIds.size() < 1) {
            return Collections.emptyMap();
        }
        var result = this.getAll(callIds);
//        var serviceRoomIds = result.values().stream()
//                .map(Call::getServiceRoomId)
//                .collect(Collectors.toSet());
        return result;
    }

    /**
     * Tries to insert calls, and returns with the NOT inserted one if it is already inserted
     * @param createCallInfo
     * @return
     */
    public Map<String, Call> insertAll(Collection<CreateCallInfo> createCallInfo) {
        if (createCallInfo == null || createCallInfo.size() < 1) {
            return Collections.emptyMap();
        }
        var proposedModels = createCallInfo.stream().collect(Collectors.toMap(
                info -> info.callId,
                info -> {
                    var proposedCallId = Utils.firstNotNull(info.callId, UUID.randomUUID().toString());
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
        var notInsertedModels = Try.wrap(() -> this.storage.insertAll(proposedModels), null);
        if (notInsertedModels == null) {
            return Collections.emptyMap();
        }
        return notInsertedModels.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> this.wrapCall(entry.getValue())
        ));
    }

    public Set<String> removeAll(Set<String> callIds) {
        var calls = this.getAll(callIds);
        var clientIds = calls.values().stream().map(call -> call.getClientIds())
                .flatMap(s -> s.stream())
                .collect(Collectors.toSet());
        var result = Try.<Set<String>>wrap(() -> this.storage.deleteAll(callIds), Collections.emptySet());
        synchronized (this) {
            callIds.forEach(this.updated::remove);
        }
        this.clientsRepositoryRepo.deleteAll(clientIds);
        this.clientsRepositoryRepo.save();
        this.fetched.clear();
        return result;
    }

    public void dump() {
        var localKeys = this.storage.localKeys();
        logger.info("Dumped storage local part. {}", JsonUtils.objectToString(
                this.storage.getAll(localKeys).values().stream().map(call -> String.format("%s::%s", call.getRoomId(), call.getCallId())).collect(Collectors.toList())
        ));
        var remoteKeys = this.storage.keys().stream().filter(key -> !localKeys.contains(key)).collect(Collectors.toSet());
        logger.info("Dumped storage remote part. {}", JsonUtils.objectToString(
                this.storage.getAll(remoteKeys).values().stream().map(call -> String.format("%s::%s", call.getRoomId(), call.getCallId())).collect(Collectors.toList())
        ));
    }

    public Map<String, Call> getAllLocallyStored() {
        var callIds = this.storage.localKeys();
        if (callIds == null || callIds.size() < 1) {
            return Collections.emptyMap();
        }
        return this.fetchAll(callIds);

    }

    public Call get(String callId) {
        return this.fetched.get(callId);
    }

    public Map<String, Call> getAll(Collection<String> callIds) {
        if (callIds == null || callIds.size() < 1) {
            return Collections.emptyMap();
        }
        var callIdSet = Set.copyOf(callIds);
//        logger.info("To fetch callIds: {}", callIdSet);
        return this.fetched.getAll(callIdSet);
    }

    public void save() {
        synchronized (this) {
            if (0 < this.updated.size()) {
//                Try2.createForAction(() -> this.storage.setAll(this.updated)).execute();
                Try.wrap(() -> this.storage.setAll(this.updated));
            }
            this.updated.clear();
        }
        this.clientsRepositoryRepo.save();
        this.fetched.clear();
    }

    Call wrapCall(Models.Call model) {
        var result = new Call(
                model,
                this,
                this.clientsRepositoryRepo
        );
        this.fetched.add(result.getCallId(), result);
        return result;
    }

    synchronized void update(Models.Call call) {
        this.updated.put(call.getCallId(), call);
    }

    private Call fetchOne(String callId) {
        var model = Try.wrap(() -> this.storage.get(callId), null);
        if (model == null) {
            return null;
        }
        return this.wrapCall(model);
    }

    private Map<String, Call> fetchAll(Set<String> callIds) {
        var models = Try.wrap(() -> this.storage.getAll(callIds), null);
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

    public record CreateCallInfo(ServiceRoomId serviceRoomId, String marker, String callId, Long timestamp) {

    }
}
