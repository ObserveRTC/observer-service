package org.observertc.observer.hamokdiscovery.statics;

import io.reactivex.rxjava3.schedulers.Schedulers;
import org.observertc.observer.hamokdiscovery.HamokDiscovery;
import org.observertc.observer.hamokendpoints.HamokConnectionConfig;
import org.observertc.observer.hamokendpoints.HamokEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StaticDiscovery implements HamokDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(StaticDiscovery.class);

    private final Map<UUID, HamokConnectionConfig> hamokConnections = new ConcurrentHashMap<>();
    private volatile boolean run = false;
    private final Supplier<HamokEndpoint> hamokEndpointSupplier;

    public StaticDiscovery(Supplier<HamokEndpoint> hamokEndpointSupplier) {
        this.hamokEndpointSupplier = hamokEndpointSupplier;
    }


    public void add(HamokConnectionConfig hamokConnectionConfig) {
        var removed = this.hamokConnections.put(hamokConnectionConfig.connectionId(), hamokConnectionConfig);
        if (!this.run) {
            return;
        }
        if (removed == null) {
            logger.warn("Overrided hamok connection config. removed connection: {}, new connection: {}", removed, hamokConnectionConfig);

        }
        this.hamokEndpointSupplier.get().addConnection(hamokConnectionConfig);
    }

    public boolean remove(UUID connectionId) {
        var removed = this.hamokConnections.remove(connectionId);
        if (removed == null) {
            return false;
        }
        if (!this.run) {
            return true;
        }
        this.hamokEndpointSupplier.get().removeConnection(connectionId);
        return true;
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
        for (var hamokConnectionConfig : this.hamokConnections.values()) {
            this.hamokEndpointSupplier.get().addConnection(hamokConnectionConfig);
        }
    }

    @Override
    public void stop() {
        if (!this.run) {
            return;
        }
        this.run = false;
        for (var connectionId : this.hamokConnections.keySet()) {
            this.hamokEndpointSupplier.get().removeConnection(connectionId);
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

    @Override
    public void onDisconnect(UUID connectionId) {
        var hamokConnectionConfig = this.hamokConnections.get(connectionId);
        if (hamokConnectionConfig == null) {
            return;
        }
        logger.warn("Connection {} is reported to be disconnected. Static Discovery will put it back", hamokConnectionConfig);
        Schedulers.computation().scheduleDirect(() -> {
            this.hamokEndpointSupplier.get().addConnection(hamokConnectionConfig);
        }, 5000, TimeUnit.MILLISECONDS);
    }

    @Override
    public List<HamokConnectionConfig> getActiveConnections() {
        return this.hamokConnections.values().stream().collect(Collectors.toList());
    }
}
