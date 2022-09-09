package org.observertc.observer.hamokendpoints.kubernetes;

import java.net.InetAddress;

public record RemotePeerDiscoveryEvent(
        RemotePeerDiscoveryEventTypes eventType,
        InetAddress address
) {

}
