package org.observertc.observer.evaluators.listeners;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.dto.CallDTO;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.repositories.RepositoryEvents;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Prototype
class CallEnded extends EventReporterAbstract.CallEventReporterAbstract {

    private static final Logger logger = LoggerFactory.getLogger(CallEnded.class);

    @Inject
    ObserverConfig observerConfig;

    @Inject
    RepositoryEvents repositoryEvents;

    @PostConstruct
    void setup() {
        this.repositoryEvents
                .removedCalls()
                .subscribe(this::receiveRemovedCalls);
    }

    private void receiveRemovedCalls(List<CallDTO> callDTOs) {
        if (Objects.isNull(callDTOs) || callDTOs.size() < 1) {
            return;
        }

        var reports = callDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        this.forward(reports);
    }

    private CallEventReport makeReport(CallDTO callDTO) {
        return this.makeReport(callDTO, callDTO.started);
    }

    protected CallEventReport makeReport(CallDTO callDTO, Long timestamp) {
        try {
            String callId = UUIDAdapter.toStringOrNull(callDTO.callId);
            var result = CallEventReport.newBuilder()
                    .setName(CallEventType.CALL_ENDED.name())
                    .setCallId(callId)
                    .setServiceId(callDTO.serviceId)
                    .setRoomId(callDTO.roomId)
                    .setTimestamp(timestamp)
                    .build();
            logger.info("Call \"{}\" for service \"{}\" at room \"{}\" is ended", callDTO.callId, callDTO.serviceId, callDTO.roomId);
            return result;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }
}
