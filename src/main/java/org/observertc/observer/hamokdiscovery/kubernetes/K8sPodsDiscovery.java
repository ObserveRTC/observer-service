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
import org.observertc.observer.hamokdiscovery.RemotePeer;
import org.observertc.observer.hamokdiscovery.RemotePeerDiscovery;
import org.observertc.observer.hamokdiscovery.RemotePeerDiscoveryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class K8sPodsDiscovery implements RemotePeerDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(K8sPodsDiscovery.class);

    private Subject<RemotePeerDiscoveryEvent> subject = PublishSubject.create();

    private Storage<String, DiscoveredRemotePeer> remotePeers;
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
        this.subject.subscribe(event -> {
            logger.info("Pod with hostname {}:{} is {}", event.remotePeer().host(), event.remotePeer().port(), event.eventType());
        });
        var remoteAddressesStorage = MemoryStorage.<String, DiscoveredRemotePeer>builder()
                .setId("remote-addresses")
                .setExpiration(60 * 60 * 1000) // 1h
                .setConcurrency(true)
                .build();
        this.remotePeers = remoteAddressesStorage;
        remoteAddressesStorage.events().expiredEntry().subscribe(expiredEntry -> {
            var remotePeer = expiredEntry.getOldValue();
            if (remotePeer == null) return;
            logger.info("Discovered Remote Peer is expired, hence removed from the storage. Id: {} State: {}, PodName: {}, HostName: {}, Address: {}",
                    remotePeer.id,
                    remotePeer.state,
                    remotePeer.podName,
                    remotePeer.inetAddress.getHostName(),
                    remotePeer.inetAddress.getHostAddress()
            );
        });
        remoteAddressesStorage.events().createdEntry().subscribe(expiredEntry -> {
            var remotePeer = expiredEntry.getNewValue();
            if (remotePeer == null) return;
            logger.info("Discovered Remote Peer is added. Id: {} State: {}, PodName: {}, HostName: {}, Address: {}",
                    remotePeer.id,
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
            logger.info("Discovered Remote Peer is updated. Id: {} PrevState: {} State: {}, PodName: {}, HostName: {}, Address: {}",
                    oldRemotePeer.id,
                    oldRemotePeer.state,
                    newRemotePeer.state,
                    newRemotePeer.podName,
                    newRemotePeer.inetAddress.getHostName(),
                    newRemotePeer.inetAddress.getHostAddress()
            );
        });
    }

    @Override
    public Observable<RemotePeerDiscoveryEvent> events() {
        return this.subject;
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

    private boolean update() {
        boolean result = false;
        Set<String> visitedIds = new HashSet<>();
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
                if (waiting) {
                    continue;
                }
                var metadata = pod.getMetadata();
                var podName = metadata.getName();
                if (!podName.startsWith(this.namePrefix)) {
                    continue;
                }
                var id = metadata.getUid();
                if (id == null) continue;
                var ipAddress = getPodIp(pod);
                if (ipAddress == null) {
                    var savedRemotePeer = this.remotePeers.get(id);
                    if (savedRemotePeer != null) {
                        var remotePeer = new RemotePeer(savedRemotePeer.inetAddress.getHostName(), this.remotePort);
                        var event = RemotePeerDiscoveryEvent.createRemovedRemotePeerDiscoveryEvent(remotePeer);
                        this.subject.onNext(event);
                        this.remotePeers.set(id, new DiscoveredRemotePeer(
                                id,
                                podName,
                                RemotePeerState.INACTIVE,
                                null
                        ));
                        result = true;
                    }
                    continue;
                }
                if (ipAddress.isLoopbackAddress() || ipAddress.isLinkLocalAddress()) {
                    logger.debug("{} is local address, will not be used as remote", ipAddress);
                    continue;
                }
                var match = this.localAddresses.stream().anyMatch(addr -> Arrays.equals(addr.getAddress(), ipAddress.getAddress()));
                if (match) {
                    logger.debug("{} is detected local address, will not be used as remote", ipAddress);
                    continue;
                }
                visitedIds.add(id);
                var savedRemotePeer = this.remotePeers.get(id);
                if (savedRemotePeer == null) {
                    if (terminated || !running) {
                        // not interested in undiscovered but already terminated or not running pods
                        continue;
                    }
                    savedRemotePeer = new DiscoveredRemotePeer(
                            id,
                            podName,
                            RemotePeerState.ACTIVE,
                            ipAddress
                    );
                    this.remotePeers.set(savedRemotePeer.id, savedRemotePeer);

                    var remotePeer = new RemotePeer(ipAddress.getHostName(), this.remotePort);
                    var event = RemotePeerDiscoveryEvent.createAddedRemotePeerDiscoveryEvent(remotePeer);
                    this.subject.onNext(event);
                    result = true;

                } else if (RemotePeerState.ACTIVE.equals(savedRemotePeer.state)) {
                    if (!terminated) {
                        // if it is active, then nothing to be done
                        continue;
                    }
                    var updatedRemotePeer = new DiscoveredRemotePeer(
                            savedRemotePeer.id,
                            podName,
                            RemotePeerState.INACTIVE,
                            ipAddress
                    );
                    this.remotePeers.set(updatedRemotePeer.id, updatedRemotePeer);

                    var remotePeer = new RemotePeer(ipAddress.getHostName(), this.remotePort);
                    var event = RemotePeerDiscoveryEvent.createRemovedRemotePeerDiscoveryEvent(remotePeer);
                    this.subject.onNext(event);

                    result = true;
                } else { // inactive

                }
            }
            if (_continue == null) {
                break;
            }
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

    private enum RemotePeerState {
        ACTIVE,
        INACTIVE
    }

    private record DiscoveredRemotePeer(
            String id,
            String podName,
            RemotePeerState state,
            InetAddress inetAddress
    ) {

    }
}
