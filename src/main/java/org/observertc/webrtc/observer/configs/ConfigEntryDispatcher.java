package org.observertc.webrtc.observer.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.map.MapEvent;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.dto.ConfigDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.Objects;


@Singleton
public class ConfigEntryDispatcher implements EntryListener<ConfigType, ConfigDTO> {
    private static final Logger logger = LoggerFactory.getLogger(ConfigEntryDispatcher.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Subject<ConfigEvent<ObserverReportConfig>> observerReportConfig = PublishSubject.create();
    private ConfigTypeVisitor<ConfigDTO, Void> dispatch;
    public Observable<ConfigEvent<ObserverReportConfig>> observableObserverReportConfig() {
        return this.observerReportConfig;
    }

    @PostConstruct
    void setup() {

    }



    @Override
    public void entryAdded(EntryEvent<ConfigType, ConfigDTO> event) {
        this.dispatch(event.getKey(), ConfigOperation.ADDED, event.getValue());

    }

    @Override
    public void entryEvicted(EntryEvent<ConfigType, ConfigDTO> event) {
        logger.info("Config entry ({}) is evicted", event.getKey());
    }

    @Override
    public void entryExpired(EntryEvent<ConfigType, ConfigDTO> event) {
        logger.info("Config entry ({}) is expired", event.getKey());
    }

    @Override
    public void entryRemoved(EntryEvent<ConfigType, ConfigDTO> event) {
        this.dispatch(event.getKey(), ConfigOperation.REMOVED, event.getValue());
    }

    @Override
    public void entryUpdated(EntryEvent<ConfigType, ConfigDTO> event) {
        this.dispatch(event.getKey(), ConfigOperation.UPDATED, event.getValue());
    }

    @Override
    public void mapCleared(MapEvent event) {
        logger.info("Config Map is cleared, {} item(s) deleted", event.getNumberOfEntriesAffected());
    }

    @Override
    public void mapEvicted(MapEvent event) {
        logger.info("Config Map is evicted, {} item(s) deleted", event.getNumberOfEntriesAffected());
    }

    private void dispatch(ConfigType configType, ConfigOperation configOperation, ConfigDTO configDTO) {
        if (Objects.isNull(configDTO)) {
            logger.warn("No ConfigDTO is received for config type {} and operation {}", configType, configOperation);
            return;
        }
        var payload = configDTO.payload;
        try {
            switch (configType) {
                case OBSERVER_REPORT:
                    var config = OBJECT_MAPPER.readValue(payload, ObserverReportConfig.class);
                    ConfigEvent<ObserverReportConfig> configEvent = ConfigEvent.makeObserverReportConfigBuilder()
                            .withConfig(config)
                            .withOperationType(configOperation)
                            .build();
                    Observable.just(configEvent)
                            .subscribe(this.observerReportConfig);
                break;
                default:
                    logger.warn("Unhandled config key ({}) is tried to be dispatched", configType, Objects.toString(configDTO));
            }
        } catch (Throwable t) {
            logger.warn("An exception occurred during execution", t);
        }
    }


}
