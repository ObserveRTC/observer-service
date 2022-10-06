
package org.observertc.observer.hamokdiscovery;

import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.micronaut.context.BeanProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.observertc.observer.configbuilders.ConfigConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class DiscoveryBuilderService extends AbstractBuilder {
    private static final Logger logger = LoggerFactory.getLogger(DiscoveryBuilderService.class);

    private final List<String> packages;
    private DiscoveryBuildersEssentials essentials;

    public DiscoveryBuilderService() {
        Package thisPackage = this.getClass().getPackage();
        Package[] packages = Package.getPackages();
        this.packages = Arrays.stream(packages)
                .filter(p -> p.getName().startsWith(thisPackage.getName()))
                .map(Package::getName)
                .collect(Collectors.toList());
    }

    @Inject
    BeanProvider<CoreV1Api> coreV1ApiBeanProvider;

    @PostConstruct
    void setup() {
        this.essentials = new DiscoveryBuildersEssentials(
                this.coreV1ApiBeanProvider
        );
    }

    public RemotePeerDiscovery build(Map<String, Object> configMap) {
        var config = ConfigConverter.convert(DiscoveryBuilderConfig.class, configMap);
        String builderClassName = AbstractBuilder.getBuilderClassName("", config.type, "Builder");
        Optional<Builder> builderHolder = this.tryInvoke(builderClassName);
        if (!builderHolder.isPresent()) {
            logger.error("Cannot find endpoint builder for {} in packages: {}", config.type, String.join(",", this.packages ));
            return null;
        }
        DiscoveryBuilder discoveryBuilder = (DiscoveryBuilder) builderHolder.get();
        discoveryBuilder.setBuildingEssentials(this.essentials);
        discoveryBuilder.withConfiguration(config.config);

        var result = discoveryBuilder.build();
        return result;
    }
}
