package org.observertc.observer.repositories;

import io.github.balazskreith.hamok.ModifiedStorageEntry;
import io.github.balazskreith.hamok.memorystorages.MemoryStorageBuilder;
import io.github.balazskreith.hamok.storagegrid.SeparatedStorage;
import io.micronaut.context.BeanProvider;
import io.reactivex.rxjava3.core.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
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
public class SfuTransportsRepository {

    private static final Logger logger = LoggerFactory.getLogger(SfuTransportsRepository.class);

    private static final String STORAGE_ID = "observertc-sfu-transports";

    private SeparatedStorage<String, Models.SfuTransport> storage;

    @Inject
    private HamokService service;

    @Inject
    private ObserverConfig.InternalBuffersConfig bufferConfig;

    @Inject
    BeanProvider<SfusRepository> sfusRepositoryBeanProvider;

    private Map<String, Models.SfuTransport> updated;
    private Set<String> deleted;
    private CachedFetches<String, SfuTransport> fetched;

    @PostConstruct
    void setup() {
        var baseStorage = new MemoryStorageBuilder<String, Models.SfuTransport>()
                .setConcurrency(true)
                .setId(STORAGE_ID)
                .build();
        this.storage = this.service.getStorageGrid().separatedStorage(baseStorage)
                .setKeyCodec(SerDeUtils.createStrToByteFunc(), SerDeUtils.createBytesToStr())
                .setValueCodec(
                        Mapper.create(Models.SfuTransport::toByteArray, logger)::map,
                        Mapper.<byte[], Models.SfuTransport>create(bytes -> Models.SfuTransport.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(bufferConfig.debouncers.maxItems)
                .setMaxCollectedStorageTimeInMs(bufferConfig.debouncers.maxTimeInMs)
                .setMaxMessageValues(1000)
                .build();

        this.fetched = CachedFetches.<String, SfuTransport>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
        this.updated = new HashMap<>();
        this.deleted = new HashSet<>();
    }

    synchronized void update(Models.SfuTransport sfuTransport) {
        var sfuTransportId = sfuTransport.getTransportId();
        this.updated.put(sfuTransportId, sfuTransport);
        var removed = this.deleted.remove(sfuTransportId);
        if (removed) {
            logger.debug("In this transaction, SfuTransport was deleted before it was updated");
        }
    }

    synchronized void delete(String sfuTransportId) {
        var removed = this.updated.remove(sfuTransportId);
        if (removed != null) {
            logger.debug("In this transaction, SfuTransport was updated before it was deleted");
        }
    }

    synchronized void deleteAll(Set<String> sfuTransportIds) {
        this.deleted.addAll(sfuTransportIds);
        sfuTransportIds.forEach(sfuTransportId -> {
            var removed = this.updated.remove(sfuTransportId);
            if (removed != null) {
                logger.debug("In this transaction, SfuTransport was updated before it was deleted");
            }
        });
    }

    public synchronized void save() {
        if (0 < this.deleted.size()) {
            this.storage.deleteAll(this.deleted);
            this.deleted.clear();
        }
        if (0 < this.updated.size()) {
            this.storage.setAll(this.updated);
            this.updated.clear();
        }
        this.fetched.clear();
    }

    Observable<List<ModifiedStorageEntry<String, Models.SfuTransport>>> observableDeletedEntries() {
        return this.storage.collectedEvents().deletedEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.SfuTransport>>> observableExpiredEntries() {
        return this.storage.collectedEvents().expiredEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.SfuTransport>>> observableCreatedEntries() {
        return this.storage.collectedEvents().createdEntries();
    }

    public SfuTransport get(String sfuTransportId) {
        return this.fetched.get(sfuTransportId);
    }

    public Map<String, SfuTransport> getAll(Collection<String> sfuTransportIds) {
        if (sfuTransportIds == null || sfuTransportIds.size() < 1) {
            return Collections.emptyMap();
        }
        var set = Set.copyOf(sfuTransportIds);
        return this.fetched.getAll(set);
    }

    private SfuTransport fetchOne(String sfuTransportId) {
        var model = this.storage.get(sfuTransportId);
        if (model == null) {
            return null;
        }
        return this.wrapSfuTransport(model);
    }

    private Map<String, SfuTransport> fetchAll(Set<String> sfuTransportIds) {
        var models = this.storage.getAll(sfuTransportIds);

        if (models == null || models.isEmpty()) {
            return Collections.emptyMap();
        }
        return models.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    var model = entry.getValue();
                    return this.wrapSfuTransport(model);
                }
        ));
    }

    SfuTransport wrapSfuTransport(Models.SfuTransport model) {
        var result = new SfuTransport(
                model,
                this.sfusRepositoryBeanProvider.get(),
                this
        );
        this.fetched.add(result.getSfuTransportId(), result);
        return result;
    }
}
