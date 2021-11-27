package org.observertc.webrtc.observer.evaluators.listeners;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.evaluators.listeners.attachments.ClientAttachment;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.utils.DTOGenerators;
import org.observertc.webrtc.schemas.reports.CallEventReport;

import javax.inject.Inject;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
class ClientLeftTest {

    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ClientLeft clientLeft;


    @Test
    void reportFieldsShouldHaveTheRightValues() throws InterruptedException, ExecutionException, TimeoutException {
        var callEventReportPromise = new CompletableFuture<CallEventReport>();
        var clientDTO = dtoGenerators.getClientDTO();

        this.clientLeft.getObservableReports().subscribe(callEventReportPromise::complete);
        this.hazelcastMaps.getClients().put(clientDTO.clientId, clientDTO);
        this.hazelcastMaps.getClients().remove(clientDTO.clientId);
        var callEventReport = callEventReportPromise.get(5, TimeUnit.SECONDS);
        var timestampThreshold = Instant.now().plusSeconds(5).toEpochMilli();

        Assertions.assertEquals(CallEventType.CLIENT_LEFT.name(), callEventReport.getName());
        Assertions.assertEquals(clientDTO.serviceId, callEventReport.getServiceId());
        Assertions.assertEquals(clientDTO.roomId, callEventReport.getRoomId());

        Assertions.assertEquals(clientDTO.mediaUnitId, callEventReport.getMediaUnitId());
        Assertions.assertEquals(clientDTO.callId.toString(), callEventReport.getCallId());
        Assertions.assertEquals(clientDTO.userId, callEventReport.getUserId());
        Assertions.assertEquals(clientDTO.clientId.toString(), callEventReport.getClientId());
        Assertions.assertTrue(callEventReport.getTimestamp() < timestampThreshold);
    }

    @Test
    void reportAttachmentFieldsShouldHaveTheRightValues() throws InterruptedException, ExecutionException, TimeoutException {
        var callEventReportPromise = new CompletableFuture<CallEventReport>();
        var clientDTO = dtoGenerators.getClientDTO();

        this.clientLeft.getObservableReports().subscribe(callEventReportPromise::complete);
        this.hazelcastMaps.getClients().put(clientDTO.clientId, clientDTO);
        this.hazelcastMaps.getClients().remove(clientDTO.clientId);

        String attachmentInBase64 = callEventReportPromise.get(5, TimeUnit.SECONDS).getAttachments();
        ClientAttachment attachment = ClientAttachment.builder().fromBase64(attachmentInBase64).build();
        Assertions.assertEquals(clientDTO.timeZoneId, attachment.timeZoneId);
    }

    @Test
    void ifFieldsForAttachmentIsNullNoProblemOccurs() throws InterruptedException, ExecutionException, TimeoutException {
        var callEventReportPromise = new CompletableFuture<CallEventReport>();
        var clientDTO = ClientDTO.builder().from(dtoGenerators.getClientDTO())
                .withTimeZoneId(null)
                .build();

        this.clientLeft.getObservableReports().subscribe(callEventReportPromise::complete);
        this.hazelcastMaps.getClients().put(clientDTO.clientId, clientDTO);
        this.hazelcastMaps.getClients().remove(clientDTO.clientId);

        String attachmentInBase64 = callEventReportPromise.get(5, TimeUnit.SECONDS).getAttachments();
        ClientAttachment attachment = ClientAttachment.builder().fromBase64(attachmentInBase64).build();
        Assertions.assertEquals(clientDTO.timeZoneId, attachment.timeZoneId);
    }
}