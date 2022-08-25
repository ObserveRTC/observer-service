package org.observertc.observer.repositories;

import io.github.balazskreith.hamok.storagegrid.StorageGrid;
import io.github.balazskreith.hamok.transports.Endpoint;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.repositories.endpoints.EndpointBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;

@Singleton
public class HamokService {

    private static final Logger logger = LoggerFactory.getLogger(HamokService.class);

    @Inject
    ObserverConfig config;

    @Inject
    CoreV1Api coreV1Api;

    private Endpoint endpoint;

    private StorageGrid storageGrid;

    @PostConstruct
    void setup() {
        var storageGrid = StorageGrid.builder()
                .withContext()
                .withAutoDiscovery(true)
                .withRaftMaxLogRetentionTimeInMs()
                .build();
        var endpointBuilder = new EndpointBuilderImpl();
        endpointBuilder.setBeans(Map.of(
                coreV1Api.getClass(), coreV1Api
        ));
        endpointBuilder.setEndpointId();
        logger.info("Hamok is ready");
    }

    @PreDestroy
    void teardown() {
        logger.info("Closed");
    }


    public StorageGrid getStorageGrid() {
        return this.storageGrid;
    }
}
