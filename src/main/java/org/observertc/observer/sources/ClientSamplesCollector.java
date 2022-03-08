package org.observertc.observer.sources;

import io.reactivex.rxjava3.core.Observable;
import org.observertc.observer.common.ObservableCollector;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.samples.ObservedClientSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class ClientSamplesCollector {

    private static final Logger logger = LoggerFactory.getLogger(ClientSamplesCollector.class);

    @Inject
    ObserverConfig observerConfig;

    private ObservableCollector<ObservedClientSample> observableCollector;

    @PostConstruct
    void setup() {
        var maxItems = observerConfig.buffers.clientSamplesCollector.maxItems;
        var maxTimeInMs = observerConfig.buffers.clientSamplesCollector.maxTimeInS * 1000;
        this.observableCollector = ObservableCollector.<ObservedClientSample>builder()
                .withLogger(logger)
                .withMaxItems(maxItems)
                .withMaxTimeInMs(maxTimeInMs)
                .build();
    }

    @PreDestroy
    void teardown() {
        if (!this.observableCollector.isClosed()) {
            this.observableCollector.onComplete();
        }
    }

    public void add(ObservedClientSample observedClientSample) {
        this.observableCollector.add(observedClientSample);
    }

    public void addAll(List<ObservedClientSample> observedClientSamples) {
        this.observableCollector.addAll(observedClientSamples);
    }

    public Observable<List<ObservedClientSample>> observableClientSamples() {
        return this.observableCollector;
    }
}
