package org.observertc.observer.repositories.endpoints;

import io.github.balazskreith.hamok.transports.Endpoint;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

public class EndpointBuilderImpl extends AbstractBuilder implements EndpointBuilder {
    private static final Logger logger = LoggerFactory.getLogger(EndpointBuilderImpl.class);

    public static final String ENDPOINT_ID_PARAM = "endpointId";

    private final List<String> packages;
    private Map<Class, Object> beans = new HashMap<>();
    private UUID endpointId;

    public EndpointBuilderImpl() {
        Package thisPackage = this.getClass().getPackage();
        Package[] packages = Package.getPackages();
        this.packages = Arrays.stream(packages)
                .filter(p -> p.getName().startsWith(thisPackage.getName()))
                .map(Package::getName)
                .collect(Collectors.toList());
    }

    public Endpoint build() {
        Config config = this.convertAndValidate(Config.class);
        String builderClassName = AbstractBuilder.getBuilderClassName("", config.type, "EndpointBuilder");
        Optional<Builder> builderHolder = this.tryInvoke(builderClassName);
        if (!builderHolder.isPresent()) {
            logger.error("Cannot find endpoint builder for {} in packages: {}", config.type, String.join(",", this.packages ));
            return null;
        }
        EndpointBuilder endpointBuilder = (EndpointBuilder) builderHolder.get();
        endpointBuilder.setBeans(this.beans);
        endpointBuilder.setEndpointId(this.endpointId);
        endpointBuilder.withConfiguration(config.config);

        var result = endpointBuilder.build();
        return result;
    }

    @Override
    public void setEndpointId(UUID endpointId) {
        this.endpointId = endpointId;
    }

    @Override
    public void setBeans(Map<Class, Object> beans) {
        this.beans = beans;
    }

    public static class Config {

        @NotNull
        public String type;

        public Map<String, Object> config;

    }
}
