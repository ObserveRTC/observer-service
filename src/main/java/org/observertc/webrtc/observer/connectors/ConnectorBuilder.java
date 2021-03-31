package org.observertc.webrtc.observer.connectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.connectors.encoders.Encoder;
import org.observertc.webrtc.observer.connectors.encoders.EncoderBuilder;
import org.observertc.webrtc.observer.connectors.sinks.Sink;
import org.observertc.webrtc.observer.connectors.sinks.SinkBuilder;
import org.observertc.webrtc.observer.connectors.transformations.Transformation;
import org.observertc.webrtc.observer.connectors.transformations.TransformationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.*;

@Prototype
public class ConnectorBuilder extends AbstractBuilder implements Function<Map<String, Object>, Optional<Connector>> {
    private static final Logger logger = LoggerFactory.getLogger(ConnectorBuilder.class);
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Disposable disposable;

    public ConnectorBuilder() {

    }

    @Override
    public Optional<Connector> apply(Map<String, Object> source) throws Throwable {
        this.getConfig().clear();
        this.withConfiguration(source);
        return this.build();
    }

    public void withConfiguration(ConnectorConfig config) {
        Map<String, Object> map = OBJECT_MAPPER.convertValue(config, Map.class);
        this.withConfiguration(map);
    }

    public Optional<Connector> build() {
        ConnectorConfig config = this.convertAndValidate(ConnectorConfig.class);
        Connector result = new Connector(config.name);

        for (Map<String, Object> transformationConfig : config.transformations) {
            TransformationBuilder transformationBuilder = new TransformationBuilder();
            transformationBuilder.withConfiguration(transformationConfig);
            Optional<Transformation> transformationOptional = transformationBuilder.build();
            if (!transformationOptional.isPresent()) {
                logger.warn("Cannot make a transformation object from {}", ObjectToString.toString(transformationConfig));
                continue;
            }
            Transformation transformation = transformationOptional.get();
            result.withTransformation(transformation);
        }

        EncoderBuilder encoderBuilder = new EncoderBuilder();
        encoderBuilder.withConfiguration(config.encoder);
        Optional<Encoder> encoderHolder = encoderBuilder.build();
        if (!encoderHolder.isPresent()) {
            logger.warn("Cannot make a pipeline without an encoder");
            return Optional.empty();
        }
        Encoder encoder = encoderHolder.get();
        logger.info("Pipeline {} uses encoder {}, provided message format: {}", config.name, encoder.getClass().getSimpleName(), encoder.getMessageFormat().name());
        result.withEncoder(encoder);

        result.withBuffer(config.buffer);

        SinkBuilder sinkBuilder = new SinkBuilder();
        sinkBuilder.withConfiguration(config.sink);
        Sink sink = sinkBuilder.build();
        if (Objects.isNull(sink)) {
            logger.warn("Sink was not build for pipeline {}, this pipeline cannot be built.", config.name);
            return Optional.empty();
        }
        logger.info("Pipeline {} uses sink {}", config.name, sink.getClass().getSimpleName());
        result.withSink(sink);

        return Optional.of(result);
    }

    public static class ConnectorConfig {
        public String name;

        public String restartPolicy = RestartPolicy.Never.name();

        public List<Map<String, Object>> transformations = new ArrayList<>();

        public BufferConfig buffer = new BufferConfig();

        public Map<String, Object> encoder = new HashMap<>();

        @NotNull
        public Map<String, Object> sink;

    }
}
