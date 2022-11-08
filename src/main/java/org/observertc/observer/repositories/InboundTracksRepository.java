package org.observertc.observer.repositories;

import io.github.balazskreith.hamok.ModifiedStorageEntry;
import io.github.balazskreith.hamok.memorystorages.MemoryStorageBuilder;
import io.github.balazskreith.hamok.storagegrid.SeparatedStorage;
import io.micronaut.context.BeanProvider;
import io.reactivex.rxjava3.core.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.BackgroundTasksExecutor;
import org.observertc.observer.HamokService;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Try;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.mappings.Mapper;
import org.observertc.observer.mappings.SerDeUtils;
import org.observertc.schemas.dtos.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Singleton
public class InboundTracksRepository  implements RepositoryStorageMetrics {

    private static final Logger logger = LoggerFactory.getLogger(InboundTracksRepository.class);

    private static final String STORAGE_ID = "observertc-inbound-video-tracks";
    private static final int MAX_KEYS = 1000;
    private static final int MAX_VALUES = 100;

    private Set<String> deleted;
    private Map<String, Models.InboundTrack> updated;

    private SeparatedStorage<String, Models.InboundTrack> storage;
    private CachedFetches<String, InboundTrack> fetched;

    @Inject
    BeanProvider<PeerConnectionsRepository> peerConnectionsRepositoryBeanProvider;

    @Inject
    private ObserverConfig.RepositoryConfig config;

    @Inject
    private HamokService hamokService;

    @Inject
    private ObserverConfig.InternalBuffersConfig bufferConfig;

    @Inject
    private BackgroundTasksExecutor backgroundTasksExecutor;

    @PostConstruct
    void setup() {
        var baseStorage = new MemoryStorageBuilder<String, Models.InboundTrack>()
                .setId(STORAGE_ID)
                .setConcurrency(true)
//                .setExpiration(config.mediaTracksMaxIdleTimeInS * 1000)
                .build();
        this.storage = this.hamokService.getStorageGrid().separatedStorage(baseStorage)
                .setKeyCodec(SerDeUtils.createStrToByteFunc(), SerDeUtils.createBytesToStr())
                .setValueCodec(
                        Mapper.create(Models.InboundTrack::toByteArray, logger)::map,
                        Mapper.<byte[], Models.InboundTrack>create(bytes -> Models.InboundTrack.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(bufferConfig.debouncers.maxItems)
                .setMaxCollectedStorageTimeInMs(bufferConfig.debouncers.maxTimeInMs)
                .setMaxMessageKeys(MAX_KEYS)
                .setMaxMessageValues(MAX_VALUES)
                .build();

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

        this.fetched = CachedFetches.<String, InboundTrack>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
        this.updated = new HashMap<>();
        this.deleted = new HashSet<>();
    }

    Observable<List<ModifiedStorageEntry<String, Models.InboundTrack>>> observableDeletedEntries() {
        return this.storage.collectedEvents().deletedEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.InboundTrack>>> observableExpiredEntries() {
        return this.storage.collectedEvents().expiredEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.InboundTrack>>> observableCreatedEntries() {
        return this.storage.collectedEvents().createdEntries();
    }

    public InboundTrack get(String trackId) {
        return this.fetched.get(trackId);
    }

    public Map<String, InboundTrack> getAll(Collection<String> trackIds) {
        if (trackIds == null || trackIds.size() < 1) {
            return Collections.emptyMap();
        }
        var set = Set.copyOf(trackIds);
        return this.fetched.getAll(set);
    }

    public Map<String, InboundTrack> fetchRecursively(Set<String> inboundTrackIds) {
        return this.getAll(inboundTrackIds);
    }

    synchronized void update(Models.InboundTrack inboundTrack) {
        this.updated.put(inboundTrack.getTrackId(), inboundTrack);
        var removed = this.deleted.remove(inboundTrack.getTrackId());
        if (removed) {
            logger.debug("In this transaction InboundVideoTrack is deleted before it was updated");
        }
    }

    synchronized void delete(String trackId) {
        this.deleted.add(trackId);
        var removed = this.updated.remove(trackId);
        if (removed != null) {
            logger.debug("In this transaction InboundVideoTrack is updated before it was deleted");
        }
    }

    synchronized void deleteAll(Set<String> trackIds) {
        if (trackIds == null || trackIds.size() < 1) {
            return;
        }
        this.deleted.addAll(trackIds);
        trackIds.forEach(trackId -> {
            var removed = this.updated.remove(trackId);
            if (removed != null) {
                logger.debug("In this transaction InboundVideoTrack is updated before it was deleted");
            }
        });
    }

    public synchronized void save() {
        if (0 < this.deleted.size()) {
            Try.wrap(() -> this.storage.deleteAll(this.deleted));
            this.deleted.clear();
        }
        if (0 < this.updated.size()) {
            Try.wrap(() -> this.storage.setAll(this.updated));
            this.updated.clear();
        }
        this.fetched.clear();
    }

    @Override
    public String storageId() {
        return this.storage.getId();
    }

    @Override
    public int localSize() {
        return this.storage.localSize();
    }

    InboundTrack wrapInboundTrack(Models.InboundTrack model) {
        var result =  new InboundTrack(
                this.peerConnectionsRepositoryBeanProvider.get(),
                model,
                this
        );
        this.fetched.add(result.getTrackId(), result);
        return result;
    }

    void checkCollidingEntries() {
        this.storage.checkCollidingEntries();
    }

    private InboundTrack fetchOne(String trackId) {
        var model = Try.wrap(() -> this.storage.get(trackId), null);
        if (model == null) {
            return null;
        }
        return this.wrapInboundTrack(model);
    }

    private Map<String, InboundTrack> fetchAll(Set<String> trackIds) {
        var models = Try.wrap(() -> this.storage.getAll(Set.copyOf(trackIds)), null);

        if (models == null || models.isEmpty()) {
            return Collections.emptyMap();
        }
        return models.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    var model = entry.getValue();
                    return this.wrapInboundTrack(model);
                }
        ));
    }

}