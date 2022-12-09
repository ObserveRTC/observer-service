package org.observertc.observer.hamokendpoints.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import io.github.balazskreith.hamok.common.UuidTools;
import io.github.balazskreith.hamok.storagegrid.messages.Message;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.observertc.observer.HamokService;
import org.observertc.observer.common.Utils;
import org.observertc.observer.hamokdiscovery.RemotePeerDiscovery;
import org.observertc.observer.hamokendpoints.HamokEndpoint;
import org.observertc.observer.hamokendpoints.HamokMessageCodec;
import org.observertc.schemas.dtos.Hamokmessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


public class WebsocketEndpoint implements HamokEndpoint {
    private static final String INTERNAL_CLOSED_MESSAGE = "INTERNAL_CLOSED_MESSAGE";
    private static final Logger logger = LoggerFactory.getLogger(WebsocketEndpoint.class);

    private final Subject<Message> inboundChannel = PublishSubject.create();
    private final Subject<Message> outboundChannel = PublishSubject.create();
    private final Subject<UUID> stateChangedEvent = PublishSubject.create();

    private final AtomicReference<WebSocketServer> server = new AtomicReference<>(null);
//    private final Map<UUID, UUID> endpointConnectionMappings = new ConcurrentHashMap<>();
//    private final Map<UUID, WebsocketConnection> connections = new ConcurrentHashMap<>();
    private final Map<UUID, WebsocketConnection> remoteEndpoints = new ConcurrentHashMap<>();
    private final Map<UUID, WebsocketConnection> pendingConnections = new ConcurrentHashMap<>();

    private final RemotePeerDiscovery discovery;
    private final ObjectMapper mapper = new ObjectMapper();
    private final int serverPort;
    private final String serverHost;
    private final ExecutorService connectingExecutor = Executors.newSingleThreadExecutor();

    private final HamokMessageCodec codec = new HamokMessageCodec();

    WebsocketEndpoint(RemotePeerDiscovery discovery, String serverHost, int serverPort, int maxMessageSize) {
        this.discovery = discovery;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.outboundChannel.subscribe(message -> {
            if (message == null) {
                return;
            }
//            var data = this.mapper.writeValueAsBytes(message);
            byte[] data = null;
            try {
                var hamokMessage = this.codec.encode(message);
                data = hamokMessage.toByteArray();
            } catch (Throwable t) {
                logger.warn("Exception occurred while encoding message {} to byte array", message, t);
                return;
            }
            if (data == null) {
                logger.warn("Tried to send null data");
                return;
            }
            Iterator<WebsocketConnection> destinations;
            if (message.destinationId == null) {
                destinations = this.remoteEndpoints.values().iterator();
            } else {
                var connection = this.remoteEndpoints.get(message.destinationId);
                if (connection != null) {
                    destinations = Utils.wrapWithIterator(connection);
                } else {
                    destinations = this.remoteEndpoints.values().iterator();
                }
            }
            for (var it = destinations; it.hasNext(); ) {
                var connection = it.next();
                connection.send(data);
            }
        });


        this.discovery.connectionStateChanged().subscribe(connectionStateChangedEvent -> {
            var hamokConnection = connectionStateChangedEvent.hamokConnection();
            var serverUri = this.createUri(hamokConnection.remoteHost(), hamokConnection.remotePort());
            var connectionId = hamokConnection.connectionId();

            logger.info("Connection state changed: {}", connectionStateChangedEvent);

            switch (connectionStateChangedEvent.actualState()) {
                case ACTIVE -> {
                    var connection = new WebsocketConnection(
                            connectionId,
                            ConnectionBuffer.discardingBuffer(),
                            serverUri,
                            this.mapper,
                            Schedulers.from(connectingExecutor),
                            maxMessageSize
                    );
                    logger.info("Add connection to {}. RemoteHost: {}, remotePort: {}",
                            connection.getServerUri(),
                            hamokConnection.remoteHost(),
                            hamokConnection.remotePort()
                    );
                    this.pendingConnections.put(connectionId, connection);
                    connection.endpointStateChanged().subscribe(stateChangeEvent -> {
                        logger.info("Connection endpoint state changed: {}", stateChangeEvent);
                        var remoteEndpointId = stateChangeEvent.endpointId();
                        if (remoteEndpointId == null) {
                            logger.warn("Connection State Changed, but there was no remote endpoint id for the connection {}", stateChangeEvent);
                            return;
                        }
                        switch (stateChangeEvent.state()) {
                            case JOINED -> {
                                this.pendingConnections.remove(connectionId);
                                this.remoteEndpoints.put(remoteEndpointId, connection);
                                logger.info("Connection {} for remote endpoint {} is joined", connectionId, remoteEndpointId);
                            }
                            case DETACHED -> {
                                this.remoteEndpoints.remove(remoteEndpointId);
                                logger.info("Connection {} for remote endpoint {} is detached", connectionId, remoteEndpointId);
                            }
                        }
                        this.stateChangedEvent.onNext(connectionId);
//                        this.stateChangedEvent.onNext(connectionId);
                    });
                    logger.info("Open connection to {}. serverUri: {}", connectionId, serverUri);
                    connection.open();
                }
                case INACTIVE -> {
                    WebsocketConnection connection = this.pendingConnections.get(connectionId);
                    if (connection != null) {
                        logger.info("Remove pending connection for {}. RemoteHost: {}, remotePort: {}",
                                connection.getServerUri(),
                                hamokConnection.remoteHost(),
                                hamokConnection.remotePort()
                        );
                        connection.close();
                        return;
                    }

                    var remoteEndpointIdHolder = this.remoteEndpoints.entrySet()
                            .stream()
                            .filter(entry -> UuidTools.equals(entry.getValue().getConnectionId(), connectionId))
                            .map(Map.Entry::getKey)
                            .findFirst();

                    if (remoteEndpointIdHolder.isEmpty()) {
                        return;
                    }
                    connection = this.remoteEndpoints.remove(remoteEndpointIdHolder.get());
                    if (connection == null) {
                        return;
                    }
                    connection.close();
                    logger.info("Remove connection for {}. RemoteHost: {}, remotePort: {}",
                            connection.getServerUri(),
                            hamokConnection.remoteHost(),
                            hamokConnection.remotePort()
                    );
                    this.stateChangedEvent.onNext(connectionId);
                }
            }
        });
    }

