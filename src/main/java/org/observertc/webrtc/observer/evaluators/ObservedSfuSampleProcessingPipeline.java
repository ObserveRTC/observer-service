package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.evaluators.listeners.SfuEvents;
import org.observertc.webrtc.observer.samples.ObservedSfuSample;
import org.observertc.webrtc.observer.sinks.OutboundReportsCollector;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class ObservedSfuSampleProcessingPipeline {

    private final Subject<List<ObservedSfuSample>> sfuSamples = PublishSubject.create();

    @Inject
    ObserverConfig observerConfig;

    @Inject
    SfuEvents sfuEvents;

    @Inject
    ObservedSfuSampleObfuscator obfuscator;

    @Inject
    AddNewSfuEntities addNewSfuEntities;

    @Inject
    CollectSfuSamples collectSfuSamples;

    @Inject
    OutboundReportEncoder outboundReportEncoder;

    @Inject
    OutboundReportsCollector outboundReportsObserver;

    @Inject
    DemuxCollectedSfuSamples demuxCollectedSfuSamples;

    @PostConstruct
    void setup() {
        var observableCollectedSfuSamples = sfuSamples
                .map(this.obfuscator)
                .map(this.collectSfuSamples)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .share();

        observableCollectedSfuSamples
                .subscribe(this.addNewSfuEntities);

        observableCollectedSfuSamples
                .subscribe(this.demuxCollectedSfuSamples);

        this.debounce(this.demuxCollectedSfuSamples.getSfuTransportReport())
                .subscribe(this.outboundReportEncoder::encodeSfuTransportReport);

        this.debounce(this.demuxCollectedSfuSamples.getSfuInboundRtpPadReport())
                .subscribe(this.outboundReportEncoder::encodeSfuInboundRtpPadReport);

        this.debounce(this.demuxCollectedSfuSamples.getSfuOutboundRtpPadReport())
                .subscribe(this.outboundReportEncoder::encodeSfuOutboundRtpPadReport);

        this.debounce(this.demuxCollectedSfuSamples.getSctpStreamReport())
                .subscribe(this.outboundReportEncoder::encodeSfuSctpStreamReport);

        this.debounce(this.demuxCollectedSfuSamples.getSfuTransportReport())
                .subscribe(this.outboundReportEncoder::encodeSfuTransportReport);

        this.debounce(this.sfuEvents.getObservableSfuEventReports())
                .subscribe(this.outboundReportEncoder::encodeSfuEventReport);
    }

//    @Override
//    public void accept(List<ObservedSfuSample> observedSfuSamples) throws Throwable {
//        this.sfuSamples.onNext(observedSfuSamples);
//    }

    private<T> Observable<List<T>> debounce(Observable<T> source) {
        var debounceConfig = this.observerConfig.internalCollectors.sfuProcessDebouncers;
        var maxItems = debounceConfig.maxItems;
        var maxTimeInS = debounceConfig.maxTimeInS;
        if (maxItems < 1 && maxTimeInS < 1) {
            return source.map(List::of);
        }
        if (maxItems < 1) {
            return source.buffer(maxTimeInS, TimeUnit.SECONDS);
        }
        if (maxTimeInS < 1) {
            return source.buffer(maxItems);
        }
        return source.buffer(maxTimeInS, TimeUnit.SECONDS, maxItems);
    }

    public Observer<List<ObservedSfuSample>> getObservedSfuSamplesObserver() {
        return this.sfuSamples;
    }

    public Observable<List<OutboundReport>> getObservableOutboundReports() {
        return this.outboundReportEncoder
                .getObservableOutboundReports();
    }

}
