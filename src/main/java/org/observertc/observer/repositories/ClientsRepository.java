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
public class ClientsRepository {

    private static final Logger logger = LoggerFactory.getLogger(ClientsRepository.class);

    private static final String STORAGE_ID = "observertc-clients";

    private Set<String> deleted;
    private Map<String, Models.Client> updated;
    private CachedFetches<String, Client> fetched;

    private SeparatedStorage<String, Models.Client> storage;

    @Inject
    ObserverConfig.InternalBuffersConfig.DebouncersCollectorConfig buffersConfig;

    @Inject
    BeanProvider<CallsRepository> callsProvider;

    @Inject
    private ObserverConfig.RepositoryConfig config;

    @Inject
    private HamokService hamokService;

    @Inject
    private PeerConnectionsRepository peerConnectionsRepositoryRepo;

    @PostConstruct
    void setup() {
        var baseStorage = new MemoryStorageBuilder<String, Models.Client>()
                .setId(STORAGE_ID)
                .setConcurrency(true)
                .setExpiration(config.clientMaxIdleTimeInS * 1000)
                .build();
        this.storage = this.hamokService.getStorageGrid().<String, Models.Client>separatedStorage(baseStorage)
                .setKeyCodec(SerDeUtils.createStrToByteFunc(), SerDeUtils.createBytesToStr())
                .setValueCodec(
                        Mapper.create(Models.Client::toByteArray, logger)::map,
                        Mapper.<byte[], Models.Client>create(bytes -> Models.Client.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(buffersConfig.maxItems)
                .setMaxCollectedStorageTimeInMs(buffersConfig.maxTimeInMs)
                .setMaxMessageValues(1000)
                .build();
        this.fetched = CachedFetches.<String, Client>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
        this.updated = new HashMap<>();
        this.deleted = new HashSet<>();
    }

    Observable<List<ModifiedStorageEntry<String, Models.Client>>> observableDeletedEntries() {
        return this.storage.collectedEvents().deletedEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.Client>>> observableExpiredEntries() {
        return this.storage.collectedEvents().expiredEntries();
    }

    Observable<List<ModifiedStorageEntry<String, Models.Client>>> observableCreatedEntries() {
        return this.storage.collectedEvents().createdEntries();
    }

    synchronized void update(Models.Client client) {
        this.updated.put(client.getClientId(), client);
        var removed = this.deleted.remove(client.getClientId());
        if (removed) {
            logger.debug("In this transaction, Client was deleted before it was updated");
        }
    }

    synchronized void delete(String clientId) {
        this.deleted.add(clientId);
        var removed = this.updated.remove(clientId);
        if (removed != null) {
            logger.debug("In this transaction, Client was updated before it was deleted");
        }
    }

    synchronized void deleteAll(Set<String> clientIds) {
        this.deleted.addAll(clientIds);
        clientIds.forEach(clientId -> {
            var removed = this.updated.remove(clientId);
            if (removed != null) {
                logger.debug("In this transaction, Client was updated before it was deleted");
            }
        });
    }

    public synchronized void save() {
        if (0 < this.deleted.size()) {
            this.storage.deleteAll(this.deleted);
            var deletedClients = this.getAll(this.deleted);
            var peerConnectionIds = deletedClients.values().stream()
                    .map(Client::getPeerConnectionIds)
                    .flatMap(set -> set.stream())
                    .collect(Collectors.toSet());
            if (0 < peerConnectionIds.size()) {
                this.peerConnectionsRepositoryRepo.deleteAll(peerConnectionIds);
            }
            this.deleted.clear();
        }
        if (0 < this.updated.size()) {
            this.storage.setAll(this.updated);
            this.updated.clear();
        }
        this.peerConnectionsRepositoryRepo.save();
        this.fetched.clear();
    }

    public Client get(String clientId) {
        return this.fetched.get(clientId);
    }

    public Map<String, Client> getAll(Collection<String> clientIds) {
        if (clientIds == null || clientIds.size() < 1) {
            return Collections.emptyMap();
        }
        var set = Set.copyOf(clientIds);
        return this.fetched.getAll(set);
    }

    private Client fetchOne(String clientId) {
        var model = this.storage.get(clientId);
        if (model == null) {
            return null;
        }
        return this.wrapClient(model);
    }

    private Map<String, Client> fetchAll(Set<String> clientIds) {
        var models = this.storage.getAll(clientIds);

        if (models == null || models.isEmpty()) {
            return Collections.emptyMap();
        }
        return models.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    var model = entry.getValue();
                    return this.wrapClient(model);
                }
        ));
    }

    Client wrapClient(Models.Client model) {
        var result = new Client(
                this.callsProvider.get(),
                model,
                this,
                this.peerConnectionsRepositoryRepo
        );
        this.fetched.add(result.getClientId(), result);
        return result;
    }
}
