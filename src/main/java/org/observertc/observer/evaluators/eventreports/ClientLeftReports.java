package org.observertc.observer.evaluators.eventreports;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.observertc.observer.evaluators.eventreports.attachments.ClientAttachment;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.repositories.Call;
import org.observertc.observer.repositories.CallsRepository;
import org.observertc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.observer.samples.ServiceRoomId;
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
public class ClientLeftReports {

    private static final Logger logger = LoggerFactory.getLogger(ClientLeftReports.class);

    @Inject
    CallsRepository callsRepository;

    @Inject
    CallEndedReports callEndedReports;

    @PostConstruct
    void setup() {

    }

    public List<CallEventReport> mapRemovedClients(List<Models.Client> clientDTOs) {
        if (Objects.isNull(clientDTOs) || clientDTOs.size() < 1) {
            return Collections.EMPTY_LIST;
        }

        var serviceRoomIds = clientDTOs.stream()
                .map(model -> ServiceRoomId.make(model.getServiceId(), model.getRoomId()))
                .collect(Collectors.toSet());
        var calls = this.callsRepository.getAllMappedByCallIds(serviceRoomIds);
        var reports = new LinkedList<CallEventReport>();
        var now = Instant.now().toEpochMilli();
        for (var clientModel : clientDTOs) {
            var call = calls.get(clientModel.getClientId());
            var report = this.perform(call, clientModel, now);
            if (report != null) {
                reports.add(report);
            }
        }
        this.callsRepository.save();
        return reports;
    }


    public List<CallEventReport> mapExpiredClients(List<RepositoryExpiredEvent<Models.Client>> expiredClientDTOs) {
        if (Objects.isNull(expiredClientDTOs) || expiredClientDTOs.size() < 1) {
            return Collections.EMPTY_LIST;
        }
        var serviceRoomIds = expiredClientDTOs.stream()
                .map(event -> ServiceRoomId.make(event.getValue().getServiceId(), event.getValue().getRoomId()))
                .collect(Collectors.toSet());
        var calls = this.callsRepository.getAllMappedByCallIds(serviceRoomIds);
        var reports = new LinkedList<CallEventReport>();
        for (var event : expiredClientDTOs) {
            var clientModel = event.getValue();
            var call = calls.get(clientModel.getClientId());
            var report = this.perform(call, clientModel, event.estimatedLastTouch());
            if (report != null) {
                reports.add(report);
            }
        }
        this.callsRepository.save();
        return reports;
    }

    private CallEventReport perform(Call call, Models.Client clientModel, Long timestamp) {
        if (call == null) {
            logger.warn("Did not found call for client {}", clientModel.getClientId());
            return null;
        }
        var removed = call.removeClient(clientModel.getClientId());
        if (call.getClientIds().size() < 1) {
            var model = call.getModel();
            if (this.callsRepository.remove(call.getServiceRoomId())) {
                // end the call here
                this.callEndedReports.accept(model);
            }
        }
        if (!removed) {
            logger.warn("Did not removed client {} from call {}. Already removed?", clientModel.getClientId(), call.getCallId());
            return null;
        }

        return this.makeReport(clientModel, timestamp);
    }

    private CallEventReport makeReport(Models.Client clientDTO, Long timestamp) {
        try {
            ClientAttachment attachment = ClientAttachment.builder()
                    .withTimeZoneId(clientDTO.getTimeZoneId())
                    .build();
            String message = String.format("Client left");
            var result = CallEventReport.newBuilder()
                    .setName(CallEventType.CLIENT_LEFT.name())
                    .setCallId(clientDTO.getCallId())
                    .setServiceId(clientDTO.getServiceId())
                    .setRoomId(clientDTO.getRoomId())
                    .setClientId(clientDTO.getClientId())
                    .setMediaUnitId(clientDTO.getMediaUnitId())
                    .setUserId(clientDTO.getUserId())
                    .setTimestamp(timestamp)
                    .setAttachments(attachment.toBase64())
                    .setMarker(clientDTO.getMarker())
                    .setMessage(message)
                    .build();
            logger.info("Client {} LEFT call \"{}\" in service \"{}\" at room \"{}\"", clientDTO.getClientId(), clientDTO.getCallId(), clientDTO.getServiceId(), clientDTO.getRoomId());
            return result;
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while making report", ex);
            return null;
        }
    }
}
