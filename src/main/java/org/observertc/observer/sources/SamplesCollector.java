package org.observertc.observer.sources;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.common.ObservableCollector;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.metrics.SourceMetrics;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.samples.ObservedSfuSamples;
import org.observertc.observer.samples.SamplesVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Singleton
public class SamplesCollector {

    private static final Logger logger = LoggerFactory.getLogger(SamplesCollector.class);

    private Subject<ObservedClientSamples> observedClientSamplesSubject = PublishSubject.create();
    private Subject<ObservedSfuSamples> observedSfuSamplesSubject = PublishSubject.create();

    public Observable<ObservedClientSamples> observableClientSamples() {
        return this.observedClientSamplesSubject;
    }

    public Observable<ObservedSfuSamples> observableSfuSamples() {
        return this.observedSfuSamplesSubject;
    }

    private final ObservableCollector<ReceivedSamples> collector;
    private final int overloadedThreshold;
    private final int maxConsecutiveOverload;
    private volatile int consecutiveOverload = 0;

    public SamplesCollector(ObserverConfig observerConfig) {
        var collectorConfig = observerConfig.buffers.samplesCollector;
        var maxItems = collectorConfig.maxItems;
        var maxTimeInMs = collectorConfig.maxTimeInMs;

        this.collector = ObservableCollector.<ReceivedSamples>builder()
                .withMaxTimeInMs(maxTimeInMs)
                .withMaxItems(maxItems)
                .withEmptyForward(true)
                .withCreateTimerAfterEmitFlag(true)
                .build();

        this.collector.observableEmittedItems().subscribe(this::forward);

        if (0 < collectorConfig.overloadThreshold) {
            var overloadedThreshold = collectorConfig.overloadThreshold;
            if (overloadedThreshold < maxItems) {
                logger.warn("Config for SamplesCollector is invalid. THe number of items indicate the collector is overloaded {} cannot be smaller than the maximum allowed items {}. In this case the overloadThreshold will be the maximum number of items",
                        overloadedThreshold,
                        maxItems);
                overloadedThreshold = maxItems;
            }
            var maxConsecutiveOverloaded = collectorConfig.maxConsecutiveOverloaded;
            if (maxConsecutiveOverloaded < 1) {
                logger.warn("Config for SamplesCollector is invalid. The maxConsecutiveOverloaded cannot be smaller than one (in config it is: {}) if overloadThreshold is set. In this case we set the maxConsecutiveOverloaded to 1",
                        maxConsecutiveOverloaded
                );
                maxConsecutiveOverloaded = 1;
            }
            this.overloadedThreshold = overloadedThreshold;
            this.maxConsecutiveOverload = maxConsecutiveOverloaded;
        } else {
            this.overloadedThreshold = -1;
            this.maxConsecutiveOverload = -1;
        }
        logger.info("SamplesCollector is Initialized. maxItems: {}, maxTimeIMs: {}, overloadedThreshold: {}, max consecutive overload: {}",
                maxItems,
                maxTimeInMs,
                this.overloadedThreshold,
                this.maxConsecutiveOverload
        );
    }

    @Inject
    ObserverConfig.SourcesConfig config;

    @Inject
    SourceMetrics sourceMetrics;

    private Predicate<String> serviceIdsPredicate = (obj) -> true;

    @PostConstruct
    void init() {

        var allowedServiceIds = this.config.allowedServiceIds;
        if (allowedServiceIds != null) {
            this.serviceIdsPredicate = serviceId -> serviceId != null && allowedServiceIds.contains(serviceId);
            logger.info("Observer is restricted to allow traffics from serviceIds: ", JsonUtils.objectToString(allowedServiceIds));
        }

    }

    @PreDestroy
    void teardown() {
        this.collector.flush();
    }


    public void accept(ReceivedSamples receivedSamples) {
        if (receivedSamples.samples == null) {
            return;
        }
        if (!this.config.acceptClientSamples) {
            receivedSamples.samples.clientSamples = null;
        }
        if (!this.config.acceptSfuSamples) {
            receivedSamples.samples.sfuSamples = null;
        }
        if (receivedSamples.samples.clientSamples == null && receivedSamples.samples.sfuSamples == null) {
            return;
        }
        this.collector.add(receivedSamples);
    }

    private void forward(List<ReceivedSamples> receivedSamples) {

        this.sourceMetrics.setBufferedSamples(receivedSamples.size());

        if (receivedSamples.size() < 1) {
            synchronized (this) {
                this.observedClientSamplesSubject.onNext(ObservedClientSamples.EMPTY_SAMPLES);
            }
            synchronized (this) {
                this.observedSfuSamplesSubject.onNext(ObservedSfuSamples.EMPTY_SAMPLES);
            }
            return;
        } else if (0 < this.overloadedThreshold) {
            var collectedSamplesSize = this.collector.size();
            if (this.overloadedThreshold < collectedSamplesSize + receivedSamples.size()) {

                this.sourceMetrics.incrementOverloadedSamplesCollector();

                if (this.maxConsecutiveOverload <= ++this.consecutiveOverload) {
                    logger.warn("Dropping {} number of Samples due to consecutive overload. The current load on the collector is {}", receivedSamples.size(), collectedSamplesSize);
                    return;
                }
                logger.warn("Overloaded Collector is detected! The number of collected samples are {}, The number of new samples going to be added to the collector is {}, the collector is overloaded {} consecutive times, maxConsecutive overload before the samples will be dropped is {}",
                        collectedSamplesSize,
                        receivedSamples.size(),
                        this.consecutiveOverload,
                        this.maxConsecutiveOverload
                );

            } else {
                this.consecutiveOverload = 0;
            }
        }
        var observedClientSamplesBuilder = ObservedClientSamples.builder();
        var observedSfuSamplesBuilder = ObservedSfuSamples.builder();
        for (var receivedSample : receivedSamples) {
            if (Objects.isNull(receivedSample) || Objects.isNull(receivedSample.samples)) {
                continue;
            }
            if (!this.serviceIdsPredicate.test(receivedSample.serviceId)) {
                logger.warn("Got receivedSample from a not allowed serviceId {}", receivedSample.serviceId);
                continue;
            }

            SamplesVisitor.streamClientSamples(receivedSample.samples)
                    .forEach(clientSample -> {
                        observedClientSamplesBuilder.add(
                                receivedSample.serviceId,
                                receivedSample.mediaUnitId,
                                clientSample
                        );
                    });


            SamplesVisitor.streamSfuSamples(receivedSample.samples)
                    .forEach(sfuSample -> {
                        observedSfuSamplesBuilder.add(
                                receivedSample.serviceId,
                                receivedSample.mediaUnitId,
                                sfuSample
                        );
                    });

        }
        var observedClientSamples = observedClientSamplesBuilder.build();
        if (observedClientSamples != null) {
            this.sourceMetrics.incrementObservedClientSamplesSamples(observedClientSamples.size());
            synchronized (this) {
                this.observedClientSamplesSubject.onNext(observedClientSamples);
            }
        }

        var observedSfuSamples = observedSfuSamplesBuilder.build();
        if (observedSfuSamples != null) {
            this.sourceMetrics.incrementObservedSfuSamplesSamples(observedSfuSamples.size());
            synchronized (this) {
                this.observedSfuSamplesSubject.onNext(observedSfuSamples);
            }
        }
    }
}
