package org.observertc.webrtc.observer.sinks;

import org.observertc.webrtc.observer.codecs.Decoder;
import org.observertc.webrtc.observer.configbuilders.AbstractBuilder;
import org.observertc.webrtc.observer.configbuilders.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class SinkBuilder extends AbstractBuilder {
    private static final Logger logger = LoggerFactory.getLogger(SinkBuilder.class);
    private final List<String> packages;
    private Queue<Object> subjects = new LinkedList<>();

    public SinkBuilder() {
        Package thisPackage = this.getClass().getPackage();
        Package[] packages = Package.getPackages();
        this.packages = Arrays.stream(packages)
                .filter(p -> p.getName().startsWith(thisPackage.getName()))
                .map(Package::getName)
                .collect(Collectors.toList());
    }

    public SinkBuilder setDecoder(Decoder decoder) {
        if (Objects.nonNull(decoder)) {
            this.subjects.add(decoder);
        }
        return this;
    }

    public Sink build() {
        Config config = this.convertAndValidate(Config.class);
        String builderClassName = AbstractBuilder.getBuilderClassName(config.type);
        Optional<Builder> builderHolder = this.tryInvoke(builderClassName);
        if (!builderHolder.isPresent()) {
            logger.error("Cannot find sink builder for {} in packages: {}", config.type, String.join(",", this.packages ));
            return null;
        }
        Builder<Sink> sinkBuilder = (Builder<Sink>) builderHolder.get();
        sinkBuilder.withConfiguration(config.config);
        while (!this.subjects.isEmpty()) {
            Object subject = this.subjects.poll();
            sinkBuilder.set(subject);
        }
        return sinkBuilder.build();
    }

    public static class Config {

        @NotNull
        public String type;

        public Map<String, Object> config;

    }
}
