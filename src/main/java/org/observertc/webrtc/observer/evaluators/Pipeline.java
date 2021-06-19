package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.samples.ObservedSampleBuilder;
import org.observertc.webrtc.observer.sinks.OutboundReportSender;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Singleton
public class Pipeline  {

    private final Subject<ObservedSampleBuilder> input = PublishSubject.create();

    @Inject
    CallAssigner callAssigner;

    @Inject
    InboundAudioTrackReportsMaker inboundAudioTrackReportsMaker;

    @Inject
    OutboundReportSender outboundReportSender;

    @PostConstruct
    void setup() {
        var samplesBuffer = this.input
                .buffer(1000, TimeUnit.SECONDS, 30)
                .share();

        samplesBuffer
                .map(this.callAssigner)
                .filter(Objects::nonNull)
                .subscribe();
    }

    public Observer<ObservedSampleBuilder> getInput() {
        return this.input;
    }
}
