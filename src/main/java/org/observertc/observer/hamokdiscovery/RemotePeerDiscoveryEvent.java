package org.observertc.observer.hamokdiscovery;

public record RemotePeerDiscoveryEvent(
        RemotePeerDiscoveryEventTypes eventType,
        RemotePeer remotePeer
) {

    public static RemotePeerDiscoveryEvent createRemovedRemotePeerDiscoveryEvent(RemotePeer remotePeer) {
        return new RemotePeerDiscoveryEvent(
                RemotePeerDiscoveryEventTypes.REMOVED,
                remotePeer
        );
    }

    public static RemotePeerDiscoveryEvent createAddedRemotePeerDiscoveryEvent(RemotePeer remotePeer) {
        return new RemotePeerDiscoveryEvent(
                RemotePeerDiscoveryEventTypes.ADDED,
                remotePeer
        );
    }

}
