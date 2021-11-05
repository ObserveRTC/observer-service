package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.common.OutboundReportTypeVisitors;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.evaluators.listeners.SfuEvents;
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
    OutboundReportsObserver outboundReportsObserver;

    @Inject
    DemuxCollectedSfuSamples demuxCollectedSfuSamples;

    @PostConstruct
    void setup() {
        var sfuSamplesBufferMaxTimeInS = this.observerConfig.evaluators.sfuSamplesBufferMaxTimeInS;
        var sfuSamplesBufferMaxItems = this.observerConfig.evaluators.sfuSamplesBufferMaxItems;
        var sfuReportsPreCollectingMaxTimeInS = this.observerConfig.evaluators.sfuReportsPreCollectingTimeInS;
        var sfuReportsPreCollectingMaxItems = this.observerConfig.evaluators.sfuReportsPreCollectingMaxItems;

        var samplesBuffer = this.sfuSampleSubject
                .buffer(sfuSamplesBufferMaxTimeInS, TimeUnit.SECONDS, sfuSamplesBufferMaxItems)
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

        this.demuxCollectedSfuSamples
                .getSfuTransportReport()
                .buffer(sfuReportsPreCollectingMaxTimeInS, TimeUnit.SECONDS, sfuReportsPreCollectingMaxItems)
                .subscribe(this.outboundReportEncoder::encodeSfuTransportReport);

        this.demuxCollectedSfuSamples
                .getSfuRtpSourceReport()
                .buffer(sfuReportsPreCollectingMaxTimeInS, TimeUnit.SECONDS, sfuReportsPreCollectingMaxItems)
                .subscribe(this.outboundReportEncoder::encodeSfuInboundRtpPadReport);

        this.demuxCollectedSfuSamples
                .getSfuRtpSinkReport()
                .buffer(sfuReportsPreCollectingMaxTimeInS, TimeUnit.SECONDS, sfuReportsPreCollectingMaxItems)
                .subscribe(this.outboundReportEncoder::encodeSfuOutboundRtpPadReport);

        this.demuxCollectedSfuSamples
                .getSctpStreamReport()
                .buffer(sfuReportsPreCollectingMaxTimeInS, TimeUnit.SECONDS, sfuReportsPreCollectingMaxItems)
                .subscribe(this.outboundReportEncoder::encodeSfuSctpStreamReport);

        this.sfuEvents
                .getObservableSfuEventReports()
                .buffer(sfuReportsPreCollectingMaxTimeInS, TimeUnit.SECONDS, sfuReportsPreCollectingMaxItems)
                .subscribe(this.outboundReportEncoder::encodeSfuEventReport);
    }

    private Predicate<OutboundReport> makeOutboundReportPredicate() {
        var config = this.observerConfig.outboundReports;
        var typeVisitor = OutboundReportTypeVisitors.makeTypeFilter(config);
        return report -> {
            return typeVisitor.apply(null, report.getType());
        };
    }

    @Override
    public void accept(ObservedSfuSample observedSfuSample) throws Throwable {
        this.sfuSampleSubject.onNext(observedSfuSample);
    }

    public Observable<OutboundReport> getObservableOutboundReport() {
        var typeFilter = this.makeOutboundReportPredicate();
        return this.outboundReportEncoder
                .getObservableOutboundReport()
                .filter(typeFilter);
    }
}
