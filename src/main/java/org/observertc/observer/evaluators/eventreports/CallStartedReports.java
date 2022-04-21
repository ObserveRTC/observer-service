package org.observertc.observer.evaluators.eventreports;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.dto.CallDTO;
import org.observertc.observer.events.CallEventType;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Prototype
public class CallStartedReports {

    private static final Logger logger = LoggerFactory.getLogger(CallStartedReports.class);

    @PostConstruct
    void setup() {

    }

    public List<CallEventReport> mapAddedCalls(List<CallDTO> callDTOs) {
        if (Objects.isNull(callDTOs) || callDTOs.size() < 1) {
            return Collections.EMPTY_LIST;
        }

        var reports = callDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return reports;
    }

    private CallEventReport makeReport(CallDTO callDTO) {
        try {
            String callId = UUIDAdapter.toStringOrNull(callDTO.callId);
            String message = String.format("Call (%s) started ", callId);
            var result = CallEventReport.newBuilder()
                    .setName(CallEventType.CALL_STARTED.name())
                    .setCallId(callId)
                    .setServiceId(callDTO.serviceId)
                    .setRoomId(callDTO.roomId)
                    .setTimestamp(callDTO.started)
                    .setMarker(callDTO.marker)
                    .setMessage(message)
                    .build();
            logger.info("Call is registered with id \"{}\" for service \"{}\" at room \"{}\"", callDTO.callId, callDTO.serviceId, callDTO.roomId);
            return result;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }
}
