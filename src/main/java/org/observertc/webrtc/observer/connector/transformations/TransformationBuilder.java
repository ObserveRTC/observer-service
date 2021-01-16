package org.observertc.webrtc.observer.connector.transformations;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * To make the package scannable, we need to add this annotation
 */
@Prototype
public class TransformationBuilder extends AbstractBuilder {

    private static final Logger logger = LoggerFactory.getLogger(TransformationBuilder.class);

    public TransformationBuilder() {

    }

    public Optional<Transformation> build() {
        Config config = this.convertAndValidate(Config.class);

        String builderClassName = AbstractBuilder.getBuilderClassName(config.type);
        Optional<Builder> builderHolder = this.tryInvoke(builderClassName);
        if (!builderHolder.isPresent()) {
            logger.warn("Transformation builder for class {} has not been found", config.type);
            return Optional.empty();
        }
        Builder<Transformation> builder = builderHolder.get();
        builder.withConfiguration(config.config);
        Transformation result = builder.build();
        if (Objects.isNull(result)) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    public static class Config {

        public String type;

        public Map<String, Object> config;

    }
}
