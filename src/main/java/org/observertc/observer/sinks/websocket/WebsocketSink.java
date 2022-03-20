package org.observertc.observer.sinks.websocket;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.annotations.NonNull;
import jakarta.websocket.*;
import org.observertc.observer.reports.Report;
import org.observertc.observer.sinks.Sink;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

public class WebsocketSink extends Sink {
    private URI endpointURI;
    private int maxRetry = 0;
    private Session session;
    private ObjectMapper mapper = new ObjectMapper();

    public WebsocketSink(URI endpointURI, int maxRetry) {
        this.endpointURI = endpointURI;
        this.maxRetry = maxRetry;
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        logger.info("Websocket {} is opened", userSession.getId());
        this.session = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        logger.info("Websocket {} is closed", userSession.getId(), reason.toString());
        this.session = null;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        logger.info("Received message {}", message);
    }


    @Override
    public void open() {
        if (Objects.nonNull(this.session)) {
            return;
        }
        this.connect(true);
    }

    private void ping() {
        try {
            String data = "Ping";
            ByteBuffer payload = ByteBuffer.wrap(data.getBytes());
            this.session.getBasicRemote().sendPing(payload);
        } catch (IOException e) {
            logger.warn("Exception occurred during ping", e);
            if (Objects.nonNull(this.session)) {
                try {
                    this.session.close();
                } catch (IOException ioException) {
                    logger.warn("Exception while closing websocket", ioException);
                }
                this.session = null;
            }
            this.connect(false);
        }
    }



    @Override
    public void accept(@NonNull List<Report> reports) {
        if (reports.size() < 1) {
            this.ping();
            return;
        } else if (Objects.isNull(this.session)) {
            this.connect(false);
        }
        int sent = 0;
        for (int tried = 0; tried < 3; ++tried) {
            try {
                int recordsCounter = 0;
                for (var report : reports) {
                    if (++recordsCounter < sent) {
                        continue;
                    }
                    var encodedReport = mapper.writeValueAsString(report);
//                    var message = ByteBuffer.wrap(encodedReport);
                    this.session.getBasicRemote().sendText(encodedReport);
                    ++sent;
                }
                break;
            } catch (Exception ex) {
                logger.error("Unexpected exception while sending reports", ex);
                this.connect(false);
            }
        }

    }

    private void connect(boolean initial) {
        Exception thrown = null;
        for (int retried = 0; initial || retried < this.maxRetry; ++retried) {
            try {
                if (!initial) {
                    logger.warn("Retry connecting websocket {}. Tried: {}", this.endpointURI, retried);
                }
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                container.connectToServer(this, this.endpointURI);
                thrown = null;
                break;
            } catch (Exception e) {
                thrown = e;
                logger.warn("Exception while connecting to endpoint {}", this.endpointURI, e);
                this.session = null;
            } finally {
                initial = false;
            }
        }
        if (Objects.nonNull(thrown)) {
            throw new RuntimeException(thrown);
        }
    }
}
