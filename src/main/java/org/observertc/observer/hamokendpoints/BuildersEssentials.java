package org.observertc.observer.hamokendpoints;

import io.kubernetes.client.openapi.apis.CoreV1Api;

import java.util.UUID;

public record BuildersEssentials(
        CoreV1Api coreV1Api,
        UUID localEndpointId
) {
}
