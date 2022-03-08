package org.observertc.observer.evaluators.listeners;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.schemas.reports.CallEventReport;
import org.observertc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

abstract class EventReporterAbstract<DTO, TReport> {

    public static abstract class SfuEventReporterAbstract<T> extends EventReporterAbstract<T, SfuEventReport> {

    }

    public static abstract class CallEventReporterAbstract<T> extends EventReporterAbstract<T, CallEventReport> {

    }

    private static final Logger logger = LoggerFactory.getLogger(EventReporterAbstract.class);

    private Subject<TReport> reports = PublishSubject.create();

    public Observable<TReport> getObservableReports() { return this.reports; }

    protected<DTO> void bindListener(Observable<List<DTO>> observable, Consumer<List<DTO>> subscriber) {
        observable.subscribe(subscriber);
    }

    protected abstract TReport makeReport(DTO input, Long timestamp);


    protected void forward(TReport report) {
        if (Objects.isNull(report)) {
            return;
        }
        try {
            synchronized (this) {
                this.reports.onNext(report);
            }
        } catch (Exception ex) {
            logger.warn("Cannot send report forward {}", report, ex);
        }
    }
}
