package org.observertc.observer.sinks.socket;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.annotations.NonNull;
import jakarta.websocket.ClientEndpoint;
import org.observertc.observer.reports.Report;
import org.observertc.observer.sinks.Sink;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.List;
import java.util.Objects;

@ClientEndpoint
public class SocketSink extends Sink {
    private int maxRetry = 0;
    private Writer writer;
    private String host = null;
    private int port;
    private ObjectMapper mapper = new ObjectMapper();

    public SocketSink(String host, int port, int maxRetry) {
        this.host = host;
        this.port = port;
        this.maxRetry = maxRetry;

    }

    @Override
    public void open() {
        if (Objects.nonNull(this.writer)) {
            return;
        }
        this.connect(true);
        super.open();
    }


    @Override
    public void process(@NonNull List<Report> reports) {
        if (reports.size() < 1) {
            return;
        } else if (Objects.isNull(this.writer)) {
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
                    this.writer.write(encodedReport);
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
                    logger.warn("Retry connecting websocket {}:{}. Tried: {}", this.host, this.port, retried);
                }
                Socket socket = new Socket(this.host, this.port);
                OutputStream output = socket.getOutputStream();
                this.writer = new PrintWriter(output, true);
                thrown = null;
                break;
            } catch (Exception e) {
                thrown = e;
                this.writer = null;
                logger.warn("Exception while connecting to endpoint {}:{}", this.host, this.port, e);
            } finally {
                initial = false;
            }
        }
        if (Objects.nonNull(thrown)) {
            throw new RuntimeException(thrown);
        } else {
            logger.info("Socket for {}:{} is opened", this.host, this.port);
        }
    }
}



