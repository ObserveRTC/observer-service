package org.observertc.webrtc.observer.sinks.socketio;

import io.micronaut.context.annotation.Prototype;
import io.socket.client.IO;
import io.socket.engineio.client.transports.WebSocket;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;
import org.observertc.webrtc.observer.sinks.Sink;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Prototype
public class SocketIOSinkBuilder extends AbstractBuilder implements Builder<Sink> {

    public Sink build() {
        Config config = this.convertAndValidate(Config.class);
        IO.Options options = new IO.Options();
        options.transports = config.transports;
        options.reconnectionAttempts = config.maxRetry;
        options.reconnectionDelay = config.reconnectionDelayInMs;
        options.timeout = config.timeoutInMs;
        SocketIOSink result = new SocketIOSink(options, config.uri);
        return result;
    }

    public static class Config {

        @NotNull
        public String uri = null;

        public String[] transports = new String[] { WebSocket.NAME };

        @Min(1)
        public int maxRetry = 3;

        @Min(1000)
        public int reconnectionDelayInMs = 1000;

        @Min(500)
        public int timeoutInMs = 500;
    }
}
