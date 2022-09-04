package org.observertc.observer.hamokendpoints.kubernetes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.balazskreith.hamok.storagegrid.messages.Message;
import io.github.balazskreith.hamok.transports.UdpSocketEndpoint;
import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.hamokendpoints.BuildersEssentials;
import org.observertc.observer.hamokendpoints.EndpointBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Function;

@Prototype
public class KubernetesEndpointBuilder extends AbstractBuilder implements EndpointBuilder {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesEndpointBuilder.class);

    private BuildersEssentials essentials;

    public KubernetesEndpointBuilder() {

    }

    @Override
    public K8sEndpoint build() {
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
        var endpoint = UdpSocketEndpoint.builder()
                .setUnicastListenerPort(config.unicastListenerPort)
                .setUnicastSendingPort(config.unicastSendingPort)
                .setEncoder(encoder)
                .setDecoder(decoder)
                .build();

        var k8sApplicationPods = new K8sApplicationPodsDiscovery(
                config.namespace,
                config.podsName,
                this.essentials.coreV1ApiProvider().get()
        );

       return new K8sEndpoint(k8sApplicationPods, endpoint);
    }

    public void setBuildingEssentials(BuildersEssentials essentials) {
        this.essentials = essentials;
    }

    public static class Config {

        public String namespace;

        public String podsName;

        public int unicastListenerPort = 5602;

        public int unicastSendingPort = 5602;
    }
}
