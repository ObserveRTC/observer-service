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
public class SfuSctpChannelsRepository implements RepositoryStorageMetrics {

    private static final Logger logger = LoggerFactory.getLogger(SfuSctpChannelsRepository.class);

    private static final String STORAGE_ID = "observertc-sfu-sctp-streams";

    private SeparatedStorage<String, Models.SfuSctpChannel> storage;
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

    private Map<String, Models.SfuSctpChannel> updated;
    private Set<String> deleted;
    private CachedFetches<String, SfuSctpChannel> fetched;

    @PostConstruct
    void setup() {
        var baseStorage = new MemoryStorageBuilder<String, Models.SfuSctpChannel>()
                .setConcurrency(true)
                .setId(STORAGE_ID)
                .build();
        var storageBuilder = this.service.getStorageGrid().separatedStorage(baseStorage)
                .setKeyCodec(SerDeUtils.createStrToByteFunc(), SerDeUtils.createBytesToStr())
                .setValueCodec(
                        Mapper.create(Models.SfuSctpChannel::toByteArray, logger)::map,
                        Mapper.<byte[], Models.SfuSctpChannel>create(bytes -> Models.SfuSctpChannel.parseFrom(bytes), logger)::map
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

        this.fetched = CachedFetches.<String, SfuSctpChannel>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
        this.updated = new HashMap<>();
        this.deleted = new HashSet<>();
    }

    synchronized void update(Models.SfuSctpChannel sfuSctpChannel) {
        var sctpChannelId = sfuSctpChannel.getSfuSctpChannelId();
        this.updated.put(sctpChannelId, sfuSctpChannel);
        var removed = this.deleted.remove(sctpChannelId);
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

    Observable<List<ModifiedStorageEntry<String, Models.SfuSctpChannel>>> observableDeletedEntries() {
        return this.storage.collectedEvents().deletedEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.SfuSctpChannel>>> observableExpiredEntries() {
        return this.storage.collectedEvents().expiredEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.SfuSctpChannel>>> observableCreatedEntries() {
        return this.storage.collectedEvents().createdEntries();
    }

    public SfuSctpChannel get(String sctpStreamId) {
        return this.fetched.get(sctpStreamId);
    }

    public Map<String, SfuSctpChannel> getAll(Collection<String> sctpStreamIds) {
        if (sctpStreamIds == null || sctpStreamIds.size() < 1) {
            return Collections.emptyMap();
        }
        var set = Set.copyOf(sctpStreamIds);
        return this.fetched.getAll(set);
    }

    private SfuSctpChannel fetchOne(String sfuSctpStreamId) {
        var model = Try.wrap(() -> this.storage.get(sfuSctpStreamId), null);
        if (model == null) {
            return null;
        }
        return this.wrap(model);
    }

    private Map<String, SfuSctpChannel> fetchAll(Set<String> sfuSctpStreamIds) {
        var models = Try.wrap(() -> this.storage.getAll(sfuSctpStreamIds), null);

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

    SfuSctpChannel wrap(Models.SfuSctpChannel model) {
        var result = new SfuSctpChannel(
                model,
                this,
                this.sfuTransportsRepositoryBeanProvider.get()
        );
        this.fetched.add(result.getSfuSctpStreamId(), result);
        return result;
    }
}
