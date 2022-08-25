package org.observertc.observer.evaluators.eventreports;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.repositories.HamokStorages;
import org.observertc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.observer.utils.DTOGenerators;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@MicronautTest
class PeerConnectionClosedReportsTest {
    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    PeerConnectionClosedReports peerConnectionClosedReports;

    @Inject
    HamokStorages hazelcastMaps;

    @Test
    void shouldHasExpectedValuesWhenRemoved() throws Throwable {
        var expected = dtoGenerators.getPeerConnectionDTO();

        var reports = this.peerConnectionClosedReports.mapRemovedPeerConnections(List.of(expected));
        var actual = reports.get(0);

        Assertions.assertEquals(expected.serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(expected.mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(expected.marker, actual.marker, "marker field");
        Assertions.assertNotEquals(expected.created, actual.timestamp, "timestamp field");
        Assertions.assertEquals(expected.callId.toString(), actual.callId, "callId field");
        Assertions.assertEquals(expected.roomId, actual.roomId, "roomId field");

        Assertions.assertEquals(expected.clientId.toString(), actual.clientId, "clientId field");
        Assertions.assertEquals(expected.userId, actual.userId, "userId field");
        Assertions.assertEquals(expected.peerConnectionId.toString(), actual.peerConnectionId, "peerConnectionId field");
        Assertions.assertEquals(null, actual.mediaTrackId, "mediaTrackId field");
        Assertions.assertEquals(null,  actual.SSRC, "SSRC field");
        Assertions.assertEquals(null, actual.sampleTimestamp, "sampleTimestamp field");
        Assertions.assertEquals(null, actual.sampleSeq, "sampleSeq field");
        Assertions.assertEquals(CallEventType.PEER_CONNECTION_CLOSED.name(), actual.name, "name field");
        Assertions.assertNotEquals(null, actual.message, "message field");
        Assertions.assertEquals(null, actual.value, "value field");
        Assertions.assertEquals(null, actual.attachments, "attachments field");

    }


    @Test
    void shouldHasExpectedValuesWhenExpired() {
        UUID callId = UUID.randomUUID();
        var clientDTO = this.dtoGenerators.getClientDTOBuilder()
                .withCallId(callId)
                .build();
        var peerConnectionDTO = this.dtoGenerators.getPeerConnectionDTOBuilder()
                .withCallId(callId)
                .withClientId(clientDTO.clientId)
                .build();
        var mediaTrackDTO = this.dtoGenerators.getMediaTrackDTOBuilder()
                .withCallId(callId)
                .withClientId(clientDTO.clientId)
                .withPeerConnectionId(peerConnectionDTO.peerConnectionId)
                .withDirection(StreamDirection.INBOUND)
                .build();
        this.hazelcastMaps.getClients().put(clientDTO.clientId, clientDTO);
        this.hazelcastMaps.getPeerConnections().put(peerConnectionDTO.peerConnectionId, peerConnectionDTO);
        this.hazelcastMaps.getClientToPeerConnectionIds().put(clientDTO.clientId, peerConnectionDTO.peerConnectionId);
        this.hazelcastMaps.getMediaTracks().put(mediaTrackDTO.trackId, mediaTrackDTO);
        this.hazelcastMaps.getPeerConnectionToInboundTrackIds().put(peerConnectionDTO.peerConnectionId, mediaTrackDTO.trackId);

        var lastTouched = Instant.now().minusMillis(6000).toEpochMilli();
        var expired = RepositoryExpiredEvent.make(this.hazelcastMaps.getPeerConnections().remove(peerConnectionDTO.peerConnectionId), lastTouched);
        var reports = this.peerConnectionClosedReports.mapExpiredPeerConnections(List.of(expired));
        var actual = reports.get(0);

        Assertions.assertEquals(lastTouched, actual.timestamp, "timestamp field");
        Assertions.assertNull(this.hazelcastMaps.getPeerConnections().get(peerConnectionDTO.peerConnectionId));
        Assertions.assertEquals(0, this.hazelcastMaps.getClientToPeerConnectionIds().get(clientDTO.clientId).size());
        Assertions.assertNull(this.hazelcastMaps.getMediaTracks().get(mediaTrackDTO.trackId));
        Assertions.assertEquals(0, this.hazelcastMaps.getPeerConnectionToInboundTrackIds().get(mediaTrackDTO.peerConnectionId).size());
    }
}