package org.observertc.observer.evaluators.listeners;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.dto.ClientDTO;
import org.observertc.observer.evaluators.listeners.attachments.ClientAttachment;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.repositories.RepositoryEvents;
import org.observertc.schemas.reports.CallEventReport;
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
            ClientAttachment attachment = ClientAttachment.builder()
                    .withTimeZoneId(clientDTO.timeZoneId)
                    .build();
            var result = CallEventReport.newBuilder()
                    .setName(CallEventType.CLIENT_JOINED.name())
                    .setCallId(callId)
                    .setServiceId(clientDTO.serviceId)
                    .setRoomId(clientDTO.roomId)
                    .setClientId(clientId)
                    .setMediaUnitId(clientDTO.mediaUnitId)
                    .setUserId(clientDTO.userId)
                    .setTimestamp(timestamp)
                    .setAttachments(attachment.toBase64())
                    .build();
            logger.info("Client with id {} is joined to call \"{}\" for service \"{}\" at room \"{}\"", clientDTO.clientId, clientDTO.callId, clientDTO.serviceId, clientDTO.roomId);
            return result;
        } catch (Exception ex) {
           logger.warn("Cannot make report for client DTO", ex);
           return null;
        }
    }
}
