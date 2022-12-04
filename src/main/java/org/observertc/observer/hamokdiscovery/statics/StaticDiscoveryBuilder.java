package org.observertc.observer.hamokdiscovery.statics;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.ConfigConverter;
import org.observertc.observer.hamokdiscovery.DiscoveryBuilder;
import org.observertc.observer.hamokdiscovery.DiscoveryBuildersEssentials;
import org.observertc.observer.hamokdiscovery.HamokConnection;
import org.observertc.observer.hamokdiscovery.RemotePeerDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

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
            var connectionId = UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
            var remotePeerConfig = ConfigConverter.convert(StaticDiscoveryConfig.StaticDiscoveryConfigRemotePeer.class, (Map<String, Object>) entry.getValue());
            var hamokConnection = new HamokConnection(
                    connectionId,
                    remotePeerConfig.host,
                    remotePeerConfig.port
            );
            result.add(hamokConnection);
        }
        return result;
    }


    @Override
    public void setBuildingEssentials(DiscoveryBuildersEssentials essentials) {
        this.essentials = essentials;
    }
}
