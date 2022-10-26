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
import org.observertc.observer.hamokendpoints.HamokEndpoint;
import org.observertc.observer.hamokendpoints.HamokEndpointBuilderService;
import org.observertc.observer.repositories.RepositorySweeper;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

@Singleton
public class HamokService  implements InfoSource {

    public static final UUID localEndpointId = UUID.randomUUID();

    private static final Logger logger = LoggerFactory.getLogger(HamokService.class);

    @Inject
    ObserverConfig.HamokConfig config;

    @Inject
    BeanProvider<RepositorySweeper> repositorySweeperBeanProvider;

    @Inject
    BeanProvider<CoreV1Api> coreV1ApiProvider;

    @Inject
    HamokEndpointBuilderService hamokEndpointBuilderService;

    private volatile boolean running = false;
    private HamokEndpoint endpoint;
    private StorageGrid storageGrid;
    private Set<UUID> remotePeers = Collections.synchronizedSet(new HashSet<>());

    @PostConstruct
    private void setup() {
        var storageGridConfig = this.config.storageGrid;
        var memberName = this.getRandomMemberName();
        this.storageGrid = StorageGrid.builder()
                .withContext(memberName)
                .withRaftMaxLogRetentionTimeInMs(storageGridConfig.raftMaxLogEntriesRetentionTimeInMinutes * 60 * 1000)
                .withApplicationCommitIndexSyncTimeoutInMs(storageGridConfig.applicationCommitIndexSyncTimeoutInMs)
                .withHeartbeatInMs(storageGridConfig.heartbeatInMs)
                .withFollowerMaxIdleInMs(storageGridConfig.followerMaxIdleInMs)
                .withPeerMaxIdleTimeInMs(storageGridConfig.peerMaxIdleInMs)
                .withRequestTimeoutInMs(storageGridConfig.requestTimeoutInMs)
                .withSendingHelloTimeoutInMs(storageGridConfig.sendingHelloTimeoutInMs)
                .withLocalEndpointId(localEndpointId)
                .withAutoDiscovery(false)
                .build();

        this.storageGrid.events().joinedRemoteEndpoints()
                .subscribe(endpointId -> {
                    logger.info("Endpoint {} joined to StorageGrid", endpointId);
                    remotePeers.add(endpointId);
                });
        this.storageGrid.events().detachedRemoteEndpoints()
                .subscribe(endpointId -> {
                    logger.info("Endpoint {} detached from StorageGrid", endpointId);
                    remotePeers.remove(endpointId);
                });
        this.storageGrid.errors().subscribe(err -> {
            logger.warn("Error occurred in storageGrid. Code: {}", err.getCode(), err.getException());
        });
    }

    @PreDestroy
    private void teardown() {
        if (this.repositorySweeperBeanProvider.get().isStarted()) {
            this.repositorySweeperBeanProvider.get().stop();
        }
        logger.info("Closed");
    }

    void start() {
        if (this.running) {
            logger.warn("Attempted to start twice");
            return;
        }
        this.running = true;
        if (this.endpoint == null) {
            this.endpoint = this.hamokEndpointBuilderService.build(this.config.endpoint);

            if (this.endpoint != null) {
                this.endpoint.inboundChannel().subscribe(this.storageGrid.transport().getReceiver());
                this.storageGrid.transport().getSender().subscribe(this.endpoint.outboundChannel());
                this.endpoint.remoteEndpointJoined().subscribe(remoteEndpointId -> {
                    this.storageGrid.addRemoteEndpointId(remoteEndpointId);
                });
                this.endpoint.remoteEndpointDetached().subscribe(remoteEndpointId -> {
                    this.storageGrid.removeRemoteEndpointId(remoteEndpointId);
                });
                this.endpoint.start();
            } else {
                logger.warn("Endpoint for hamok has not been built, the server cannot share its internal data with other instances in the grid");
            }
        } else {
            this.endpoint.start();
        }

        logger.info("Hamok is started");
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

    private int alreadyLoggedFlags = 0;
    public boolean isReady() {
        if (!this.endpoint.isReady()) {
            if ((this.alreadyLoggedFlags & 1) == 0) {
                logger.info("Waiting for endpoint to be ready");
                this.alreadyLoggedFlags = 1;
            }
            return false;
        }

        var sentTwoTimesHello = (2 * this.config.storageGrid.sendingHelloTimeoutInMs) / 1000;
        var elapsedSecSinceReady = this.endpoint.elapsedSecSinceReady();
        if ((this.alreadyLoggedFlags & 2) == 0) {
            logger.info("Observer service endpoint is ready");
            logger.info("Elapsed secs since endpoint is ready {}s, we must wait for two HELLO packets ({}s) before we check the next stage ", elapsedSecSinceReady, sentTwoTimesHello);
            this.alreadyLoggedFlags += 2;
        }

        if (elapsedSecSinceReady < sentTwoTimesHello) {
            return false;
        }
        if ((this.alreadyLoggedFlags & 4) == 0) {
            logger.info("Number of remote endpoints {}, leader {}", this.remotePeers.size(), this.storageGrid.endpoints().getLeaderEndpointId());
            this.alreadyLoggedFlags += 4;
        }

        if (0 < this.remotePeers.size() && this.storageGrid.endpoints().getLeaderEndpointId() == null) {
            return false;
        }
        if ((this.alreadyLoggedFlags & 8) == 0) {
            logger.info("Ready");
            this.alreadyLoggedFlags += 8;
        }
        this.alreadyLoggedFlags = this.alreadyLoggedFlags & 0xFE;
        if (!this.repositorySweeperBeanProvider.get().isStarted()) {
            this.repositorySweeperBeanProvider.get().start();
        }
        return true;
    }

    private String getRandomMemberName() {
        var memberNamesPool = this.config.memberNamesPool;
        if (memberNamesPool == null || memberNamesPool.size() < 1) {
            return "The One";
        }
        int index = Math.abs(((int)  UUID.randomUUID().getMostSignificantBits()) % memberNamesPool.size());
        var result = this.config.memberNamesPool.get(index);
        return result;
    }

    private void checkCollidingEntries() {

    }
}
