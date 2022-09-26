package org.observertc.observer.hamokdiscovery;

import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.micronaut.context.BeanProvider;

public record DiscoveryBuildersEssentials(
        BeanProvider<CoreV1Api> coreV1ApiProvider
) {
}
