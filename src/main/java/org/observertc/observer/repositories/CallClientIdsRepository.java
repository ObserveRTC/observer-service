package org.observertc.observer.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.balazskreith.hamok.memorystorages.MemoryStorageBuilder;
import io.github.balazskreith.hamok.storagegrid.FederatedStorage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.mappings.SerDeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;

@Singleton
public class CallClientIdsRepository {

    private static final Logger logger = LoggerFactory.getLogger(CallClientIdsRepository.class);

    private static final String STORAGE_ID = "observertc-calls-client-ids";

    private FederatedStorage<String, Set<String>> storage;

    @Inject
    private ClientsRepository clientsRepository;

    @Inject
    private HamokService service;

    private final Map<String, Set<String>> updated = new HashMap<>();
    private final Set<String> deleted = new HashSet<>();

    @PostConstruct
    void setup() {
        var baseStorage = new MemoryStorageBuilder<String, Set<String>>()
                .setConcurrency(true)
                .setId(STORAGE_ID)
                .build();
        var mapper = new ObjectMapper();
        this.storage = this.service.getStorageGrid().federatedStorage(baseStorage)
                .setKeyCodec(SerDeUtils.createStrToByteFunc(), SerDeUtils.createBytesToStr())
                .setValueCodec(SerDeUtils.createToJson(mapper, logger), SerDeUtils.createFromJson(Set.class, mapper, logger))
                .setMergeOperator((set1, set2) -> {
                    var result = new HashSet<String>();
                    result.addAll(set1);
                    result.addAll(set2);
                    return Collections.unmodifiableSet(result);
                })
                .setMaxCollectedStorageEvents(1000)
                .setMaxCollectedStorageTimeInMs(100)
                .setMaxMessageValues(1000)
                .build();
    }

    synchronized void update(String callId, Set<String> clientIds) {
        this.updated.put(callId, clientIds);
        var removed = this.deleted.remove(callId);
        if (removed) {
            logger.debug("In this transaction, Call's clientIds was deleted before it was updated");
        }
    }

    synchronized void delete(String callId) {
        this.deleted.add(callId);
        var removed = this.updated.remove(callId);
        if (removed != null) {
            logger.debug("In this transaction, Call's clientIds was updated before it was deleted");
        }
    }

    public Map<String, Set<String>> getAll(Collection<String> callIds) {
        if (callIds == null || callIds.size() < 1) {
            return Collections.emptyMap();
        }
        var result = this.storage.getAll(Set.copyOf(callIds));

        if (result == null) {
            return Collections.emptyMap();
        }
        return result;
    }

    public Set<String> get(String callId) {
        return this.storage.get(callId);
    }

    public void save() {
        if (0 < this.deleted.size()) {
            this.storage.deleteAll(this.deleted);
            this.clientsRepository.deleteAll(this.deleted);
            this.deleted.clear();
        }
        if (0 < this.updated.size()) {
            this.storage.setAll(this.updated);
            this.updated.clear();
        }
        // this is a leaf
    }
}
