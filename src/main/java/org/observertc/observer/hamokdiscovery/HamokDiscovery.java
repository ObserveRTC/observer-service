package org.observertc.observer.hamokdiscovery;

import org.observertc.observer.hamokendpoints.HamokConnectionConfig;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

public interface HamokDiscovery {
    boolean isReady();
    void start();
    void stop();
    List<InetAddress> getLocalAddresses();
    void onDisconnect(UUID connectionId);

    List<HamokConnectionConfig> getActiveConnections();
}
