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
public class SfuTransportsRepository implements RepositoryStorageMetrics{

    private static final Logger logger = LoggerFactory.getLogger(SfuTransportsRepository.class);

    private static final String STORAGE_ID = "observertc-sfu-transports";

    private SeparatedStorage<String, Models.SfuTransport> storage;
    private static final int MAX_KEYS = 1000;
    private static final int MAX_VALUES = 100;

    @Inject
    private HamokService service;

    @Inject
    private ObserverConfig.InternalBuffersConfig bufferConfig;

    @Inject
    BeanProvider<SfusRepository> sfusRepositoryBeanProvider;

    @Inject
    SfuInboundRtpPadsRepository sfuInboundRtpPadsRepository;

    @Inject
    SfuOutboundRtpPadsRepository sfuOutboundRtpPadsRepository;

    @Inject
    SfuSctpStreamsRepository sfuSctpStreamsRepository;

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
                .setMaxMessageKeys(MAX_KEYS)
                .setMaxMessageValues(MAX_VALUES)
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

    public synchronized void deleteAll(Set<String> sfuTransportIds) {
        if (sfuTransportIds == null || sfuTransportIds.size() < 1) {
            return;
        }
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
            var sfuTransports = this.getAll(this.deleted);
            var sfuInboundRtpPadIds = new HashSet<String>();
            var sfuOutboundRtpPadIds = new HashSet<String>();
            var sfuSctpStreamIds = new HashSet<String>();
            for (var sfuTransport : sfuTransports.values()) {
                sfuInboundRtpPadIds.addAll(sfuTransport.getInboundRtpPadIds());
                sfuOutboundRtpPadIds.addAll(sfuTransport.getOutboundRtpPadIds());
                sfuSctpStreamIds.addAll(sfuTransport.getSctpStreamIds());
            }
            if (0 < sfuInboundRtpPadIds.size()) {
                this.sfuInboundRtpPadsRepository.deleteAll(sfuInboundRtpPadIds);
            }
            if (0 < sfuOutboundRtpPadIds.size()) {
                this.sfuOutboundRtpPadsRepository.deleteAll(sfuOutboundRtpPadIds);
            }
            if (0 < sfuSctpStreamIds.size()) {
                this.sfuSctpStreamsRepository.deleteAll(sfuSctpStreamIds);
            }
            Try.wrap(() -> this.storage.deleteAll(this.deleted));
            this.deleted.clear();
        }
        if (0 < this.updated.size()) {
            Try.wrap(() -> this.storage.setAll(this.updated));
            this.updated.clear();
        }
        this.sfuInboundRtpPadsRepository.save();
        this.sfuOutboundRtpPadsRepository.save();
        this.sfuSctpStreamsRepository.save();
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

    @Override
    public String storageId() {
        return this.storage.getId();
    }

    @Override
    public int localSize() {
        return this.storage.localSize();
    }

    private SfuTransport fetchOne(String sfuTransportId) {
        var model = Try.wrap(() -> this.storage.get(sfuTransportId), null);
        if (model == null) {
            return null;
        }
        return this.wrapSfuTransport(model);
    }

    private Map<String, SfuTransport> fetchAll(Set<String> sfuTransportIds) {
        var models = Try.wrap(() -> this.storage.getAll(sfuTransportIds), null);

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
                this,
                this.sfuInboundRtpPadsRepository,
                this.sfuOutboundRtpPadsRepository,
                this.sfuSctpStreamsRepository
        );
        this.fetched.add(result.getSfuTransportId(), result);
        return result;
    }
}
