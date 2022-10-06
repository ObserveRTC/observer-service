package org.observertc.observer.hamokdiscovery.kubernetes;

import javax.validation.constraints.Min;

public class K8sPodsDiscoveryConfig {

    @Min(1024)
    public int port;

    public String namespace = "default";

    public String prefix = null;
}
