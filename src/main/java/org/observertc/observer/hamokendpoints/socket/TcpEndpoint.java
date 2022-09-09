package org.observertc.observer.hamokendpoints.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.balazskreith.hamok.storagegrid.messages.Message;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.schedulers.Schedulers;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


public class TcpEndpoint implements HamokEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(TcpEndpoint.class);

    private final PublishSubject<Message> inboundChannel = PublishSubject.create();
    private final PublishSubject<Message> outboundChannel = PublishSubject.create();
    private AtomicReference<DisposableServer> server = new AtomicReference<>(null);
    private final List<Connection> connections = Collections.synchronizedList(new LinkedList<>());
    private final SocketEndpointConfig config;
    private final ObjectMapper mapper = new ObjectMapper();

    TcpEndpoint(SocketEndpointConfig config) {
        this.config = config;
        this.outboundChannel.subscribe(message -> {
            var bytes = mapper.writeValueAsBytes(message);
            for (var connection : this.connections) {
                logger.info("Sending message {} to {}", message.type, connection.address());
                connection.outbound().sendByteArray(Mono.just(bytes)).then();
            }
        });
        this.tryConnect(0);
    }

    private DisposableServer createServer() {
        logger.info("Local binding address {}:{}", this.config.localhost, this.config.unicastListeningPort);
        return TcpServer.create()
//                .host(this.config.localhost)
                .port(config.unicastListeningPort)
                .handle((inbound, outbound) -> outbound.sendString(Mono.just("hello")))
//                .handle((inbound, outbound) -> inbound.receive().then())

//                .doOnConnection(connection -> {
//                    logger.info("Connection accepted from {}", connection.address());
//                    connection.inbound().receive().asByteArray().subscribe(bytes -> {
//                        try {
//                            var message = this.mapper.readValue(bytes, Message.class);
//                            logger.info("Receiving message {}", message.type);
//                        } catch (Exception ex) {
//                            logger.warn("Exception occurred while decoding message", ex);
//                        }
//                    });
//                })
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
        logger.info("Initialized");
    }

    private void tryConnect(int attempt) {
        logger.info("Try connect to address {}:{}", this.config.remoteHost, this.config.unicastSendingPort);
        try {
            var client = TcpClient.create()
                    .host(this.config.remoteHost)
                    .port(this.config.unicastSendingPort)
                    .handle((inbound, outbound) -> outbound.sendString(Mono.just("hello")))
                    .connectNow();
            logger.info("Success on connecting");
            this.connections.add(client);
        } catch (Exception ex) {
            if (10 < attempt) {
                throw ex;
            }
            Schedulers.io().scheduleDirect(() -> {
                this.tryConnect(attempt + 1);
            }, 3000, TimeUnit.MILLISECONDS);
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
