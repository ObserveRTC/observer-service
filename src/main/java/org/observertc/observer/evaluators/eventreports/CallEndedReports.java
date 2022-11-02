package org.observertc.observer.evaluators.eventreports;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Singleton;
import org.observertc.observer.events.CallEventType;
import org.observertc.schemas.dtos.Models;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class CallEndedReports {

    private static final Logger logger = LoggerFactory.getLogger(CallEndedReports.class);

    private Subject<List<CallEventReport>> output = PublishSubject.<List<CallEventReport>>create().toSerialized();

    public void accept(Collection<Models.Call> models) {
        if (Objects.isNull(models) || models.size() < 1) {
            return;
        }

        var reports = models.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (0 < reports.size()) {
            this.output.onNext(reports);
        }
    }

    private CallEventReport makeReport(Models.Call callModel) {
        Long timestamp;
        if (callModel.hasTouched()) {
            timestamp = callModel.getTouched();
        } else {
            timestamp = Instant.now().toEpochMilli();
        }
        try {
            String message = String.format("Call (%s) is ended", callModel.getCallId());
            var result = CallEventReport.newBuilder()
                    .setName(CallEventType.CALL_ENDED.name())
                    .setCallId(callModel.getCallId())
                    .setServiceId(callModel.getServiceId())
                    .setRoomId(callModel.getRoomId())
                    .setTimestamp(timestamp)
                    .setMarker(callModel.getMarker())
                    .setMessage(message)
                    .build();
            logger.info("Call \"{}\" for service \"{}\" at room \"{}\" is ENDED", callModel.getCallId(), callModel.getServiceId(), callModel.getRoomId());
            return result;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }

    public Observable<List<CallEventReport>> getOutput() {
        return this.output;
    }
}
