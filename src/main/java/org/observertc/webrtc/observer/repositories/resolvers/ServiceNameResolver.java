package org.observertc.webrtc.observer.repositories.resolvers;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.configs.stores.ServiceMapsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@Prototype
public class ServiceNameResolver implements Function<UUID, String> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceNameResolver.class);

    @Inject
    ServiceMapsStore serviceMapsStore;

    private Map<UUID, String> dictionary;
    private String defaultServiceName;
    private AtomicBoolean updated = new AtomicBoolean(true);

    public ServiceNameResolver(ObserverConfig config) {
        this.defaultServiceName = config.outboundReports.defaultServiceName;
    }

    @PostConstruct
    void setup() {
        logger.info("Default service name is {}", this.defaultServiceName);
        logger.info("Service Name dictionary config {}", ObjectToString.toString(this.dictionary));
        this.serviceMapsStore
                .observableOnUpdated()
                .subscribe(serviceMapStore -> {
                    this.updated.set(true);
                });
    }

    @Override
    public String apply(UUID uuid) {
        if (this.updated.compareAndSet(true, false)) {
            this.dictionary = this.fetch();
        }
        String result = this.dictionary.getOrDefault(uuid, this.defaultServiceName);
        return result;
    }

    private Map<UUID, String> fetch() {
        Map<UUID, String> result = new HashMap<>();
        Map<String, ServiceMapEntity> entries = this.serviceMapsStore.findAll();
        Iterator<ServiceMapEntity> it = entries.values().iterator();
        while (it.hasNext()) {
            ServiceMapEntity entity = it.next();
            for (UUID serviceUUID : entity.uuids) {
                result.put(serviceUUID, entity.name);
            }
        }
        return result;
    };
}
