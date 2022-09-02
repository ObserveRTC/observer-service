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
public class SfuOutboundRtpPadsRepository implements RepositoryStorageMetrics {

    private static final Logger logger = LoggerFactory.getLogger(SfuOutboundRtpPadsRepository.class);

    private static final String STORAGE_ID = "observertc-sfu-outbound-rtp-pads";

    private SeparatedStorage<String, Models.SfuOutboundRtpPad> storage;

    @Inject
    private HamokService service;

    @Inject
    private ObserverConfig.InternalBuffersConfig bufferConfig;

    @Inject
    BeanProvider<SfuMediaSinksRepository> sfuMediaSinksRepositoryBeanProvider;

    private Map<String, Models.SfuOutboundRtpPad> updated;
    private Set<String> deleted;
    private CachedFetches<String, SfuOutboundRtpPad> fetched;

    @PostConstruct
    void setup() {
        var baseStorage = new MemoryStorageBuilder<String, Models.SfuOutboundRtpPad>()
                .setConcurrency(true)
                .setId(STORAGE_ID)
                .build();
        this.storage = this.service.getStorageGrid().separatedStorage(baseStorage)
                .setKeyCodec(SerDeUtils.createStrToByteFunc(), SerDeUtils.createBytesToStr())
                .setValueCodec(
                        Mapper.create(Models.SfuOutboundRtpPad::toByteArray, logger)::map,
                        Mapper.<byte[], Models.SfuOutboundRtpPad>create(bytes -> Models.SfuOutboundRtpPad.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(bufferConfig.debouncers.maxItems)
                .setMaxCollectedStorageTimeInMs(bufferConfig.debouncers.maxTimeInMs)
                .setMaxMessageValues(1000)
                .build();
        this.fetched = CachedFetches.<String, SfuOutboundRtpPad>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
        this.updated = new HashMap<>();
        this.deleted = new HashSet<>();
    }

    synchronized void update(Models.SfuOutboundRtpPad sfuOutboundRtpPad) {
        var rtpPadId = sfuOutboundRtpPad.getRtpPadId();
        this.updated.put(rtpPadId, sfuOutboundRtpPad);
        var removed = this.deleted.remove(rtpPadId);
        if (removed) {
            logger.debug("In this transaction, SfuOutboundRtpPad was deleted before it was updated");
        }
    }

    synchronized void delete(String rtpPadId) {
        var removed = this.updated.remove(rtpPadId);
        if (removed != null) {
            logger.debug("In this transaction, SfuOutboundRtpPad was updated before it was deleted");
        }
    }

    public synchronized void deleteAll(Set<String> rtpPadIds) {
        if (rtpPadIds == null || rtpPadIds.size() < 1) {
            return;
        }
        this.deleted.addAll(rtpPadIds);
        rtpPadIds.forEach(rtpPadId -> {
            var removed = this.updated.remove(rtpPadId);
            if (removed != null) {
                logger.debug("In this transaction, SfuOutboundRtpPad was updated before it was deleted");
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

    Observable<List<ModifiedStorageEntry<String, Models.SfuOutboundRtpPad>>> observableDeletedEntries() {
        return this.storage.collectedEvents().deletedEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.SfuOutboundRtpPad>>> observableExpiredEntries() {
        return this.storage.collectedEvents().expiredEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.SfuOutboundRtpPad>>> observableCreatedEntries() {
        return this.storage.collectedEvents().createdEntries();
    }

    public SfuOutboundRtpPad get(String rtpPadId) {
        return this.fetched.get(rtpPadId);
    }

    public Map<String, SfuOutboundRtpPad> getAll(Collection<String> rtpPadIds) {
        if (rtpPadIds == null || rtpPadIds.size() < 1) {
            return Collections.emptyMap();
        }
        var set = Set.copyOf(rtpPadIds);
        return this.fetched.getAll(set);
    }

    private SfuOutboundRtpPad fetchOne(String rtpPadId) {
        var model = this.storage.get(rtpPadId);
        if (model == null) {
            return null;
        }
        return this.wrapSfuOutboundRtpPad(model);
    }

    private Map<String, SfuOutboundRtpPad> fetchAll(Set<String> rtpPadIds) {
        var models = this.storage.getAll(rtpPadIds);

        if (models == null || models.isEmpty()) {
            return Collections.emptyMap();
        }
        return models.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    var model = entry.getValue();
                    return this.wrapSfuOutboundRtpPad(model);
                }
        ));
    }

    SfuOutboundRtpPad wrapSfuOutboundRtpPad(Models.SfuOutboundRtpPad model) {
        var result = new SfuOutboundRtpPad(
                model,
                this,
                this.sfuMediaSinksRepositoryBeanProvider.get()
        );
        this.fetched.add(result.getRtpPadId(), result);
        return result;
    }
}
