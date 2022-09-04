package org.observertc.observer.hamokendpoints;

import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.micronaut.context.BeanProvider;

import java.util.UUID;

public record BuildersEssentials(
        BeanProvider<CoreV1Api> coreV1ApiProvider,
        UUID localEndpointId
) {
}
