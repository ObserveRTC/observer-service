package org.observertc.webrtc.observer.repositories.resolvers;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.entities.ServiceMapEntity;
import org.observertc.webrtc.observer.repositories.ServiceMapsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Prototype
public class ServiceNameResolver implements Function<UUID, String> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceNameResolver.class);

    @Inject
    ServiceMapsRepository serviceMapsRepository;

    private Map<UUID, String> dictionary;
    private String defaultServiceName;
    private int counter;


    public ServiceNameResolver(ObserverConfig config) {
        this.defaultServiceName = config.outboundReports.defaultServiceName;
    }

    @PostConstruct
    void setup() {
        this.dictionary = this.fetch();
        logger.info("Default service name is {}", this.defaultServiceName);
        logger.info("Service Name dictionary config {}", ObjectToString.toString(this.dictionary));
    }

    @Override
    public String apply(UUID uuid) {
        if (1000 < ++this.counter) {
            this.dictionary = this.fetch();
            this.counter = 0;
        }
        String result = this.dictionary.getOrDefault(uuid, this.defaultServiceName);
        return result;
    }

    private Map<UUID, String> fetch() {
        Map<UUID, String> result = new HashMap<>();
        Map<String, ServiceMapEntity> entries = this.serviceMapsRepository.findAll();
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
