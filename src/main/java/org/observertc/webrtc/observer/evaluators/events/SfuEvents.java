package org.observertc.webrtc.observer.evaluators.events;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.repositories.RepositoryEvents;
import org.observertc.webrtc.schemas.reports.SfuEventReport;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;

@Singleton
public class SfuEvents {

    private Subject<SfuEventReport> reports = PublishSubject.create();

    @Inject
    RepositoryEvents repositoryEvents;

    @Inject
    SfuRtpPadAdded sfuRtpPadAdded;

    @PostConstruct
    void setup() {
        this.repositoryEvents
                .addedSfuRtpPads()
                .subscribe(this.sfuRtpPadAdded::receiveAddedSfuRtpPads);

        this.repositoryEvents
                .updatedSfuRtpPads()
                .subscribe(this.sfuRtpPadAdded::receiveUpdatedSfuRtpPads);
    }

    @PreDestroy
    void teardown() {

    }

    public Observable<SfuEventReport> getReports() {
        return this.reports;
    }

    void forward(List<SfuEventReport> sfuEventReports) throws Throwable {
        if (Objects.isNull(sfuEventReports) || sfuEventReports.size() < 1) {
            return;
        }
        synchronized (this) {
            sfuEventReports.forEach(sfuEventReport -> {
                this.reports.onNext(sfuEventReport);
            });
        }
    }
}
