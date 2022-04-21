package org.observertc.observer.evaluators.eventreports;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.utils.DTOGenerators;

import java.util.List;

@MicronautTest
class CallEndedReportsTest {

    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    CallEndedReports callEndedReports;

    @Test
    void shouldHasExpectedValues() throws Throwable {
        var expected = dtoGenerators.getCallDTO();

        var reports = this.callEndedReports.mapCallDTOs(List.of(expected));
        var actual = reports.get(0);

        Assertions.assertEquals(expected.serviceId, actual.serviceId, "serviceId field");
        Assertions.assertNull(actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(expected.marker, actual.marker, "marker field");
        Assertions.assertNotNull(actual.timestamp, "timestamp field");
        Assertions.assertEquals(expected.callId.toString(), actual.callId, "callId field");
        Assertions.assertEquals(expected.roomId, actual.roomId, "roomId field");

        Assertions.assertEquals(null, actual.clientId, "clientId field");
        Assertions.assertEquals(null, actual.userId, "userId field");
        Assertions.assertEquals(null, actual.peerConnectionId, "peerConnectionId field");
        Assertions.assertEquals(null, actual.mediaTrackId, "mediaTrackId field");
        Assertions.assertEquals(null,  actual.SSRC, "SSRC field");
        Assertions.assertEquals(null, actual.sampleTimestamp, "sampleTimestamp field");
        Assertions.assertEquals(null, actual.sampleSeq, "sampleSeq field");
        Assertions.assertEquals(CallEventType.CALL_ENDED.name(), actual.name, "name field");
        Assertions.assertNotEquals(null, actual.message, "message field");
        Assertions.assertEquals(null, actual.value, "value field");
        Assertions.assertEquals(null, actual.attachments, "attachments field");
    }
}