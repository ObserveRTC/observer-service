package org.observertc.observer.evaluators.eventreports;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.repositories.Call;
import org.observertc.observer.repositories.CallsRepository;
import org.observertc.schemas.reports.ObserverEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Singleton
public class CallSummaryReports {
    private static final Logger logger = LoggerFactory.getLogger(CallSummaryReports.class);

    private Subject<List<ObserverEventReport>> output = PublishSubject.<List<ObserverEventReport>>create().toSerialized();

    @Inject
    private CallsRepository callsRepository;

    @PostConstruct
    void setup() {

    }

    public void accept(Set<String> callIds) {
        if (Objects.isNull(callIds) || callIds.size() < 1) {
            return;
        }

//        var calls = this.callsRepository.fetchRecursively(callIds);
//        for (var call : calls.values()) {
//
//        }
    }


    private ObserverEventReport makeReport(Call call) {
        return null;
    }


    public Observable<List<ObserverEventReport>> getOutput() {
        return this.output;
    }
}
