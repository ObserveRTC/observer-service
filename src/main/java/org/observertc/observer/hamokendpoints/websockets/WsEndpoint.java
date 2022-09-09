package org.observertc.observer.hamokendpoints.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.balazskreith.hamok.storagegrid.messages.Message;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.observertc.observer.hamokendpoints.HamokEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;


public class WsEndpoint implements HamokEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(WsEndpoint.class);

    private final PublishSubject<Message> inboundChannel = PublishSubject.create();
    private final PublishSubject<Message> outboundChannel = PublishSubject.create();
    private AtomicReference<WebSocketServer> server = new AtomicReference<>(null);

    private final Map<UUID, InetSocketAddress> addresses = new ConcurrentHashMap<>();
    private final Map<InetSocketAddress, WebSocket> clients = new ConcurrentHashMap<>();

    private final WsEndpointConfig config;
    private final ObjectMapper mapper = new ObjectMapper();

    WsEndpoint(WsEndpointConfig config) {
        this.config = config;
        this.outboundChannel.subscribe(message -> {
            var data = this.mapper.writeValueAsString(message);
            if (data == null) {
                logger.warn("Tried to send null data");
                return;
            }
            for (var ws : this.clients) {
                try {
                    ws.send(data);
                } catch (Exception ex) {
                    logger.warn("Error while sending data", ex);
                }

            }
        });

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
            try {
                server.stop(1000, "closed");
            } catch (InterruptedException e) {
                logger.warn("Error while stopping websocket server", e);
            }
        } else {
            server.start();
        }
        this.tryConnect(0);
        logger.info("Initialized");
    }

    private void tryConnect(int attempt) {
        logger.info("Try connect to address {}:{}", this.config.remoteHost, this.config.unicastSendingPort);
        var uri = String.format("ws://%s:%d", this.config.remoteHost, this.config.unicastSendingPort);
        var client = new WebSocketClient(URI.create(uri)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                logger.info("Success on connecting");
            }

            @Override
            public void onMessage(String data) {
                Message message = null;
                try {
                    message = mapper.readValue(data, Message.class);
                } catch (JsonProcessingException e) {
                    logger.warn("Error", e);
                    return;
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {

            }

            @Override
            public void onError(Exception ex) {
                logger.warn("Unsuccessful connect", ex);
            }
        };
        try {
            if (!client.connectBlocking()) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.tryConnect(attempt + 1);
            } else {
                logger.info("Connected to {}", client.getRemoteSocketAddress().getAddress());
                this.clients.add(client);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
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
            try {
                server.stop(1000, "closed");
            } catch (InterruptedException e) {
                logger.warn("Error while stopping websocket server", e);
            }
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

    private WebSocketServer createServer() {
        logger.info("Local binding address {}:{}", this.config.localhost, this.config.unicastListeningPort);
        var address = new InetSocketAddress(this.config.localhost, this.config.unicastListeningPort);
        var server = new WebSocketServer(address) {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                logger.info("Got websocket, remoteAddress {}", conn.getReadyState(), conn.getRemoteSocketAddress().getAddress());
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {

            }

            @Override
            public void onMessage(WebSocket conn, String data) {
//                logger.info("Got message {}", data);
                Message message = null;
                try {
                    message = mapper.readValue(data, Message.class);
                } catch (JsonProcessingException e) {
                    logger.warn("Error", e);
                    return;
                }
                inboundChannel.onNext(message);
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {

            }

            @Override
            public void onStart() {

            }
        };

    }
}
