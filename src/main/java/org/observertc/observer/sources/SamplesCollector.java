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

    public SamplesCollector(ObserverConfig observerConfig) {
        var maxItems = observerConfig.buffers.samplesCollector.maxItems;
        var maxTimeInMs = observerConfig.buffers.samplesCollector.maxTimeInMs;

        this.collector = ObservableCollector.<ReceivedSamples>builder()
                .withMaxTimeInMs(maxTimeInMs)
                .withMaxItems(maxItems)
                .withEmptyForward(true)
                .withCreateTimerAfterEmitFlag(true)
                .build();

        this.collector.observableEmittedItems().subscribe(this::forward);
    }

    @Inject
    ObserverConfig observerConfig;

    @Inject
    SourceMetrics sourceMetrics;

    private Predicate<String> serviceIdsPredicate = (obj) -> true;

    @PostConstruct
    void init() {
        var allowedServiceIds = this.observerConfig.sources.allowedServiceIds;
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
        if (!this.observerConfig.sources.acceptClientSamples) {
            receivedSamples.samples.clientSamples = null;
        }
        if (!this.observerConfig.sources.acceptSfuSamples) {
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
