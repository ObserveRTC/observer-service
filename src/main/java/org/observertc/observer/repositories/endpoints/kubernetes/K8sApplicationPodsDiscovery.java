package org.observertc.observer.repositories.endpoints.kubernetes;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class K8sApplicationPodsDiscovery extends Observable<K8sApplicationPodsDiscovery.K8sApplicationPodIpEvent> {

    private static final Logger logger = LoggerFactory.getLogger(K8sApplicationPodsDiscovery.class);

    private Subject<K8sApplicationPodIpEvent> subject = PublishSubject.create();

    private Map<String, InetAddress> inetAddresses = new ConcurrentHashMap<>();
    private final String namespace;
    private final String namePrefix;
    private final CoreV1Api api;

    private volatile boolean run = true;
    private List<InetAddress> localAddresses = Collections.emptyList();
    private AtomicReference<Thread> thread = new AtomicReference<>(null);

    public K8sApplicationPodsDiscovery(
            String namespace,
            String namePrefix,
            CoreV1Api api
    ) {
        this.namespace = namespace;
        this.namePrefix = namePrefix;
        this.api = api;

        this.localAddresses = this.getLocalAddresses();
        this.localAddresses.forEach(addr -> {
            logger.info("Found local address: {}", addr);
        });
    }

    public void start() {
        logger.info("started");

        this.thread.set(new Thread(this::process));
        this.thread.get().start();
    }

    public void stop() {
        this.run = false;
        var thread = this.thread.getAndSet(null);
        if (thread == null) {
            try {
                thread.join(10000);
                if (thread.isAlive()) {
                    thread.interrupt();
                }
            } catch (InterruptedException e) {
                logger.warn("Exception while stopping thread", e);
            }
        }
        logger.info("stopped");
    }

    private List<InetAddress> getLocalAddresses() {
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
                var metadata = pod.getMetadata();
                var podName = metadata.getName();
                if (!podName.startsWith(this.namePrefix)) {
                    continue;
                }
                var id = metadata.getUid();
                if (id == null) continue;
                var ipAddress = getPodIp(pod);
                if (ipAddress == null) {
                    var removedAddress = this.inetAddresses.remove(id);
                    if (removedAddress != null) {
                        this.subject.onNext(new K8sApplicationPodIpEvent(
                                EventType.REMOVED,
                                removedAddress
                        ));
                        result = true;
                    }
                    continue;
                } else if (ipAddress.isLoopbackAddress() || ipAddress.isLinkLocalAddress()) {
                    logger.debug("{} is local address, will not be used as remote", ipAddress);
                    continue;
                } else {
                    var match = this.localAddresses.stream().anyMatch(addr -> Arrays.equals(addr.getAddress(), ipAddress.getAddress()));
                    if (match) {
                        logger.debug("{} is detected local address, will not be used as remote", ipAddress);
                        continue;
                    }
                }
                visitedIds.add(id);
                var savedAddress = this.inetAddresses.put(id, ipAddress);
                if (savedAddress == null) {
                    this.subject.onNext(new K8sApplicationPodIpEvent(
                            EventType.ADDED,
                            ipAddress
                    ));
                    result = true;
                } else if (!Arrays.equals(savedAddress.getAddress(), ipAddress.getAddress())) {
                    this.subject.onNext(new K8sApplicationPodIpEvent(
                            EventType.REMOVED,
                            savedAddress
                    ));
                    this.subject.onNext(new K8sApplicationPodIpEvent(
                            EventType.ADDED,
                            ipAddress
                    ));
                    result = true;
                }
            }
            if (_continue == null) {
                break;
            }
        }
        var idsToRemove = this.inetAddresses.keySet().stream()
                .filter(savedId -> !visitedIds.contains(savedId))
                .collect(Collectors.toList());

        for (var id : idsToRemove) {
            var removedAddress = this.inetAddresses.remove(id);
            this.subject.onNext(new K8sApplicationPodIpEvent(
                    EventType.REMOVED,
                    removedAddress
            ));
            result = true;
        }
        return result;
    }

    @Override
    protected void subscribeActual(@NonNull Observer<? super K8sApplicationPodIpEvent> observer) {
        this.subject.subscribe(observer);
    }

    public enum EventType {
        REMOVED,
        ADDED
    }

    public record K8sApplicationPodIpEvent(
            EventType eventType,
            InetAddress address) {

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
}
