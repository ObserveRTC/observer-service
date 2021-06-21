package org.observertc.webrtc.observer.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.configs.ConfigEntriesDispatcher;
import org.observertc.webrtc.observer.dto.ConfigDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

@Singleton
public class ConfigRepository  {
    private static final Logger logger = LoggerFactory.getLogger(ConfigRepository.class);
    public static final String OBSERVER_CONFIG_KEY = "observerConfig";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ConfigEntriesDispatcher configEntriesDispatcher;

    @PostConstruct
    void setup() {
        this.hazelcastMaps.getConfigurations().addEntryListener(this.configEntriesDispatcher, true);

        ConfigDTO observerConfigDTO = this.hazelcastMaps.getConfigurations().get(OBSERVER_CONFIG_KEY);
        if (Objects.nonNull(observerConfigDTO)) {
            this.configEntriesDispatcher.dispatch(OBSERVER_CONFIG_KEY, observerConfigDTO);
        }

    }

    public void updateObserverConfig(ObserverConfig observerConfig) {
        byte[] payload;
        try {
            payload = OBJECT_MAPPER.writeValueAsBytes(observerConfig);
        } catch (JsonProcessingException e) {
            logger.warn("Object Mapper cannot serialize observer config {}", observerConfig);
            return;
        }
        ConfigDTO configDTO = ConfigDTO.of(payload);
        this.hazelcastMaps.getConfigurations().put(OBSERVER_CONFIG_KEY, configDTO);
    }

}
