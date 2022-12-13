package org.observertc.observer.hamokendpoints.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.observertc.observer.common.ObservableState;
import org.observertc.observer.common.Utils;
import org.observertc.observer.hamokdiscovery.HamokDiscovery;
import org.observertc.observer.hamokendpoints.*;
import org.observertc.schemas.dtos.Hamokmessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;


public class WebsocketEndpoint implements HamokEndpoint {
    private static final String INTERNAL_CLOSED_MESSAGE = "INTERNAL_CLOSED_MESSAGE";
    private static final Logger logger = LoggerFactory.getLogger(WebsocketEndpoint.class);
    private static final int CLIENT_HELLO_RECEIVED_FLAG = 1;
    private static final int OPEN_CONNECTION_FLAG = 2;

    private static String createUri(String hostname, int port) {
        return String.format("ws://%s:%d", hostname, port);
    }

    private final Subject<Message> inboundChannel = PublishSubject.create();
    private final Subject<Message> outboundChannel = PublishSubject.create();
    private final Subject<UUID> stateChangedEvent = PublishSubject.create();

    private final ObservableState<HamokEndpointState> state = new ObservableState<>(HamokEndpointState.CREATED);
    private final AtomicReference<WebSocketServer> server = new AtomicReference<>(null);
    private final Map<UUID, WebsocketHamokConnection> remoteEndpoints = new ConcurrentHashMap<>();
    private final Map<UUID, WebsocketHamokConnection> connections = new ConcurrentHashMap<>();

    private final Supplier<HamokDiscovery> discoverySupplier;
    private final ObjectMapper mapper = new ObjectMapper();
    private final int serverPort;
    private final String serverHost;
    private final ExecutorService connectingExecutor = Executors.newSingleThreadExecutor();
    private final int maxMessageSize;
    private final HamokMessageCodec codec = new HamokMessageCodec();
    private final Runnable refreshHamokEndpoints;
    private Map<UUID, Integer> pendingEndpointStates = new HashMap<>();


