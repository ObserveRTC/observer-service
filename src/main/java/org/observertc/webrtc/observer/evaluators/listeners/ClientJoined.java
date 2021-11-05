package org.observertc.webrtc.observer.evaluators.listeners;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.repositories.RepositoryEvents;
import org.observertc.webrtc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

@Prototype
class ClientJoined extends EventReporterAbstract.CallEventReporterAbstract<ClientDTO> {

    private static final Logger logger = LoggerFactory.getLogger(ClientJoined.class);

    @Inject
    ObserverConfig observerConfig;

    @Inject
    RepositoryEvents repositoryEvents;

    @PostConstruct
    void setup() {
        this.repositoryEvents
                .addedClients()
                .subscribe(this::receiveAddedCalls);
    }

    private void receiveAddedCalls(List<ClientDTO> clientDTOs) {
        if (Objects.isNull(clientDTOs) || clientDTOs.size() < 1) {
            return;
        }

        clientDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .forEach(this::forward);
    }

    private CallEventReport makeReport(ClientDTO clientDTO) {
        return this.makeReport(clientDTO, clientDTO.joined);
    }

    @Override
    protected CallEventReport makeReport(ClientDTO clientDTO, Long timestamp) {
        try {
            String callId = UUIDAdapter.toStringOrNull(clientDTO.callId);
            String clientId = UUIDAdapter.toStringOrNull(clientDTO.clientId);
            var result = CallEventReport.newBuilder()
                    .setName(CallEventType.CLIENT_JOINED.name())
                    .setCallId(callId)
                    .setServiceId(clientDTO.serviceId)
                    .setRoomId(clientDTO.roomId)
                    .setClientId(clientId)
                    .setMediaUnitId(clientDTO.mediaUnitId)
                    .setUserId(clientDTO.userId)
                    .setTimestamp(timestamp)
                    .build();
            logger.info("Client with id {} is joined to call \"{}\" for service \"{}\" at room \"{}\"", clientDTO.clientId, clientDTO.callId, clientDTO.serviceId, clientDTO.roomId);
            return result;
        } catch (Exception ex) {
           logger.warn("Cannot make report for client DTO", ex);
           return null;
        }
    }
}
