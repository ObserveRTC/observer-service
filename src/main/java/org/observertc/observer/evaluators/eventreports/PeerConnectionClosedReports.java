package org.observertc.observer.evaluators.eventreports;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.repositories.Client;
import org.observertc.observer.repositories.ClientsRepository;
import org.observertc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.schemas.dtos.Models;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Prototype
public class PeerConnectionClosedReports {

    private static final Logger logger = LoggerFactory.getLogger(PeerConnectionClosedReports.class);

    @Inject
    ClientsRepository clientsRepository;

    @PostConstruct
    void setup() {

    }

    public List<CallEventReport> mapRemovedPeerConnections(List<Models.PeerConnection> peerConnectionDTOs) {
        if (Objects.isNull(peerConnectionDTOs) || peerConnectionDTOs.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        var clientIds = peerConnectionDTOs.stream().map(Models.PeerConnection::getClientId)
                .collect(Collectors.toSet());
        var clients = this.clientsRepository.getAll(clientIds);
        var reports = new LinkedList<CallEventReport>();
        var now = Instant.now().toEpochMilli();
        for (var peerConnectionModel : peerConnectionDTOs) {
            var client = clients.get(peerConnectionModel.getClientId());
            var report = this.perform(client, peerConnectionModel, now);
            if (report != null) {
                reports.add(report);
            }
        }
        this.clientsRepository.save();
        return reports;
    }

    public List<CallEventReport> mapExpiredPeerConnections(List<RepositoryExpiredEvent<Models.PeerConnection>> expiredPeerConnectionDTOs) {
        if (Objects.isNull(expiredPeerConnectionDTOs) || expiredPeerConnectionDTOs.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        var clientIds = expiredPeerConnectionDTOs.stream().map(event -> event.getValue().getClientId())
                .collect(Collectors.toSet());
        var clients = this.clientsRepository.getAll(clientIds);
        var reports = new LinkedList<CallEventReport>();
        for (var event : expiredPeerConnectionDTOs) {
            var peerConnectionModel = event.getValue();
            var client = clients.get(peerConnectionModel.getClientId());
            var report = this.perform(client, peerConnectionModel, event.estimatedLastTouch());
            if (report != null) {
                reports.add(report);
            }
        }
        this.clientsRepository.save();
        return reports;
    }

    private CallEventReport perform(Client client, Models.PeerConnection peerConnectionModel, Long timestamp) {
        if (client == null) {
            logger.warn("Did not found client for peerConnection {}", peerConnectionModel.getPeerConnectionId());
            return null;
        }
        var removed = client.removePeerConnection(peerConnectionModel.getPeerConnectionId());
        if (!removed) {
            logger.warn("Did not removed peerConnection {} from client {}. Already removed?", peerConnectionModel.getPeerConnectionId(), peerConnectionModel.getClientId());
            return null;
        }
        return this.makeReport(peerConnectionModel, timestamp);
    }

    private CallEventReport makeReport(Models.PeerConnection peerConnectionDTO, Long timestamp) {
        try {
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
}
