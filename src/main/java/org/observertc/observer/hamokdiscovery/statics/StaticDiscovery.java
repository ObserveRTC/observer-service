package org.observertc.observer.hamokdiscovery.statics;

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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StaticDiscovery implements RemotePeerDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(StaticDiscovery.class);

    private final Map<UUID, HamokConnection> hamokConnections = new ConcurrentHashMap<>();
    private final Subject<HamokConnectionStateChangedEvent> stateChanged = PublishSubject.create();
    private volatile boolean run = false;

    public void add(HamokConnection hamokConnection) {
        var removed = this.hamokConnections.put(hamokConnection.connectionId(), hamokConnection);
        if (!this.run) {
            return;
        }
        if (removed == null) {
            logger.warn("Overrided hamok connection. removed connection: {}, new connection: {}", removed, hamokConnection);

        }
        this.stateChanged.onNext(new HamokConnectionStateChangedEvent(
                hamokConnection,
                null,
                HamokConnectionState.ACTIVE
        ));
    }

    public boolean remove(UUID connectionId) {
        var removed = this.hamokConnections.remove(connectionId);
        if (removed == null) {
            return false;
        }
        if (!this.run) {
            return true;
        }
        this.stateChanged.onNext(new HamokConnectionStateChangedEvent(
                removed,
                HamokConnectionState.ACTIVE,
                HamokConnectionState.INACTIVE
        ));
        return true;
    }

    @Override
    public Observable<HamokConnectionStateChangedEvent> connectionStateChanged() {
        return this.stateChanged;
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
        for (var hamokConnection : this.hamokConnections.values()) {
            this.stateChanged.onNext(new HamokConnectionStateChangedEvent(
                    hamokConnection,
                    null,
                    HamokConnectionState.ACTIVE
            ));
        }
    }

    @Override
    public void stop() {
        if (!this.run) {
            return;
        }
        this.run = false;
        for (var hamokConnection : this.hamokConnections.values()) {
            this.stateChanged.onNext(new HamokConnectionStateChangedEvent(
                    hamokConnection,
                    HamokConnectionState.ACTIVE,
                    HamokConnectionState.INACTIVE
            ));
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
