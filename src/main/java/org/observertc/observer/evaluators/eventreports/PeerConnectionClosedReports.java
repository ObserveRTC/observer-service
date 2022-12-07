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

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class PeerConnectionClosedReports {

    private static final Logger logger = LoggerFactory.getLogger(PeerConnectionClosedReports.class);

    private Subject<List<CallEventReport>> output = PublishSubject.<List<CallEventReport>>create().toSerialized();

    @PostConstruct
    void setup() {

    }

    public void accept(List<Models.PeerConnection> peerConnectionDTOs) {
        if (Objects.isNull(peerConnectionDTOs) || peerConnectionDTOs.size() < 1) {
            return;
        }
        var reports = peerConnectionDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (0 < reports.size()) {
            this.output.onNext(reports);
        }
    }


    private CallEventReport makeReport(Models.PeerConnection peerConnectionDTO) {
        try {
            var timestamp = peerConnectionDTO.hasSampleTouched() ? peerConnectionDTO.getSampleTouched() : Instant.now().toEpochMilli();
            String message = String.format("Peer Connection (%s) is closed", peerConnectionDTO.getPeerConnectionId());
            var report = CallEventReport.newBuilder()
                    .setName(CallEventType.PEER_CONNECTION_CLOSED.name())
                    .setCallId(peerConnectionDTO.getCallId())
                    .setServiceId(peerConnectionDTO.getServiceId())
                    .setRoomId(peerConnectionDTO.getRoomId())
                    .setClientId(peerConnectionDTO.getClientId())
                    .setMediaUnitId(peerConnectionDTO.getMediaUnitId())
                    .setUserId(peerConnectionDTO.getUserId())
                    .setPeerConnectionId(peerConnectionDTO.getPeerConnectionId())
                    .setTimestamp(timestamp)
                    .setMarker(peerConnectionDTO.getMarker())
                    .setMessage(message)
                    .build();
            logger.info("Peer Connection {} is CLOSED at call \"{}\" in service \"{}\" at room \"{}\"", peerConnectionDTO.getPeerConnectionId(), peerConnectionDTO.getCallId(), peerConnectionDTO.getServiceId(), peerConnectionDTO.getRoomId());
            return report;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }

    public Observable<List<CallEventReport>> getOutput() {
        return this.output;
    }

}
