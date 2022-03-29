package org.observertc.observer.components.eventreports;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.components.eventreports.attachments.ClientAttachment;
import org.observertc.observer.dto.ClientDTO;
import org.observertc.observer.events.CallEventType;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Prototype
public class ClientJoinedReports {

    private static final Logger logger = LoggerFactory.getLogger(ClientJoinedReports.class);

    @PostConstruct
    void setup() {

    }

    public List<CallEventReport> mapAddedClient(List<ClientDTO> clientDTOs) {
        if (Objects.isNull(clientDTOs) || clientDTOs.size() < 1) {
            return Collections.EMPTY_LIST;
        }

        var reports = clientDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return reports;
    }

    private CallEventReport makeReport(ClientDTO clientDTO) {
        try {
            String callId = UUIDAdapter.toStringOrNull(clientDTO.callId);
            String clientId = UUIDAdapter.toStringOrNull(clientDTO.clientId);
            ClientAttachment attachment = ClientAttachment.builder()
                    .withTimeZoneId(clientDTO.timeZoneId)
                    .build();
            String message = String.format("Client is joined");
            var result = CallEventReport.newBuilder()
                    .setName(CallEventType.CLIENT_JOINED.name())
                    .setCallId(callId)
                    .setServiceId(clientDTO.serviceId)
                    .setRoomId(clientDTO.roomId)
                    .setClientId(clientId)
                    .setMediaUnitId(clientDTO.mediaUnitId)
                    .setUserId(clientDTO.userId)
                    .setTimestamp(clientDTO.joined)
                    .setAttachments(attachment.toBase64())
                    .setMarker(clientDTO.marker)
                    .setMessage(message)
                    .build();
            logger.info("Client with id {} is joined to call \"{}\" for service \"{}\" at room \"{}\"", clientDTO.clientId, clientDTO.callId, clientDTO.serviceId, clientDTO.roomId);
            return result;
        } catch (Exception ex) {
           logger.warn("Cannot make report for client DTO", ex);
           return null;
        }
    }
}
