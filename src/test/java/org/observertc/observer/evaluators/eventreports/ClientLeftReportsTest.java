package org.observertc.observer.evaluators.eventreports;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.evaluators.eventreports.attachments.ClientAttachment;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.observer.utils.ModelsGenerator;

import java.time.Instant;
import java.util.List;

@MicronautTest
class ClientLeftReportsTest {

    @Inject
    ModelsGenerator modelsGenerator;

    @Inject
    ClientLeftReports clientLeftReports;

    @Inject
    HamokStorages hazelcastMaps;

    @Test
    void shouldHasExpectedValuesWhenRemoved() throws Throwable {
        var expected = modelsGenerator.getClientDTO();

        var reports = this.clientLeftReports.mapRemovedClients(List.of(expected));
        var actual = reports.get(0);

        Assertions.assertEquals(expected.serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(expected.mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(expected.marker, actual.marker, "marker field");
        Assertions.assertNotEquals(expected.joined, actual.timestamp, "timestamp field");
        Assertions.assertEquals(expected.callId.toString(), actual.callId, "callId field");
        Assertions.assertEquals(expected.roomId, actual.roomId, "roomId field");

        Assertions.assertEquals(expected.clientId.toString(), actual.clientId, "clientId field");
        Assertions.assertEquals(expected.userId, actual.userId, "userId field");
        Assertions.assertEquals(null, actual.peerConnectionId, "peerConnectionId field");
        Assertions.assertEquals(null, actual.mediaTrackId, "mediaTrackId field");
        Assertions.assertEquals(null,  actual.SSRC, "SSRC field");
        Assertions.assertEquals(null, actual.sampleTimestamp, "sampleTimestamp field");
        Assertions.assertEquals(null, actual.sampleSeq, "sampleSeq field");
        Assertions.assertEquals(CallEventType.CLIENT_LEFT.name(), actual.name, "name field");
        Assertions.assertNotEquals(null, actual.message, "message field");
        Assertions.assertEquals(null, actual.value, "value field");
        Assertions.assertNotEquals(null, actual.attachments, "attachments field");

        ClientAttachment attachment = ClientAttachment.builder().fromBase64(actual.attachments).build();
        Assertions.assertEquals(expected.timeZoneId, attachment.timeZoneId);
    }

    @Test
    void shouldHasExpectedValuesWhenExpired() throws Throwable {
        var expected = modelsGenerator.getClientDTO();
        var lastTouched = Instant.now().minusMillis(6000).toEpochMilli();
        var expired = RepositoryExpiredEvent.make(expected, lastTouched);

        var reports = this.clientLeftReports.mapExpiredClients(List.of(expired));
        var actual = reports.get(0);

        Assertions.assertEquals(lastTouched, actual.timestamp, "timestamp field");
    }

    @Test
    void shouldRemoveAbandonedCall() {
        var callDTO = this.modelsGenerator.getCallDTO();
        var clientDTO = this.modelsGenerator.getClientDTOBuilder()
                .withCallId(callDTO.callId)
                .build();
        var peerConnectionDTO = this.modelsGenerator.getPeerConnectionDTOBuilder()
                .withCallId(callDTO.callId)
                .withClientId(clientDTO.clientId)
                .build();
        var mediaTrackDTO = this.modelsGenerator.getMediaTrackDTOBuilder()
                .withCallId(callDTO.callId)
                .withClientId(clientDTO.clientId)
                .withPeerConnectionId(peerConnectionDTO.peerConnectionId)
                .withDirection(StreamDirection.INBOUND)
                .build();
        this.hazelcastMaps.getCalls().put(callDTO.callId, callDTO);
        this.hazelcastMaps.getClients().put(clientDTO.clientId, clientDTO);
        this.hazelcastMaps.getCallToClientIds().put(callDTO.callId, clientDTO.clientId);
        this.hazelcastMaps.getPeerConnections().put(peerConnectionDTO.peerConnectionId, peerConnectionDTO);
        this.hazelcastMaps.getClientToPeerConnectionIds().put(clientDTO.clientId, peerConnectionDTO.peerConnectionId);
        this.hazelcastMaps.getMediaTracks().put(mediaTrackDTO.trackId, mediaTrackDTO);
        this.hazelcastMaps.getPeerConnectionToInboundTrackIds().put(peerConnectionDTO.peerConnectionId, mediaTrackDTO.trackId);

        var lastTouched = Instant.now().minusMillis(6000).toEpochMilli();
        var expired = RepositoryExpiredEvent.make(this.hazelcastMaps.getClients().remove(clientDTO.clientId), lastTouched);
        this.clientLeftReports.mapExpiredClients(List.of(expired));

        Assertions.assertNull(this.hazelcastMaps.getCalls().get(callDTO.callId));
        Assertions.assertNull(this.hazelcastMaps.getClients().get(clientDTO.clientId));
        Assertions.assertEquals(0, this.hazelcastMaps.getCallToClientIds().get(callDTO.callId).size());
        Assertions.assertNull(this.hazelcastMaps.getPeerConnections().get(peerConnectionDTO.peerConnectionId));
        Assertions.assertEquals(0, this.hazelcastMaps.getClientToPeerConnectionIds().get(clientDTO.clientId).size());
        Assertions.assertNull(this.hazelcastMaps.getMediaTracks().get(mediaTrackDTO.trackId));
        Assertions.assertEquals(0, this.hazelcastMaps.getPeerConnectionToInboundTrackIds().get(mediaTrackDTO.peerConnectionId).size());
    }
}