package org.observertc.observer.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.balazskreith.hamok.storagegrid.PropagatedCollections;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.HamokService;
import org.observertc.observer.mappings.Mapper;
import org.observertc.observer.mappings.SerDeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class CallClientIdsRepository implements RepositoryStorageMetrics {

    private static final Logger logger = LoggerFactory.getLogger(CallClientIdsRepository.class);

    private static final String PROPAGATED_COLLECTION_ID = "observertc-calls-client-ids";
    private static final int MAX_VALUES = 100;

    private PropagatedCollections<String, String, Set<String>> collections;

    @Inject
    private ClientsRepository clientsRepository;

    @Inject
    private HamokService service;

    private final Map<String, Set<String>> updated = new HashMap<>();
    private final Map<String, Set<String>> deleted = new HashMap<>();

    @PostConstruct
    void setup() {
        var mapper = new ObjectMapper();
        this.collections = this.service.getStorageGrid().<String, String>propagatedSets()
                .setGridEndpointId(PROPAGATED_COLLECTION_ID)
                .setKeyCodec(SerDeUtils.createStrToByteFunc(), SerDeUtils.createBytesToStr())
                .setCollectionCodec(
                        Mapper.<Set, byte[]>create(mapper::writeValueAsBytes, logger)::map,
                        Mapper.<byte[], Set>create(bytes -> mapper.readValue(bytes, Set.class), logger)::map
                )
                .setMaxMessageValues(MAX_VALUES)
                .build();

    }

    synchronized void add(String callId, String clientId) {
        // extend update
        var updatedClientIds = this.updated.get(callId);
        if (updatedClientIds == null) {
            updatedClientIds = new HashSet<>();
            this.updated.put(callId, updatedClientIds);
        }
        updatedClientIds.add(clientId);

        // check removals
        var deletedClientIds = this.deleted.get(callId);
        if (deletedClientIds != null) {
            var removed = deletedClientIds.remove(clientId);
            if (removed) {
                logger.debug("In this transaction, Call's clientIds was deleted before it was updated");
            }
            return;
        }

    }

    synchronized void delete(String callId, String clientId) {
        // extend deletion
        var deletedClientIds = this.deleted.get(callId);
        if (deletedClientIds == null) {
            deletedClientIds = new HashSet<>();
            this.deleted.put(callId, deletedClientIds);
        }
        deletedClientIds.add(clientId);

        // check updates
        var updatedClientIds = this.updated.get(callId);
        if (updatedClientIds != null) {
            var removed = updatedClientIds.remove(clientId);
            if (removed) {
                logger.debug("In this transaction, Call's clientIds was updated before it was deleted");
            }
            return;
        }
    }

    public Map<String, Set<String>> getAll(Collection<String> callIds) {
        if (callIds == null || callIds.size() < 1) {
            return Collections.emptyMap();
        }
        var result = this.collections.getAll(Set.copyOf(callIds));

        if (result == null) {
            return Collections.emptyMap();
        }
        return result;
    }

    public Set<String> get(String callId) {
        return this.collections.get(callId);
    }

    public void save() {
        if (0 < this.deleted.size()) {
            this.collections.removeAll(this.deleted);
            var clientIds = this.deleted.values()
                    .stream()
                    .flatMap(s -> s.stream())
                    .collect(Collectors.toSet());
            this.clientsRepository.deleteAll(clientIds);
            this.deleted.clear();
        }
        if (0 < this.updated.size()) {
            this.collections.addAll(this.updated);
            this.updated.clear();
        }
        this.clientsRepository.save();
        // this is a leaf
    }

    @Override
    public String storageId() {
        return PROPAGATED_COLLECTION_ID;
    }

    @Override
    public int localSize() {
        return this.collections.size();
    }
}
