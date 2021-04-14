package org.observertc.webrtc.observer.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.dto.ConfigDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

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
