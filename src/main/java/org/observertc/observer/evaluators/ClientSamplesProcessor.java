package org.observertc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.observer.reports.Report;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.evaluators.listeners.CallEvents;
import org.observertc.observer.samples.ObservedClientSample;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class ClientSamplesProcessor {

    private final Subject<List<ObservedClientSample>> clientSamples = PublishSubject.create();
    private final Subject<List<Report>> reports = PublishSubject.create();

    public final Observer<List<ObservedClientSample>> getObservedClientSampleObserver() { return this.clientSamples; }
    public final Observable<List<Report>> getObservableReports() {
        return this.reports;
    }

    @Inject
    ObserverConfig observerConfig;

    @Inject
    CallEvents callEvents;

    @Inject
    ObservedClientSampleObfuscator obfuscator;

    @Inject
    CreateCallMetaData createCallMetaData;

    @Inject
    AddNewCallEntities addNewCallEntities;

    @Inject
    CollectCallSamples collectCallSamples;

    @Inject
    DemuxCollectedCallSamples demuxCollectedCallSamples;


    @PostConstruct
    void setup() {

        var observableCollectedCallSamples = this.clientSamples
                .map(this.obfuscator)
                .map(this.collectCallSamples)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .share();

        observableCollectedCallSamples
                .subscribe(this.createCallMetaData);

        observableCollectedCallSamples
                .subscribe(this.addNewCallEntities);

        observableCollectedCallSamples
                .subscribe(this.demuxCollectedCallSamples);

        this.createCallMetaData.getObservableCallMetaReports()
                .map(callMetaReports -> callMetaReports
                        .stream()
                        .map(Report::fromCallMetaReport)
                        .collect(Collectors.toList()))
                .subscribe(this::forward);

        this.callEvents.getObservableCalEventReports()
                .map(callEventReports ->
                        callEventReports
                                .stream()
                                .map(Report::fromCallEventReport)
                                .collect(Collectors.toList()))
                .subscribe(this::forward);

        this.demuxCollectedCallSamples.getObservableReport()
                .subscribe(this::forward);

    }

    private void forward(List<Report> reports) {
        synchronized (this) {
            this.reports.onNext(reports);
        }
    }

}
