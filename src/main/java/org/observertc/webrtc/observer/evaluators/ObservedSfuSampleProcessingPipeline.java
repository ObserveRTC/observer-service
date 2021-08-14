package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.OutboundReports;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.samples.ObservedSfuSample;
import org.observertc.webrtc.observer.sinks.OutboundReportsObserver;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class ObservedSfuSampleProcessingPipeline implements Consumer<ObservedSfuSample> {

    private final Subject<ObservedSfuSample> sfuSampleSubject = PublishSubject.create();

    @Inject
    ObserverConfig observerConfig;

    @Inject
    HazelcastEventSubscriber hazelcastEventSubscriber;

    @Inject
    ObservedSfuSampleObfuscator obfuscator;

    @Inject
    AddNewSfuEntities addNewSfuEntities;

    @Inject
    ListenSfuEntryChanges listenSfuEntryChanges;

    @Inject
    ListenSfuTransportEntryChanges listenSfuTransportEntryChanges;

    @Inject
    ListenSfuRtpStreamEntryChanges listenSfuRtpStreamEntryChanges;

    @Inject
    CollectSfuSamples collectSfuSamples;

    @Inject
    OutboundReportEncoder outboundReportEncoder;

    @Inject
    OutboundReportsObserver outboundReportsObserver;

    @Inject
    DemuxCollectedSfuSamples demuxCollectedSfuSamples;

    @PostConstruct
    void setup() {
        var clientSamplesBufferMaxTimeInS = this.observerConfig.evaluators.sfuSamplesBufferMaxTimeInS;
        var clientSamplesBufferMaxItems = this.observerConfig.evaluators.sfuSamplesBufferMaxItems;
        this.hazelcastEventSubscriber
                .withSfuEntriesLocalListener(this.listenSfuEntryChanges)
                .withSfuTransportEntriesLocalListener(this.listenSfuTransportEntryChanges)
                .withSfuRtpStreamEntriesLocalListener(this.listenSfuRtpStreamEntryChanges)
        ;

        var samplesBuffer = this.sfuSampleSubject
                .buffer(clientSamplesBufferMaxTimeInS, TimeUnit.SECONDS, clientSamplesBufferMaxItems)
                .share();

        var observableCollectedSfuSamples = samplesBuffer
                .map(this.obfuscator)
                .map(this.collectSfuSamples)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .share();

        observableCollectedSfuSamples
                .subscribe(this.addNewSfuEntities);

        observableCollectedSfuSamples
                .subscribe(this.demuxCollectedSfuSamples);

        this.addNewSfuEntities
                .getObservableCallEventReports()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeSfuEventReport);

        this.listenSfuEntryChanges
                .getObservableSfuEventReports()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeSfuEventReport);

        this.listenSfuTransportEntryChanges
                .getObservableSfuEventReports()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeSfuEventReport);

        this.listenSfuRtpStreamEntryChanges
                .getObservableSfuEventReports()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeSfuEventReport);

        this.demuxCollectedSfuSamples
                .getSfuTransportReport()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeSfuTransportReport);

        this.demuxCollectedSfuSamples
                .getSfuInboundRtpStreamReport()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeSfuInboundRtpStreamReport);

        this.demuxCollectedSfuSamples
                .getSfuOutboundRtpStreamReport()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeSfuOutboundRtpStreamReport);

        this.demuxCollectedSfuSamples
                .getSctpStreamReport()
                .buffer(30, TimeUnit.SECONDS, 1000)
                .subscribe(this.outboundReportEncoder::encodeSfuSctpStreamReport);


        var reportsBufferMaxItems = this.observerConfig.evaluators.reportsBufferMaxItems;
        var reportsBufferMaxRetainInS = this.observerConfig.evaluators.reportsBufferMaxRetainInS;
        this.outboundReportEncoder
                .getObservableOutboundReport()
                .buffer(reportsBufferMaxRetainInS, TimeUnit.SECONDS, reportsBufferMaxItems)
                .map(OutboundReports::fromList)
                .subscribe(this.outboundReportsObserver);
    }

    @Override
    public void accept(ObservedSfuSample observedSfuSample) throws Throwable {
        this.sfuSampleSubject.onNext(observedSfuSample);
    }
}
