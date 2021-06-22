package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.samples.ObservedClientSample;
import org.observertc.webrtc.observer.sinks.OutboundReportSender;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Singleton
public class ProcessingPipeline implements Consumer<ObservedClientSample> {

    private final Subject<ObservedClientSample> input = PublishSubject.create();

    @Inject
    Obfuscator obfuscator;

    @Inject
    BuildCallSamples buildCallSamples;

    @Inject
    ReportMediaTracks reportMediaTracks;

    @Inject
    AddNewEntities addNewEntities;

    @Inject
    CollectClientSamples collectClientSamples;

    @Inject
    OutboundReportSender outboundReportSender;

    @PostConstruct
    void setup() {
        var samplesBuffer = this.input
                .buffer(1000, TimeUnit.SECONDS, 30)
                .share();

        var observableCollectedCallSamples = samplesBuffer
                // TODO: measure a start time
                .map(this.obfuscator)
                .map(this.collectClientSamples)
                .lift(this.buildCallSamples)
                .filter(Objects::nonNull)
                .share();

        observableCollectedCallSamples
                .subscribe(this.reportMediaTracks);

        observableCollectedCallSamples
                .subscribe(this.addNewEntities);

        // TODO: measure the end time somehow
    }

    @Override
    public void accept(ObservedClientSample observedClientSample) throws Throwable {
        this.input.onNext(observedClientSample);
    }
}
