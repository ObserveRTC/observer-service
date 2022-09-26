package org.observertc.observer.hamokendpoints.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.balazskreith.hamok.storagegrid.messages.Message;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.observertc.observer.HamokService;
import org.observertc.observer.hamokdiscovery.RemotePeerDiscovery;
import org.observertc.observer.hamokendpoints.HamokEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


public class WebsocketEndpoint implements HamokEndpoint {
    private static final String INTERNAL_CLOSED_MESSAGE = "INTERNAL_CLOSED_MESSAGE";
    private static final Logger logger = LoggerFactory.getLogger(WebsocketEndpoint.class);

    private final PublishSubject<Message> inboundChannel = PublishSubject.create();
    private final PublishSubject<Message> outboundChannel = PublishSubject.create();

    private final AtomicReference<WebSocketServer> server = new AtomicReference<>(null);
    private final Map<UUID, String> addresses = new ConcurrentHashMap<>();
    private final Map<String, WebsocketConnection> connections = new ConcurrentHashMap<>();
    private final RemotePeerDiscovery discovery;
    private final ObjectMapper mapper = new ObjectMapper();
    private final int serverPort;
    private final String serverHost;
    private final long createdTimestampInSec = Instant.now().getEpochSecond();
    private final ExecutorService connectingExecutor = Executors.newSingleThreadExecutor();

    WebsocketEndpoint(RemotePeerDiscovery discovery, String serverHost, int serverPort) {
        this.discovery = discovery;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.outboundChannel.subscribe(message -> {
            if (message == null) {
                return;
            }
            var data = this.mapper.writeValueAsString(message);
            if (data == null) {
                logger.warn("Tried to send null data");
                return;
            }
            Iterable<WebsocketConnection> connections;
            if (message.destinationId == null) {
                connections = this.connections.values();
            } else {
                var serverUri = this.addresses.get(message.destinationId);
                if (serverUri != null) {
                    var connection = this.connections.get(serverUri);
                    if (connection != null) {
                        connections = List.of(connection);
                    } else {
                        connections = this.connections.values();
                    }
                } else {
                    connections = this.connections.values();
                }
            }
            for (var connection : connections) {
                try {
                    connection.send(data);
                } catch (Exception ex) {
                    logger.warn("Error while sending data", ex);
                }
            }
        });

        this.discovery.events().subscribe(event -> {
            var remotePeer = event.remotePeer();
            var serverUri = this.createUri(remotePeer.host(), remotePeer.port());
            switch (event.eventType()) {
                case ADDED -> {
                    var connection = new WebsocketConnection(
                            ConnectionBuffer.discardingBuffer(),
                            serverUri,
                            this.mapper,
                            Schedulers.from(connectingExecutor)
                    );
                    logger.info("Add connection to {}. RemoteHost: {}, remotePort: {}",
                            connection.getServerUri(),
                            event.remotePeer().host(),
                            event.remotePeer().port()
                    );
                    connection.remoteIdentifiers().subscribe(remoteIdentifier -> {
                        this.addresses.put(remoteIdentifier.endpointId, connection.getServerUri());
                        logger.info("Remote endpoint id {} is bound to {}",
                                remoteIdentifier.endpointId,
                                connection.getServerUri()
                        );
                    });
                    this.connections.put(connection.getServerUri(), connection);
                    connection.open();
                }
                case REMOVED -> {
                    var connection = this.connections.remove(serverUri);
                    if (connection != null) {
                        logger.info("Remove connection for {}. RemoteHost: {}, remotePort: {}",
                                connection.getServerUri(),
                                event.remotePeer().host(),
                                event.remotePeer().port()
                        );
                        var remoteIdentifier = connection.getRemoteIdentifiers();
                        if (remoteIdentifier != null && remoteIdentifier.endpointId != null) {
                            this.addresses.remove(remoteIdentifier.endpointId);
                        }
                        connection.close();
                    }
                }
            }
        });
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public int elapsedSecSinceReady() {
        return (int) (Instant.now().getEpochSecond() - createdTimestampInSec);
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
                    message = mapper.readValue(data, Message.class);
//                    logger.info("Got message from {}, {}", createUri(serverHost, serverPort), message.type);
                    inboundChannel.onNext(message);
                } catch (JsonProcessingException e) {
                    logger.warn("Error occurred while deserializing message", e);
                    return;
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
