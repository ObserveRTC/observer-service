package org.observertc.observer.hamokendpoints.websocket;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
    private final Subject<RemoteIdentifiers> remoteEndpointIdsSubject = PublishSubject.create();
    private final Subject<EndpointStateChange> endpointStateChangedSubject = PublishSubject.create();
    private volatile boolean disposed = false;
    private volatile boolean opened = false;
    private volatile boolean joined = false;

    private RemoteIdentifiers remoteIdentifiers = null;

    private final ConnectionBuffer buffer;
    private final String serverUri;
    private final ObjectMapper mapper;
    private final Scheduler scheduler;

    public WebsocketConnection(ConnectionBuffer buffer, String serverUri, ObjectMapper mapper, Scheduler scheduler) {
        this.buffer = buffer;
        this.serverUri = serverUri;
        this.mapper = mapper;
        this.scheduler = scheduler;
    }

    public String getServerUri() {
        return this.serverUri;
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

    public RemoteIdentifiers getRemoteIdentifiers() {
        return remoteIdentifiers;
    }

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

    public void send(String message) {
        var client = this.client.get();
        if (client == null || client.getReadyState() != ReadyState.OPEN) {
            this.buffer.add(message);
            return;
        }
        while (this.buffer.isEmpty() == false) {
            client.send(this.buffer.poll());
        }
        client.send(message);
    }

    public Observable<RemoteIdentifiers> remoteIdentifiers() {
        return this.remoteEndpointIdsSubject;
    }

    public Observable<EndpointStateChange> endpointStateChanged() {
        return this.endpointStateChangedSubject;
    }

    private WebSocketClient createClient() {
        return new WebSocketClient(URI.create(this.serverUri)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                logger.info("Connected to {}, address is {}", this.uri, this.getRemoteSocketAddress());
                if (!joined && remoteIdentifiers != null) {
                    // join!
                    joined = true;
                    endpointStateChangedSubject.onNext(new EndpointStateChange(
                            EndpointState.JOINED,
                            remoteIdentifiers.endpointId
                    ));
                }
            }

            @Override
            public void onMessage(String data) {
                try {
                    var remoteIdentifierWasNull = remoteIdentifiers == null;
                    var remoteIdentifiersHolder = mapper.readValue(data, RemoteIdentifiers.class);
                    if (remoteIdentifiersHolder.endpointId != null && remoteIdentifiersHolder.serverUri != null) {
                        remoteIdentifiers = remoteIdentifiersHolder;
                        remoteEndpointIdsSubject.onNext(remoteIdentifiersHolder);
                        if (!joined && remoteIdentifierWasNull) {
                            // join!
                            joined = true;
                            endpointStateChangedSubject.onNext(new EndpointStateChange(
                                    EndpointState.JOINED,
                                    remoteIdentifiers.endpointId
                            ));
                        }
                    }

                } catch (JsonProcessingException e) {
                    logger.warn("Error in received message", e);
                    return;
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                logger.info("Connection to {} is closed with code: {}, reason: {}, byremote: {}", uri, code, reason, remote);
                if (joined && remoteIdentifiers != null) {
                    // detach
                    joined = false;
                    endpointStateChangedSubject.onNext(new EndpointStateChange(
                            EndpointState.DETACHED,
                            remoteIdentifiers.endpointId
                    ));
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
                var process = scheduler.scheduleDirect(() -> {
                    connecting.set(null);
                    tryConnect(newClient);
                }, 5000, TimeUnit.MILLISECONDS);
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