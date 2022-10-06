package org.observertc.observer.hamokdiscovery.statics;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.ConfigConverter;
import org.observertc.observer.hamokdiscovery.DiscoveryBuilder;
import org.observertc.observer.hamokdiscovery.DiscoveryBuildersEssentials;
import org.observertc.observer.hamokdiscovery.RemotePeer;
import org.observertc.observer.hamokdiscovery.RemotePeerDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Prototype
public class StaticDiscoveryBuilder extends AbstractBuilder implements DiscoveryBuilder {
    private static final Logger logger = LoggerFactory.getLogger(StaticDiscoveryBuilder.class);

    private DiscoveryBuildersEssentials essentials;

    @Override
    public RemotePeerDiscovery build() {
        var config = this.convertAndValidate(StaticDiscoveryConfig.class);
        var result = new StaticDiscovery();
        for (var entry : config.peers.entrySet()) {
            var key = entry.getKey();
            var remotePeerConfig = ConfigConverter.convert(StaticDiscoveryConfig.StaticDiscoveryConfigRemotePeer.class, (Map<String, Object>) entry.getValue());
            var remotePeer = new RemotePeer(remotePeerConfig.host, remotePeerConfig.port);
            result.add(key, remotePeer);
        }
        return result;
    }


    @Override
    public void setBuildingEssentials(DiscoveryBuildersEssentials essentials) {
        this.essentials = essentials;
    }
}
