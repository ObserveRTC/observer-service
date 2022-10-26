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
public class SfuSctpStreamsRepository implements RepositoryStorageMetrics {

    private static final Logger logger = LoggerFactory.getLogger(SfuSctpStreamsRepository.class);

    private static final String STORAGE_ID = "observertc-sfu-sctp-streams";

    private SeparatedStorage<String, Models.SfuSctpStream> storage;
    private static final int MAX_KEYS = 1000;
    private static final int MAX_VALUES = 100;

    @Inject
    private HamokService service;

    @Inject
    private ObserverConfig.InternalBuffersConfig bufferConfig;

    @Inject
    BeanProvider<SfuTransportsRepository> sfuTransportsRepositoryBeanProvider;

    private Map<String, Models.SfuSctpStream> updated;
    private Set<String> deleted;
    private CachedFetches<String, SfuSctpStream> fetched;

    @PostConstruct
    void setup() {
        var baseStorage = new MemoryStorageBuilder<String, Models.SfuSctpStream>()
                .setConcurrency(true)
                .setId(STORAGE_ID)
                .build();
        this.storage = this.service.getStorageGrid().separatedStorage(baseStorage)
                .setKeyCodec(SerDeUtils.createStrToByteFunc(), SerDeUtils.createBytesToStr())
                .setValueCodec(
                        Mapper.create(Models.SfuSctpStream::toByteArray, logger)::map,
                        Mapper.<byte[], Models.SfuSctpStream>create(bytes -> Models.SfuSctpStream.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(bufferConfig.debouncers.maxItems)
                .setMaxCollectedStorageTimeInMs(bufferConfig.debouncers.maxTimeInMs)
                .setMaxMessageKeys(MAX_KEYS)
                .setMaxMessageValues(MAX_VALUES)
                .build();
        this.fetched = CachedFetches.<String, SfuSctpStream>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
        this.updated = new HashMap<>();
        this.deleted = new HashSet<>();
    }

    synchronized void update(Models.SfuSctpStream sfuSctpStream) {
        var sctpStreamId = sfuSctpStream.getSfuSctpStreamId();
        this.updated.put(sctpStreamId, sfuSctpStream);
        var removed = this.deleted.remove(sctpStreamId);
        if (removed) {
            logger.debug("In this transaction, SfuSctpStream was deleted before it was updated");
        }
    }

    synchronized void delete(String sctpStreamId) {
        var removed = this.updated.remove(sctpStreamId);
        if (removed != null) {
            logger.debug("In this transaction, SfuSctpStream was updated before it was deleted");
        }
    }

    public synchronized void deleteAll(Set<String> sctpStreamIds) {
        if (sctpStreamIds == null || sctpStreamIds.size() < 1) {
            return;
        }
        this.deleted.addAll(sctpStreamIds);
        sctpStreamIds.forEach(sctpStreamId -> {
            var removed = this.updated.remove(sctpStreamIds);
            if (removed != null) {
                logger.debug("In this transaction, SfuSctpStream was updated before it was deleted");
            }
        });
    }

    public void save() {
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

    Observable<List<ModifiedStorageEntry<String, Models.SfuSctpStream>>> observableDeletedEntries() {
        return this.storage.collectedEvents().deletedEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.SfuSctpStream>>> observableExpiredEntries() {
        return this.storage.collectedEvents().expiredEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.SfuSctpStream>>> observableCreatedEntries() {
        return this.storage.collectedEvents().createdEntries();
    }

    public SfuSctpStream get(String sctpStreamId) {
        return this.fetched.get(sctpStreamId);
    }

    public Map<String, SfuSctpStream> getAll(Collection<String> sctpStreamIds) {
        if (sctpStreamIds == null || sctpStreamIds.size() < 1) {
            return Collections.emptyMap();
        }
        var set = Set.copyOf(sctpStreamIds);
        return this.fetched.getAll(set);
    }

    private SfuSctpStream fetchOne(String sfuSctpStreamId) {
        var model = Try.wrap(() -> this.storage.get(sfuSctpStreamId), null);
        if (model == null) {
            return null;
        }
        return this.wrapSfuSctpStream(model);
    }

    private Map<String, SfuSctpStream> fetchAll(Set<String> sfuSctpStreamIds) {
        var models = Try.wrap(() -> this.storage.getAll(sfuSctpStreamIds), null);

        if (models == null || models.isEmpty()) {
            return Collections.emptyMap();
        }
        return models.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    var model = entry.getValue();
                    return this.wrapSfuSctpStream(model);
                }
        ));
    }

    void checkCollidingEntries() {
        this.storage.checkCollidingEntries();
    }

    SfuSctpStream wrapSfuSctpStream(Models.SfuSctpStream model) {
        var result = new SfuSctpStream(
                model,
                this,
                this.sfuTransportsRepositoryBeanProvider.get()
        );
        this.fetched.add(result.getSfuSctpStreamId(), result);
        return result;
    }
}