    @Override
    public Set<UUID> getActiveRemoteEndpointIds() {
        return Collections.unmodifiableSet(this.remoteEndpoints.keySet());
    }

    @Override
    public boolean reconnectToEndpoint(UUID endpointId) {
        var connection = this.remoteEndpoints.get(endpointId);
        if (connection == null) {
            // maybe in pending? maybe. if it is pending then it will be checked by connecting to it
            return false;
        }
        return connection.reconnect();
    }

    @Override
    public boolean isReady() {
        return true;
    }

    public Observable<UUID> stateChangedEvent() {
        return this.stateChangedEvent;
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
        this.startServer();
        this.discovery.start();
        logger.info("Initialized");
    }

    @Override
    public boolean isRunning() {
        return this.server.get() != null;
    }

    @Override
    public void stop() {
        this.connectingExecutor.shutdownNow();
        var websocketServer = this.server.getAndSet(null);
        if (websocketServer != null) {
            this.stopServer(websocketServer);
            return;
        }
        this.discovery.stop();
    }

    private WebSocketServer createServer(int attempt) {
        logger.info("Local binding address {}:{}", this.serverHost, this.serverPort);
        var address = new InetSocketAddress(this.serverHost, this.serverPort);
        return new WebSocketServer(address) {

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {

                logger.info("Accepted connection from {}", conn.getRemoteSocketAddress());
                var remoteIdentifiers = new RemoteIdentifiers();
                remoteIdentifiers.endpointId = HamokService.localEndpointId;
                remoteIdentifiers.serverUri = createUri(serverHost, serverPort);
                try {
                    var message = mapper.writeValueAsString(remoteIdentifiers);
                    conn.send(message);
                } catch (JsonProcessingException e) {
                    logger.warn("Failed to send address info to connected localhost", e);
                }
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                logger.info("Closed connection from {}", conn.getRemoteSocketAddress());

            }

            @Override
            public void onMessage(WebSocket conn, String data) {
                Message message = null;
                try {
                    var hamokMessage = Hamokmessage.HamokMessage.parseFrom(data.getBytes(StandardCharsets.UTF_8));
                    message = codec.decode(hamokMessage);
//                    message = mapper.readValue(data, Message.class);
//                    logger.info("Got message from {}, {}", createUri(serverHost, serverPort), message.type);
                    inboundChannel.onNext(message);
                } catch (JsonProcessingException e) {
                    logger.warn("Error occurred while deserializing message", e);
                    return;
                } catch (InvalidProtocolBufferException e) {
                    logger.warn("Error occurred while deserializing message", e);
                } catch (Throwable e) {
                    logger.warn("Error occurred while deserializing message", e);
                }
            }

            @Override
            public void onMessage(WebSocket conn, ByteBuffer data) {
                Message message = null;
                try {
                    var bytes = data.array();
                    if (bytes == null || bytes.length < 1) {
                        logger.info("Got empty message from {}", createUri(serverHost, serverPort));
                        return;
                    }
                    var hamokMessage = Hamokmessage.HamokMessage.parseFrom(bytes);
                    message = codec.decode(hamokMessage);
//                    message = mapper.readValue(bytes, Message.class);
//                    logger.info("Got message from {}, {}", createUri(serverHost, serverPort), message.type);
                    inboundChannel.onNext(message);
                } catch (JsonProcessingException e) {
                    logger.warn("Error occurred while deserializing message", e);
                    return;
                } catch (IOException e) {
                    logger.warn("Error occurred while deserializing message", e);
                    return;
                } catch (Throwable e) {
                    logger.warn("Error occurred while deserializing message", e);
                }
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                logger.warn("Error occurred on connection {}", conn != null ? conn.getRemoteSocketAddress() : "Null", ex);
            }

            @Override
            public void onStart() {
                logger.info("Websocket server for {}:{} is started", serverHost, serverPort);
            }
        };
    }

    private void stopServer(WebSocketServer websocketServer) {
        if (websocketServer == null) {
            return;
        }
        try {
            websocketServer.stop(5000, INTERNAL_CLOSED_MESSAGE);
        } catch (InterruptedException e) {
            logger.warn("Interrupted meanwhile stopped");
            return;
        }
    }

    private void startServer() {
        var websocketServer = this.server.getAndSet(null);
        if (websocketServer != null) {
            this.stopServer(websocketServer);
        }
        websocketServer = this.createServer(0);
        if (!this.server.compareAndSet(null, websocketServer)) {
           this.stopServer(websocketServer);
           return;
        }
        try {
            websocketServer.start();
        } catch (Exception ex) {
            Schedulers.from(this.connectingExecutor).scheduleDirect(() -> {
                startServer();
            }, 5000, TimeUnit.MILLISECONDS);
        }
    }

    private String createUri(String hostname, int port) {
        return String.format("ws://%s:%d", hostname, port);
    }
}
