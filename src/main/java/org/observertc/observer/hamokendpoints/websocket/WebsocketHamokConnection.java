package org.observertc.observer.hamokendpoints.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.Opcode;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.observertc.observer.common.ObservableState;
import org.observertc.observer.hamokendpoints.EndpointInternalMessage;
import org.observertc.observer.hamokendpoints.HamokConnection;
import org.observertc.observer.hamokendpoints.HamokConnectionConfig;
import org.observertc.observer.hamokendpoints.HamokConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class WebsocketHamokConnection implements HamokConnection {

    private static final int INTERNAL_CLOSE_REASON = 4224;
    private static final Logger logger = LoggerFactory.getLogger(WebsocketHamokConnection.class);

    private static String createUri(String hostname, int port) {
        return String.format("ws://%s:%d", hostname, port);
    }

    private static boolean tryConnect(WebSocketClient client) {
        try {
            logger.info("Trying to connect to {}", client.getRemoteSocketAddress());
            return client.connectBlocking(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("Interrupted while connecting to {}", client.getRemoteSocketAddress(), e);
            return false;
        }
    }

    private final HamokConnectionConfig config;
    private final String serverUri;
    private final Subject<byte[]> outgoingMessages = PublishSubject.create();
    private final ObservableState<HamokConnectionState> state = new ObservableState<>(HamokConnectionState.CREATED);
    private final AtomicReference<WebSocketClient> client = new AtomicReference<>(null);
    private final AtomicReference<Disposable> outgoingMessageSubscriber = new AtomicReference<>(null);
    private final AtomicReference<UUID> remoteEndpointId = new AtomicReference<>(null);
    private final Map<UUID, CompletableFuture<EndpointInternalMessage>> pendingRequests = new ConcurrentHashMap<>();
    private final ObjectMapper mapper;
    private final int maxMessageSize;


    public WebsocketHamokConnection(
            HamokConnectionConfig config,
            ObjectMapper mapper,
            int maxMessageSize
    ) {
        this.config = config;
        this.mapper = mapper;
        this.maxMessageSize = maxMessageSize;
        this.serverUri = createUri(config.remoteHost(), config.remotePort());
        this.state.stateChanges().subscribe(stateChange -> {
            switch (stateChange.actualState()) {
                case OPEN -> {
                    var subscriber = this.outgoingMessages
                        .observeOn(Schedulers.io())
                        .subscribe(data -> {
                            var client = this.client.get();
                            if (client == null || client.getReadyState() != ReadyState.OPEN) {
                                logger.warn("Cannot send message to a not ready connection. ");
                                return;
                            }
                            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
                            for (int position = this.maxMessageSize; ; position += this.maxMessageSize) {
                                if (position < byteBuffer.capacity()) {
                                    byteBuffer.limit(position);
                                    client.sendFragmentedFrame(Opcode.BINARY, byteBuffer, false);
                                    continue;
                                }
                                byteBuffer.limit(byteBuffer.capacity());
                                client.sendFragmentedFrame(Opcode.BINARY, byteBuffer, true);
                                break;
                            }
                        });
                    var prevSubscriber = this.outgoingMessageSubscriber.getAndSet(subscriber);
                    if (prevSubscriber != null) {
                        logger.warn("After opening a connection a subscriber is found to outgoing messages. will be disposed");
                        prevSubscriber.dispose();
                    }
                }
                case CLOSING -> {
                    var subscriber = this.outgoingMessageSubscriber.getAndSet(null);
                    if (subscriber != null) {
                        subscriber.dispose();
                    }
                }
            }
        });
    }

    @Override
    public UUID getConnectionId() {
        return this.config.connectionId();
    }

    @Override
    public HamokConnectionState getState() {
        return this.state.get();
    }

    @Override
    public Observable<ObservableState.StateChangeEvent<HamokConnectionState>> stateChange() {
        return this.state.stateChanges();
    }

    @Override
    public void open() {
        if (!this.state.compareAndSetState(HamokConnectionState.CREATED, HamokConnectionState.CONNECTING)) {
            logger.warn("Attempted to open a connection already in {} state", this.getState());
            return;
        }
        var client = this.client.getAndSet(null);
        if (client != null) {
            if (!client.isClosed()) {
                logger.warn("Found a client in connection trying to open.");
                client.close(INTERNAL_CLOSE_REASON);
            }
        }
        client = this.createClient();
        if (!this.client.compareAndSet(null, client)) {
            logger.warn("Concurrent opening for config {}", this.config);
            client.close(INTERNAL_CLOSE_REASON);
            return;
        }
        tryConnect(client);
    }

    @Override
    public void close() {
        if (HamokConnectionState.CLOSED.equals(state.get()) || HamokConnectionState.CLOSING.equals(state.get())) {
            logger.warn("Attempted to close connection twice {}", this.config);
            return;
        }
        this.state.setState(HamokConnectionState.CLOSING);
        var client = this.client.getAndSet(null);
        if (client != null) {
            client.close(INTERNAL_CLOSE_REASON);
        } else {
            this.state.setState(HamokConnectionState.CLOSED);
        }
    }

    public void sendHamokMessage(byte[] data) {
        this.outgoingMessages.onNext(data);
    }

    public UUID getRemoteEndpointId() {
        return this.remoteEndpointId.get();
    }

    public EndpointInternalMessage.StateResponse requestState() {
        var request = EndpointInternalMessage.createStateRequest(
                UUID.randomUUID()
        );
        var response = this.request(request);
        if (response == null) {
            return null;
        }
        return response.asStateResponse();
    }

    private WebSocketClient createClient() {
        return new WebSocketClient(URI.create(this.serverUri)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                logger.info("Connected to {}, address is {}", this.uri, this.getRemoteSocketAddress());
            }

            @Override
            public void onMessage(String data) {
                // internal message always in string!
                // handling internal requests from the server to give state if necessary
                try {
                    var internalMessage = mapper.readValue(data, EndpointInternalMessage.class);
                    switch (internalMessage.type) {
                        case OPEN_NOTIFICATION -> {
                            var openNotification = internalMessage.asOpenNotification();
                            if (openNotification.remoteEndpointId() == null) {
                                logger.warn("No remote endpoint was reported in open notification");
                                return;
                            }
                            remoteEndpointId.set(openNotification.remoteEndpointId());
                            if (state.compareAndSetState(HamokConnectionState.CONNECTING, HamokConnectionState.OPEN)) {
                                logger.info("Connection is opened {}", config);
                            } else {
                                logger.warn("Expected to be in a {} state, but found {}", HamokConnectionState.CONNECTING, getState());
                            }

                        }
                        case STATE_RESPONSE -> {
                            var pendingRequest = pendingRequests.get(internalMessage.requestId);
                            if (pendingRequest == null) {
                                logger.warn("Cannot resolve a non-existing request for response {}", internalMessage);
                                return;
                            }
                            pendingRequest.complete(internalMessage);
                        }
                    }
                } catch (JsonProcessingException e) {
                    logger.warn("Error in received message {}", data, e);
                    return;
                } catch (Exception ex) {
                    logger.warn("Error occurred while executing operations for received message {}", data, ex);
                    return;
                }
            }


            @Override
            public void onClose(int code, String reason, boolean remote) {
                logger.info("Connection {} is closed. code: {}, reason: {}, byremote: {}", config, code, reason, remote);
                state.setState(HamokConnectionState.CLOSED);
            }

            @Override
            public void onError(Exception ex) {
                logger.warn("Connection to {} is reported an error", serverUri, ex);
            }
        };
    }

    private EndpointInternalMessage request(EndpointInternalMessage message) {
        if (message == null || message.requestId == null) {
            logger.warn("Cannot send a request without message or requestId", message);
            return null;
        }
        var client = this.client.get();
        if (client == null || client.isClosed()) {
            logger.warn("Attempted to send request to a non-existing or closed client");
            return null;
        }

        String data;
        try {
            data = this.mapper.writeValueAsString(message);
        } catch (Exception e) {
            logger.warn("Failed to parse message to string {}", message);
            return null;
        }
        var promise = new CompletableFuture<EndpointInternalMessage>();
        this.pendingRequests.put(message.requestId, promise);
        client.send(data);

        try {
            return promise.get(30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.warn("Error occurred while waiting for a response for message {}", message, e);
            return null;
        }
    }
}
