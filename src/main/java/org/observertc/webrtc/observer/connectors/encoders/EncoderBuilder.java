package org.observertc.webrtc.observer.connectors.encoders;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * To make the package scannable, we need to add this annotation
 */
@Prototype
public class EncoderBuilder extends AbstractBuilder {

    private static final Logger logger = LoggerFactory.getLogger(EncoderBuilder.class);

    public EncoderBuilder() {

    }

    public Optional<Encoder> build() {
        EncoderConfig config = this.convertAndValidate(EncoderConfig.class);

        String builderClassName = AbstractBuilder.getBuilderClassName(config.type);
        Optional<Builder> builderHolder = this.tryInvoke(builderClassName);
        if (!builderHolder.isPresent()) {
            final Package thisPackage = this.getClass().getPackage();
            final Package[] packages = Package.getPackages();
            var packageList = Arrays.stream(packages)
                    .filter(p -> p.getName().startsWith(thisPackage.getName()))
                    .map(Package::getName)
                    .collect(Collectors.toList());
            logger.warn("Encoder builder for class {} has not been found in packages {}", config.type, packageList);
            return Optional.empty();
        }
        Builder<Encoder> builder = builderHolder.get();
        builder.withConfiguration(config.config);
        Encoder result = builder.build();
        if (Objects.isNull(result)) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

}
