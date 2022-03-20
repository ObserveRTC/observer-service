package org.observertc.observer.sources;

import io.micronaut.scheduling.annotation.Scheduled;
import io.reactivex.rxjava3.core.Observable;
import org.observertc.observer.common.ObservableCollector;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.samples.ObservedClientSample;
import org.observertc.observer.samples.ObservedClientSampleBuilder;
import org.observertc.observer.samples.ObservedSfuSample;
import org.observertc.observer.samples.ObservedSfuSampleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Singleton
public class SamplesCollector {

    private static final Logger logger = LoggerFactory.getLogger(SamplesCollector.class);

    @Inject
    ObserverConfig observerConfig;

    private ObservableCollector<ObservedClientSample> observableClientSampleCollector;
    private ObservableCollector<ObservedSfuSample> observableSfuSampleCollector;
    private Predicate<String> serviceFilter = (serviceId) -> true;

    @PostConstruct
    void setup() {
        var maxItems = observerConfig.buffers.clientSamplesCollector.maxItems;
        var maxTimeInMs = observerConfig.buffers.clientSamplesCollector.maxTimeInS * 1000;
        this.observableClientSampleCollector = ObservableCollector.<ObservedClientSample>builder()
                .withLogger(logger)
                .withMaxItems(maxItems)
                .withMaxTimeInMs(maxTimeInMs)
                .build();

        this.observableSfuSampleCollector = ObservableCollector.<ObservedSfuSample>builder()
                .withLogger(logger)
                .withMaxItems(maxItems)
                .withMaxTimeInMs(maxTimeInMs)
                .build();

        if (Objects.nonNull(this.observerConfig.sources.allowedServiceIds) && 0 < this.observerConfig.sources.allowedServiceIds.size()) {
            this.serviceFilter = this.observerConfig.sources.allowedServiceIds::contains;
        }
    }

    @PreDestroy
    void teardown() {
        if (!this.observableClientSampleCollector.isClosed()) {
            this.observableClientSampleCollector.onComplete();
        }
        if (!this.observableSfuSampleCollector.isClosed()) {
            this.observableSfuSampleCollector.onComplete();
        }
    }

    public void add(ReceivedSamples receivedSamples) throws Exception {
        var serviceId = receivedSamples.serviceId;
        var mediaUnitId = receivedSamples.mediaUnitId;
        var samples = receivedSamples.samples;
        if (!this.serviceFilter.test(serviceId)) {
            logger.debug("Service {} is not allowed", serviceId);
            return;
        }
        if (Objects.nonNull(samples.clientSamples)) {
            for (var clientSample : samples.clientSamples) {
                var observedClientSample = ObservedClientSampleBuilder.from(clientSample)
                        .withServiceId(serviceId)
                        .withMediaUnitId(mediaUnitId)
                        .build();
                this.observableClientSampleCollector.add(observedClientSample);
            }
        }
        if (Objects.nonNull(samples.sfuSamples)) {
            for (var sfuSample : samples.sfuSamples) {
                var observedSfuSample = ObservedSfuSampleBuilder.from(sfuSample)
                        .withServiceId(serviceId)
                        .withMediaUnitId(mediaUnitId)
                        .build();
                this.observableSfuSampleCollector.add(observedSfuSample);
            }
        }
    }

    @Scheduled(fixedDelay = "10s", initialDelay = "5m")
    void refresh() {
        this.observableClientSampleCollector.checkTime();
        this.observableSfuSampleCollector.checkTime();
    }

    public Observable<List<ObservedClientSample>> observableClientSamples() {
        return this.observableClientSampleCollector;
    }

    public Observable<List<ObservedSfuSample>> observableSfuSamples() {
        return this.observableSfuSampleCollector;
    }
}
