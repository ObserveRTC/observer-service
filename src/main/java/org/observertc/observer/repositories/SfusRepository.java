package org.observertc.observer.repositories;

import io.github.balazskreith.hamok.ModifiedStorageEntry;
import io.github.balazskreith.hamok.memorystorages.MemoryStorageBuilder;
import io.github.balazskreith.hamok.storagegrid.SeparatedStorage;
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
public class SfusRepository implements RepositoryStorageMetrics {

    private static final Logger logger = LoggerFactory.getLogger(SfusRepository.class);

    private static final String STORAGE_ID = "observertc-sfus";
    private static final int MAX_KEYS = 1000;
    private static final int MAX_VALUES = 100;

    private SeparatedStorage<String, Models.Sfu> storage;

    @Inject
    private HamokService service;

    @Inject
    private ObserverConfig observerConfig;

    @Inject
    private Backups backups;


    @Inject
    private SfuTransportsRepository sfuTransportsRepository;

    @Inject
    private ObserverConfig.HamokConfig hamokConfig;

    private Map<String, Models.Sfu> updated;
    private Set<String> deleted;
    private CachedFetches<String, Sfu> fetched;


    @PostConstruct
    void setup() {
        var baseStorage = new MemoryStorageBuilder<String, Models.Sfu>()
                .setConcurrency(true)
                .setId(STORAGE_ID)
                .build();
        var storageBuilder = this.service.getStorageGrid().separatedStorage(baseStorage)
                .setKeyCodec(SerDeUtils.createStrToByteFunc(), SerDeUtils.createBytesToStr())
                .setValueCodec(
                        Mapper.create(Models.Sfu::toByteArray, logger)::map,
                        Mapper.<byte[], Models.Sfu>create(bytes -> Models.Sfu.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(this.observerConfig.buffers.debouncers.maxItems)
                .setMaxCollectedStorageTimeInMs(this.observerConfig.buffers.debouncers.maxTimeInMs)
                .setMaxMessageKeys(MAX_KEYS)
                .setMaxMessageValues(MAX_VALUES)
                .setThrowingExceptionOnRequestTimeout(!this.hamokConfig.usePartialResponses)
                ;

        if (this.observerConfig.repository.useBackups) {
            storageBuilder.setDistributedBackups(this.backups);
        }

        this.storage = storageBuilder.build();

        this.fetched = CachedFetches.<String, Sfu>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
        this.updated = new HashMap<>();
        this.deleted = new HashSet<>();
    }

    synchronized void update(Models.Sfu sfu) {
        var sfuId = sfu.getSfuId();
        this.updated.put(sfuId, sfu);
        var removed = this.deleted.remove(sfuId);
        if (removed) {
            logger.debug("In this transaction, Sfu was deleted before it was updated");
        }
    }

    synchronized void delete(String sfuId) {
        var removed = this.updated.remove(sfuId);
        if (removed != null) {
            logger.debug("In this transaction, Sfu was updated before it was deleted");
        }
    }

    public synchronized void deleteAll(Set<String> sfuIds) {
        if (sfuIds == null || sfuIds.size() < 1) {
            return;
        }
        this.deleted.addAll(sfuIds);
        sfuIds.forEach(sfuId -> {
            var removed = this.updated.remove(sfuId);
            if (removed != null) {
                logger.debug("In this transaction, Sfu was updated before it was deleted");
            }
        });
    }

    public Map<String, Sfu> fetchRecursively(Set<String> sfuIds) {
        if (sfuIds == null || sfuIds.size() < 1) {
            return Collections.emptyMap();
        }
        var result = this.getAll(sfuIds);
        var transportIds = result.values().stream()
                .map(Sfu::getSfuTransportIds)
                .flatMap(s -> s.stream())
                .collect(Collectors.toSet());
        this.sfuTransportsRepository.fetchRecursively(transportIds);
        return result;
    }

    public synchronized void save() {
        if (0 < this.deleted.size()) {
            var sfus = Try.<Map<String, Sfu>>wrap(() -> this.getAll(this.deleted), Collections.emptyMap());
            var sfuTransportIds = sfus.values().stream()
                    .map(Sfu::getSfuTransportIds)
                    .flatMap(s -> s.stream())
                    .collect(Collectors.toSet());
            Try.wrap(() -> this.sfuTransportsRepository.deleteAll(sfuTransportIds));
            this.storage.deleteAll(this.deleted);
            this.deleted.clear();
        }
        if (0 < this.updated.size()) {
            Try.wrap(() -> this.storage.setAll(this.updated));
            this.updated.clear();
        }
        this.sfuTransportsRepository.save();
        this.fetched.clear();
    }

    Observable<List<ModifiedStorageEntry<String, Models.Sfu>>> observableDeletedEntries() {
        return this.storage.collectedEvents().deletedEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.Sfu>>> observableExpiredEntries() {
        return this.storage.collectedEvents().expiredEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.Sfu>>> observableCreatedEntries() {
        return this.storage.collectedEvents().createdEntries();
    }

    public Sfu add(String serviceId, String mediaUnitId, String sfuId, Long timestamp, String timeZoneId, String marker) {
        var modelBuilder = Models.Sfu.newBuilder()
                .setServiceId(serviceId)
                .setSfuId(sfuId)
                .setJoined(timestamp)

                .setSampleTouched(timestamp)
                .setMediaUnitId(mediaUnitId)
                // marker
                // timeZoneId
                ;
        if (marker != null) {
            modelBuilder.setMarker(marker);
        }
        if (timeZoneId != null) {
            modelBuilder.setTimeZoneId(timeZoneId);
        }
        var model = modelBuilder.build();
        this.updated.put(model.getSfuId(), model);
        return this.wrapSfu(model);
    }

    public void remove(String sfuId) {
        this.deleted.add(sfuId);
    }

    public Sfu get(String sfuId) {
        return this.fetched.get(sfuId);
    }

    public Map<String, Sfu> getAll(Collection<String> sfuIds) {
        if (sfuIds == null || sfuIds.size() < 1) {
            return Collections.emptyMap();
        }
        var set = Set.copyOf(sfuIds);
        return this.fetched.getAll(set);
    }

    public Map<String, Sfu> getAllLocallyStored() {
        var callIds = this.storage.localKeys();
        if (callIds == null || callIds.size() < 1) {
            return Collections.emptyMap();
        }
        return this.fetchAll(callIds);
    }

    @Override
    public String storageId() {
        return this.storage.getId();
    }

    @Override
    public int localSize() {
        return this.storage.localSize();
    }

    private Sfu fetchOne(String sfuId) {
        var model = Try.wrap(() -> this.storage.get(sfuId), null);
        if (model == null) {
            return null;
        }
        return this.wrapSfu(model);
    }

    private Map<String, Sfu> fetchAll(Set<String> sfuIds) {
        var models = Try.wrap(() -> this.storage.getAll(sfuIds), null);

        if (models == null || models.isEmpty()) {
            return Collections.emptyMap();
        }
        return models.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    var model = entry.getValue();
                    return this.wrapSfu(model);
                }
        ));
    }

    void checkCollidingEntries() {
        this.storage.checkCollidingEntries();
    }

    private Sfu wrapSfu(Models.Sfu model) {
        var result = new Sfu(
                model,
                this,
                this.sfuTransportsRepository
        );
        this.fetched.add(result.getSfuId(), result);
        return result;
    }

}
