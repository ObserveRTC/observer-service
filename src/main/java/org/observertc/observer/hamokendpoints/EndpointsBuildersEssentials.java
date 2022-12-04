package org.observertc.observer.hamokendpoints;

import org.observertc.observer.hamokdiscovery.DiscoveryBuilderService;

public record EndpointsBuildersEssentials(
        DiscoveryBuilderService discoveryBuilderService,
        Runnable refreshRemoteEndpointIdsCallback
) {
}
