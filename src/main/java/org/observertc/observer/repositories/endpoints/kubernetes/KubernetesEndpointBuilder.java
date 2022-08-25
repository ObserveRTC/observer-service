package org.observertc.observer.repositories.endpoints.kubernetes;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.balazskreith.hamok.mappings.Codec;
import io.github.balazskreith.hamok.mappings.Mapper;
import io.github.balazskreith.hamok.storagegrid.messages.Message;
import io.github.balazskreith.hamok.transports.Endpoint;
import io.github.balazskreith.hamok.transports.UdpSocketEndpoint;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.repositories.endpoints.EndpointBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class KubernetesEndpointBuilder extends AbstractBuilder implements EndpointBuilder {


    public KubernetesEndpointBuilder() {

    }

    private Map<Class, Object> beans = Collections.emptyMap();

    @Override
    public Endpoint build() {
        var config = this.convertAndValidate(Config.class);
        var mapper = new ObjectMapper();
        var endpoint = UdpSocketEndpoint.builder()
                .setUnicastListenerPort(config.unicastListenerPort)
                .setUnicastSendingPort(config.unicastSendingPort)
                .setCodec(Codec.create(
                        Mapper.create(mapper::writeValueAsBytes),
                        Mapper.create(bytes -> mapper.readValue(bytes, Message.class))
                ))
                .build();

        var coreV1Api = this.beans.get(CoreV1Api.class);
        if (coreV1Api == null) {
            throw new RuntimeException("Cannot build KubernetesEndpoint without CoreV1Api bean");
        }
        var k8sApplicationPods = new K8sApplicationPodsDiscovery(
                config.namespace,
                config.podsName,
                (CoreV1Api) coreV1Api
        );

       return new K8sEndpoint(k8sApplicationPods, endpoint);
    }

    @Override
    public void setEndpointId(UUID endpointId) {

    }

    @Override
    public void setBeans(Map<Class, Object> beans) {
        this.beans = beans;
    }

    public static class Config {

        public String namespace;

        public String podsName;

        public int unicastListenerPort = 5602;

        public int unicastSendingPort = 5602;
    }
}
