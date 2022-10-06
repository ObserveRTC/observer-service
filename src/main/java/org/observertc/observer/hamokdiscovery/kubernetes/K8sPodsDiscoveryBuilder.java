package org.observertc.observer.hamokdiscovery.kubernetes;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.hamokdiscovery.DiscoveryBuilder;
import org.observertc.observer.hamokdiscovery.DiscoveryBuildersEssentials;
import org.observertc.observer.hamokdiscovery.RemotePeerDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class K8sPodsDiscoveryBuilder extends AbstractBuilder implements DiscoveryBuilder {
    private static final Logger logger = LoggerFactory.getLogger(K8sPodsDiscoveryBuilder.class);

    private DiscoveryBuildersEssentials essentials;

    @Override
    public RemotePeerDiscovery build() {
        var config = this.convertAndValidate(K8sPodsDiscoveryConfig.class);
        return new K8sPodsDiscovery(
                config.namespace,
                config.prefix,
                config.port,
                this.essentials.coreV1ApiProvider().get()
        );
    }

    @Override
    public void setBuildingEssentials(DiscoveryBuildersEssentials essentials) {
        this.essentials = essentials;
    }
}
