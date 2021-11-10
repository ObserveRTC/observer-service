package org.observertc.webrtc.observer.sinks.socketio;


import io.reactivex.rxjava3.annotations.NonNull;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.common.OutboundReports;
import org.observertc.webrtc.observer.sinks.Sink;

import java.util.Objects;

/**
 * https://developpaper.com/implementing-socket-io-client-function-in-java/
 */
public class SocketIOSink extends Sink {
    private volatile boolean connecting = false;
    private Socket socket = null;
    private final String uri;
    private final IO.Options options;
    public SocketIOSink(IO.Options options, String uri) {
        this.uri = uri;
        this.options = options;
        this.connect();
    }

    private void connect() {
        if (this.connecting) {
            logger.warn("Attempted to connect twice at the same time");
            return;
        }
        try {
            this.connecting = true;
            final Socket socket = IO.socket(uri, options);
            socket.on(Socket.EVENT_CONNECT, objects -> {
                logger.info("Socket {} is connected to {}. Received additional arguments: {}",
                        socket.id(), this.uri, objects);
                this.socket = socket;
                this.connecting = false;
            });
            socket.on (Socket.EVENT_CONNECT_ERROR, objects -> {
                logger.warn("Error occurred while connecting to {} is failed. {}", this.uri, objects);
                this.close();
            });
            socket.connect();
//            socket.emit("myEvent", "myMessage".getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            this.connecting = false;
            logger.warn("Error occurred while connecting to {}", this.uri, ex);
            this.close();
        }
    }

    @Override
    public void accept(@NonNull OutboundReports outboundReports) {
        if (Objects.isNull(this.socket) || !this.socket.connected()) {
            this.connect();
        }
        int sent = 0;
        for (int tried = 0; tried < 3; ++tried) {
            try {
                int recordsCounter = 0;
                for (OutboundReport outboundReport : outboundReports) {
                    if (++recordsCounter < sent) {
                        continue;
                    }
                    String eventType = outboundReport.getType() != null ? outboundReport.getType().name() : "Unknown";
                    byte[] bytes = outboundReport.getBytes();
                    this.socket.emit(eventType, bytes);
                    ++sent;
                }
                break;
            } catch (Exception ex) {
                logger.error("Unexpected exception while sending reports", ex);
            }
        }

    }
}



