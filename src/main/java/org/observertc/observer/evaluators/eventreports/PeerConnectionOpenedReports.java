package org.observertc.observer.evaluators.eventreports;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.events.CallEventType;
import org.observertc.schemas.dtos.Models;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Prototype
public class PeerConnectionOpenedReports {

    private static final Logger logger = LoggerFactory.getLogger(PeerConnectionOpenedReports.class);

    @PostConstruct
    void setup() {

    }

    public List<CallEventReport> mapAddedPeerConnections(List<Models.PeerConnection> peerConnectionDTOs) {
        if (Objects.isNull(peerConnectionDTOs) || peerConnectionDTOs.size() < 1) {
            return Collections.EMPTY_LIST;
        }

        var reports = peerConnectionDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return reports;
    }

    private CallEventReport makeReport(Models.PeerConnection peerConnectionDTO) {
        try {
            String message = String.format("PeerConnection (%s) is opened", peerConnectionDTO.getPeerConnectionId());
            var report = CallEventReport.newBuilder()
                    .setName(CallEventType.PEER_CONNECTION_OPENED.name())
                    .setCallId(peerConnectionDTO.getCallId())
                    .setServiceId(peerConnectionDTO.getServiceId())
                    .setRoomId(peerConnectionDTO.getRoomId())
                    .setClientId(peerConnectionDTO.getClientId())
                    .setMediaUnitId(peerConnectionDTO.getMediaUnitId())
                    .setUserId(peerConnectionDTO.getUserId())
                    .setPeerConnectionId(peerConnectionDTO.getPeerConnectionId())
                    .setTimestamp(peerConnectionDTO.getOpened())
                    .setMarker(peerConnectionDTO.getMarker())
                    .setMessage(message)
                    .build();
            logger.info("Peer Connection {} is OPENED at call \"{}\" in service \"{}\" at room \"{}\"", peerConnectionDTO.getPeerConnectionId(), peerConnectionDTO.getCallId(), peerConnectionDTO.getServiceId(), peerConnectionDTO.getRoomId());
            return report;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }
}
