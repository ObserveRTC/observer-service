package org.observertc.webrtc.observer.compressors;

import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class DecompressorBuilder extends AbstractBuilder {
    private static final Decompressor NULL_DECOMPRESSOR = Decompressor.of(message -> message, () -> {});
    private static final Logger logger = LoggerFactory.getLogger(DecompressorBuilder.class);
    private Config providedConfig = null;
    private final List<String> packages;

    public DecompressorBuilder() {
        Package thisPackage = this.getClass().getPackage();
        Package[] packages = Package.getPackages();
        this.packages = Arrays.stream(packages)
                .filter(p -> p.getName().startsWith(thisPackage.getName()))
                .map(Package::getName)
                .collect(Collectors.toList());
    }

    public DecompressorBuilder withConfig(Config config) {
        this.providedConfig = config;
        return this;
    }

    public Decompressor build() {
        Config fetchedConfig = Objects.nonNull(this.providedConfig) ? this.providedConfig : this.convertAndValidate(Config.class);
        Objects.requireNonNull(fetchedConfig);
        if (DecompressorType.NONE.equals(fetchedConfig.type)) {
            return NULL_DECOMPRESSOR;
        }
        String klassName = fetchedConfig.type.getKlassName();
        String builderClassName = AbstractBuilder.getBuilderClassName(klassName);
        Optional<Builder> builderHolder = this.tryInvoke(builderClassName);
        if (!builderHolder.isPresent()) {
            logger.error("Cannot find decompressor builder for {} in packages: {}", fetchedConfig.type, String.join(",", this.packages ));
            return NULL_DECOMPRESSOR;
        }
        Builder<Decompressor> builder = (Builder<Decompressor>) builderHolder.get();
        builder.withConfiguration(fetchedConfig.config);
        var result = builder.build();
        return result;
    }

    public static class Config {

        public DecompressorType type = DecompressorType.NONE;

        public Map<String, Object> config = null;
    }

}
