package org.observertc.webrtc.observer.sources;

import io.reactivex.rxjava3.core.Observable;
import org.observertc.webrtc.observer.common.ObservableCollector;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.samples.ObservedSfuSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class SfuSamplesCollector {

    private static final Logger logger = LoggerFactory.getLogger(SfuSamplesCollector.class);

    @Inject
    ObserverConfig observerConfig;

    private ObservableCollector<ObservedSfuSample> observableCollector;

    @PostConstruct
    void setup() {
        var maxItems = observerConfig.internalCollectors.sfuSamples.maxItems;
        var maxTimeInMs = observerConfig.internalCollectors.sfuSamples.maxTimeInS * 1000;
        this.observableCollector = ObservableCollector.<ObservedSfuSample>builder()
                .withResilientInput(true)
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

    public void add(ObservedSfuSample observedSfuSample) {
        this.observableCollector.add(observedSfuSample);
    }

    public void addAll(List<ObservedSfuSample> observedSfuSamples) {
        this.observableCollector.addAll(observedSfuSamples);
    }

    public Observable<List<ObservedSfuSample>> observableSfuSamples() {
        return this.observableCollector;
    }
}
