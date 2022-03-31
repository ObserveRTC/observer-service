package org.observertc.observer.components.eventreports;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.dto.CallDTO;
import org.observertc.observer.events.CallEventType;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Prototype
public class CallEndedReports {

    private static final Logger logger = LoggerFactory.getLogger(CallEndedReports.class);

    public List<CallEventReport> mapCallDTOs(List<CallDTO> callDTOs) throws Throwable {
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
        Long timestamp = Instant.now().toEpochMilli();
        try {
            String callId = UUIDAdapter.toStringOrNull(callDTO.callId);
            String message = String.format("Call (%s) is ended", callDTO.callId);
            var result = CallEventReport.newBuilder()
                    .setName(CallEventType.CALL_ENDED.name())
                    .setCallId(callId)
                    .setServiceId(callDTO.serviceId)
                    .setRoomId(callDTO.roomId)
                    .setTimestamp(timestamp)
                    .setMarker(callDTO.marker)
                    .setMessage(message)
                    .build();
            logger.info("Call \"{}\" for service \"{}\" at room \"{}\" is ENDED", callDTO.callId, callDTO.serviceId, callDTO.roomId);
            return result;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }
}
