package org.observertc.observer.evaluators.listeners;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.schemas.reports.CallEventReport;
import org.observertc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

abstract class EventReporterAbstract<TReport> {

    public static abstract class SfuEventReporterAbstract extends EventReporterAbstract<SfuEventReport> {

    }

    public static abstract class CallEventReporterAbstract extends EventReporterAbstract<CallEventReport> {

    }

    private static final Logger logger = LoggerFactory.getLogger(EventReporterAbstract.class);

    private Subject<List<TReport>> reports = PublishSubject.create();

    public Observable<List<TReport>> getObservableReports() { return this.reports; }

    protected void forward(List<TReport> reports) {
        if (Objects.isNull(reports)) {
            return;
        }
        if (reports.size() < 1) {
            return;
        }
        try {
            synchronized (this) {
                this.reports.onNext(reports);
            }
        } catch (Exception ex) {
            logger.warn("Cannot send report forward {}", reports, ex);
        }
    }
}
