package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.common.OutboundReportTypeVisitors;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.evaluators.listeners.CallEvents;
import org.observertc.webrtc.observer.samples.ObservedClientSample;
import org.observertc.webrtc.observer.sinks.OutboundReportsObserver;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class ObservedClientSampleProcessingPipeline implements Consumer<ObservedClientSample> {

    private final Subject<ObservedClientSample> clientSampleSubject = PublishSubject.create();

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
    OutboundReportsObserver outboundReportsObserver;

    @Inject
    DemuxCollectedCallSamples demuxCollectedCallSamples;


    @PostConstruct
    void setup() {
        var clientSamplesBufferMaxTimeInS = this.observerConfig.evaluators.clientSamplesBufferMaxTimeInS;
        var clientSamplesBufferMaxItems = this.observerConfig.evaluators.clientSamplesBufferMaxItems;
        var clientReportsPreCollectingMaxTimeInS = this.observerConfig.evaluators.clientReportsPreCollectingTimeInS;
        var clientReportsPreCollectingMaxItems = this.observerConfig.evaluators.clientReportsPreCollectingMaxItems;

        var samplesBuffer = this.clientSampleSubject
                .buffer(clientSamplesBufferMaxTimeInS, TimeUnit.SECONDS, clientSamplesBufferMaxItems)
                .share();

        var observableCollectedCallSamples = samplesBuffer
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

        this.reportCallMetaData
                .getObservableCallMetaReports()
                .buffer(clientReportsPreCollectingMaxTimeInS, TimeUnit.SECONDS, clientReportsPreCollectingMaxItems)
                .subscribe(this.outboundReportEncoder::encodeCallMetaReports);

        this.callEvents
                .getObservableCalEventReports()
                .buffer(clientReportsPreCollectingMaxTimeInS, TimeUnit.SECONDS, clientReportsPreCollectingMaxItems)
                .subscribe(this.outboundReportEncoder::encodeCallEventReports);

        this.demuxCollectedCallSamples
                .getObservableClientTransportReport()
                .buffer(clientReportsPreCollectingMaxTimeInS, TimeUnit.SECONDS, clientReportsPreCollectingMaxItems)
                .subscribe(this.outboundReportEncoder::encodeClientTransportReport);

        this.demuxCollectedCallSamples
                .getObservableClientDataChannelReport()
                .buffer(clientReportsPreCollectingMaxTimeInS, TimeUnit.SECONDS, clientReportsPreCollectingMaxItems)
                .subscribe(this.outboundReportEncoder::encodeClientDataChannelReport);

        this.demuxCollectedCallSamples
                .getObservableClientExtensionReport()
                .buffer(clientReportsPreCollectingMaxTimeInS, TimeUnit.SECONDS, clientReportsPreCollectingMaxItems)
                .subscribe(this.outboundReportEncoder::encodeClientExtensionReport);

        this.demuxCollectedCallSamples
                .getObservableInboundAudioTrackReport()
                .buffer(clientReportsPreCollectingMaxTimeInS, TimeUnit.SECONDS, clientReportsPreCollectingMaxItems)
                .subscribe(this.outboundReportEncoder::encodeInboundAudioTrackReport);

        this.demuxCollectedCallSamples
                .getObservableInboundVideoTrackReport()
                .buffer(clientReportsPreCollectingMaxTimeInS, TimeUnit.SECONDS, clientReportsPreCollectingMaxItems)
                .subscribe(this.outboundReportEncoder::encodeInboundVideoTrackReport);

        this.demuxCollectedCallSamples
                .getObservableOutboundAudioTrackReport()
                .buffer(clientReportsPreCollectingMaxTimeInS, TimeUnit.SECONDS, clientReportsPreCollectingMaxItems)
                .subscribe(this.outboundReportEncoder::encodeOutboundAudioTrackReport);

        this.demuxCollectedCallSamples
                .getObservableOutboundVideoTrackReport()
                .buffer(clientReportsPreCollectingMaxTimeInS, TimeUnit.SECONDS, clientReportsPreCollectingMaxItems)
                .subscribe(this.outboundReportEncoder::encodeOutboundVideoTrackReport);

        this.demuxCollectedCallSamples
                .getObservableMediaTrackReport()
                .buffer(clientReportsPreCollectingMaxTimeInS, TimeUnit.SECONDS, clientReportsPreCollectingMaxItems)
                .subscribe(this.outboundReportEncoder::encodeMediaTrackReport);
    }

    private Predicate<OutboundReport> makeOutboundReportPredicate() {
        var config = this.observerConfig.outboundReports;
        var typeVisitor = OutboundReportTypeVisitors.makeTypeFilter(config);
        return report -> {
            return typeVisitor.apply(null, report.getType());
        };
    }

    @Override
    public void accept(ObservedClientSample observedClientSample) throws Throwable {
        this.clientSampleSubject.onNext(observedClientSample);
    }

    public Observable<OutboundReport> getObservableOutboundReport() {
        var typeFilter = this.makeOutboundReportPredicate();
        return this.outboundReportEncoder
                .getObservableOutboundReport()
                .filter(typeFilter);
    }
}
