package org.observertc.observer.evaluators.eventreports;

import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.observer.events.CallEventType;
import org.observertc.schemas.dtos.Models;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@Prototype
public class CallEndedReports {

    private static final Logger logger = LoggerFactory.getLogger(CallEndedReports.class);

    private Subject<CallEventReport> subject = PublishSubject.<CallEventReport>create().toSerialized();

    public void accept(Models.Call callModel) {
        Long timestamp = Instant.now().toEpochMilli();
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
            this.subject.onNext(result);
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
        }
    }

    public Observable<CallEventReport> getOutput() {
        return this.subject;
    }
}
