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
public class PeerConnectionsRepository {

    private static final Logger logger = LoggerFactory.getLogger(ClientsRepository.class);

    private static final String STORAGE_ID = "observertc-peer-connections";

    private Set<String> deleted;
    private Map<String, Models.PeerConnection> updated;
    private CachedFetches<String, PeerConnection> fetched;

    private SeparatedStorage<String, Models.PeerConnection> storage;

    @Inject
    BeanProvider<ClientsRepository> clientsProvider;

    @Inject
    InboundAudioTracksRepository inboundAudioTracksRepository;

    @Inject
    InboundVideoTracksRepository inboundVideoTracksRepository;

    @Inject
    OutboundAudioTracksRepository outboundAudioTracksRepository;

    @Inject
    OutboundVideoTracksRepository outboundVideoTracksRepository;

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
                .setExpiration(config.peerConnectionsMaxIdleTime * 1000)
                .build();
        this.storage = this.hamokService.getStorageGrid().separatedStorage(baseStorage)
                .setKeyCodec(SerDeUtils.createStrToByteFunc(), SerDeUtils.createBytesToStr())
                .setValueCodec(
                        Mapper.create(Models.PeerConnection::toByteArray, logger)::map,
                        Mapper.<byte[], Models.PeerConnection>create(bytes -> Models.PeerConnection.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(bufferConfig.debouncers.maxItems)
                .setMaxCollectedStorageTimeInMs(bufferConfig.debouncers.maxTimeInMs)
                .setMaxMessageValues(1000)
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

    synchronized void deleteAll(Set<String> peerConnectionIds) {
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
            this.storage.deleteAll(this.deleted);
            var inboundAudioTrackIds = new HashSet<String>();
            var inboundVideoTrackIds = new HashSet<String>();
            var outboundAudioTrackIds = new HashSet<String>();
            var outboundVideoTrackIds = new HashSet<String>();
            var peerConnections = this.getAll(this.deleted);
            peerConnections.values().forEach(peerConnection -> {
                if (0 < peerConnection.getInboundAudioTrackIds().size()) {
                    inboundAudioTrackIds.addAll(peerConnection.getInboundVideoTrackIds());
                }
                if (0 < peerConnection.getInboundVideoTrackIds().size()) {
                    inboundVideoTrackIds.addAll(peerConnection.getInboundVideoTrackIds());
                }
                if (0 < peerConnection.getOutboundAudioTrackIds().size()) {
                    outboundAudioTrackIds.addAll(peerConnection.getOutboundAudioTrackIds());
                }
                if (0 < peerConnection.getOutboundVideoTrackIds().size()) {
                    outboundVideoTrackIds.addAll(peerConnection.getOutboundVideoTrackIds());
                }
            });
            if (0 < inboundAudioTrackIds.size()) {
                this.inboundAudioTracksRepository.deleteAll(inboundAudioTrackIds);
            }
            if (0 < inboundVideoTrackIds.size()) {
                this.inboundVideoTracksRepository.deleteAll(inboundVideoTrackIds);
            }
            if (0 < outboundAudioTrackIds.size()) {
                this.outboundAudioTracksRepository.deleteAll(outboundAudioTrackIds);
            }
            if (0 < outboundVideoTrackIds.size()) {
                this.outboundVideoTracksRepository.deleteAll(outboundVideoTrackIds);
            }
            this.deleted.clear();
        }
        if (0 < this.updated.size()) {
            this.storage.setAll(this.updated);
            this.updated.clear();
        }
        this.inboundAudioTracksRepository.save();
        this.inboundVideoTracksRepository.save();
        this.outboundAudioTracksRepository.save();
        this.outboundVideoTracksRepository.save();
        this.fetched.clear();
    }

    PeerConnection wrapPeerConnection(Models.PeerConnection model) {
        var result = new PeerConnection(
                this.clientsProvider.get(),
                model,
                this,
                this.inboundAudioTracksRepository,
                this.inboundVideoTracksRepository,
                this.outboundAudioTracksRepository,
                this.outboundVideoTracksRepository
        );
        this.fetched.add(result.getPeerConnectionId(), result);
        return result;
    }

    private PeerConnection fetchOne(String peerConnectionId) {
        var model = this.storage.get(peerConnectionId);
        if (model == null) {
            return null;
        }
        return this.wrapPeerConnection(model);
    }

    private Map<String, PeerConnection> fetchAll(Set<String> peerConnectionIds) {
        var models = this.storage.getAll(peerConnectionIds);

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
