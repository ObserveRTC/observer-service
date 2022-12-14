package org.observertc.observer.hamokendpoints.websocket;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.hamokendpoints.EndpointBuilder;
import org.observertc.observer.hamokendpoints.EndpointsBuildersEssentials;
import org.observertc.observer.hamokendpoints.HamokEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Prototype
public class WebsocketEndpointBuilder extends AbstractBuilder implements EndpointBuilder {
    private static final Logger logger = LoggerFactory.getLogger(WebsocketEndpointBuilder.class);

    private EndpointsBuildersEssentials essentials;

    @Override
    public HamokEndpoint build() {
        var config = this.convertAndValidate(WebsocketEndpointConfig.class);
        String serverHost;
        try {
            serverHost = Utils.firstNotNull(config.serverHost,
                    InetAddress.getLocalHost().getCanonicalHostName(),
                    InetAddress.getLocalHost().getHostName()
            );
        } catch (UnknownHostException e) {
            logger.warn("Error occurred while retrieving localhost name. the default will be used, which is localhost", e);
            serverHost = "localhost";
        }
        var result = new WebsocketEndpoint(
                this.essentials.hamokDiscoveryService(),
                this.essentials.refreshRemoteEndpointIdsCallback(),
                serverHost,
                config.serverPort,
                config.maxMessageSize
        );

        var refreshRemoteEndpointIdsCallback = this.essentials.refreshRemoteEndpointIdsCallback();
        if (refreshRemoteEndpointIdsCallback != null) {
            result.stateChangedEvent().subscribe(event -> {
                refreshRemoteEndpointIdsCallback.run();
            });
        } else {
            logger.warn("Missing refreshRemoteEndpointIdsCallback from builder essentials, will not add remote endpoints!");
        }
        return result;
    }


    @Override
    public void setBuildingEssentials(EndpointsBuildersEssentials essentials) {
        this.essentials = essentials;
    }

}
