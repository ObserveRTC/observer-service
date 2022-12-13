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
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.hamokdiscovery.HamokDiscoveryService;
import org.observertc.observer.hamokendpoints.HamokEndpoint;
import org.observertc.observer.hamokendpoints.HamokEndpointService;
import org.observertc.observer.metrics.HamokMetrics;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    BeanProvider<HamokMetrics> hamokMetricsBeanProvider;

    @Inject
    HamokEndpointService hamokEndpointService;

    @Inject
    HamokDiscoveryService hamokDiscoveryService;

//    @Inject
//    Sandbox sandbox;

    private volatile boolean running = false;
    private final AtomicReference<HamokEndpoint> endpointHolder = new AtomicReference<>();
    private StorageGrid storageGrid;
    private Set<UUID> remotePeers = Collections.synchronizedSet(new HashSet<>());
    private final Map<UUID, Long> inactivity = new ConcurrentHashMap<>();


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
                    // TODO: There is a wierd error here, for some reason the reconnect kills the connection, and every other connection
//                    var endpoint = this.endpointHolder.get();
//                    if (endpoint == null) {
//                        return;
//                    }
//                    if (!endpoint.reconnectToEndpoint(endpointId)) {
//                        logger.warn("Was not able to reconnect to endpoint {}", endpointId);
//                        this.storageGrid.removeRemoteEndpointId(endpointId);
//                    }
                });
        this.storageGrid.events().notRespondingEndpointIds().subscribe(notRespondingRemoteEndpointIds -> {
            logger.info("Reported endpoint ids are not responding: {}", JsonUtils.objectToString(notRespondingRemoteEndpointIds));
            this.hamokMetricsBeanProvider.get().incrementNotRespondingRemotePeerIds();
            var hamokEndpoint = this.hamokEndpointService.get();
            if (hamokEndpoint == null) {
                return;
            }
            var now = Instant.now().toEpochMilli();
            for (var remoteEndpointId : notRespondingRemoteEndpointIds) {
                var inactivityStarted = this.inactivity.get(notRespondingRemoteEndpointIds);
                if (inactivityStarted == null) {
                    this.inactivity.put(remoteEndpointId, now);
                    continue;
                }
                var elapsedInMs = now - inactivityStarted;
                if (elapsedInMs < 3000) {
                    // debouncing purpose
                    continue;
                }
                if (90000 < elapsedInMs) {
                    // reset
                    this.inactivity.put(remoteEndpointId, now);
                    continue;
                }
                // okay so this is inactive and consecutively reporting for more than 10s. we need to cut ir.
                hamokEndpoint.removeConnectionByEndpointId(remoteEndpointId);
            }
        });
        this.storageGrid.errors().subscribe(err -> {
            logger.warn("Error occurred in storageGrid. Code: {}", err.getCode(), err.getException());
        });

        this.hamokEndpointService.setHamokDiscoveryService(this.hamokDiscoveryService);
        this.hamokDiscoveryService.setEndpointService(this.hamokEndpointService);
    }

    private volatile boolean firstRefresh = false;

    public void refreshRemoteEndpointId() {
        var endpoint = this.endpointHolder.get();
        var storageGrid = this.storageGrid;
        if (storageGrid == null || endpoint == null) {
            logger.warn("To refresh a remote endpoint storageGrid end endpoint must exists");
            return;
        }
        var activeRemoteEndpointIds = endpoint.getActiveRemoteEndpointIds();
        if (!this.firstRefresh) {
            if (activeRemoteEndpointIds.size() < this.config.minRemotePeers) {
                logger.info("Hamok first endpoint refresh will not be executed, because the required number of remote peer is {}, and currently there is {} available {}", this.config.minRemotePeers, activeRemoteEndpointIds.size());
                return;
            }
            this.firstRefresh = true;
        }
        var currentRemoteEndpointIds = storageGrid.endpoints().getRemoteEndpointIds();
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
        var hamokDiscovery = this.hamokDiscoveryService.get();
        if (hamokDiscovery != null) {
            hamokDiscovery.start();
        }
        var endpoint = this.endpointHolder.get();
        if (this.endpointHolder.get() == null) {
            endpoint = this.hamokEndpointService.get();

            if (endpoint != null) {
                endpoint.inboundChannel().subscribe(this.storageGrid.transport().getReceiver());
                this.storageGrid.transport().getSender().subscribe(endpoint.outboundChannel());
                if (this.endpointHolder.compareAndSet(null, endpoint)) {
                    endpoint.start();
                } else {
                    endpoint.stop();
                }
                endpoint.stateChanged().subscribe();
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
        var hamokDiscovery = this.hamokDiscoveryService.get();
        if (hamokDiscovery != null) {
            hamokDiscovery.stop();
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

    public Set<UUID> getRemoteEndpointIds() {
        var endpoint = this.endpointHolder.get();
        if (endpoint == null) {
            return Collections.emptySet();
        }
        var activeRemoteEndpointIds = endpoint.getActiveRemoteEndpointIds();
        if (activeRemoteEndpointIds == null) {
            return Collections.emptySet();
        }
        return activeRemoteEndpointIds;
    }

    private int alreadyLoggedFlags = 0;
    public boolean isReady() {
        var endpoint = this.endpointHolder.get();
        if (endpoint == null) {
            return true;
        }
        if (!hamokEndpointService.isReady()) {
            if ((this.alreadyLoggedFlags & 1) == 0) {
                logger.info("Waiting for endpoint to be ready");
                this.alreadyLoggedFlags = 1;
            }
            return false;
        }

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
