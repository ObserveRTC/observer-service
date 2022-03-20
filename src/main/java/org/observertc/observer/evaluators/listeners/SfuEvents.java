package org.observertc.observer.evaluators.listeners;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.observer.repositories.RepositoryEvents;
import org.observertc.schemas.reports.SfuEventReport;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;

@Singleton
public class SfuEvents {

    private Subject<List<SfuEventReport>> reports = PublishSubject.create();

    @Inject
    RepositoryEvents repositoryEvents;

    @Inject
    SfuJoined sfuJoined;

    @Inject
    SfuLeft sfuLeft;

    @Inject
    SfuTransportOpened sfuTransportOpened;

    @Inject
    SfuTransportClosed sfuTransportClosed;

    @Inject
    SfuRtpPadAdded sfuRtpPadAdded;

    @Inject
    SfuRtpPadRemoved sfuRtpPadRemoved;

    @PostConstruct
    void setup() {

        this.sfuJoined.getObservableReports()
                .subscribe(this::forward);

        this.sfuLeft.getObservableReports()
                .subscribe(this::forward);

        this.sfuTransportOpened.getObservableReports()
                .subscribe(this::forward);

        this.sfuTransportClosed.getObservableReports()
                .subscribe(this::forward);

        this.sfuRtpPadAdded.getObservableReports()
                .subscribe(this::forward);

        this.sfuRtpPadRemoved.getObservableReports()
                .subscribe(this::forward);
    }

    @PreDestroy
    void teardown() {

    }

    public Observable<List<SfuEventReport>> getObservableSfuEventReports() {
        return this.reports;
    }

    void forward(List<SfuEventReport> sfuEventReports) throws Throwable {
        if (Objects.isNull(sfuEventReports)) {
            return;
        }
        if (sfuEventReports.size() < 1) {
            return;
        }
        synchronized (this) {
           this.reports.onNext(sfuEventReports);
        }
    }
}
