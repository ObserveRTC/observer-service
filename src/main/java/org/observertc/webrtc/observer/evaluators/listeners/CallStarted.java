package org.observertc.webrtc.observer.evaluators.listeners;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.repositories.RepositoryEvents;
import org.observertc.webrtc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

@Prototype
class CallStarted extends EventReporterAbstract.CallEventReporterAbstract<CallDTO> {

    private static final Logger logger = LoggerFactory.getLogger(CallStarted.class);

    @Inject
    ObserverConfig observerConfig;

    @Inject
    RepositoryEvents repositoryEvents;

    @PostConstruct
    void setup() {
        this.repositoryEvents
                .addedCalls()
                .subscribe(this::receiveAddedCalls);
    }

    private void receiveAddedCalls(List<CallDTO> callDTOs) {
        if (Objects.isNull(callDTOs) || callDTOs.size() < 1) {
            return;
        }

        callDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .forEach(this::forward);
    }

    private CallEventReport makeReport(CallDTO callDTO) {
        return this.makeReport(callDTO, callDTO.started);
    }

    @Override
    protected CallEventReport makeReport(CallDTO callDTO, Long timestamp) {
        try {
            String callId = UUIDAdapter.toStringOrNull(callDTO.callId);
            var result = CallEventReport.newBuilder()
                    .setName(CallEventType.CALL_STARTED.name())
                    .setCallId(callId)
                    .setServiceId(callDTO.serviceId)
                    .setRoomId(callDTO.roomId)
                    .setTimestamp(timestamp)
                    .build();
            logger.info("Call is registered with id \"{}\" for service \"{}\" at room \"{}\"", callDTO.callId, callDTO.serviceId, callDTO.roomId);
            return result;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }
}
