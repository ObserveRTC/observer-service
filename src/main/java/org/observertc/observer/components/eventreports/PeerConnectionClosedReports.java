package org.observertc.observer.components.eventreports;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.dto.PeerConnectionDTO;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.observer.repositories.tasks.RemovePeerConnectionsTask;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Prototype
public class PeerConnectionClosedReports {

    private static final Logger logger = LoggerFactory.getLogger(PeerConnectionClosedReports.class);

    @Inject
    Provider<RemovePeerConnectionsTask> removePeerConnectionsTaskProvider;

    @PostConstruct
    void setup() {

    }

    public List<CallEventReport> mapRemovedPeerConnections(List<PeerConnectionDTO> peerConnectionDTOs) {
        if (Objects.isNull(peerConnectionDTOs) || peerConnectionDTOs.size() < 1) {
            return Collections.EMPTY_LIST;
        }

        var reports = peerConnectionDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return reports;
    }

    public List<CallEventReport> mapExpiredPeerConnections(List<RepositoryExpiredEvent<PeerConnectionDTO>> expiredPeerConnectionDTOs) {
        if (Objects.isNull(expiredPeerConnectionDTOs) || expiredPeerConnectionDTOs.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        var removePeerConnectionsTask = removePeerConnectionsTaskProvider.get();
        expiredPeerConnectionDTOs.stream()
                .map(expiredDTO -> expiredDTO.getValue())
                .forEach(removePeerConnectionsTask::addRemovedPeerConnectionDTO);

        if (!removePeerConnectionsTask.execute().succeeded()) {
            logger.warn("Remove Peer Connection are failed");
            return Collections.EMPTY_LIST;
        }

        var reports = expiredPeerConnectionDTOs.stream()
                .filter(Objects::nonNull)
                .map(expiredPeerConnectionDTO -> {
                    var timestamp = expiredPeerConnectionDTO.estimatedLastTouch();
                    var peerConnectionDTO = expiredPeerConnectionDTO.getValue();
                    if (Objects.isNull(peerConnectionDTO) || Objects.isNull(peerConnectionDTO.peerConnectionId)) {
                        return null;
                    }
                    var report = this.makeReport(peerConnectionDTO, timestamp);
                    return report;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return reports;
    }

    private CallEventReport makeReport(PeerConnectionDTO peerConnectionDTO) {
        Long now = Instant.now().toEpochMilli();
        return this.makeReport(peerConnectionDTO, now);
    }

    private CallEventReport makeReport(PeerConnectionDTO peerConnectionDTO, Long timestamp) {
        try {
            String callId = UUIDAdapter.toStringOrNull(peerConnectionDTO.callId);
            String clientId = UUIDAdapter.toStringOrNull(peerConnectionDTO.clientId);
            String peerConnectionId = UUIDAdapter.toStringOrNull(peerConnectionDTO.peerConnectionId);
            var report = CallEventReport.newBuilder()
                    .setName(CallEventType.PEER_CONNECTION_CLOSED.name())
                    .setCallId(callId)
                    .setServiceId(peerConnectionDTO.serviceId)
                    .setRoomId(peerConnectionDTO.roomId)
                    .setClientId(clientId)
                    .setMediaUnitId(peerConnectionDTO.mediaUnitId)
                    .setUserId(peerConnectionDTO.userId)
                    .setPeerConnectionId(peerConnectionId)
                    .setTimestamp(timestamp)
                    .build();
            logger.info("Peer Connection {} is closed at call \"{}\" in service \"{}\" at room \"{}\"", peerConnectionDTO.peerConnectionId, peerConnectionDTO.callId, peerConnectionDTO.serviceId, peerConnectionDTO.roomId);
            return report;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }
}
