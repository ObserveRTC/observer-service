package org.observertc.observer.hamokendpoints;

import org.observertc.observer.hamokdiscovery.HamokDiscoveryService;

public record EndpointsBuildersEssentials(
        HamokDiscoveryService hamokDiscoveryService,
        Runnable refreshRemoteEndpointIdsCallback
) {
}
