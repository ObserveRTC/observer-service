package org.observertc.observer.evaluators.eventreports;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Singleton;
import org.observertc.observer.evaluators.eventreports.attachments.ClientAttachment;
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
public class ClientLeftReports {

    private static final Logger logger = LoggerFactory.getLogger(ClientLeftReports.class);

    private Subject<List<CallEventReport>> output = PublishSubject.<List<CallEventReport>>create().toSerialized();

    @PostConstruct
    void setup() {

    }

    public void accept(List<Models.Client> clientDTOs) {
        if (Objects.isNull(clientDTOs) || clientDTOs.size() < 1) {
            return;
        }

        var reports = clientDTOs.stream()
                .map(this::makeReport)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (0 < reports.size()) {
            this.output.onNext(reports);
        }
    }

    private CallEventReport makeReport(Models.Client clientDTO) {
        try {
            var timestamp = clientDTO.hasTouched() ? clientDTO.getTouched() : Instant.now().toEpochMilli();
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

    public Observable<List<CallEventReport>> getOutput() {
        return this.output;
    }
}
