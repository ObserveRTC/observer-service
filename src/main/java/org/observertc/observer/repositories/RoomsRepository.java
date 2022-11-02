package org.observertc.observer.repositories;

import io.github.balazskreith.hamok.ModifiedStorageEntry;
import io.github.balazskreith.hamok.memorystorages.MemoryStorageBuilder;
import io.github.balazskreith.hamok.storagegrid.ReplicatedStorage;
import io.reactivex.rxjava3.core.Observable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.HamokService;
import org.observertc.observer.common.Try;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.mappings.Mapper;
import org.observertc.observer.samples.ServiceRoomId;
import org.observertc.schemas.dtos.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class RoomsRepository implements RepositoryStorageMetrics {

    private static final Logger logger = LoggerFactory.getLogger(RoomsRepository.class);
    private static final int MAX_KEYS = 1000;
    private static final int MAX_VALUES = 100;

    private static final String STORAGE_ID = "observertc-rooms";

    public record CreateRoomInfo(
      ServiceRoomId serviceRoomId,
      String callId
    ) {

    }

    private ReplicatedStorage<ServiceRoomId, Models.Room> storage;
    private Map<ServiceRoomId, Models.Room> updated;
    private CachedFetches<ServiceRoomId, Room> fetched;

    @Inject
    private HamokService service;

    @Inject
    private CallsRepository callsRepository;

    @Inject
    private ObserverConfig.InternalBuffersConfig bufferConfig;

    @PostConstruct
    void setup() {
        var baseStorage = new MemoryStorageBuilder<ServiceRoomId, Models.Room>()
                .setConcurrency(true)
                .setId(STORAGE_ID)
                .build();
        this.storage = this.service.getStorageGrid().replicatedStorage(baseStorage)
                .setKeyCodec(ServiceRoomId::toBytes, ServiceRoomId::fromBytes)
                .setValueCodec(
                        Mapper.create(Models.Room::toByteArray, logger)::map,
                        Mapper.<byte[], Models.Room>create(bytes -> Models.Room.parseFrom(bytes), logger)::map
                )
                .setMaxCollectedStorageEvents(bufferConfig.debouncers.maxItems)
                .setMaxCollectedStorageTimeInMs(bufferConfig.debouncers.maxTimeInMs)
                .setMaxMessageKeys(MAX_KEYS)
                .setMaxMessageValues(MAX_VALUES)
                .build();


        this.fetched = CachedFetches.<ServiceRoomId, Room>builder()
                .onFetchOne(this::fetchOne)
                .onFetchAll(this::fetchAll)
                .build();
        this.updated = new HashMap<>();
    }

    Observable<List<ModifiedStorageEntry<ServiceRoomId, Models.Room>>> observableDeletedEntries() {
        return this.storage.collectedEvents().deletedEntries();
    }

    Observable<List<ModifiedStorageEntry<ServiceRoomId, Models.Room>>> observableCreatedEntries() {
        return this.storage.collectedEvents().createdEntries();
    }

    public Map<ServiceRoomId, Room> fetchRecursively(Set<ServiceRoomId> serviceRoomIds) {
        var result = this.getAll(serviceRoomIds);
        var callIds = result.values().stream()
                .map(Room::getCallId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        this.callsRepository.fetchRecursively(callIds);
        return result;
    }

    /**
     * Tries to insert calls, and returns with the NOT inserted one if it is already inserted
     * @return
     */
    public Map<ServiceRoomId, Room> insertAll(Map<ServiceRoomId, String> callIds) {
        if (callIds == null || callIds.size() < 1) {
            return Collections.emptyMap();
        }
        var proposedModels = callIds.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    var proposedCallId = Utils.firstNotNull(entry.getValue(), UUID.randomUUID().toString());
                    var serviceRoomId = entry.getKey();
                    var builder = Models.Room.newBuilder()
                            .setServiceId(serviceRoomId.serviceId)
                            .setRoomId(serviceRoomId.roomId)
                            .setCallId(proposedCallId)
                            ;

                    return builder.build();
                }
        ));
        var notInsertedModels = Try.wrap(() -> this.storage.insertAll(proposedModels), null);
        if (notInsertedModels == null) {
            return Collections.emptyMap();
        }
        return notInsertedModels.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> this.wrapRoom(entry.getValue())
        ));
    }

    public Set<ServiceRoomId> removeAll(Set<ServiceRoomId> serviceRoomIds) {
        var rooms = this.getAll(serviceRoomIds);
        var callIds = rooms.values().stream().map(call -> call.getCallId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        var result = Try.<Set<ServiceRoomId>>wrap(() -> this.storage.deleteAll(serviceRoomIds), Collections.emptySet());
        synchronized (this) {
            serviceRoomIds.forEach(this.updated::remove);
        }
        this.callsRepository.removeAll(callIds);
        this.fetched.clear();
        return result;
    }

    public Room get(ServiceRoomId serviceRoomId) {
        return this.fetched.get(serviceRoomId);
    }

    public Map<ServiceRoomId, Room> getAll(Collection<ServiceRoomId> serviceRoomIds) {
        if (serviceRoomIds == null || serviceRoomIds.size() < 1) {
            return Collections.emptyMap();
        }
        return this.fetched.getAll(Set.copyOf(serviceRoomIds));
    }

    public void save() {
        synchronized (this) {
            if (0 < this.updated.size()) {
                Try.wrap(() -> this.storage.setAll(this.updated));
            }
            this.updated.clear();
        }
        this.callsRepository.save();
        this.fetched.clear();
    }

    private Room wrapRoom(Models.Room model) {
        var result = new Room(
                model,
                this,
                this.callsRepository
        );
        this.fetched.add(result.getServiceRoomId(), result);
        return result;
    }

    synchronized void update(Models.Room room) {
        var serviceRoomId = ServiceRoomId.make(room.getServiceId(), room.getRoomId());
        this.updated.put(serviceRoomId, room);
    }

    private Room fetchOne(ServiceRoomId serviceRoomId) {
        var model = Try.wrap(() -> this.storage.get(serviceRoomId), null);
        if (model == null) {
            return null;
        }
        return this.wrapRoom(model);
    }

    private Map<ServiceRoomId, Room> fetchAll(Set<ServiceRoomId> serviceRoomIds) {
        var models = Try.wrap(() -> this.storage.getAll(serviceRoomIds), null);

        if (models == null || models.isEmpty()) {
            return Collections.emptyMap();
        }
        return models.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    var model = entry.getValue();
                    return this.wrapRoom(model);
                }
        ));
    }

    @Override
    public String storageId() {
        return this.storage.getId();
    }

    @Override
    public int localSize() {
        return this.storage.localSize();
    }
}
