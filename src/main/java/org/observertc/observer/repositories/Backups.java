package org.observertc.observer.repositories;

import io.github.balazskreith.hamok.storagegrid.InMemoryDistributedBackups;
import io.github.balazskreith.hamok.storagegrid.backups.DistributedBackups;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.HamokService;
import org.observertc.observer.configs.ObserverConfig;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Singleton
class Backups implements DistributedBackups {

    private static final String BACKUP_STORAGE_ID = "observertc-repository-backups";

    private DistributedBackups distributedBackups;

    @Inject
    HamokService hamokService;

    @Inject
    ObserverConfig observerConfig;

    @PostConstruct
    void setup() {
//        var useBackups = this.observerConfig.repository.useBackups;
        this.distributedBackups = InMemoryDistributedBackups.builder()
                .setGrid(this.hamokService.getStorageGrid())
                .setId(BACKUP_STORAGE_ID)
                .setMaxEventCollectingTimeInMs(100)
                .setMaxEventCollectingTimeInMs(500)
                .build();
    }

    @Override
    public String getId() {
        return this.distributedBackups.getId();
    }

    @Override
    public void save(String key, String value) {
        this.distributedBackups.save(key, value);
    }

    @Override
    public Map<String, Optional<String>> loadFrom(UUID destinationEndpointId) {
        return this.distributedBackups.loadFrom(destinationEndpointId);
    }

    @Override
    public void delete(String key) {
        this.distributedBackups.delete(key);
    }

    @Override
    public void store(String key, String value, UUID sourceEndpointId) {
        this.distributedBackups.store(key, value, sourceEndpointId);
    }

    @Override
    public Map<String, String> extract(UUID endpointId) {
        return this.distributedBackups.extract(endpointId);
    }

    @Override
    public void evict(Set<String> keys, UUID endpointId) {
        this.distributedBackups.evict(keys, endpointId);
    }

    @Override
    public void close() throws Exception {
        this.distributedBackups.close();
    }
}
