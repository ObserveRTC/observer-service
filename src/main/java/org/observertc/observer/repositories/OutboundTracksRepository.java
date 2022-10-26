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
public class OutboundTracksRepository implements RepositoryStorageMetrics {

    private static final Logger logger = LoggerFactory.getLogger(OutboundTracksRepository.class);

    private static final String STORAGE_ID = "observertc-outbound-audio-tracks";
    private static final int MAX_KEYS = 1000;
    private static final int MAX_VALUES = 100;

    private Set<String> deleted;
    private Map<String, Models.OutboundTrack> updated;

    private SeparatedStorage<String, Models.OutboundTrack> storage;
    private CachedFetches<String, OutboundTrack> fetched;

    @Inject
    BeanProvider<PeerConnectionsRepository> peerConnectionsRepositoryBeanProvider;

    @Inject
    private ObserverConfig.RepositoryConfig config;

    @Inject
    private HamokService hamokService;

    @Inject
    private ObserverConfig.InternalBuffersConfig bufferConfig;

    @PostConstruct
    void setup() {
        var baseStorage = new MemoryStorageBuilder<String, Models.OutboundTrack>()
                .setId(STORAGE_ID)
                .setConcurrency(true)
//                .setExpiration(config.mediaTracksMaxIdleTimeInS * 1000)
                .build();
        this.storage = this.hamokService.getStorageGrid().separatedStorage(baseStorage)
                .setKeyCodec(SerDeUtils.createStrToByteFunc(), SerDeUtils.createBytesToStr())
                .setValueCodec(
                        Mapper.create(Models.OutboundTrack::toByteArray, logger)::map,
                        Mapper.<byte[], Models.OutboundTrack>create(bytes -> Models.OutboundTrack.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(bufferConfig.debouncers.maxItems)
                .setMaxCollectedStorageTimeInMs(bufferConfig.debouncers.maxTimeInMs)
                .setMaxMessageKeys(MAX_KEYS)
                .setMaxMessageValues(MAX_VALUES)
                .build();
        this.fetched = CachedFetches.<String, OutboundTrack>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
        this.updated = new HashMap<>();
        this.deleted = new HashSet<>();
    }

    Observable<List<ModifiedStorageEntry<String, Models.OutboundTrack>>> observableDeletedEntries() {
        return this.storage.collectedEvents().deletedEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.OutboundTrack>>> observableExpiredEntries() {
        return this.storage.collectedEvents().expiredEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.OutboundTrack>>> observableCreatedEntries() {
        return this.storage.collectedEvents().createdEntries();
    }

    public OutboundTrack get(String trackId) {
        return this.fetched.get(trackId);
    }

    public Map<String, OutboundTrack> getAll(Collection<String> trackIds) {
        if (trackIds == null || trackIds.size() < 1) {
            return Collections.emptyMap();
        }
        var set = Set.copyOf(trackIds);
        return this.fetched.getAll(set);
    }

    public Map<String, OutboundTrack> fetchRecursively(Set<String> inboundTrackIds) {
        return this.getAll(inboundTrackIds);
    }

    synchronized void update(Models.OutboundTrack OutboundAudioTrack) {
        this.updated.put(OutboundAudioTrack.getTrackId(), OutboundAudioTrack);
        var removed = this.deleted.remove(OutboundAudioTrack.getTrackId());
        if (removed) {
            logger.debug("In this transaction OutboundAudioTrack is deleted before it was updated");
        }
    }

    synchronized void delete(String trackId) {
        this.deleted.add(trackId);
        var removed = this.updated.remove(trackId);
        if (removed != null) {
            logger.debug("In this transaction OutboundAudioTrack is updated before it was deleted");
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
                logger.debug("In this transaction InboundAudioTrack is updated before it was deleted");
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

    private OutboundTrack fetchOne(String trackId) {
        var model = this.storage.get(trackId);
        if (model == null) {
            return null;
        }

        return this.wrapOutboundAudioTrack(model);
    }

    private Map<String, OutboundTrack> fetchAll(Set<String> trackIds) {
        var models = this.storage.getAll(Set.copyOf(trackIds));

        if (models == null || models.isEmpty()) {
            return Collections.emptyMap();
        }
        return models.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    var model = entry.getValue();
                    return this.wrapOutboundAudioTrack(model);
                }
        ));
    }

    void checkCollidingEntries() {
        this.storage.checkCollidingEntries();
    }

    OutboundTrack wrapOutboundAudioTrack(Models.OutboundTrack model) {
        var result = new OutboundTrack(
                this.peerConnectionsRepositoryBeanProvider.get(),
                model,
                this
        );
        this.fetched.add(result.getTrackId(), result);
        return result;
    }

}
