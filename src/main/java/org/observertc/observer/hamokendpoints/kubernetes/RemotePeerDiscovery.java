package org.observertc.observer.hamokendpoints.kubernetes;

import io.reactivex.rxjava3.core.Observable;

import java.net.InetAddress;
import java.util.List;

public interface RemotePeerDiscovery {
    Observable<RemotePeerDiscoveryEvent> events();
    boolean isReady();
    void start();
    void stop();
    int elapsedSecSinceReady();
    public List<InetAddress> getLocalAddresses();
}
