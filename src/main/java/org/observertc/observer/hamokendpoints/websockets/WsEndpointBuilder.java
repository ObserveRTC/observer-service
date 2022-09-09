package org.observertc.observer.hamokendpoints.websockets;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.hamokendpoints.BuildersEssentials;
import org.observertc.observer.hamokendpoints.EndpointBuilder;
import org.observertc.observer.hamokendpoints.HamokEndpoint;

import java.util.UUID;

@Prototype
public class WsEndpointBuilder extends AbstractBuilder implements EndpointBuilder {

    private UUID endpointId;
    private BuildersEssentials essentials;

    @Override
    public HamokEndpoint build() {
        var config = this.convertAndValidate(WsEndpointConfig.class);
        return new WsEndpoint(config);
    }

    public void setBuildingEssentials(BuildersEssentials essentials) {
        this.essentials = essentials;
    }

}