    WebsocketEndpoint(
            Supplier<HamokDiscovery> discoverySupplier,
            Runnable refreshHamokEndpoints,
            String serverHost,
            int serverPort,
            int maxMessageSize
    ) {
        this.discoverySupplier = discoverySupplier;
        this.refreshHamokEndpoints = refreshHamokEndpoints;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.maxMessageSize = maxMessageSize;
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
            Iterator<WebsocketHamokConnection> destinations;
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
                connection.sendHamokMessage(data);
            }
        });

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
        if (!this.state.compareAndSetState(HamokEndpointState.CREATED, HamokEndpointState.STARTING)) {
            logger.warn("Attempted to start a server not in CREATED state. Actual state is: {}", this.state.get());
            return;
        }
        if (this.server.get() != null) {
            logger.warn("Attempted to start twice");
            return;
        }
        this.startServer();
        var hamokDiscovery = this.discoverySupplier.get();
        if (hamokDiscovery != null) {
            hamokDiscovery.getActiveConnections()
                    .stream()
                    .filter(config -> !this.connections.containsKey(config.connectionId()))
                    .forEach(this::addConnection);
        } else {
            logger.warn("No Discovery service is available for endpoint");
        }
        logger.info("Initialized");
    }

    @Override
    public boolean isRunning() {
        return this.server.get() != null;
    }

    @Override
    public void stop() {
        if (HamokEndpointState.STOPPING.equals(this.state.get()) || HamokEndpointState.STOPPED.equals(this.state.get())) {
            logger.warn("Attempted to stop the server twice");
            return;
        }
        this.state.setState(HamokEndpointState.STOPPING);
        this.connectingExecutor.shutdownNow();
        var websocketServer = this.server.getAndSet(null);
        if (websocketServer != null) {
            this.stopServer(websocketServer);
            return;
        }
        var hamokDiscovery = this.discoverySupplier.get();
        if (hamokDiscovery != null) {
            hamokDiscovery.getActiveConnections().stream().map(c -> c.connectionId()).forEach(this::removeConnection);
        } else {
            logger.warn("No Discovery service is available for endpoint");
        }
    }

    private void setPendingEndpointFlag(UUID endpointId, int flag) {
        // mutex because of the restrictive access not because of the resource
        synchronized (this) {
            String flagStr = null;
            switch (flag) {
                case OPEN_CONNECTION_FLAG -> flagStr = "OPEN_CONNECTION_FLAG";
                case CLIENT_HELLO_RECEIVED_FLAG -> flagStr = "CLIENT_HELLO_RECEIVED_FLAG";
                default -> flagStr = "Unknown (" + flag + ")";
            }
            logger.info("Set state flag {} for endpoint {}", flagStr, endpointId);
            var state = this.pendingEndpointStates.remove(endpointId);
            if (state == null) {
                this.pendingEndpointStates.put(endpointId, flag);
                return;
            }
            state = state | flag;
            if ((state & CLIENT_HELLO_RECEIVED_FLAG) == 0 || (state & OPEN_CONNECTION_FLAG) == 0) {
                this.pendingEndpointStates.put(endpointId, state);
                return;
            }
        }
        // if we are here then the connection is established
        logger.info("Established connection to endpoint {}", endpointId);
        var foundConnection = this.connections.values().stream()
                .filter(connection -> UuidTools.equals(connection.getRemoteEndpointId(), endpointId))
                .findFirst();
        if (foundConnection.isEmpty()) {
            logger.warn("No Connection was found for endpoint {}, though the state indicate it is established", endpointId);
            return;
        }
        this.remoteEndpoints.put(endpointId, foundConnection.get());
        this.refreshHamokEndpoints.run();
    }

    private WebSocketServer createServer() {
        logger.info("Local binding address {}:{}", this.serverHost, this.serverPort);
        var address = new InetSocketAddress(this.serverHost, this.serverPort);
        var pendingStateRequests = new ConcurrentHashMap<UUID, CompletableFuture<EndpointInternalMessage>>();
        return new WebSocketServer(address) {

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                logger.info("Accepted connection from {}", conn.getRemoteSocketAddress());
                var message = EndpointInternalMessage.createServerHello(
                        HamokService.localEndpointId,
                        serverHost,
                        serverPort
                );
                try {
                    var data = mapper.writeValueAsString(message);
                    conn.send(data);
                } catch (Exception e) {
                    logger.warn("Failed to send message {} to connection {}", message, conn.getRemoteSocketAddress(), e);
                    return;
                }
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                logger.info("Closed connection from {}", conn.getRemoteSocketAddress());

                // should reconnect if not internal closing
            }

            @Override
            public void onMessage(WebSocket conn, String data) {
                // internal messages for state and ping requests!
                EndpointInternalMessage message;
                try {
                    message = mapper.readValue(data, EndpointInternalMessage.class);
                    switch (message.type) {
                        case CLIENT_HELLO -> {
                            var clientHello = message.asClientHello();
                            if (clientHello.remoteEndpointId() == null) {
                                logger.warn("Client Hello message received without remote endpoint id");
                                return;
                            }
                            // does the server has a connection to this endpoint?
                            setPendingEndpointFlag(clientHello.remoteEndpointId(), CLIENT_HELLO_RECEIVED_FLAG);
                        }
                        default -> {
                            logger.warn("Cannot parse message {}", data);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Failed to parse data {} from {}", data, conn.getRemoteSocketAddress());
                    return;
                }

            }

            @Override
            public void onMessage(WebSocket conn, ByteBuffer data) {
                // hamok messages
                Message message = null;
                try {
                    var bytes = data.array();
                    if (bytes == null || bytes.length < 1) {
                        logger.info("Got empty message from {}", createUri(serverHost, serverPort));
                        return;
                    }
                    var hamokMessage = Hamokmessage.HamokMessage.parseFrom(bytes);
                    message = codec.decode(hamokMessage);
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
                if (!state.compareAndSetState(HamokEndpointState.STARTING, HamokEndpointState.STARTED)) {
                    logger.warn("Websocket was not in STARTING state when it was ready");
                }
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
        websocketServer = this.createServer();
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

    @Override
    public Set<UUID> getActiveRemoteEndpointIds() {
        return this.remoteEndpoints.keySet();
    }

    @Override
    public Observable<ObservableState.StateChangeEvent<HamokEndpointState>> stateChanged() {
        return this.state.stateChanges();
    }

    @Override
    public void addConnection(HamokConnectionConfig connectionConfig) {
        if (connectionConfig == null) {
            logger.warn("Attempted to add a connection without a config");
            return;
        }
        if (this.connections.containsKey(connectionConfig.connectionId())) {
            logger.warn("Attempted to add a connection twice. {}", connectionConfig);
            return;
        }
        var hasConnection = this.connections.values().stream()
                .anyMatch(connection -> connection.getRemoteHost() == connectionConfig.remoteHost() && connection.getRemotePort() == connectionConfig.remotePort());
        if (hasConnection) {
            logger.warn("Attempted to add a connection twice. {}", connectionConfig);
            return;
        }

        var connection = new WebsocketHamokConnection(
                connectionConfig,
                this.mapper,
                this.maxMessageSize
        ) {

            @Override
            public String getLocalHost() {
                return serverHost;
            }

            @Override
            public int getLocalPort() {
                return serverPort;
            }
        };
        this.connections.put(connection.getConnectionId(), connection);
        connection.stateChange().subscribe(stateChangedEvent -> {
            switch (stateChangedEvent.actualState()) {
                case OPEN -> {
                    var remoteEndpointId = connection.getRemoteEndpointId();
                    if (remoteEndpointId == null) {
                        logger.warn("No remote endpoint is available for remote connection {}", connection.getConnectionId());
                        return;
                    }
                    setPendingEndpointFlag(remoteEndpointId, OPEN_CONNECTION_FLAG);
                }
                case CLOSED -> {
                    var remoteEndpointId = connection.getRemoteEndpointId();
                    if (remoteEndpointId != null) {
                        this.remoteEndpoints.remove(remoteEndpointId);
                        this.refreshHamokEndpoints.run();
                    }
                    if (this.connections.containsKey(connection.getConnectionId())) {
                        this.removeConnection(connection.getConnectionId());
                    }
                    var discovery = this.discoverySupplier.get();
                    if (discovery != null) {
                        discovery.onDisconnect(connection.getConnectionId(), connection.getRemoteHost(), connection.getRemotePort());
                    } else {
                        logger.warn("Connection is closed but discovery service cannot be notificed, because there is not a supplied one");
                    }
                }
            }
        });
        connection.open();
    }

    @Override
    public void removeConnection(UUID connectionId) {
        if (connectionId == null) {
            logger.warn("Attempted to remove a connection without a connectionId");
            return;
        }
        var connection = this.connections.remove(connectionId);
        if (connection == null) {
            logger.debug("Attempted to remove a non existing connection {}", connectionId);
            return;
        }
        connection.close();
    }

    @Override
    public void removeConnectionByEndpointId(UUID endpointId) {
        if (endpointId == null) return;
        var connection = this.remoteEndpoints.get(endpointId);
        if (connection == null) {
            logger.warn("Attempted to remove a not existing connection");
            return;
        }
        this.removeConnection(connection.getConnectionId());
    }
}
