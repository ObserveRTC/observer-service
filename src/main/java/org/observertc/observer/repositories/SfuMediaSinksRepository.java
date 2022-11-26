package org.observertc.observer.repositories;

import io.github.balazskreith.hamok.ModifiedStorageEntry;
import io.github.balazskreith.hamok.memorystorages.MemoryStorageBuilder;
import io.github.balazskreith.hamok.storagegrid.SeparatedStorage;
import io.micronaut.context.BeanProvider;
import io.reactivex.rxjava3.core.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.HamokService;
import org.observertc.observer.common.Try;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.mappings.Mapper;
import org.observertc.observer.mappings.SerDeUtils;
import org.observertc.schemas.dtos.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class SfuMediaSinksRepository implements RepositoryStorageMetrics {

    private static final Logger logger = LoggerFactory.getLogger(SfuMediaSinksRepository.class);

    private static final String STORAGE_ID = "observertc-sfu-media-sinks";

    private SeparatedStorage<String, Models.SfuMediaSink> storage;
    private static final int MAX_KEYS = 1000;
    private static final int MAX_VALUES = 100;

    @Inject
    private HamokService service;

    @Inject
    private ObserverConfig observerConfig;

    @Inject
    private Backups backups;


    @Inject
    BeanProvider<SfuTransportsRepository> sfuTransportsRepositoryBeanProvider;

    @Inject
    private BeanProvider<SfuOutboundRtpPadsRepository> sfuOutboundRtpPadsRepositoryBeanProvider;

    private Map<String, Models.SfuMediaSink> updated;
    private Set<String> deleted;
    private CachedFetches<String, SfuMediaSink> fetched;

    @PostConstruct
    void setup() {
        var baseStorage = new MemoryStorageBuilder<String, Models.SfuMediaSink>()
                .setConcurrency(true)
                .setId(STORAGE_ID)
                .build();
        var storageBuilder = this.service.getStorageGrid().separatedStorage(baseStorage)
                .setKeyCodec(SerDeUtils.createStrToByteFunc(), SerDeUtils.createBytesToStr())
                .setValueCodec(
                        Mapper.create(Models.SfuMediaSink::toByteArray, logger)::map,
                        Mapper.<byte[], Models.SfuMediaSink>create(bytes -> Models.SfuMediaSink.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(this.observerConfig.buffers.debouncers.maxItems)
                .setMaxCollectedStorageTimeInMs(this.observerConfig.buffers.debouncers.maxTimeInMs)
                .setMaxMessageKeys(MAX_KEYS)
                .setMaxMessageValues(MAX_VALUES)
                ;

        if (this.observerConfig.repository.useBackups) {
            storageBuilder.setDistributedBackups(this.backups);
        }

        this.storage = storageBuilder.build();

        this.fetched = CachedFetches.<String, SfuMediaSink>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
        this.updated = new HashMap<>();
        this.deleted = new HashSet<>();
    }

    synchronized void update(Models.SfuMediaSink sfuSink) {
        var sfuSinkId = sfuSink.getSfuSinkId();
        this.updated.put(sfuSinkId, sfuSink);
        var removed = this.deleted.remove(sfuSinkId);
        if (removed) {
            logger.debug("In this transaction, SfuMediaSink was deleted before it was updated");
        }
    }

    synchronized void delete(String sfuSinkId) {
        var removed = this.updated.remove(sfuSinkId);
        if (removed != null) {
            logger.debug("In this transaction, SfuMediaSink was updated before it was deleted");
        }
    }

    synchronized void deleteAll(Set<String> sfuSinkIds) {
        if (sfuSinkIds == null || sfuSinkIds.size() < 1) {
            return;
        }
        this.deleted.addAll(sfuSinkIds);
        sfuSinkIds.forEach(sfuSinkId -> {
            var removed = this.updated.remove(sfuSinkId);
            if (removed != null) {
                logger.debug("In this transaction, SfuMediaSink was updated before it was deleted");
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

    Observable<List<ModifiedStorageEntry<String, Models.SfuMediaSink>>> observableDeletedEntries() {
        return this.storage.collectedEvents().deletedEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.SfuMediaSink>>> observableExpiredEntries() {
        return this.storage.collectedEvents().expiredEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.SfuMediaSink>>> observableCreatedEntries() {
        return this.storage.collectedEvents().createdEntries();
    }

    public SfuMediaSink get(String sfuSinkId) {
        return this.fetched.get(sfuSinkId);
    }

    public Map<String, SfuMediaSink> getAll(Collection<String> sfuSinkIds) {
        if (sfuSinkIds == null || sfuSinkIds.size() < 1) {
            return Collections.emptyMap();
        }
        var set = Set.copyOf(sfuSinkIds);
        return this.fetched.getAll(set);
    }

    private SfuMediaSink fetchOne(String sfuMediaSinkId) {
        var model = Try.wrap(() -> this.storage.get(sfuMediaSinkId), null);
        if (model == null) {
            return null;
        }
        return this.wrap(model);
    }

    private Map<String, SfuMediaSink> fetchAll(Set<String> sfuMediaSinkIds) {
        var models = Try.wrap(() -> this.storage.getAll(sfuMediaSinkIds), null);

        if (models == null || models.isEmpty()) {
            return Collections.emptyMap();
        }
        return models.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    var model = entry.getValue();
                    return this.wrap(model);
                }
        ));
    }

    void checkCollidingEntries() {
        this.storage.checkCollidingEntries();
    }

    SfuMediaSink wrap(Models.SfuMediaSink model) {
        var result = new SfuMediaSink(
                model,
                this,
                this.sfuTransportsRepositoryBeanProvider.get(),
                this.sfuOutboundRtpPadsRepositoryBeanProvider.get()
        );
        this.fetched.add(result.getSfuSinkId(), result);
        return result;
    }
}
