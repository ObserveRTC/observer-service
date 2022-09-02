package org.observertc.observer.repositories;

import io.github.balazskreith.hamok.ModifiedStorageEntry;
import io.github.balazskreith.hamok.memorystorages.MemoryStorageBuilder;
import io.github.balazskreith.hamok.storagegrid.SeparatedStorage;
import io.micronaut.context.BeanProvider;
import io.reactivex.rxjava3.core.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.HamokService;
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
public class SfuMediaStreamsRepository implements RepositoryStorageMetrics {

    private static final Logger logger = LoggerFactory.getLogger(SfuMediaStreamsRepository.class);

    private static final String STORAGE_ID = "observertc-sfu-media-streams";

    private SeparatedStorage<String, Models.SfuMediaStream> storage;

    @Inject
    private HamokService service;

    @Inject
    private ObserverConfig.InternalBuffersConfig bufferConfig;

    @Inject
    private BeanProvider<SfuTransportsRepository> sfuTransportsRepositoryBeanProvider;

    @Inject
    private SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository;

    @Inject
    private BeanProvider<SfuMediaSinksRepository> sfuMediaSinksRepositoryBeanProvider;

    private Map<String, Models.SfuMediaStream> updated;
    private Set<String> deleted;
    private CachedFetches<String, SfuMediaStream> fetched;

    @PostConstruct
    void setup() {
        var baseStorage = new MemoryStorageBuilder<String, Models.SfuMediaStream>()
                .setConcurrency(true)
                .setId(STORAGE_ID)
                .setExpiration(5 * 60 * 1000)
                .build();
        this.storage = this.service.getStorageGrid().separatedStorage(baseStorage)
                .setKeyCodec(SerDeUtils.createStrToByteFunc(), SerDeUtils.createBytesToStr())
                .setValueCodec(
                        Mapper.create(Models.SfuMediaStream::toByteArray, logger)::map,
                        Mapper.<byte[], Models.SfuMediaStream>create(bytes -> Models.SfuMediaStream.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(bufferConfig.debouncers.maxItems)
                .setMaxCollectedStorageTimeInMs(bufferConfig.debouncers.maxTimeInMs)
                .setMaxMessageValues(1000)
                .build();
        this.fetched = CachedFetches.<String, SfuMediaStream>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
        this.updated = new HashMap<>();
        this.deleted = new HashSet<>();
    }


    synchronized void update(Models.SfuMediaStream sfuSource) {
        var sfuSourceId = sfuSource.getSfuStreamId();
        this.updated.put(sfuSourceId, sfuSource);
        var removed = this.deleted.remove(sfuSourceId);
        if (removed) {
            logger.debug("In this transaction, SfuMediaStream was deleted before it was updated");
        }
    }

    synchronized void delete(String sfuMediaStreamId) {
        var removed = this.updated.remove(sfuMediaStreamId);
        if (removed != null) {
            logger.debug("In this transaction, SfuMediaStream was updated before it was deleted");
        }
    }

    synchronized void deleteAll(Set<String> sfuMediaStreamIds) {
        if (sfuMediaStreamIds == null || sfuMediaStreamIds.size() < 1) {
            return;
        }
        this.deleted.addAll(sfuMediaStreamIds);
        sfuMediaStreamIds.forEach(sfuMediaStreamId -> {
            var removed = this.updated.remove(sfuMediaStreamId);
            if (removed != null) {
                logger.debug("In this transaction, SfuMediaStream was updated before it was deleted");
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

    @Override
    public String storageId() {
        return this.storage.getId();
    }

    @Override
    public int localSize() {
        return this.storage.localSize();
    }

    Observable<List<ModifiedStorageEntry<String, Models.SfuMediaStream>>> observableDeletedEntries() {
        return this.storage.collectedEvents().deletedEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.SfuMediaStream>>> observableExpiredEntries() {
        return this.storage.collectedEvents().expiredEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.SfuMediaStream>>> observableCreatedEntries() {
        return this.storage.collectedEvents().createdEntries();
    }

    public SfuMediaStream get(String sfuStreamId) {
        return this.fetched.get(sfuStreamId);
    }

    public Map<String, SfuMediaStream> getAll(Collection<String> sfuMediaStreamIds) {
        if (sfuMediaStreamIds == null || sfuMediaStreamIds.size() < 1) {
            return Collections.emptyMap();
        }
        var set = Set.copyOf(sfuMediaStreamIds);
        return this.fetched.getAll(set);
    }

    SfuMediaStream wrapSfuMediaStream(Models.SfuMediaStream model) {
        var result =  new SfuMediaStream(
                model,
                this,
                this.sfuMediaSinksRepositoryBeanProvider.get(),
                this.sfuTransportsRepositoryBeanProvider.get(),
                this.sfuInboundRtpPadsRepository

        );
        this.fetched.add(result.getSfuStreamId(), result);
        return result;
    }

    private SfuMediaStream fetchOne(String sfuStreamId) {
        var model = this.storage.get(sfuStreamId);
        if (model == null) {
            return null;
        }
        return this.wrapSfuMediaStream(model);
    }

    private Map<String, SfuMediaStream> fetchAll(Set<String> sfuStreamIds) {
        var models = this.storage.getAll(sfuStreamIds);

        if (models == null || models.isEmpty()) {
            return Collections.emptyMap();
        }
        return models.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    var model = entry.getValue();
                    return this.wrapSfuMediaStream(model);
                }
        ));
    }

}
