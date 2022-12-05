package org.observertc.observer.hamokendpoints.websocket;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.Opcode;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class WebsocketConnection {

    private static final Logger logger = LoggerFactory.getLogger(WebsocketConnection.class);

    private static final int CLOSE_WITHOUT_RECONNECT_CODE = 7887;

    public enum EndpointState {
        JOINED,
        DETACHED
    }

    public record EndpointStateChange(
            EndpointState state,
            UUID endpointId
    ){

    }

    private final AtomicReference<Disposable> connecting = new AtomicReference<>(null);
    private final AtomicReference<WebSocketClient> client = new AtomicReference<>(null);
    private final Subject<EndpointStateChange> endpointStateChangedSubject = PublishSubject.create();

    private volatile boolean disposed = false;
    private volatile boolean opened = false;
//    private volatile boolean joined = false;

//    private AtomicReference<EndpointState> stateHolder = new AtomicReference<>(EndpointState.DETACHED);
    private AtomicReference<RemoteIdentifiers> remoteIdentifiersHolder = new AtomicReference<>(null);

    private final ConnectionBuffer buffer;
    private final String serverUri;
    private final ObjectMapper mapper;
    private final Scheduler scheduler;
    private final int maxMessageSize;

    public WebsocketConnection(
            ConnectionBuffer buffer,
            String serverUri,
            ObjectMapper mapper,
            Scheduler scheduler,
            int maxMessageSize
    ) {
        this.buffer = buffer;
        this.serverUri = serverUri;
        this.mapper = mapper;
        this.scheduler = scheduler;
        this.maxMessageSize = maxMessageSize;
    }

    public String getServerUri() {
        return this.serverUri;
    }

    public boolean isOpened() {
        return this.opened;
    }

    public boolean isJoined() {
        return this.disposed == false && this.opened == true && this.remoteIdentifiersHolder.get() != null;
    }

    public UUID getRemoteEndpointId() {
        var remoteIdentifiers = this.remoteIdentifiersHolder.get();
        if (remoteIdentifiers == null) {
            return null;
        }
        return remoteIdentifiers.endpointId;
    }

    public void open() {
        if (this.opened) {
            logger.warn("Attempted to open twice");
            return;
        }
        this.opened = true;
        var client = this.client.get();
        if (client == null) {
            client = this.createClient();
            if (!this.client.compareAndSet(null, client)) {
                client.close(CLOSE_WITHOUT_RECONNECT_CODE);
                return;
            }
        }
        WebSocketClient finalClient = client;
        var process = scheduler.scheduleDirect(() -> {
            connecting.set(null);
            tryConnect(finalClient);
        }, 5000, TimeUnit.MILLISECONDS);
        if (!connecting.compareAndSet(null, process)) {
            process.dispose();
        }
    }

//    public RemoteIdentifiers getRemoteIdentifiers() {
//        return remoteIdentifiers;
//    }

    public void close() {
        if (this.disposed) {
            logger.warn("Attempted to close a connection to {} twice", this.serverUri);
        }
        this.disposed = true;
        var client = this.client.getAndSet(null);
        if (client == null) {
            return;
        }
        client.close();
    }

    public void send(byte[] message) {
        var client = this.client.get();
        if (client == null || client.getReadyState() != ReadyState.OPEN) {
            this.buffer.add(message);
            return;
        }
        for (boolean messageSent = false; this.buffer.isEmpty() == false || messageSent == false; ) {
            byte[] data;
            if (this.buffer.isEmpty()) {
                data = message;
                messageSent = true;
            } else {
                data = this.buffer.poll();
            }

            if (this.maxMessageSize <= 0) {
                client.send(data);
                continue;
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
        }
    }

//    public Observable<RemoteIdentifiers> remoteIdentifiers() {
//        return this.remoteEndpointIdsSubject;
//    }

    public Observable<EndpointStateChange> endpointStateChanged() {
        return this.endpointStateChangedSubject;
    }

    private WebSocketClient createClient() {
        var backoffTimeInMs = new AtomicInteger(5000);
        return new WebSocketClient(URI.create(this.serverUri)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                logger.info("Connected to {}, address is {}", this.uri, this.getRemoteSocketAddress());
            }

            @Override
            public void onMessage(String data) {
                if (remoteIdentifiersHolder.get() != null) {
                    var currentRemoteEndpointId = remoteIdentifiersHolder.get();
                    logger.warn("Received unexpected data on server {}. Current remote endpoint id {}, remote server uri: {}, received data: {}",
                            serverUri,
                            currentRemoteEndpointId.endpointId,
                            currentRemoteEndpointId.serverUri,
                            data
                    );
                    return;
                }
                try {
                    var remoteIdentifiers = mapper.readValue(data, RemoteIdentifiers.class);
                    if (!remoteIdentifiersHolder.compareAndSet(null, remoteIdentifiers)) {
                        logger.warn("Already set remote identifier on server {}. Current remote endpoint id {}, received remote endpoint id: {}",
                                serverUri,
                                remoteIdentifiersHolder.get(),
                                remoteIdentifiers
                        );
                        return;
                    }
                    logger.info("Remote identifiers received, endpoint is joined. {}", remoteIdentifiersHolder.get());
                    endpointStateChangedSubject.onNext(new EndpointStateChange(
                            EndpointState.JOINED,
                            remoteIdentifiers.endpointId
                    ));
                    backoffTimeInMs.set(5000);
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
                var removedRemoteIdentifiers = remoteIdentifiersHolder.getAndSet(null);
                if (removedRemoteIdentifiers != null) {
                    logger.info("Connection to {} is closed with code: {}, reason: {}, byremote: {}. Remote endpoint id: {}", uri, code, reason, remote, removedRemoteIdentifiers.endpointId);
                    endpointStateChangedSubject.onNext(new EndpointStateChange(
                            EndpointState.DETACHED,
                            removedRemoteIdentifiers.endpointId
                    ));
                } else {
                    logger.info("Connection to {} is closed with code: {}, reason: {}, byremote: {}. No remote endpoint id is registered", uri, code, reason, remote);
                }
                if (disposed || code == CLOSE_WITHOUT_RECONNECT_CODE) {
                    // goodbye
                    return;
                }
                var newClient = createClient();
                if (!client.compareAndSet(this, newClient)) {
                    newClient.close(CLOSE_WITHOUT_RECONNECT_CODE);
                    return;
                }
                if (!opened) {
                    return;
                }
                var newBackoffTimeInMs = Math.min(15 * 3600 * 1000, backoffTimeInMs.get() * 2);
                backoffTimeInMs.set(newBackoffTimeInMs);
                logger.info("Retrying to connect to {} backoff time in ms {}", uri, backoffTimeInMs.get());
                var process = scheduler.scheduleDirect(() -> {
                    connecting.set(null);
                    tryConnect(newClient);
                }, backoffTimeInMs.get(), TimeUnit.MILLISECONDS);
                if (!connecting.compareAndSet(null, process)) {
                    process.dispose();
                }
            }

            @Override
            public void onError(Exception ex) {
                logger.info("Connection to {} is reported an error", serverUri, ex);
            }
        };
    }

    private boolean tryConnect(WebSocketClient client) {
        try {
            return client.connectBlocking(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("Interrupted while connecting to {}", client.getRemoteSocketAddress(), e);
            return false;
        }
    }
}
