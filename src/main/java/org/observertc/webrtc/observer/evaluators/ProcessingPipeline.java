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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Singleton
public class ProcessingPipeline implements Consumer<ObservedClientSample> {

    private final Subject<ObservedClientSample> input = PublishSubject.create();

    @Inject
    ObserverConfig observerConfig;

    @Inject
    Obfuscator obfuscator;

    @Inject
    ReportMediaTracks reportMediaTracks;

    @Inject
    ReportCallMetaData reportCallMetaData;

    @Inject
    UpdateRepositories updateRepositories;

    @Inject
    ReportClientChanges reportClientChanges;

    @Inject
    CollectCallSamples collectCallSamples;

    @Inject
    OutboundReportEncoder outboundReportEncoder;

    @Inject
    OutboundReportsObserver outboundReportsObserver;

    @PostConstruct
    void setup() {
        var samplesBuffer = this.input
                .buffer(1000, TimeUnit.SECONDS, 30)
                .share();

        var observableCollectedCallSamples = samplesBuffer
                // TODO: measure a start time
                .map(this.obfuscator)
                .map(this.collectCallSamples)
                .filter(Objects::nonNull)
                .share();

        observableCollectedCallSamples
                .subscribe(this.reportMediaTracks);

        observableCollectedCallSamples
                .subscribe(this.reportCallMetaData);

        observableCollectedCallSamples
                .subscribe(this.updateRepositories);

        // TODO: measure the end time somehow
        this.reportCallMetaData
                .observableCallMetaReports()
                .buffer(1000, TimeUnit.SECONDS, 30)
                .subscribe(this.outboundReportEncoder::encodeCallMetaReports);

        this.reportClientChanges
                .getObservableCallEventReports()
                .buffer(1000, TimeUnit.SECONDS, 30)
                .subscribe(this.outboundReportEncoder::encodeCallEventReports);

        this.outboundReportEncoder
                .buffer(observerConfig.evaluators.reportsBufferMaxItems, TimeUnit.SECONDS, observerConfig.evaluators.reportsBufferMaxRetainInS)
                .map(OutboundReports::fromList)
                .subscribe(this.outboundReportsObserver);
    }

    @Override
    public void accept(ObservedClientSample observedClientSample) throws Throwable {
        this.input.onNext(observedClientSample);
    }
}
