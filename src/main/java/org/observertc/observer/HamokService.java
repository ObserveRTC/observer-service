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
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.hamokendpoints.HamokEndpoint;
import org.observertc.observer.hamokendpoints.HamokEndpointBuilderService;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class HamokService  implements InfoSource {

    public static final UUID localEndpointId = UUID.randomUUID();

    private static final Logger logger = LoggerFactory.getLogger(HamokService.class);

    @Inject
    ObserverConfig.HamokConfig config;

    @Inject
    BeanProvider<CoreV1Api> coreV1ApiProvider;

    @Inject
    HamokEndpointBuilderService hamokEndpointBuilderService;

//    @Inject
//    Sandbox sandbox;

    private volatile boolean running = false;
    private final AtomicReference<HamokEndpoint> endpointHolder = new AtomicReference<>();
    private StorageGrid storageGrid;
    private Set<UUID> remotePeers = Collections.synchronizedSet(new HashSet<>());

    @PostConstruct
    private void setup() {
        var storageGridConfig = this.config.storageGrid;
        var memberName = this.getRandomMemberName();
        this.storageGrid = StorageGrid.builder()
                .withContext(memberName)
                .withRaftMaxLogRetentionTimeInMs(storageGridConfig.raftMaxLogEntriesRetentionTimeInMinutes * 60 * 1000)
                .withHeartbeatInMs(storageGridConfig.heartbeatInMs)
                .withFollowerMaxIdleInMs(storageGridConfig.followerMaxIdleInMs)
                .withPeerMaxIdleTimeInMs(storageGridConfig.peerMaxIdleInMs)
                .withRequestTimeoutInMs(storageGridConfig.requestTimeoutInMs)
                .withSendingHelloTimeoutInMs(storageGridConfig.sendingHelloTimeoutInMs)
                .withLocalEndpointId(localEndpointId)
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
        this.storageGrid.events().inactiveEndpoints()
                .subscribe(endpointId -> {
                    logger.info("Endpoint {} is reported to be inactive", endpointId);
                    remotePeers.remove(endpointId);
                });

        this.storageGrid.errors().subscribe(err -> {
            logger.warn("Error occurred in storageGrid. Code: {}", err.getCode(), err.getException());
        });


    }

    public void refreshRemoteEndpointId() {
        var endpoint = this.endpointHolder.get();
        var storageGrid = this.storageGrid;
        if (storageGrid == null || endpoint == null) {
            logger.warn("To refresh a remote endpoint storageGrid end endpoint must exists");
            return;
        }
        var currentRemoteEndpointIds = storageGrid.endpoints().getRemoteEndpointIds();
        var activeRemoteEndpointIds = endpoint.getActiveRemoteEndpointIds();
        logger.info("Updating remote endpoint ids. Current remote endpointIds: {}, active remote endpoint ids: {}",
                JsonUtils.objectToString(currentRemoteEndpointIds),
                JsonUtils.objectToString(activeRemoteEndpointIds)
        );
        for (var activeRemoteEndpointId : activeRemoteEndpointIds) {
            if (currentRemoteEndpointIds.contains(activeRemoteEndpointId)) {
                continue;
            }
            storageGrid.addRemoteEndpointId(activeRemoteEndpointId);
        }
        for (var currentRemoteEndpointId : currentRemoteEndpointIds) {
            if (activeRemoteEndpointIds.contains(currentRemoteEndpointId)) {
                continue;
            }
            storageGrid.removeRemoteEndpointId(currentRemoteEndpointId);
        }
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
        var endpoint = this.endpointHolder.get();
        if (this.endpointHolder.get() == null) {
            endpoint = this.hamokEndpointBuilderService.build(this.config.endpoint);

            if (endpoint != null) {
                endpoint.inboundChannel().subscribe(this.storageGrid.transport().getReceiver());
                this.storageGrid.transport().getSender().subscribe(endpoint.outboundChannel());
                if (this.endpointHolder.compareAndSet(null, endpoint)) {
                    endpoint.start();
                } else {
                    endpoint.stop();
                }
            } else {
                logger.warn("Endpoint for hamok has not been built, the server cannot share its internal data with other instances in the grid");
            }
        } else if (!endpoint.isRunning()){
            endpoint.start();
        }

        logger.info("Hamok is started");

//        logger.warn("DEBUG PURPUSE ADD ONE EXTRA NOT EXISTING ENDPOINT");
//        this.storageGrid.addRemoteEndpointId(UUID.randomUUID());
    }

    boolean isRunning() {
        return this.running;
    }

    void stop() {
        if (!this.running) {
            return;
        }
        this.running = false;
        var endpoint = this.endpointHolder.get();
        if (endpoint != null && endpoint.isRunning()) {
            endpoint.stop();
        }
        this.endpointHolder.set(null);
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

    private boolean remotePeersWereReady = false;
    public boolean areRemotePeersReady() {
        if (this.remotePeersWereReady) {
            return true;
        }
        var endpoint = this.endpointHolder.get();
        if (endpoint == null) {
            this.remotePeersWereReady = true;
            return true;
        }

        if (0 < this.config.minRemotePeers) {
            var remotePeersNum = Utils.firstNotNull(endpoint.getActiveRemoteEndpointIds(), Collections.emptySet()).size();
            if (remotePeersNum < this.config.minRemotePeers) {
                logger.info("Waiting for remote peers to be ready. Minimum number of peers must be ready is {}, currently there are {} number of remote peers", this.config.minRemotePeers, remotePeersNum);
                return false;
            }
        }
        this.remotePeersWereReady = true;
        return true;
    }

    private int alreadyLoggedFlags = 0;
    public boolean isReady() {
        var endpoint = this.endpointHolder.get();
        if (endpoint == null) {
            return true;
        }
//        if (!endpoint.isReady()) {
//            if ((this.alreadyLoggedFlags & 1) == 0) {
//                logger.info("Waiting for endpoint to be ready");
//                this.alreadyLoggedFlags = 1;
//            }
//            return false;
//        }

        if ((this.alreadyLoggedFlags & 4) == 0) {
            logger.info("Ready");
            this.alreadyLoggedFlags += 4;
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
