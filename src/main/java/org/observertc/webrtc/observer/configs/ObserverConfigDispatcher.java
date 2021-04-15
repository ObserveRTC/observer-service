package org.observertc.webrtc.observer.configs;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.configbuilders.ConfigHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

@Singleton
public class ObserverConfigDispatcher implements Consumer<Map<String, Object>> {

    private static final Logger logger = LoggerFactory.getLogger(ObserverConfigDispatcher.class);
    private final Subject<Event> sentinelSubject = PublishSubject.create();
    private final Subject<Event> pcFiltersSubject = PublishSubject.create();
    private final Subject<Event> callFiltersSubject = PublishSubject.create();
    private final Subject<Event> connectorsSubject = PublishSubject.create();
    private final Subject<Event> serviceMappingsSubject = PublishSubject.create();
    private Map<String, Consumer<Event>> consumers = new HashMap<>();
    private ConfigHolder<ObserverConfig> configHolder;

    public ObserverConfigDispatcher(ObserverConfig defaultConfig) {
        this.configHolder = new ConfigHolder(defaultConfig, ObserverConfig.class);
    }

    @PostConstruct
    void setup() {
        this.consumers.put("sentinels", sentinelSubject::onNext);
        this.consumers.put("callFilters", callFiltersSubject::onNext);
        this.consumers.put("pcFilters", callFiltersSubject::onNext);
        this.consumers.put("connectors", connectorsSubject::onNext);
        this.consumers.put("servicemappings", serviceMappingsSubject::onNext);
    }

    @Override
    public void accept(Map<String, Object> updatedConfig) {
        this.configHolder.renew(updatedConfig);
        ObserverConfig observerConfig = this.configHolder.getConfig();
        Map<String, Object> additions = this.configHolder.getAdditionsFlatMap();
        final Event addEvent = new Event(observerConfig, EventType.ADDITION);
        additions.keySet().stream().forEach(key -> this.dispatch(key, addEvent));

        Map<String, Object> removals = this.configHolder.getRemovalsFlatMap();
        final Event removeEvent = new Event(observerConfig, EventType.REMOVAL);
        removals.keySet().stream().forEach(key -> this.dispatch(key, removeEvent));
    }

    public Observable<Event> onSentinelsChanged() {
        return this.sentinelSubject;
    }

    public Observable<Event> onServiceMappingsChanged() {
        return this.serviceMappingsSubject;
    }

    public Observable<Event> onConnectorsChanged() {
        return this.connectorsSubject;
    }

    public Observable<Event> onCallFiltersChanged() {
        return this.callFiltersSubject;
    }

    public Observable<Event> onPCFiltersChanged() {
        return this.pcFiltersSubject;
    }

    public ObserverConfig getConfig() {
        return this.configHolder.getConfig();
    }

    private void dispatch(String key, Event event) {
        Iterator<Map.Entry<String, Consumer<Event>>> it = this.consumers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Consumer<Event>> entry = it.next();
            String prefix = entry.getKey();
            Consumer<Event> consumer = entry.getValue();
            if (key.startsWith(prefix)) {
                consumer.accept(event);
            }
        }
    }

    public enum EventType {
        ADDITION,
        REMOVAL,
    }

    public static class Event {
        public final ObserverConfig config;
        public final EventType eventType;

        public Event(ObserverConfig config, EventType eventType) {
            this.config = config;
            this.eventType = eventType;
        }
    }
}
