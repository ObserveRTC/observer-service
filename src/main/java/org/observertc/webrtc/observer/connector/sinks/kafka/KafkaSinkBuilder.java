package org.observertc.webrtc.observer.connector.sinks.kafka;

import io.micronaut.context.annotation.Prototype;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;
import org.observertc.webrtc.observer.configbuilders.ConfigConverter;
import org.observertc.webrtc.observer.connector.sinks.Sink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.function.BiConsumer;

@Prototype
public class KafkaSinkBuilder extends AbstractBuilder implements Builder<Sink> {
    private final static Logger logger = LoggerFactory.getLogger(KafkaSinkBuilder.class);

    public Sink build() {
        Config config = this.convertAndValidate(Config.class);
        KafkaSink result = new KafkaSink();
        Map<String, Object> flattenedProperties = ConfigConverter.flatten(config.properties, ".");
        this.evaluateProperties(flattenedProperties);
        Properties properties = new Properties();
        flattenedProperties.entrySet().stream().forEach(entry -> properties.put(entry.getKey(), entry.getValue()));

        result
                .forTopic(config.topic)
                .byReconnectOnFailure(config.tryReconnectOnFailure)
                .withProperties(properties)
        ;

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
        check.accept(ProducerConfig.CLIENT_ID_CONFIG, KafkaSink.class.getSimpleName() + new Random().nextInt(10000));
        check.accept(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class);
        check.accept(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BytesSerializer.class);
    }

    public static class Config {

        @NotNull
        public Map<String, Object> properties;

        public boolean tryReconnectOnFailure = true;

        @NotNull
        public String topic;

    }
}
