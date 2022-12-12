
package org.observertc.observer.hamokdiscovery;

import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.micronaut.context.BeanProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.observertc.observer.configbuilders.ConfigConverter;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.hamokendpoints.HamokEndpointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Singleton
public class HamokDiscoveryService extends AbstractBuilder implements Supplier<HamokDiscovery> {
    private static final Logger logger = LoggerFactory.getLogger(HamokDiscoveryService.class);

    private final List<String> packages;
    private final AtomicReference<HamokDiscovery> hamokDiscovery = new AtomicReference<>(null);
    private AtomicReference<HamokEndpointService> hamokEndpointService = new AtomicReference<>(null);

    public HamokDiscoveryService() {
        Package thisPackage = this.getClass().getPackage();
        Package[] packages = Package.getPackages();
        this.packages = Arrays.stream(packages)
                .filter(p -> p.getName().startsWith(thisPackage.getName()))
                .map(Package::getName)
                .collect(Collectors.toList());
    }

    @Inject
    ObserverConfig.HamokConfig hamokConfig;

    @Inject
    BeanProvider<CoreV1Api> coreV1ApiBeanProvider;

    public void setEndpointService(HamokEndpointService hamokEndpointService) {
        this.hamokEndpointService.set(hamokEndpointService);
    }

    @PostConstruct
    void setup() {

    }

    private HamokDiscovery build(Map<String, Object> configMap) {
        var config = ConfigConverter.convert(DiscoveryBuilderConfig.class, configMap);
        String builderClassName = AbstractBuilder.getBuilderClassName("", config.type, "Builder");
        Optional<Builder> builderHolder = this.tryInvoke(builderClassName);
        if (!builderHolder.isPresent()) {
            logger.error("Cannot find endpoint builder for {} in packages: {}", config.type, String.join(",", this.packages ));
            return null;
        }
        DiscoveryBuilder discoveryBuilder = (DiscoveryBuilder) builderHolder.get();
        discoveryBuilder.setBuildingEssentials(new DiscoveryBuildersEssentials(
                this.coreV1ApiBeanProvider,
                this.hamokEndpointService.get()
        ));
        discoveryBuilder.withConfiguration(config.config);

        var result = discoveryBuilder.build();
        return result;
    }

    @Override
    public HamokDiscovery get() {
        var result = this.hamokDiscovery.get();
        if (result != null) {
            return result;
        }
        result = this.build(this.hamokConfig.discovery);
        if (result == null) {
            logger.warn("No Discovery is configured");
            return null;
        }
        if (!this.hamokDiscovery.compareAndSet(null, result)) {
            return this.hamokDiscovery.get();
        }
        return result;
    }
}
