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

//    @Inject
//    ReportMediaTracks reportMediaTracks;

    @Inject
    ReportCallMetaData reportCallMetaData;

    @Inject
    UpdateRepositories updateRepositories;

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
//                .map(List::of)
                // TODO: measure a start time
                .map(this.obfuscator)
                .map(this.collectCallSamples)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .share();

//        observableCollectedCallSamples
//                .subscribe(this.reportMediaTracks);
//
        observableCollectedCallSamples
                .subscribe(this.reportCallMetaData);

        observableCollectedCallSamples
                .subscribe(this.updateRepositories);

        // TODO: measure the end time somehow
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

        var reportsBufferMaxItems = this.observerConfig.evaluators.reportsBufferMaxRetainInS;
        var reportsBufferMaxRetainInS = this.observerConfig.evaluators.reportsBufferMaxRetainInS;
        this.outboundReportEncoder
                .buffer(reportsBufferMaxRetainInS, TimeUnit.SECONDS, reportsBufferMaxItems)
                .map(OutboundReports::fromList)
                .subscribe(this.outboundReportsObserver);
    }

    @Override
    public void accept(ObservedClientSample observedClientSample) throws Throwable {
        this.input.onNext(observedClientSample);
    }
}
