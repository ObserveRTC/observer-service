package org.observertc.observer;

import io.github.balazskreith.hamok.storagegrid.StorageGrid;
import io.github.balazskreith.hamok.transports.Endpoint;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.hamokendpoints.BuildersEssentials;
import org.observertc.observer.hamokendpoints.EndpointBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Singleton
public class HamokService {

    private static final Logger logger = LoggerFactory.getLogger(HamokService.class);

    @Inject
    ObserverConfig.HamokConfig config;

    @Inject
    CoreV1Api coreV1Api;

    private volatile boolean running = false;
    private Endpoint endpoint;
    private StorageGrid storageGrid;

    @PostConstruct
    private void setup() {
        this.storageGrid = StorageGrid.builder()
                .withAutoDiscovery(true)
                .withRaftMaxLogRetentionTimeInMs(this.config.raftMaxLogRetentionTimeInMs)
                .withApplicationCommitIndexSyncTimeoutInMs(this.config.applicationCommitIndexSyncTimeout)
                .withHeartbeatInMs(this.config.heartbeatInMs)
                .withFollowerMaxIdleInMs(this.config.followerMaxIdleInMs)
                .withPeerMaxIdleTimeInMs(this.config.peerMaxIdleInMs)
                .withRequestTimeoutInMs(this.config.requestTimeoutInMs)
                .withAutoDiscovery(true)
                .build();
        var endpointBuilder = new EndpointBuilderImpl();
        endpointBuilder.setBuildingEssentials(new BuildersEssentials(
                coreV1Api,
                this.storageGrid.getLocalEndpointId()
        ));
        endpointBuilder.withConfiguration(this.config.endpoint);
        this.endpoint = endpointBuilder.build();

        this.endpoint.inboundChannel().subscribe(this.storageGrid.transport().getReceiver());
        this.storageGrid.transport().getSender().subscribe(this.endpoint.outboundChannel());

        logger.info("Hamok is ready");
    }

    @PreDestroy
    private void teardown() {
        logger.info("Closed");
    }

    void start() {
        if (this.running) {
            logger.warn("Attempted to start twice");
            return;
        }
        this.running = true;
        this.endpoint.start();
    }

    boolean isRunning() {
        return this.running;
    }

    void stop() {
        if (!this.running) {
            return;
        }
        this.running = false;
        if (this.endpoint.isRunning()) {
            this.endpoint.stop();
        }
    }

    public StorageGrid getStorageGrid() {
        return this.storageGrid;
    }
}
