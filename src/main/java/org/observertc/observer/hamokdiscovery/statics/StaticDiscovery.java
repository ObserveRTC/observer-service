package org.observertc.observer.hamokdiscovery.statics;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.observer.hamokdiscovery.RemotePeer;
import org.observertc.observer.hamokdiscovery.RemotePeerDiscovery;
import org.observertc.observer.hamokdiscovery.RemotePeerDiscoveryEvent;
import org.observertc.observer.hamokdiscovery.RemotePeerDiscoveryEventTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StaticDiscovery implements RemotePeerDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(StaticDiscovery.class);

    private final Map<String, RemotePeer> remotePeers = new ConcurrentHashMap<>();
    private final Subject<RemotePeerDiscoveryEvent> events = PublishSubject.create();
    private volatile boolean run = false;

    public void add(String key, RemotePeer remotePeer) {
        var removed = this.remotePeers.put(key, remotePeer);
        if (!this.run) {
            return;
        }
        if (removed != null) {
            var event = new RemotePeerDiscoveryEvent(
                    RemotePeerDiscoveryEventTypes.REMOVED,
                    removed
            );
            this.events.onNext(event);
        }
        var event = new RemotePeerDiscoveryEvent(
                RemotePeerDiscoveryEventTypes.ADDED,
                remotePeer
        );
        this.events.onNext(event);
    }

    public boolean remove(String key) {
        var removed = this.remotePeers.remove(key);
        if (removed == null) {
            return false;
        }
        if (!this.run) {
            return true;
        }
        var event = new RemotePeerDiscoveryEvent(
                RemotePeerDiscoveryEventTypes.REMOVED,
                removed
        );
        this.events.onNext(event);
        return true;
    }

    @Override
    public Observable<RemotePeerDiscoveryEvent> events() {
        return this.events;
    }

    @Override
    public boolean isReady() {
        return this.run;
    }

    @Override
    public void start() {
        if (this.run) {
            return;
        }
        this.run = true;
        for (var remotePeer : this.remotePeers.values()) {
            var event = new RemotePeerDiscoveryEvent(
                    RemotePeerDiscoveryEventTypes.ADDED,
                    remotePeer
            );
            this.events.onNext(event);
        }
    }

    @Override
    public void stop() {
        if (!this.run) {
            return;
        }
        this.run = false;
        for (var remotePeer : this.remotePeers.values()) {
            var event = new RemotePeerDiscoveryEvent(
                    RemotePeerDiscoveryEventTypes.REMOVED,
                    remotePeer
            );
            this.events.onNext(event);
        }
    }

    @Override
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
}
