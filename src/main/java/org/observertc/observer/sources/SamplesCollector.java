package org.observertc.observer.sources;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.common.MinuteToTimeZoneOffsetConverter;
import org.observertc.observer.common.ObservableCollector;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.samples.*;
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

    private final MinuteToTimeZoneOffsetConverter minuteToTimeZoneOffsetConverter;
    private final ObservableCollector<ReceivedSamples> collector;

    public SamplesCollector(ObserverConfig observerConfig) {
        var maxItems = observerConfig.buffers.samplesCollector.maxItems;
        var maxTimeInMs = observerConfig.buffers.samplesCollector.maxTimeInMs;

        this.minuteToTimeZoneOffsetConverter = new MinuteToTimeZoneOffsetConverter();
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
        if (!this.observerConfig.sources.acceptClientSamples) {
            receivedSamples.samples.clientSamples = null;
        }
        if (!this.observerConfig.sources.acceptSfuSamples) {
            receivedSamples.samples.sfuSamples = null;
        }
        if (receivedSamples.samples == null) {
            return;
        }
        if (receivedSamples.samples.clientSamples == null && receivedSamples.samples.sfuSamples == null) {
            return;
        }
        this.collector.add(receivedSamples);
    }

    private void forward(List<ReceivedSamples> receivedSamples) {
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
                        var timeZoneId = this.minuteToTimeZoneOffsetConverter.apply(clientSample.timeZoneOffsetInHours);
//                        if (this.useServerTimestamps) {
//                            clientSample.timestamp = Instant.now().toEpochMilli();
//                        }
                        var observedClientSample =  ObservedClientSample.builder()
                                .setMediaUnitId(receivedSample.mediaUnitId)
                                .setServiceId(receivedSample.serviceId)
                                .setTimeZoneId(timeZoneId)
                                .setClientSample(clientSample)
                                .build();
                        observedClientSamplesBuilder.addObservedClientSample(observedClientSample);
                    });


            if (this.observerConfig.sources.acceptSfuSamples) {
                SamplesVisitor.streamSfuSamples(receivedSample.samples)
                        .forEach(sfuSample -> {
                            var timeZoneId = this.minuteToTimeZoneOffsetConverter.apply(sfuSample.timeZoneOffsetInHours);
//                        if (this.useServerTimestamps) {
//                            sfuSample.timestamp = Instant.now().toEpochMilli();
//                        }
                            var observedSfuSample =  ObservedSfuSample.builder()
                                    .setMediaUnitId(receivedSample.mediaUnitId)
                                    .setServiceId(receivedSample.serviceId)
                                    .setTimeZoneId(timeZoneId)
                                    .setSfuSample(sfuSample)
                                    .build();
                            observedSfuSamplesBuilder.addObservedSfuSample(observedSfuSample);
                        });
            }

        }
        var observedClientSamples = observedClientSamplesBuilder.build();
        if (observedClientSamples != null) {
            synchronized (this) {
                this.observedClientSamplesSubject.onNext(observedClientSamples);
            }
        }

        var observedSfuSamples = observedSfuSamplesBuilder.build();
        if (observedSfuSamples != null) {
            synchronized (this) {
                this.observedSfuSamplesSubject.onNext(observedSfuSamples);
            }
        }
    }
}
