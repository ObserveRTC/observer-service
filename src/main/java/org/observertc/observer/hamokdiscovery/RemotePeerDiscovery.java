package org.observertc.observer.hamokdiscovery;

import io.reactivex.rxjava3.core.Observable;

import java.net.InetAddress;
import java.util.List;

public interface RemotePeerDiscovery {
    Observable<HamokConnectionStateChangedEvent> connectionStateChanged();
//    Observable<RemotePeerDiscoveryEvent> events();
    boolean isReady();
    void start();
    void stop();
    List<InetAddress> getLocalAddresses();
}
