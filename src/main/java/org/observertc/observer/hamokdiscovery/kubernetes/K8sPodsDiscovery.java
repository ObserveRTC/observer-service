package org.observertc.observer.hamokdiscovery.kubernetes;

import io.github.balazskreith.hamok.Storage;
import io.github.balazskreith.hamok.common.UuidTools;
import io.github.balazskreith.hamok.memorystorages.MemoryStorage;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import org.observertc.observer.hamokdiscovery.HamokDiscovery;
import org.observertc.observer.hamokendpoints.HamokConnectionConfig;
import org.observertc.observer.hamokendpoints.HamokEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class K8sPodsDiscovery implements HamokDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(K8sPodsDiscovery.class);

    private final String namespace;

    private Storage<String, DiscoveredActivePod> discoveredActivePods;
    private Map<UUID, Instant> disconnects = new ConcurrentHashMap<>();
    private final String namePrefix;
    private final CoreV1Api api;

    private final Supplier<HamokEndpoint> hamokEndpointSupplier;
    private volatile boolean run = false;
    private volatile boolean ready = false;
    private List<InetAddress> localAddresses = Collections.emptyList();
    private AtomicReference<Thread> thread = new AtomicReference<>(null);
    private Long readyTimestampInSec = null;
    private final int remotePort;

    public K8sPodsDiscovery(
            Supplier<HamokEndpoint> hamokEndpointSupplier,
            String namespace,
            String servicePrefix,
            int remotePort,
            CoreV1Api api
    ) {
        this.hamokEndpointSupplier = hamokEndpointSupplier;
        this.namespace = namespace;
        this.namePrefix = servicePrefix;
        this.remotePort = remotePort;
        this.api = api;

        this.localAddresses = this.getLocalAddresses();
        this.localAddresses.forEach(addr -> {
            logger.info("Found local address: {}", addr);
        });
        var remoteAddressesStorage = MemoryStorage.<String, DiscoveredActivePod>builder()
                .setId("remote-addresses")
                .setConcurrency(true)
                .build();
        this.discoveredActivePods = remoteAddressesStorage;
        remoteAddressesStorage.events().createdEntry().subscribe(expiredEntry -> {
            var remotePeer = expiredEntry.getNewValue();
            logger.info("Discovered Remote Peer is added. {}", remotePeer);
        });
        remoteAddressesStorage.events().deletedEntry().subscribe(expiredEntry -> {
            var oldRemotePeer = expiredEntry.getOldValue();
            logger.info("Discovered Remote Pod is removed. {}}", oldRemotePeer);
        });
    }

    public boolean isReady() {
        return this.ready;
    }

    public void start() {
        if (this.run) {
            logger.warn("Attempted to start twice");
            return;
        }
        this.run = true;
        this.ready = false;
        this.thread.set(new Thread(this::process));
        this.thread.get().start();
    }

    public void stop() {
        if (!this.run) {
            return;
        }
        var thread = this.thread.getAndSet(null);
        if (thread != null) {
            try {
                thread.join(10000);
                if (thread.isAlive()) {
                    thread.interrupt();
                }
            } catch (InterruptedException e) {
                logger.warn("Exception while stopping thread", e);
            }
        }
        this.run = false;
        this.ready = false;
    }

    public List<InetAddress> getLocalAddresses() {
        var result = new LinkedList<InetAddress>();
        try {
            for (var it = NetworkInterface.getNetworkInterfaces(); it.hasMoreElements(); ) {
                var netif = it.nextElement();
                for (var jt = netif.getInetAddresses(); jt.hasMoreElements(); ) {
                    var inetAddress = jt.nextElement();
                    result.add(inetAddress);
                }
            }
            return result.stream().collect(Collectors.toList());
        } catch (SocketException e) {
            logger.warn("Error while collecting local addresses", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void onDisconnect(UUID connectionId) {
        if (this.disconnects.containsKey(connectionId)) {
            return;
        }
        String podId = null;
        for (var it = discoveredActivePods.iterator(); it.hasNext(); ) {
            var entry = it.next();
            var discoveredActivePod = entry.getValue();
            if (UuidTools.equals(discoveredActivePod.connectionId, connectionId)) {
                podId = entry.getKey();
                break;
            }
        }
        if (podId == null) {
            logger.debug("Cannot find Discovered Active Pod for connection {}, therefore it cannot be removed", connectionId);
            return;
        }
        this.removeDiscoveredActivePod(podId);
    }

    @Override
    public List<HamokConnectionConfig> getActiveConnections() {
        var result = new LinkedList<HamokConnectionConfig>();
        for (var it = discoveredActivePods.iterator(); it.hasNext(); ) {
            var entry = it.next();
            var discoveredPod = entry.getValue();
            result.add(new HamokConnectionConfig(
                    discoveredPod.connectionId,
                    discoveredPod.inetAddress.getHostName(),
                    this.remotePort
            ));
        }
        return result;
    }

    private void process() {
        int sleep = 10;
        int consecutiveFailure = 0;
        while (this.run) {
            try {
                Thread.sleep(sleep);
                var updated = this.update();
                if (updated) {
                    sleep = 10;
                } else {
                    sleep = Math.min(30000, sleep * sleep);
                    if (!this.ready) {
                        this.readyTimestampInSec = Instant.now().getEpochSecond();
                        this.ready = true;
                    }
                }
                consecutiveFailure = 0;
            } catch (InterruptedException e) {
                logger.warn("Exception occurred while discovery is running. Consecutive failure counter: {}", ++consecutiveFailure, e);
                sleep = 5000;
            }
        }
    }

    private boolean update() {
        boolean result = false;
        Set<String> visitedPodIds = new HashSet<>();
        String _continue = null;
        while(true) {
            V1PodList v1PodList;
            try {
                v1PodList = api.listNamespacedPod(
                        this.namespace,
                        null,
                        null,
                        _continue,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        false
                );
                _continue = v1PodList.getMetadata().getContinue();
            } catch (ApiException e) {
                logger.error("Error occurred while retrieving information about the k8s cluster");
                break;
            }
            for (var pod : v1PodList.getItems()) {
                var metadata = pod.getMetadata();
                var podId = metadata.getUid();
                var podName = metadata.getName();
                if (podId == null || !podName.startsWith(this.namePrefix)) {
                    continue;
                }
                if (pod.getStatus() == null) {
                    logger.warn("Pod Status is null for podId: {}, podName: {} ", podId, podName);
                    continue;
                }
                if (pod.getStatus().getContainerStatuses() == null) {
                    logger.warn("Pod Container Status is null for podId: {}, podName: {}. Maybe Pending", podId, podName);
                    continue;
                }
                boolean running = true;
                boolean terminated = false;
                boolean waiting = false;

                for (var containerStatus : pod.getStatus().getContainerStatuses()) {
                    var state = containerStatus.getState();
                    if (state == null) continue;
                    running = running && state.getRunning() != null;
                    terminated = terminated || state.getTerminated() != null;
                    if (state.getWaiting() != null) {
                        waiting = true;
                    }
                }

                var ipAddress = getPodIp(pod);
                var connectionId = UUID.nameUUIDFromBytes(podId.getBytes());

                logger.info("Checking Pod in namespace {} prefixed with {}. Pod Id: {}, Name: {}, hashed connectionId: {}, ipAddress: {}, running: {}, terminated: {}, waiting: {}",
                    this.namespace,
                    this.namePrefix,
                    podId,
                    podName,
                    connectionId,
                    ipAddress,
                    running,
                    terminated,
                    waiting
                );

                if (ipAddress == null) {
                    var discoveredActivePod = this.discoveredActivePods.get(podId);
                    if (discoveredActivePod != null) {
                        logger.warn("There is a discovered active pod which ip address become null. Container State: running: {}, terminated: {}, waiting: {}. DiscoveredActivePod: {}",
                                running,
                                terminated,
                                waiting,
                                discoveredActivePod
                        );
                        // it will not be part of the visitedIds, so we make it inactive
//                        this.setInactive(podId);
//                        result = true;
                    }
                    continue;
                }

                visitedPodIds.add(podId);

                if (ipAddress.isLoopbackAddress() || ipAddress.isLinkLocalAddress()) {
                    logger.debug("{} is local address, will not be used as remote", ipAddress);
                    continue;
                }
                var match = this.localAddresses.stream().anyMatch(addr -> Arrays.equals(addr.getAddress(), ipAddress.getAddress()));
                if (match) {
                    logger.debug("{} is detected local address, will not be used as remote", ipAddress);
                    continue;
                }

                var savedRemotePeer = this.discoveredActivePods.get(podId);
                if (running && savedRemotePeer == null) {
                    // create and make active
                    var added = this.addDiscoveredActivePod(
                            podId,
                            podName,
                            ipAddress
                    );
                    result = result || added;
                } else if (!running && savedRemotePeer != null) {
                    // make inactive
                    var removed = this.removeDiscoveredActivePod(podId);
                    result = result || removed;
                }
            }
            if (_continue == null) {
                break;
            }
        }
        var podIdsToRemove = new HashSet<String>();
        for (var it = discoveredActivePods.iterator(); it.hasNext(); ) {
            var entry = it.next();
            var podId = entry.getKey();
            if (visitedPodIds.contains(podId)) {
                continue;
            }
            podIdsToRemove.add(podId);
        }
        var removed = podIdsToRemove.stream().map(this::removeDiscoveredActivePod).reduce(false, (initial, changed) -> initial || changed);
        result = result || removed;

        var now = Instant.now().toEpochMilli();
        for (var it = this.disconnects.entrySet().iterator(); it.hasNext(); ) {
            var entry = it.next();
            var connectionId = entry.getKey();
            var disconnected = entry.getValue();
            if (connectionId == null || disconnected == null) {
                it.remove();
                continue;
            }
            var elapsedTimeInMs = now - disconnected.toEpochMilli();
            if (1000 < elapsedTimeInMs) {
                logger.info("Connection {} was removed more than 1s ago, we remove it from disconnected connections");
                it.remove();
            }
        }
        return result;
    }

    private boolean addDiscoveredActivePod(String podId, String podName, InetAddress inetAddress) {
        if (podId == null) {
            logger.warn("Attempted to set a non-existing podId {} active", podId);
            return false;
        }
        var discoveredActivePod = this.discoveredActivePods.get(podId);
        if (discoveredActivePod != null) {
            logger.warn("Attempted to add a discoveredPod twice {}", discoveredActivePod);
            return false;
        }
        var connectionId = UUID.nameUUIDFromBytes(podId.getBytes(StandardCharsets.UTF_8));
        var disconnected = this.disconnects.get(connectionId);
        if (disconnected != null) {
            logger.warn("Attempted to add an already disconnected connection. ConnectionId {}", connectionId);
            return false;
        }
        discoveredActivePod = new DiscoveredActivePod(
                podId,
                podName,
                connectionId,
                inetAddress
        );
        this.discoveredActivePods.set(podId, discoveredActivePod);
        var hamokEndpoint = hamokEndpointSupplier.get();
        if (hamokEndpoint != null) {
            hamokEndpoint.addConnection(new HamokConnectionConfig(
                    connectionId,
                    inetAddress.getHostName(),
                    this.remotePort
            ));
        } else {
            logger.info("HamokEndpoint is not available to make a connection to pod {}", discoveredActivePod);
        }

        return true;
    }

    private boolean removeDiscoveredActivePod(String podId) {
        if (podId == null) {
            logger.warn("Attempted to set a non-existing podId {} inactive", podId);
            return false;
        }
        var discoveredActivePod = this.discoveredActivePods.get(podId);
        if (discoveredActivePod == null) {
            logger.debug("Attempted to remove a non-existing discovered remote pod");
            return false;
        }
        this.discoveredActivePods.delete(discoveredActivePod.podId);
        this.disconnects.put(discoveredActivePod.connectionId, Instant.now());
        var hamokEndpoint = hamokEndpointSupplier.get();
        if (hamokEndpoint != null) {
            hamokEndpoint.removeConnection(discoveredActivePod.connectionId);
        } else {
            logger.warn("Hamok Connection cannot be removed because the endpoint is not available");
        }

        return true;
    }

    private static InetAddress getPodIp(V1Pod pod) {
        var status = pod.getStatus();
        var podIp = status.getPodIP();
        if (podIp == null) return null;
        try {
            return InetAddress.getByName(podIp);
        } catch (UnknownHostException e) {
            logger.error("Error occurred while parsing podIp {}", podIp, e);
            return null;
        }
    }

    private record DiscoveredActivePod (
            String podId,
            String podName,
            UUID connectionId,
            InetAddress inetAddress
    ) {

    }
}
