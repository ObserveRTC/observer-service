package org.observertc.observer.hamokendpoints.kubernetes;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.balazskreith.hamok.storagegrid.messages.Message;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.observertc.observer.hamokendpoints.HamokEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpClient;
import reactor.netty.tcp.TcpServer;

import java.net.InetAddress;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;


public class K8sEndpointNew implements HamokEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(K8sEndpointNew.class);

    private final K8sApplicationPodsDiscovery k8SApplicationPodsDiscovery;
    private final PublishSubject<Message> inboundChannel = PublishSubject.create();
    private final PublishSubject<Message> outboundChannel = PublishSubject.create();
    private AtomicReference<DisposableServer> server = new AtomicReference<>(null);
    private final Map<InetAddress, Connection> connections = new ConcurrentHashMap<>();
    private final KubernetesEndpointConfig config;
    private final ObjectMapper mapper = new ObjectMapper();

    K8sEndpointNew(K8sApplicationPodsDiscovery k8SApplicationPodsDiscovery, KubernetesEndpointConfig config) {
        this.k8SApplicationPodsDiscovery = k8SApplicationPodsDiscovery;
        this.config = config;
        this.k8SApplicationPodsDiscovery.events().subscribe(applicationPod -> {
            logger.info("Endpoint {} is {}", applicationPod.address(), applicationPod.eventType());
            switch (applicationPod.eventType()) {
                case ADDED -> {
                    var connection = TcpClient.create()
                            .host(applicationPod.address().getHostAddress())
                            .port(config.unicastListeningPort)
                            .connectNow();

                    this.connections.put(applicationPod.address(), connection);
                }
                case REMOVED -> {
                    var connection = this.connections.remove(applicationPod.address());
                    if (connection != null) {
                        connection.disposeNow(Duration.ofMillis(5000));
                    }
                }
            }
        });
        this.outboundChannel.subscribe(message -> {
            var bytes = mapper.writeValueAsBytes(message);
            for (var connection : this.connections.values()) {
                connection.outbound().sendByteArray(Mono.just(bytes));
            }
        });
    }

    private DisposableServer createServer() {
        var host = k8SApplicationPodsDiscovery.getLocalAddresses().stream()
                .map(addr -> addr.getHostAddress())
                .filter(localHost -> !localHost.equals("localhost"))
                .findFirst().orElse("localhost");
        return TcpServer.create()
                .host(host)
                .port(config.unicastListeningPort)
                .handle((inbound, outbound) -> {
//                            inbound.withConnection(connection -> {
//                                var address = connection.address();
//
//                            })
                    inbound.receive().asByteArray()
                            .subscribe(bytes -> {
                                try {
                                    var message = this.mapper.readValue(bytes, Message.class);
                                    this.inboundChannel.onNext(message);
                                } catch (Exception ex) {
                                    logger.warn("Exception occurred while decoding message", ex);
                                }
                            });
                    return outbound.neverComplete();
                })
                .bindNow();
    }

    @Override
    public Observable<Message> inboundChannel() {
        return this.inboundChannel;
    }

    @Override
    public Observer<Message> outboundChannel() {
        return this.outboundChannel;
    }

    @Override
    public void start() {
        if (this.server.get() != null) {
            logger.warn("Attempted to start twice");
            return;
        }
        this.k8SApplicationPodsDiscovery.start();
        var server = this.createServer();
        if (!this.server.compareAndSet(null, server)) {
            server.dispose();
        }
    }

    @Override
    public boolean isRunning() {
        return this.server.get() != null;
    }

    @Override
    public void stop() {
        if (this.server.get() == null) {
            return;
        }
        this.k8SApplicationPodsDiscovery.stop();
        var server = this.server.getAndSet(null);
        if (server != null) {
            server.dispose();
        }
    }

    @Override
    public boolean isReady() {
        return this.k8SApplicationPodsDiscovery.isReady();
    }

    @Override
    public int elapsedSecSinceReady() {
        return this.k8SApplicationPodsDiscovery.elapsedSecSinceReady();
    }
}
