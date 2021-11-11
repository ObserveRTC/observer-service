package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.evaluators.listeners.CallEvents;
import org.observertc.webrtc.observer.samples.ObservedClientSample;
import org.observertc.webrtc.observer.sinks.OutboundReportsCollector;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class ObservedClientSampleProcessingPipeline implements Consumer<List<ObservedClientSample>> {

    private final Subject<List<ObservedClientSample>> clientSamples = PublishSubject.create();

    @Inject
    ObserverConfig observerConfig;

    @Inject
    CallEvents callEvents;

    @Inject
    ObservedClientSampleObfuscator obfuscator;

    @Inject
    ReportCallMetaData reportCallMetaData;

    @Inject
    AddNewCallEntities addNewCallEntities;

    @Inject
    CollectCallSamples collectCallSamples;

    @Inject
    OutboundReportEncoder outboundReportEncoder;

    @Inject
    OutboundReportsCollector outboundReportsObserver;

    @Inject
    DemuxCollectedCallSamples demuxCollectedCallSamples;

    @PostConstruct
    void setup() {
//        clientSamples.doOnError();
//        clientSamples.doOnComplete();
//        clientSamples.doOnDispose()

        var observableCollectedCallSamples = clientSamples
                .map(this.obfuscator)
                .map(this.collectCallSamples)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .share();

        observableCollectedCallSamples
                .subscribe(this.reportCallMetaData);

        observableCollectedCallSamples
                .subscribe(this.addNewCallEntities);

        observableCollectedCallSamples
                .subscribe(this.demuxCollectedCallSamples);

        this.debounce(this.reportCallMetaData.getObservableCallMetaReports())
                .subscribe(this.outboundReportEncoder::encodeCallMetaReports);

        this.debounce(this.callEvents.getObservableCalEventReports())
                .subscribe(this.outboundReportEncoder::encodeCallEventReports);

        this.debounce(this.demuxCollectedCallSamples.getObservableClientTransportReport())
                .subscribe(this.outboundReportEncoder::encodeClientTransportReport);

        this.debounce(this.demuxCollectedCallSamples.getObservableClientDataChannelReport())
                .subscribe(this.outboundReportEncoder::encodeClientDataChannelReport);

        this.debounce(this.demuxCollectedCallSamples.getObservableClientExtensionReport())
                .subscribe(this.outboundReportEncoder::encodeClientExtensionReport);

        this.debounce(this.demuxCollectedCallSamples.getObservableInboundAudioTrackReport())
                .subscribe(this.outboundReportEncoder::encodeInboundAudioTrackReport);

        this.debounce(this.demuxCollectedCallSamples.getObservableInboundVideoTrackReport())
                .subscribe(this.outboundReportEncoder::encodeInboundVideoTrackReport);

        this.debounce(this.demuxCollectedCallSamples.getObservableOutboundAudioTrackReport())
                .subscribe(this.outboundReportEncoder::encodeOutboundAudioTrackReport);

        this.debounce(this.demuxCollectedCallSamples.getObservableOutboundVideoTrackReport())
                .subscribe(this.outboundReportEncoder::encodeOutboundVideoTrackReport);

        this.debounce(this.demuxCollectedCallSamples.getObservableMediaTrackReport())
                .subscribe(this.outboundReportEncoder::encodeMediaTrackReport);
    }

    @Override
    public void accept(List<ObservedClientSample> observedClientSamples) throws Throwable {
        clientSamples.onNext(observedClientSamples);
    }

    private<T> Observable<List<T>> debounce(Observable<T> source) {
        var debounceConfig = this.observerConfig.internalCollectors.clientProcessDebouncers;
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

    public Observable<List<OutboundReport>> getObservableOutboundReports() {
        return this.outboundReportEncoder.getObservableOutboundReports();
    }
}
