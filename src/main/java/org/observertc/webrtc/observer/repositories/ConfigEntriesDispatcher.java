package org.observertc.webrtc.observer.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.MapEvent;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.dto.ConfigDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Objects;


@Singleton
public class ConfigEntriesDispatcher implements EntryListener<String, ConfigDTO> {
    private static final Logger logger = LoggerFactory.getLogger(ConfigEntriesDispatcher.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Subject<ConfigDTO> observerConfigUpdated = PublishSubject.create();

    @Inject
    ObserverConfigDispatcher observerConfigDispatcher;

    @PostConstruct
    void setup() {
        this.observerConfigUpdated
                .map(configDTO -> {
                    byte[] payload = configDTO.payload;
                    try {
                        return OBJECT_MAPPER.readValue(payload, Map.class);
                    } catch (Throwable t) {
                        logger.warn("Exception occurred by parsing updated configuration.");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .subscribe(updatedConfig -> {
                    this.observerConfigDispatcher.accept(updatedConfig);
                });
    }

    @Override
    public void entryAdded(EntryEvent<String, ConfigDTO> event) {
        String key = event.getKey();
        ConfigDTO configDTO = event.getValue();
        switch (key) {
            case ConfigRepository.OBSERVER_CONFIG_KEY:
                this.observerConfigUpdated.onNext(configDTO);
            default:
                logger.warn("Unhandled config key ({}) is added", key);
        }
    }

    @Override
    public void entryEvicted(EntryEvent<String, ConfigDTO> event) {
        logger.info("Config entry ({}) is evicted", event.getKey());
    }

    @Override
    public void entryExpired(EntryEvent<String, ConfigDTO> event) {
        logger.info("Config entry ({}) is expired", event.getKey());
    }

    @Override
    public void entryRemoved(EntryEvent<String, ConfigDTO> event) {
        String key = event.getKey();
        ConfigDTO configDTO = ConfigDTO.of(null);
        switch (key) {
            case ConfigRepository.OBSERVER_CONFIG_KEY:
                this.observerConfigUpdated.onNext(configDTO);
            default:
                logger.warn("Unhandled config key ({}) is removed", key);
        }
    }

    @Override
    public void entryUpdated(EntryEvent<String, ConfigDTO> event) {
        String key = event.getKey();
        ConfigDTO configDTO = event.getValue();
        switch (key) {
            case ConfigRepository.OBSERVER_CONFIG_KEY:
                this.observerConfigUpdated.onNext(configDTO);
            default:
                logger.warn("Unhandled config key ({}) is updated", key);
        }
    }

    @Override
    public void mapCleared(MapEvent event) {
        logger.info("Config Map is cleared, {} item(s) deleted", event.getNumberOfEntriesAffected());
    }

    @Override
    public void mapEvicted(MapEvent event) {
        logger.info("Config Map is evicted, {} item(s) deleted", event.getNumberOfEntriesAffected());
    }
}
