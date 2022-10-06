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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Singleton
public class ClientJoinedReports {

    private static final Logger logger = LoggerFactory.getLogger(ClientJoinedReports.class);

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
            ClientAttachment attachment = ClientAttachment.builder()
                    .withTimeZoneId(clientDTO.getTimeZoneId())
                    .build();
            String message = String.format("Client is joined");
            var result = CallEventReport.newBuilder()
                    .setName(CallEventType.CLIENT_JOINED.name())
                    .setCallId(clientDTO.getCallId())
                    .setServiceId(clientDTO.getServiceId())
                    .setRoomId(clientDTO.getRoomId())
                    .setClientId(clientDTO.getClientId())
                    .setMediaUnitId(clientDTO.getMediaUnitId())
                    .setUserId(clientDTO.getUserId())
                    .setTimestamp(clientDTO.getJoined())
                    .setAttachments(attachment.toBase64())
                    .setMarker(clientDTO.getMarker())
                    .setMessage(message)
                    .build();
            logger.info("Client with id {} is JOINED to call \"{}\" for service \"{}\" at room \"{}\"", clientDTO.getClientId(), clientDTO.getCallId(), clientDTO.getServiceId(), clientDTO.getRoomId());
            return result;
        } catch (Exception ex) {
           logger.warn("Cannot make report for client DTO", ex);
           return null;
        }
    }

    public Observable<List<CallEventReport>> getOutput() {
        return this.output;
    }
}
