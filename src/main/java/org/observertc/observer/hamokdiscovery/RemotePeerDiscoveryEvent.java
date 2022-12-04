package org.observertc.observer.hamokdiscovery;

public record RemotePeerDiscoveryEvent(
        RemotePeerDiscoveryEventTypes eventType,
        HamokConnection hamokConnection
) {

    public static RemotePeerDiscoveryEvent createRemovedRemotePeerDiscoveryEvent(HamokConnection hamokConnection) {
        return new RemotePeerDiscoveryEvent(
                RemotePeerDiscoveryEventTypes.REMOVED,
                hamokConnection
        );
    }

    public static RemotePeerDiscoveryEvent createAddedRemotePeerDiscoveryEvent(HamokConnection hamokConnection) {
        return new RemotePeerDiscoveryEvent(
                RemotePeerDiscoveryEventTypes.ADDED,
                hamokConnection
        );
    }

}
