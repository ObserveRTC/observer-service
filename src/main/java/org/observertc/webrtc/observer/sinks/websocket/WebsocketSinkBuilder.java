package org.observertc.webrtc.observer.sinks.websocket;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;
import org.observertc.webrtc.observer.sinks.Sink;

import javax.validation.constraints.NotNull;
import java.net.URI;

@Prototype
public class WebsocketSinkBuilder extends AbstractBuilder implements Builder<Sink> {

    public Sink build() {
        Config config = this.convertAndValidate(Config.class);
        WebsocketSink result = new WebsocketSink(
                URI.create(config.uri),
                config.maxRetry
        );
        return result;
    }

    public static class Config {

        @NotNull
        public String uri = null;

        public int maxRetry = 3;
    }
}
