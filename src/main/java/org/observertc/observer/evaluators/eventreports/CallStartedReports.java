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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class CallStartedReports {

    private static final Logger logger = LoggerFactory.getLogger(CallStartedReports.class);

    private Subject<List<CallEventReport>> subject = PublishSubject.<List<CallEventReport>>create().toSerialized();

    public void accept(Collection<Models.Call> callDTOs) {
        if (Objects.isNull(callDTOs) || callDTOs.size() < 1) {
            return;
        }

        var reports = callDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        this.subject.onNext(reports);
    }

    private CallEventReport makeReport(Models.Call callDTO) {
        try {
            String message = String.format("Call (%s) is started", callDTO.getCallId());
            var result = CallEventReport.newBuilder()
                    .setName(CallEventType.CALL_STARTED.name())
                    .setCallId(callDTO.getCallId())
                    .setServiceId(callDTO.getServiceId())
                    .setRoomId(callDTO.getRoomId())
                    .setTimestamp(callDTO.getStarted())
                    .setMarker(callDTO.getMarker())
                    .setMessage(message)
                    .build();
            logger.info("Call is registered with id \"{}\" for service \"{}\" at room \"{}\"", callDTO.getCallId(), callDTO.getServiceId(), callDTO.getRoomId());
            return result;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }

    public Observable<List<CallEventReport>> getOutput() {
        return this.subject;
    }
}
