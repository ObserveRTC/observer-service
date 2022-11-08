package org.observertc.observer.repositories;

import io.github.balazskreith.hamok.ModifiedStorageEntry;
import io.github.balazskreith.hamok.memorystorages.MemoryStorageBuilder;
import io.github.balazskreith.hamok.storagegrid.SeparatedStorage;
import io.micronaut.context.BeanProvider;
import io.reactivex.rxjava3.core.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.BackgroundTasksExecutor;
import org.observertc.observer.HamokService;
import org.observertc.observer.common.ChainedTask;
import org.observertc.observer.common.Try;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.mappings.Mapper;
import org.observertc.observer.mappings.SerDeUtils;
import org.observertc.schemas.dtos.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Singleton
public class ClientsRepository implements RepositoryStorageMetrics  {

    private static final Logger logger = LoggerFactory.getLogger(ClientsRepository.class);

    private static final String STORAGE_ID = "observertc-clients";
    private static final int MAX_KEYS = 1000;
    private static final int MAX_VALUES = 100;

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

    @Inject
    private BackgroundTasksExecutor backgroundTasksExecutor;

    @PostConstruct
    void setup() {
        var baseStorage = new MemoryStorageBuilder<String, Models.Client>()
                .setId(STORAGE_ID)
                .setConcurrency(true)
//                .setExpiration(config.clientMaxIdleTimeInS * 1000, Schedulers.io())
                .build();
        this.storage = this.hamokService.getStorageGrid().<String, Models.Client>separatedStorage(baseStorage)
                .setKeyCodec(SerDeUtils.createStrToByteFunc(), SerDeUtils.createBytesToStr())
                .setValueCodec(
                        Mapper.create(Models.Client::toByteArray, logger)::map,
                        Mapper.<byte[], Models.Client>create(bytes -> Models.Client.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(buffersConfig.maxItems)
                .setMaxCollectedStorageTimeInMs(buffersConfig.maxTimeInMs)
                .setMaxMessageKeys(MAX_KEYS)
                .setMaxMessageValues(MAX_VALUES)
                .build();

        var checkCollision = new AtomicBoolean(false);
        this.storage.detectedEntryCollisions().subscribe(detectedCollision -> {
            logger.warn("Detected colliding items in {} for key {}", STORAGE_ID, detectedCollision.key());
            if (checkCollision.compareAndSet(false, true)) {
                this.backgroundTasksExecutor.addTask(ChainedTask.<Void>builder()
                        .withName("Remove collisions for storage: " + STORAGE_ID)
                        .withLogger(logger)
                        .addActionStage("Check collision for " + STORAGE_ID, this.storage::checkCollidingEntries)
                        .setFinalAction(() -> checkCollision.set(false))
                        .build()
                );
            }
        });

        this.fetched = CachedFetches.<String, Client>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
        this.updated = new HashMap<>();
        this.deleted = new HashSet<>();
    }

    public Observable<List<ModifiedStorageEntry<String, Models.Client>>> observableDeletedEntries() {
        return this.storage.collectedEvents().deletedEntries();
    }

    public Observable<List<ModifiedStorageEntry<String, Models.Client>>> observableExpiredEntries() {
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

    public synchronized void deleteAll(Set<String> clientIds) {
        if (clientIds == null || clientIds.size() < 1) {
            return;
        }
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
            var deletedClients = this.getAll(this.deleted);
            var peerConnectionIds = deletedClients.values().stream()
                    .map(Client::getPeerConnectionIds)
                    .flatMap(set -> set.stream())
                    .collect(Collectors.toSet());
            if (0 < peerConnectionIds.size()) {
                this.peerConnectionsRepositoryRepo.deleteAll(peerConnectionIds);
            }
            Try.wrap(() -> this.storage.deleteAll(this.deleted));
            this.deleted.clear();
        }
        if (0 < this.updated.size()) {
            Try.wrap(() -> this.storage.setAll(this.updated));
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

    public Map<String, Client> fetchRecursively(Collection<String> clientIds) {
        if (clientIds == null || clientIds.size() < 1) {
            return Collections.emptyMap();
        }
        var clients = Try.<Map<String, Client>>wrap(() -> this.getAll(clientIds), Collections.emptyMap());
        var peerConnectionIds = clients.values().stream()
                .map(Client::getPeerConnectionIds)
                .flatMap(s -> s.stream())
                .collect(Collectors.toSet());
        this.peerConnectionsRepositoryRepo.fetchRecursively(peerConnectionIds);
        return clients;
    }

    public Map<String, Client> fetchRecursivelyUpwards(Collection<String> clientIds) {
        if (clientIds == null || clientIds.size() < 1) {
            return Collections.emptyMap();
        }
        var clients = Try.<Map<String, Client>>wrap(() -> this.getAll(clientIds), Collections.emptyMap());
        var callIds = clients.values().stream()
                .map(Client::getCallId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        this.callsProvider.get().fetchRecursivelyUpwards(callIds);
        return clients;
    }

    public void clearStorage() {
        this.storage.clear();
    }

    @Override
    public String storageId() {
        return this.storage.getId();
    }

    @Override
    public int localSize() {
        return this.storage.localSize();
    }

    private Client fetchOne(String clientId) {
        var model = Try.wrap(() -> this.storage.get(clientId), null);
        if (model == null) {
            return null;
        }
        return this.wrapClient(model);
    }

    private Map<String, Client> fetchAll(Set<String> clientIds) {
        var models = Try.wrap(() -> this.storage.getAll(clientIds), null);

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

    void checkCollidingEntries() {
        this.storage.checkCollidingEntries();
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
