package org.observertc.observer.repositories.endpoints.kubernetes;

import io.github.balazskreith.hamok.storagegrid.messages.Message;
import io.github.balazskreith.hamok.transports.Endpoint;
import io.github.balazskreith.hamok.transports.UdpSocketEndpoint;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class K8sEndpoint implements Endpoint {

    private static final Logger logger = LoggerFactory.getLogger(K8sEndpoint.class);

    private final UdpSocketEndpoint endpoint;
    private final K8sApplicationPodsDiscovery k8SApplicationPodsDiscovery;

    K8sEndpoint(K8sApplicationPodsDiscovery k8SApplicationPodsDiscovery, UdpSocketEndpoint endpoint) {
        this.endpoint = endpoint;
        this.k8SApplicationPodsDiscovery = k8SApplicationPodsDiscovery;

        this.k8SApplicationPodsDiscovery.subscribe(applicationPod -> {
            switch (applicationPod.eventType()) {
                case ADDED -> this.endpoint.addRemoteAddress(applicationPod.address());
                case REMOVED -> this.endpoint.removeRemoteAddress(applicationPod.address());
            }
            logger.info("Event {} inetaddress: {}", applicationPod.eventType(), applicationPod.address());
        });
    }

    @Override
    public Observable<Message> inboundChannel() {
        return this.endpoint.inboundChannel();
    }

    @Override
    public Observer<Message> outboundChannel() {
        return this.endpoint.outboundChannel();
    }

    @Override
    public void start() {
        this.k8SApplicationPodsDiscovery.start();
        this.endpoint.start();
    }

    @Override
    public boolean isRunning() {
        return this.endpoint.isRunning();
    }

    @Override
    public void stop() {
        this.endpoint.stop();
        this.k8SApplicationPodsDiscovery.stop();
    }
}
