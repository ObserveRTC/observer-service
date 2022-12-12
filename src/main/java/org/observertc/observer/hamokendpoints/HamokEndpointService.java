package org.observertc.observer.hamokendpoints;

import io.micronaut.context.BeanProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.HamokService;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.observertc.observer.configbuilders.ConfigConverter;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.hamokdiscovery.HamokDiscoveryService;
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
public class HamokEndpointService extends AbstractBuilder implements Supplier<HamokEndpoint> {
    private static final Logger logger = LoggerFactory.getLogger(HamokEndpointService.class);

    private final List<String> packages;
    private final AtomicReference<HamokEndpoint> endpoint = new AtomicReference<>(null);
    private final AtomicReference<HamokDiscoveryService> hamokDiscoveryService = new AtomicReference<>(null);

    @Inject
    ObserverConfig.HamokConfig hamokConfig;

    @Inject
    BeanProvider<HamokService> hamokServiceBeanProvider;

    private volatile boolean isReady = false;

    public HamokEndpointService() {
        Package thisPackage = this.getClass().getPackage();
        Package[] packages = Package.getPackages();
        this.packages = Arrays.stream(packages)
                .filter(p -> p.getName().startsWith(thisPackage.getName()))
                .map(Package::getName)
                .collect(Collectors.toList());
    }

    @PostConstruct
    void setup() {

    }

    public boolean isReady() {
        return this.isReady;
    }

    public void setHamokDiscoveryService(HamokDiscoveryService hamokDiscoveryService) {
        this.hamokDiscoveryService.set(hamokDiscoveryService);
    }

    private HamokEndpoint build(Map<String, Object> configMap) {
        if (configMap == null) {
            return null;
        }
        var config = ConfigConverter.convert(EndpointBuilderConfig.class, configMap);
        String builderClassName = AbstractBuilder.getBuilderClassName("", config.type, "Builder");
        Optional<Builder> builderHolder = this.tryInvoke(builderClassName);
        if (!builderHolder.isPresent()) {
            logger.warn("Cannot find endpoint builder for {} in packages: {}", config.type, String.join(",", this.packages));
            return null;
        }
        EndpointBuilder endpointBuilder = (EndpointBuilder) builderHolder.get();
        endpointBuilder.setBuildingEssentials(new EndpointsBuildersEssentials(
                this.hamokDiscoveryService.get(),
                () -> this.hamokServiceBeanProvider.get().refreshRemoteEndpointId()
        ));
        endpointBuilder.withConfiguration(config.config);

        var result = endpointBuilder.build();
        return result;
    }

    @Override
    public HamokEndpoint get() {
        var result = this.endpoint.get();
        if (result != null) {
            return result;
        }
        result = this.build(this.hamokConfig.endpoint);
        if (!this.endpoint.compareAndSet(null, result)) {
            return this.endpoint.get();
        }
        if (result == null) {
            this.isReady = true;
            return result;
        }
        result.stateChanged().subscribe(stateChanged -> {
            switch (stateChanged.actualState()) {
                case STARTED -> this.isReady = true;
                case STOPPED -> this.isReady = false;
            }
        });
        return result;
    }
}
