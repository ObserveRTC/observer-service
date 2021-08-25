package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.common.OutboundReportTypeVisitors;
import org.observertc.webrtc.observer.common.OutboundReports;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.samples.ObservedClientSample;
import org.observertc.webrtc.observer.samples.ObservedSfuSample;
import org.observertc.webrtc.observer.sinks.OutboundReportsObserver;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class ObservedClientSampleProcessingPipeline implements Consumer<ObservedClientSample> {

    private final Subject<ObservedClientSample> clientSampleSubject = PublishSubject.create();
    private final Subject<ObservedSfuSample> sfuSampleSubject = PublishSubject.create();

    @Inject
    ObserverConfig observerConfig;

    @Inject
    HazelcastEventSubscriber hazelcastEventSubscriber;

    @Inject
    ObservedClientSampleObfuscator obfuscator;

    @Inject
    ReportCallMetaData reportCallMetaData;

    @Inject
    AddNewCallEntities addNewCallEntities;

    @Inject
    ListenClientEntryChanges listenClientEntryChanges;

    @Inject
    ListenCallEntryChanges listenCallEntryChanges;

    @Inject
    ListenPeerConnectionEntryChanges listenPeerConnectionEntryChanges;

    @Inject
    ListenMediaTrackEntryChanges listenMediaTrackEntryChanges;

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
        this.hazelcastEventSubscriber
                .withCallEntriesLocalListener(this.listenCallEntryChanges)
                .withClientEntriesLocalListener(this.listenClientEntryChanges)
                .withPeerConnectionEntriesLocalListener(this.listenPeerConnectionEntryChanges)
                .withMediaTrackEntriesLocalListener(this.listenMediaTrackEntryChanges)
        ;

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

        this.addNewCallEntities
                .getObservableCallEventReports()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeCallEventReports);

        this.reportCallMetaData
                .observableCallMetaReports()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeCallMetaReports);

        this.listenCallEntryChanges
                .getObservableCallEventReports()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeCallEventReports);

        this.listenClientEntryChanges
                .getObservableCallEventReports()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeCallEventReports);

        this.listenPeerConnectionEntryChanges
                .getObservableCallEventReports()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeCallEventReports);

        this.listenMediaTrackEntryChanges
                .getObservableCallEventReports()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeCallEventReports);

        this.demuxCollectedCallSamples
                .getObservableClientTransportReport()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeClientTransportReport);

        this.demuxCollectedCallSamples
                .getObservableClientDataChannelReport()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeClientDataChannelReport);

        this.demuxCollectedCallSamples
                .getObservableClientExtensionReport()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeClientExtensionReport);

        this.demuxCollectedCallSamples
                .getObservableInboundAudioTrackReport()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeInboundAudioTrackReport);

        this.demuxCollectedCallSamples
                .getObservableInboundVideoTrackReport()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeInboundVideoTrackReport);

        this.demuxCollectedCallSamples
                .getObservableOutboundAudioTrackReport()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeOutboundAudioTrackReport);

        this.demuxCollectedCallSamples
                .getObservableOutboundVideoTrackReport()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeOutboundVideoTrackReport);

        this.demuxCollectedCallSamples
                .getObservableMediaTrackReport()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeMediaTrackReport);



        var reportsBufferMaxItems = this.observerConfig.evaluators.reportsBufferMaxItems;
        var reportsBufferMaxRetainInS = this.observerConfig.evaluators.reportsBufferMaxRetainInS;
        var typeFilter = this.makeOutboundReportPredicate();
        this.outboundReportEncoder
                .getObservableOutboundReport()
                .filter(typeFilter)
                .buffer(reportsBufferMaxRetainInS, TimeUnit.SECONDS, reportsBufferMaxItems)
                .map(OutboundReports::fromList)
                .subscribe(this.outboundReportsObserver);
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
}
