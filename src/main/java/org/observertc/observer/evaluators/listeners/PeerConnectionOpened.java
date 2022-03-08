package org.observertc.observer.evaluators.listeners;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.dto.PeerConnectionDTO;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.repositories.RepositoryEvents;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Prototype
class PeerConnectionOpened extends EventReporterAbstract.CallEventReporterAbstract<PeerConnectionDTO> {

    private static final Logger logger = LoggerFactory.getLogger(PeerConnectionOpened.class);

    @Inject
    RepositoryEvents repositoryEvents;

    @PostConstruct
    void setup() {
        this.repositoryEvents
                .addedPeerConnection()
                .subscribe(this::receiveAddedPeerConnections);

    }

    private void receiveAddedPeerConnections(List<PeerConnectionDTO> peerConnectionDTOs) {
        if (Objects.isNull(peerConnectionDTOs) || peerConnectionDTOs.size() < 1) {
            return;
        }

        peerConnectionDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .forEach(this::forward);
    }

    private CallEventReport makeReport(PeerConnectionDTO peerConnectionDTO) {
        Long now = Instant.now().toEpochMilli();
        return this.makeReport(peerConnectionDTO, peerConnectionDTO.created);
    }

    @Override
    protected CallEventReport makeReport(PeerConnectionDTO peerConnectionDTO, Long timestamp) {
        try {
            String callId = UUIDAdapter.toStringOrNull(peerConnectionDTO.callId);
            String clientId = UUIDAdapter.toStringOrNull(peerConnectionDTO.clientId);
            String peerConnectionId = UUIDAdapter.toStringOrNull(peerConnectionDTO.peerConnectionId);
            var report = CallEventReport.newBuilder()
                    .setName(CallEventType.PEER_CONNECTION_OPENED.name())
                    .setCallId(callId)
                    .setServiceId(peerConnectionDTO.serviceId)
                    .setRoomId(peerConnectionDTO.roomId)
                    .setClientId(clientId)
                    .setMediaUnitId(peerConnectionDTO.mediaUnitId)
                    .setUserId(peerConnectionDTO.userId)
                    .setPeerConnectionId(peerConnectionId)
                    .setTimestamp(timestamp)
                    .build();
            logger.info("Peer Connection {} is opened at call \"{}\" in service \"{}\" at room \"{}\"", peerConnectionDTO.peerConnectionId, peerConnectionDTO.callId, peerConnectionDTO.serviceId, peerConnectionDTO.roomId);
            return report;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }
}
