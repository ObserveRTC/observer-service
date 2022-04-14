package org.observertc.observer.sources;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Singleton;
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
                .build();

        this.collector.observableEmittedItems().subscribe(this::forward);
    }

    @PostConstruct
    void init() {

    }

    @PreDestroy
    void teardown() {
        this.collector.flush();
    }


    public void accept(ReceivedSamples receivedSamples) {
        this.collector.add(receivedSamples);
    }

    private void forward(List<ReceivedSamples> receivedSamples) {
        if (receivedSamples.size() < 1) {
            return;
        }
        var observedClientSamplesBuilder = ObservedClientSamples.builder();
        var observedSfuSamplesBuilder = ObservedSfuSamples.builder();
        for (var receivedSample : receivedSamples) {
            if (Objects.isNull(receivedSample) || Objects.isNull(receivedSample.samples)) {
                continue;
            }
            SamplesVisitor.streamClientSamples(receivedSample.samples)
                    .forEach(clientSample -> {
                        var timeZoneId = this.minuteToTimeZoneOffsetConverter.apply(clientSample.timeZoneOffsetInHours);
                        var observedClientSample =  ObservedClientSample.builder()
                                .setMediaUnitId(receivedSample.mediaUnitId)
                                .setServiceId(receivedSample.serviceId)
                                .setTimeZoneId(timeZoneId)
                                .setClientSample(clientSample)
                                .build();
                        observedClientSamplesBuilder.addObservedClientSample(observedClientSample);
                    });

            SamplesVisitor.streamSfuSamples(receivedSample.samples)
                    .forEach(sfuSample -> {
                        var timeZoneId = this.minuteToTimeZoneOffsetConverter.apply(sfuSample.timeZoneOffsetInHours);
                        var observedSfuSample =  ObservedSfuSample.builder()
                                .setMediaUnitId(receivedSample.mediaUnitId)
                                .setServiceId(receivedSample.serviceId)
                                .setTimeZoneId(timeZoneId)
                                .setSfuSample(sfuSample)
                                .build();
                        observedSfuSamplesBuilder.addObservedSfuSample(observedSfuSample);
                    });
        }
        var observedClientSamples = observedClientSamplesBuilder.build();
        if (0 < observedClientSamples.size()) {
            synchronized (this) {
                this.observedClientSamplesSubject.onNext(observedClientSamples);
            }
        }

        var observedSfuSamples = observedSfuSamplesBuilder.build();
        if (0 < observedSfuSamples.size()) {
            synchronized (this) {
                this.observedSfuSamplesSubject.onNext(observedSfuSamples);
            }
        }
    }
}
