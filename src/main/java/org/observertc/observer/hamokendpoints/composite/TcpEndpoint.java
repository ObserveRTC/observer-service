package org.observertc.observer.hamokendpoints.composite;

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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class TcpEndpoint implements HamokEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(TcpEndpoint.class);

    private final PublishSubject<Message> inboundChannel = PublishSubject.create();
    private final PublishSubject<Message> outboundChannel = PublishSubject.create();
    private AtomicReference<DisposableServer> server = new AtomicReference<>(null);
    private final List<Connection> connections = Collections.synchronizedList(new LinkedList<>());
    private final CompositeEndpointConfig config;
    private final ObjectMapper mapper = new ObjectMapper();

    TcpEndpoint(CompositeEndpointConfig config) {
        this.config = config;
        this.connections.add(TcpClient.create()
                        .host("localhost")
                        .port(this.config.unicastSendingPort)
                        .connectNow()
        );
        this.outboundChannel.subscribe(message -> {
            var bytes = mapper.writeValueAsBytes(message);
            for (var connection : this.connections) {
                connection.outbound().sendByteArray(Mono.just(bytes));
            }
        });
    }

    private DisposableServer createServer() {
        return TcpServer.create()
                .host("localhost")
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
        var server = this.server.getAndSet(null);
        if (server != null) {
            server.dispose();
        }
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public int elapsedSecSinceReady() {
        return 999999999;
    }
}
