package org.observertc.observer.repositories.endpoints.composite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.balazskreith.hamok.storagegrid.messages.Message;
import io.github.balazskreith.hamok.transports.CompositeEndpoint;
import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.repositories.endpoints.BuildersEssentials;
import org.observertc.observer.repositories.endpoints.EndpointBuilder;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;

@Prototype
public class CompositeEndpointBuilder extends AbstractBuilder implements EndpointBuilder {

    private UUID endpointId;
    private BuildersEssentials essentials;

    @Override
    public CompositeEndpoint build() {
        var config = this.convertAndValidate(Config.class);
        var mapper = new ObjectMapper();
        Function<Message, byte[]> encoder = message -> {
            try {
                return mapper.writeValueAsBytes(message);
            } catch (JsonProcessingException e) {
                logger.error("Error occurred while serializing message {}", message, e);
                return null;
            }
        };
        Function<byte[], Message> decoder = bytes -> {
            try {
                return mapper.readValue(bytes, Message.class);
            } catch (IOException e) {
                logger.error("Error occurred while serializing message", e);
                return null;
            }
        };
        var endpoint = CompositeEndpoint.builder()
                .setUnicastListenerPort(config.unicastListenerPort)
                .setUnicastSendingPort(config.unicastSendingPort)
                .setMulticastPort(config.multicastPort)
                .setEndpointId(this.endpointId)
                .setEncoder(encoder)
                .setDecoder(decoder)
                .build();
        return endpoint;
    }

    public void setBuildingEssentials(BuildersEssentials essentials) {
        this.essentials = essentials;
    }

    public static class Config {

        public int unicastListenerPort = 5601;

        public int unicastSendingPort = 5602;

        public int multicastPort = 5600;
    }
}
