package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.OutboundReports;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.samples.ObservedClientSample;
import org.observertc.webrtc.observer.sinks.OutboundReportsObserver;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class ProcessingPipeline implements Consumer<ObservedClientSample> {

    private final Subject<ObservedClientSample> input = PublishSubject.create();

    @Inject
    ObserverConfig observerConfig;

    @Inject
    HazelcastEventSubscriber hazelcastEventSubscriber;

    @Inject
    Obfuscator obfuscator;

    @Inject
    ReportCallMetaData reportCallMetaData;

    @Inject
    AddNewEntities addNewEntities;

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

        var samplesBuffer = this.input
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
                .subscribe(this.addNewEntities);

        observableCollectedCallSamples
                .subscribe(this.demuxCollectedCallSamples);

        this.addNewEntities
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
        this.outboundReportEncoder
                .getObservableOutboundReport()
                .buffer(reportsBufferMaxRetainInS, TimeUnit.SECONDS, reportsBufferMaxItems)
                .map(OutboundReports::fromList)
                .subscribe(this.outboundReportsObserver);
    }

    @Override
    public void accept(ObservedClientSample observedClientSample) throws Throwable {
        this.input.onNext(observedClientSample);
    }
}
