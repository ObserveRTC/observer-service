package org.observertc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.evaluators.listeners.SfuEvents;
import org.observertc.observer.reports.Report;
import org.observertc.observer.samples.ObservedSfuSample;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class SfuSamplesProcessor {

    private final Subject<List<ObservedSfuSample>> sfuSamples = PublishSubject.create();
    private final Subject<List<Report>> reports = PublishSubject.create();

    public final Observer<List<ObservedSfuSample>> getObservedSfuSamplesObserver() { return this.sfuSamples; }
    public final Observable<List<Report>> getObservableReports() {
        return this.reports;
    }

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
    DemuxCollectedSfuSamples demuxCollectedSfuSamples;

    @PostConstruct
    void setup() {
        var observableCollectedSfuSamples = this.sfuSamples
                .map(this.obfuscator)
                .map(this.collectSfuSamples)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .share();

        observableCollectedSfuSamples
                .subscribe(this.addNewSfuEntities);

        observableCollectedSfuSamples
                .subscribe(this.demuxCollectedSfuSamples);

        this.sfuEvents.getObservableSfuEventReports()
                .map(callEventReports ->
                        callEventReports
                                .stream()
                                .map(Report::fromSfuEventReport)
                                .collect(Collectors.toList()))
                .subscribe(this::forward);

        this.demuxCollectedSfuSamples.getObservableReport()
                .subscribe(this::forward);

    }

    private void forward(List<Report> reports) {
        synchronized (this) {
            this.reports.onNext(reports);
        }
    }

}
