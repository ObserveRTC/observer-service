package org.observertc.observer.evaluators.eventreports;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.utils.ModelsGenerator;

import java.util.List;

@MicronautTest
class PeerConnectionOpenedReportsTest {

    @Inject
    ModelsGenerator modelsGenerator;

    @Inject
    PeerConnectionOpenedReports peerConnectionOpenedReports;

    @Test
    void shouldHasExpectedValues() throws Throwable {
        var expected = modelsGenerator.getPeerConnectionDTO();

        var reports = this.peerConnectionOpenedReports.mapAddedPeerConnections(List.of(expected));
        var actual = reports.get(0);

        Assertions.assertEquals(expected.serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(expected.mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(expected.marker, actual.marker, "marker field");
        Assertions.assertNotNull(actual.timestamp, "timestamp field");
        Assertions.assertEquals(expected.callId.toString(), actual.callId, "callId field");
        Assertions.assertEquals(expected.roomId, actual.roomId, "roomId field");

        Assertions.assertEquals(expected.clientId.toString(), actual.clientId, "clientId field");
        Assertions.assertEquals(expected.userId, actual.userId, "userId field");
        Assertions.assertEquals(expected.peerConnectionId.toString(), actual.peerConnectionId, "peerConnectionId field");
        Assertions.assertEquals(null, actual.mediaTrackId, "mediaTrackId field");
        Assertions.assertEquals(null,  actual.SSRC, "SSRC field");
        Assertions.assertEquals(null, actual.sampleTimestamp, "sampleTimestamp field");
        Assertions.assertEquals(null, actual.sampleSeq, "sampleSeq field");
        Assertions.assertEquals(CallEventType.PEER_CONNECTION_OPENED.name(), actual.name, "name field");
        Assertions.assertNotEquals(null, actual.message, "message field");
        Assertions.assertEquals(null, actual.value, "value field");
        Assertions.assertEquals(null, actual.attachments, "attachments field");
    }
}