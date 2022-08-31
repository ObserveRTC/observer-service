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
public class InboundVideoTracksRepository {

    private static final Logger logger = LoggerFactory.getLogger(InboundVideoTracksRepository.class);

    private static final String STORAGE_ID = "observertc-inbound-video-tracks";

    private Set<String> deleted;
    private Map<String, Models.InboundVideoTrack> updated;

    private SeparatedStorage<String, Models.InboundVideoTrack> storage;
    private CachedFetches<String, InboundVideoTrack> fetched;

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
        var baseStorage = new MemoryStorageBuilder<String, Models.InboundVideoTrack>()
                .setId(STORAGE_ID)
                .setConcurrency(true)
                .setExpiration(config.mediaTracksMaxIdleTimeInS * 1000)
                .build();
        this.storage = this.hamokService.getStorageGrid().separatedStorage(baseStorage)
                .setKeyCodec(SerDeUtils.createStrToByteFunc(), SerDeUtils.createBytesToStr())
                .setValueCodec(
                        Mapper.create(Models.InboundVideoTrack::toByteArray, logger)::map,
                        Mapper.<byte[], Models.InboundVideoTrack>create(bytes -> Models.InboundVideoTrack.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(bufferConfig.debouncers.maxItems)
                .setMaxCollectedStorageTimeInMs(bufferConfig.debouncers.maxTimeInMs)
                .setMaxMessageValues(1000)
                .build();
        this.fetched = CachedFetches.<String, InboundVideoTrack>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
        this.updated = new HashMap<>();
        this.deleted = new HashSet<>();
    }

    Observable<List<ModifiedStorageEntry<String, Models.InboundVideoTrack>>> observableDeletedEntries() {
        return this.storage.collectedEvents().deletedEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.InboundVideoTrack>>> observableExpiredEntries() {
        return this.storage.collectedEvents().expiredEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.InboundVideoTrack>>> observableCreatedEntries() {
        return this.storage.collectedEvents().createdEntries();
    }

    public InboundVideoTrack get(String trackId) {
        return this.fetched.get(trackId);
    }

    public Map<String, InboundVideoTrack> getAll(Collection<String> trackIds) {
        if (trackIds == null || trackIds.size() < 1) {
            return Collections.emptyMap();
        }
        var set = Set.copyOf(trackIds);
        return this.fetched.getAll(set);
    }


    synchronized void update(Models.InboundVideoTrack InboundVideoTrack) {
        this.updated.put(InboundVideoTrack.getPeerConnectionId(), InboundVideoTrack);
        var removed = this.deleted.remove(InboundVideoTrack.getTrackId());
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
            this.storage.deleteAll(this.deleted);
            this.deleted.clear();
        }
        if (0 < this.updated.size()) {
            this.storage.setAll(this.updated);
            this.updated.clear();
        }
        this.fetched.clear();
    }

    InboundVideoTrack wrapInboundVideoTrack(Models.InboundVideoTrack model) {
        var result =  new InboundVideoTrack(
                this.peerConnectionsRepositoryBeanProvider.get(),
                model,
                this
        );
        this.fetched.add(result.getTrackId(), result);
        return result;
    }

    private InboundVideoTrack fetchOne(String trackId) {
        var model = this.storage.get(trackId);
        if (model == null) {
            return null;
        }
        return this.wrapInboundVideoTrack(model);
    }

    private Map<String, InboundVideoTrack> fetchAll(Set<String> trackIds) {
        var models = this.storage.getAll(Set.copyOf(trackIds));

        if (models == null || models.isEmpty()) {
            return Collections.emptyMap();
        }
        return models.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    var model = entry.getValue();
                    return this.wrapInboundVideoTrack(model);
                }
        ));
    }
}
