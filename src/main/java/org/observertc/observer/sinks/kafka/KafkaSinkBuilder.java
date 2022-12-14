package org.observertc.observer.sinks.kafka;

import io.micronaut.context.annotation.Prototype;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.ConfigConverter;
import org.observertc.observer.configs.TransportFormatType;
import org.observertc.observer.mappings.Encoder;
import org.observertc.observer.mappings.JsonMapper;
import org.observertc.observer.reports.Report;
import org.observertc.observer.sinks.SinkBuilder;
import org.observertc.observer.sinks.Sink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Prototype
public class KafkaSinkBuilder extends AbstractBuilder implements SinkBuilder {
    private final static Logger logger = LoggerFactory.getLogger(KafkaSinkBuilder.class);

    public Sink build() {
        Config config = this.convertAndValidate(Config.class);
        Map<String, Object> flattenedProperties = ConfigConverter.flatten(config.properties, ".");
        this.evaluateProperties(flattenedProperties);
        Properties properties = new Properties();
        flattenedProperties.entrySet().stream().forEach(entry -> properties.put(entry.getKey(), entry.getValue()));

        // Recorder
        Function<Report, UUID> keyAssigner = this.makeKeyAssigner(config.keyAssignmentStrategy);
        Encoder<Report, byte[]> encoder = this.makeEncoder(config.encoder);
        Function<Report, ProducerRecord<UUID, Bytes>> recorder = report -> {
            var key = keyAssigner.apply(report);
            byte[] bytes = new byte[0];
            try {
                bytes = encoder.encode(report);
            } catch (Throwable e) {
                logger.warn("Cannot assign key", e);
            }
            var wrappedBytes = Bytes.wrap(bytes);
            var result = new ProducerRecord<UUID, Bytes>(config.topic, key, wrappedBytes);
            return result;
        };

        // Create instance
        KafkaSink result = new KafkaSink(
                properties,
                config.tryReconnectOnFailure,
                recorder
        );
        return result;
    }

    private void evaluateProperties(Map<String, Object> flattenedMap) {
        BiConsumer<String, Object> check = (property, defaultValue)->  {
            Object value = flattenedMap.get(property);
            if (Objects.isNull(value)) {
                logger.info("KafkaSink property {} value is set automatically to {}",
                        property, defaultValue);
                flattenedMap.put(property, defaultValue);
            }
        };
        var random = new SecureRandom().nextInt();
        check.accept(ProducerConfig.CLIENT_ID_CONFIG, KafkaSink.class.getSimpleName() + random);
        check.accept(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class);
        check.accept(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BytesSerializer.class);
    }

    private Encoder<Report, byte[]> makeEncoder(TransportFormatType encoder) {
        if (Objects.isNull(encoder) || TransportFormatType.NONE.equals(encoder)) {
            throw new IllegalArgumentException("Encoder cannot be null or NONE for KafkaSink");
        }
        switch (encoder) {
            case JSON:
                var mapper = JsonMapper.<Report>createObjectToBytesMapper();
                return Encoder.from(mapper);
        }
        throw new IllegalArgumentException("Not assigned encoder for type " + encoder);
    }

    private Function<Report, UUID> makeKeyAssigner(KeyAssignmentStrategy keyAssignmentStrategy) {
        switch (keyAssignmentStrategy) {
            case RANDOM:
                return report -> UUID.randomUUID();
            default:
            case INSTANCE_BASED:
                UUID instanceId = UUID.randomUUID();
                return report -> instanceId;
            case OBJECT_HIERACHY_BASED:
                var assigner = new ObjectHierarchyKeyAssigner();
                return report -> assigner.apply(report, report.type);
        }
    }

    public static class Config {

        @NotNull
        public Map<String, Object> properties;

        public boolean tryReconnectOnFailure = true;

        @NotNull
        public String topic = "reports";

        public KeyAssignmentStrategy keyAssignmentStrategy = KeyAssignmentStrategy.INSTANCE_BASED;

        public TransportFormatType encoder = TransportFormatType.JSON;
    }
}
