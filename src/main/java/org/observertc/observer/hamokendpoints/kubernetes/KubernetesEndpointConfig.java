package org.observertc.observer.hamokendpoints.kubernetes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KubernetesEndpointConfig {

    public String namespace;

    public String podsName;

    public int unicastListeningPort = 5602;

    public int unicastSendingPort = 5602;

    public String context = "Kubernetes Endpoint";
}
