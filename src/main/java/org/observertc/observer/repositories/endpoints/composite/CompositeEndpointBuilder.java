package org.observertc.observer.repositories.endpoints.composite;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.balazskreith.hamok.mappings.Codec;
import io.github.balazskreith.hamok.mappings.Mapper;
import io.github.balazskreith.hamok.storagegrid.messages.Message;
import io.github.balazskreith.hamok.transports.CompositeEndpoint;
import io.github.balazskreith.hamok.transports.Endpoint;
import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.repositories.endpoints.EndpointBuilder;

import java.util.Map;
import java.util.UUID;

@Prototype
public class CompositeEndpointBuilder extends AbstractBuilder implements EndpointBuilder {

    private UUID endpointId;

    @Override
    public <S extends Endpoint> S build() {
        var config = this.convertAndValidate(Config.class);
        var mapper = new ObjectMapper();
        var endpoint = CompositeEndpoint.builder()
                .setUnicastListenerPort(config.unicastListenerPort)
                .setUnicastSendingPort(config.unicastSendingPort)
                .setMulticastPort(config.multicastPort)
                .setEndpointId(this.endpointId)
                .setCodec(Codec.create(
                        Mapper.create(mapper::writeValueAsBytes),
                        Mapper.create(bytes -> mapper.readValue(bytes, Message.class))
                ))
                .build();
        return null;
    }

    public void setEndpointId(UUID endpointId) {
        this.endpointId = endpointId;
    }

    @Override
    public void setBeans(Map<Class, Object> beans) {

    }

    public static class Config {

        public int unicastListenerPort = 5601;

        public int unicastSendingPort = 5602;

        public int multicastPort = 5600;
    }
}
