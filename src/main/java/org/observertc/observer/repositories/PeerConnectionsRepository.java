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
public class PeerConnectionsRepository implements RepositoryStorageMetrics {

    private static final Logger logger = LoggerFactory.getLogger(ClientsRepository.class);

    private static final String STORAGE_ID = "observertc-peer-connections";
    private static final int MAX_KEYS = 1000;
    private static final int MAX_VALUES = 100;

    private Set<String> deleted;
    private Map<String, Models.PeerConnection> updated;
    private CachedFetches<String, PeerConnection> fetched;

    private SeparatedStorage<String, Models.PeerConnection> storage;

    @Inject
    BeanProvider<ClientsRepository> clientsProvider;

    @Inject
    InboundTracksRepository inboundTracksRepository;

    @Inject
    OutboundTracksRepository outboundTracksRepository;

    @Inject
    private ObserverConfig.RepositoryConfig config;

    @Inject
    private HamokService hamokService;

    @Inject
    private ObserverConfig.InternalBuffersConfig bufferConfig;


    @PostConstruct
    void setup() {
        var baseStorage = new MemoryStorageBuilder<String, Models.PeerConnection>()
                .setId(STORAGE_ID)
                .setConcurrency(true)
//                .setExpiration(config.peerConnectionsMaxIdleTime * 1000)
                .build();
        this.storage = this.hamokService.getStorageGrid().separatedStorage(baseStorage)
                .setKeyCodec(SerDeUtils.createStrToByteFunc(), SerDeUtils.createBytesToStr())
                .setValueCodec(
                        Mapper.create(Models.PeerConnection::toByteArray, logger)::map,
                        Mapper.<byte[], Models.PeerConnection>create(bytes -> Models.PeerConnection.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(bufferConfig.debouncers.maxItems)
                .setMaxCollectedStorageTimeInMs(bufferConfig.debouncers.maxTimeInMs)
                .setMaxMessageKeys(MAX_KEYS)
                .setMaxMessageValues(MAX_VALUES)
                .build();
        this.fetched = CachedFetches.<String, PeerConnection>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
        this.updated = new HashMap<>();
        this.deleted = new HashSet<>();
    }

    Observable<List<ModifiedStorageEntry<String, Models.PeerConnection>>> observableDeletedEntries() {
        return this.storage.collectedEvents().deletedEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.PeerConnection>>> observableExpiredEntries() {
        return this.storage.collectedEvents().expiredEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.PeerConnection>>> observableCreatedEntries() {
        return this.storage.collectedEvents().createdEntries();
    }

    public PeerConnection get(String peerConnectionId) {
        return this.fetched.get(peerConnectionId);
    }

    public Map<String, PeerConnection> getAll(Collection<String> peerConnectionIds) {
        if (peerConnectionIds == null || peerConnectionIds.size() < 1) {
            return Collections.emptyMap();
        }
        var set = Set.copyOf(peerConnectionIds);
        return this.fetched.getAll(set);
    }

    synchronized void update(Models.PeerConnection peerConnection) {
        this.updated.put(peerConnection.getPeerConnectionId(), peerConnection);
        var removed = this.deleted.remove(peerConnection.getPeerConnectionId());
        if (removed) {
            logger.debug("In this transaction PeerConnection is deleted before it was updated");
        }
    }

    synchronized void delete(String peerConnectionId) {
        this.deleted.add(peerConnectionId);
        var removed = this.updated.remove(peerConnectionId);
        if (removed != null) {
            logger.debug("In this transaction PeerConnection is updated before it was deleted");
        }
    }

    public synchronized void deleteAll(Set<String> peerConnectionIds) {
        if (peerConnectionIds == null || peerConnectionIds.size() < 1) {
            return;
        }
        this.deleted.addAll(peerConnectionIds);
        peerConnectionIds.forEach(peerConnectionId -> {
            var removed = this.updated.remove(peerConnectionId);
            if (removed != null) {
                logger.debug("In this transaction PeerConnection is updated before it was deleted");
            }
        });
    }

    public synchronized void save() {
        if (0 < this.deleted.size()) {
            var peerConnections = this.getAll(this.deleted);
            var inboundTrackIds = new HashSet<String>();
            var outboundTrackIds = new HashSet<String>();
            peerConnections.values().forEach(peerConnection -> {
                if (0 < peerConnection.getInboundTrackIds().size()) {
                    inboundTrackIds.addAll(peerConnection.getInboundTrackIds());
                }
                if (0 < peerConnection.getOutboundTrackIds().size()) {
                    outboundTrackIds.addAll(peerConnection.getOutboundTrackIds());
                }
            });
            if (0 < inboundTrackIds.size()) {
                this.inboundTracksRepository.deleteAll(inboundTrackIds);
            }
            if (0 < outboundTrackIds.size()) {
                this.outboundTracksRepository.deleteAll(outboundTrackIds);
            }
            Try.wrap(() -> this.storage.deleteAll(this.deleted));
            this.deleted.clear();
        }
        if (0 < this.updated.size()) {
            Try.wrap(() -> this.storage.setAll(this.updated));
            this.updated.clear();
        }
        this.inboundTracksRepository.save();
        this.outboundTracksRepository.save();
        this.fetched.clear();
    }

    public Map<String, PeerConnection> fetchRecursively(Collection<String> peerConnectionIds) {
        var result = Try.<Map<String, PeerConnection>>wrap(() -> this.getAll(peerConnectionIds), Collections.emptyMap());
        var inboundTrackIds = result.values().stream()
                .map(PeerConnection::getInboundTrackIds)
                .flatMap(s -> s.stream())
                .collect(Collectors.toSet());
        this.inboundTracksRepository.fetchRecursively(inboundTrackIds);
        var outboundTrackIds = result.values().stream()
                .map(PeerConnection::getOutboundTrackIds)
                .flatMap(s -> s.stream())
                .collect(Collectors.toSet());
        this.outboundTracksRepository.fetchRecursively(outboundTrackIds);
        return result;
    }

    @Override
    public String storageId() {
        return this.storage.getId();
    }

    @Override
    public int localSize() {
        return this.storage.localSize();
    }

    PeerConnection wrapPeerConnection(Models.PeerConnection model) {
        var result = new PeerConnection(
                this.clientsProvider.get(),
                model,
                this,
                this.inboundTracksRepository,
                this.outboundTracksRepository
        );
        this.fetched.add(result.getPeerConnectionId(), result);
        return result;
    }

    private PeerConnection fetchOne(String peerConnectionId) {
        var model = Try.wrap(() -> this.storage.get(peerConnectionId), null);
        if (model == null) {
            return null;
        }
        return this.wrapPeerConnection(model);
    }

    private Map<String, PeerConnection> fetchAll(Set<String> peerConnectionIds) {
        var models = Try.wrap(() -> this.storage.getAll(peerConnectionIds), null);

        if (models == null || models.isEmpty()) {
            return Collections.emptyMap();
        }
        return models.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    var model = entry.getValue();
                    return this.wrapPeerConnection(model);
                }
        ));
    }


}
