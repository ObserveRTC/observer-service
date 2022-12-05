package org.observertc.observer.hamokdiscovery.kubernetes;

import io.github.balazskreith.hamok.Storage;
import io.github.balazskreith.hamok.memorystorages.MemoryStorage;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.observer.hamokdiscovery.HamokConnection;
import org.observertc.observer.hamokdiscovery.HamokConnectionState;
import org.observertc.observer.hamokdiscovery.HamokConnectionStateChangedEvent;
import org.observertc.observer.hamokdiscovery.RemotePeerDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class K8sPodsDiscovery implements RemotePeerDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(K8sPodsDiscovery.class);

    private Subject<HamokConnectionStateChangedEvent> stateChanged = PublishSubject.create();

    private Storage<String, DiscoveredRemotePeer> discoveredRemotePeers;
    private final String namespace;
    private final String namePrefix;
    private final CoreV1Api api;

    private volatile boolean run = false;
    private volatile boolean ready = false;
    private List<InetAddress> localAddresses = Collections.emptyList();
    private AtomicReference<Thread> thread = new AtomicReference<>(null);
    private Long readyTimestampInSec = null;
    private final int remotePort;

    public K8sPodsDiscovery(
            String namespace,
            String servicePrefix,
            int remotePort,
            CoreV1Api api
    ) {
        this.namespace = namespace;
        this.namePrefix = servicePrefix;
        this.remotePort = remotePort;
        this.api = api;

        this.localAddresses = this.getLocalAddresses();
        this.localAddresses.forEach(addr -> {
            logger.info("Found local address: {}", addr);
        });
        var remoteAddressesStorage = MemoryStorage.<String, DiscoveredRemotePeer>builder()
                .setId("remote-addresses")
                .setExpiration(60 * 60 * 1000) // 1h
                .setConcurrency(true)
                .build();
        this.discoveredRemotePeers = remoteAddressesStorage;
        remoteAddressesStorage.events().expiredEntry().subscribe(expiredEntry -> {
            var remotePeer = expiredEntry.getOldValue();
            if (remotePeer == null) return;
            logger.info("Discovered Remote Peer is expired, hence removed from the storage. podId: {} State: {}, PodName: {}, HostName: {}, Address: {}",
                    remotePeer.podId,
                    remotePeer.state,
                    remotePeer.podName,
                    remotePeer.inetAddress.getHostName(),
                    remotePeer.inetAddress.getHostAddress()
            );
        });
        remoteAddressesStorage.events().createdEntry().subscribe(expiredEntry -> {
            var remotePeer = expiredEntry.getNewValue();
            if (remotePeer == null) return;
            logger.info("Discovered Remote Peer is added. podId: {} State: {}, PodName: {}, HostName: {}, Address: {}",
                    remotePeer.podId,
                    remotePeer.state,
                    remotePeer.podName,
                    remotePeer.inetAddress.getHostName(),
                    remotePeer.inetAddress.getHostAddress()
            );
        });
        remoteAddressesStorage.events().updatedEntry().subscribe(expiredEntry -> {
            var oldRemotePeer = expiredEntry.getOldValue();
            var newRemotePeer = expiredEntry.getNewValue();
            if (oldRemotePeer == null || newRemotePeer == null) return;
            logger.info("Discovered Remote Peer is updated. podId: {} PrevState: {} State: {}, PodName: {}, HostName: {}, Address: {}",
                    oldRemotePeer.podId,
                    oldRemotePeer.state,
                    newRemotePeer.state,
                    newRemotePeer.podName,
                    newRemotePeer.inetAddress.getHostName(),
                    newRemotePeer.inetAddress.getHostAddress()
            );
        });
        this.stateChanged.subscribe(hamokConnectionStateChangedEvent -> {
            logger.info("Hamok Connection state is changed. {}",
                hamokConnectionStateChangedEvent
            );
        });
    }

    @Override
    public Observable<HamokConnectionStateChangedEvent> connectionStateChanged() {
        return this.stateChanged;
    }

    public boolean isReady() {
        return this.ready;
    }

    public int elapsedSecSinceReady() {
        if (!this.ready) {
            return 0;
        }
        return (int) (Instant.now().getEpochSecond() - this.readyTimestampInSec);
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

    private void process() {
        int sleep = 10;
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
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void setInactive(String podId) {
        if (podId == null) {
            logger.warn("Attempted to set a non-existing podId {} inactive", podId);
            return;
        }
        var savedRemotePeer = this.discoveredRemotePeers.get(podId);
        if (savedRemotePeer == null) {
            logger.warn("Attempted to set a non-existing discoveredRemotePeer {} inactive", podId);
            return;
        }
        if (HamokConnectionState.INACTIVE.equals(savedRemotePeer.state)) {
            logger.warn("Attempted to set discoveredRemotePeer inactive twice. {} ", savedRemotePeer);
            return;
        }
        var prevState = savedRemotePeer.state;
        var updatedRemotePeer = new DiscoveredRemotePeer(
                savedRemotePeer.podId,
                savedRemotePeer.podName,
                HamokConnectionState.INACTIVE,
                savedRemotePeer.inetAddress
        );
        this.discoveredRemotePeers.set(updatedRemotePeer.podId, updatedRemotePeer);
        var connectionId = UUID.nameUUIDFromBytes(updatedRemotePeer.podId.getBytes(StandardCharsets.UTF_8));
        var hamokConnection = new HamokConnection(
                connectionId,
                savedRemotePeer.inetAddress.getHostName(),
                this.remotePort
        );
        this.stateChanged.onNext(new HamokConnectionStateChangedEvent(
                hamokConnection,
                prevState,
                updatedRemotePeer.state
        ));
    }

    private void createOrSetActive(String podId, String podName, InetAddress inetAddress) {
        if (podId == null) {
            logger.warn("Attempted to set a non-existing podId {} active", podId);
            return;
        }
        HamokConnectionState prevState = null;
        var discoveredRemotePeer = this.discoveredRemotePeers.get(podId);
        if (discoveredRemotePeer == null) {
            discoveredRemotePeer = new DiscoveredRemotePeer(
                    podId,
                    podName,
                    HamokConnectionState.ACTIVE,
                    inetAddress
            );
        } else if (HamokConnectionState.ACTIVE.equals(discoveredRemotePeer.state)) {
            logger.warn("Attempted to discoveredRemotePeer active twice. {} ", discoveredRemotePeer);
            return;
        } else {
            prevState = discoveredRemotePeer.state;
            discoveredRemotePeer = new DiscoveredRemotePeer(
                    discoveredRemotePeer.podId,
                    discoveredRemotePeer.podName,
                    HamokConnectionState.ACTIVE,
                    discoveredRemotePeer.inetAddress
            );
        }
        this.discoveredRemotePeers.set(podId, discoveredRemotePeer);

        var connectionId = UUID.nameUUIDFromBytes(discoveredRemotePeer.podId.getBytes(StandardCharsets.UTF_8));
        var hamokConnection = new HamokConnection(
                connectionId,
                discoveredRemotePeer.inetAddress.getHostName(),
                this.remotePort
        );
        this.stateChanged.onNext(new HamokConnectionStateChangedEvent(
                hamokConnection,
                prevState,
                discoveredRemotePeer.state
        ));
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

                logger.info("Iterated Pod Id: {}, Name: {}, hashed connectionId: {}, ipAddress: {}, running: {}, terminated: {}, waiting: {}",
                    podId,
                    podName,
                    connectionId,
                    ipAddress,
                    running,
                    terminated,
                    waiting
                );

                if (ipAddress == null) {
                    var savedRemotePeer = this.discoveredRemotePeers.get(podId);
                    if (savedRemotePeer != null && HamokConnectionState.ACTIVE.equals(savedRemotePeer.state)) {
                        logger.warn("Ip Address is null, but the connection state is active. podId: {}, podName: {},  running: {}, terminated: {}, waiting: {}",
                                podId,
                                podName,
                                running,
                                terminated,
                                waiting
                        );
                        // it will not be part of the visitedIds, so we make it inactive
//                        this.setInactive(podId);
                        result = true;
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

                var savedRemotePeer = this.discoveredRemotePeers.get(podId);
                if (running && savedRemotePeer == null) {
                    // create and make active
                    this.createOrSetActive(
                            podId,
                            podName,
                            ipAddress
                    );
                } else if (!running && savedRemotePeer != null) {
                    if (HamokConnectionState.INACTIVE.equals(savedRemotePeer.state)) {
                        // already inactive
                        continue;
                    }
                    // make inactive
                    this.setInactive(podId);
                }
            }
            if (_continue == null) {
                break;
            }
        }
        for (var it = discoveredRemotePeers.iterator(); it.hasNext(); ) {
            var entry = it.next();
            var podId = entry.getKey();
            if (visitedPodIds.contains(podId)) {
                continue;
            }
            this.setInactive(podId);
        }
        return result;
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


    private record DiscoveredRemotePeer(
            String podId,
            String podName,
            HamokConnectionState state,
            InetAddress inetAddress
    ) {

    }
}
