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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
class ClientJoinedTest {

    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    ClientJoined clientJoined;



    @Test
    void reportFieldsShouldHaveTheRightValues() throws InterruptedException, ExecutionException, TimeoutException {
        var callEventReportPromise = new CompletableFuture<CallEventReport>();
        var clientDTO = dtoGenerators.getClientDTO();

        this.clientJoined.getObservableReports().subscribe(callEventReportPromise::complete);
        this.hazelcastMaps.getClients().put(clientDTO.clientId, clientDTO);
        var callEventReport = callEventReportPromise.get(5, TimeUnit.SECONDS);

        Assertions.assertEquals(CallEventType.CLIENT_JOINED.name(), callEventReport.getName());
        Assertions.assertEquals(clientDTO.serviceId, callEventReport.getServiceId());
        Assertions.assertEquals(clientDTO.roomId, callEventReport.getRoomId());

        Assertions.assertEquals(clientDTO.mediaUnitId, callEventReport.getMediaUnitId());
        Assertions.assertEquals(clientDTO.callId.toString(), callEventReport.getCallId());
        Assertions.assertEquals(clientDTO.userId, callEventReport.getUserId());
        Assertions.assertEquals(clientDTO.clientId.toString(), callEventReport.getClientId());
        Assertions.assertEquals(clientDTO.joined, callEventReport.getTimestamp());
    }

    @Test
    void reportAttachmentFieldsShouldHaveTheRightValues() throws InterruptedException, ExecutionException, TimeoutException {
        var callEventReportPromise = new CompletableFuture<CallEventReport>();
        var clientDTO = dtoGenerators.getClientDTO();

        this.clientJoined.getObservableReports().subscribe(callEventReportPromise::complete);
        this.hazelcastMaps.getClients().put(clientDTO.clientId, clientDTO);

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

        this.clientJoined.getObservableReports().subscribe(callEventReportPromise::complete);
        this.hazelcastMaps.getClients().put(clientDTO.clientId, clientDTO);

        String attachmentInBase64 = callEventReportPromise.get(5, TimeUnit.SECONDS).getAttachments();
        ClientAttachment attachment = ClientAttachment.builder().fromBase64(attachmentInBase64).build();
        Assertions.assertEquals(clientDTO.timeZoneId, attachment.timeZoneId);
    }
}