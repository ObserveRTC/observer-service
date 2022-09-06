package org.observertc.observer;

import io.github.balazskreith.hamok.storagegrid.StorageGrid;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.env.MapPropertySource;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.management.endpoint.info.InfoSource;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.hamokendpoints.BuildersEssentials;
import org.observertc.observer.hamokendpoints.EndpointBuilderImpl;
import org.observertc.observer.hamokendpoints.HamokEndpoint;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

@Singleton
public class HamokService  implements InfoSource {

    private static final Logger logger = LoggerFactory.getLogger(HamokService.class);

    @Inject
    ObserverConfig.HamokConfig config;

    @Inject
    BeanProvider<CoreV1Api> coreV1ApiProvider;

    private volatile boolean running = false;
    private HamokEndpoint endpoint;
    private StorageGrid storageGrid;
    private Set<UUID> remotePeers = Collections.synchronizedSet(new HashSet<>());

    @PostConstruct
    private void setup() {
        var storageGridConfig = this.config.storageGrid;
        var memberName = this.getRandomMemberName();
        this.storageGrid = StorageGrid.builder()
                .withAutoDiscovery(true)
                .withContext(memberName)
                .withRaftMaxLogRetentionTimeInMs(storageGridConfig.raftMaxLogEntriesRetentionTimeInMinutes * 60 * 1000)
                .withApplicationCommitIndexSyncTimeoutInMs(storageGridConfig.applicationCommitIndexSyncTimeoutInMs)
                .withHeartbeatInMs(storageGridConfig.heartbeatInMs)
                .withFollowerMaxIdleInMs(storageGridConfig.followerMaxIdleInMs)
                .withPeerMaxIdleTimeInMs(storageGridConfig.peerMaxIdleInMs)
                .withRequestTimeoutInMs(storageGridConfig.requestTimeoutInMs)
                .withSendingHelloTimeoutInMs(storageGridConfig.sendingHelloTimeoutInMs)
                .withAutoDiscovery(true)
                .build();

        this.storageGrid.joinedRemoteEndpoints()
                .subscribe(endpointId -> {
                    remotePeers.add(endpointId);
                });
        this.storageGrid.detachedRemoteEndpoints()
                .subscribe(endpointId -> {
                    remotePeers.remove(endpointId);
                });
        var endpointBuilder = new EndpointBuilderImpl();
        endpointBuilder.setBuildingEssentials(new BuildersEssentials(
                this.coreV1ApiProvider,
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

    @Override
    public Publisher<PropertySource> getSource() {
        PropertySource propertySource = new MapPropertySource("ready", Map.of("ready", true));
        return Publishers.just(
            propertySource
        );
    }

    public boolean isReady() {
        if (!this.endpoint.isReady()) {
            return false;
        }
        var sentTwoTimesHello = (2 * this.config.storageGrid.sendingHelloTimeoutInMs) / 1000;
        if (this.endpoint.elapsedSecSinceReady() < sentTwoTimesHello) {
            return false;
        }
        if (0 < this.remotePeers.size() && this.storageGrid.getLeaderId() == null) {
            return false;
        }
        return true;
    }

    public String getRandomMemberName() {
        var memberNamesPool = this.config.memberNamesPool;
        if (memberNamesPool == null || memberNamesPool.size() < 1) {
            return "The One";
        }
        int index = Math.abs(((int)  UUID.randomUUID().getMostSignificantBits()) % memberNamesPool.size());
        var result = this.config.memberNamesPool.get(index);
        return result;
    }
}
