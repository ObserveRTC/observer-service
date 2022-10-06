package org.observertc.observer.evaluators.eventreports;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.utils.ModelsGenerator;
import org.observertc.schemas.reports.CallEventReport;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@MicronautTest
class InboundTrackAddedReportsTest {

    @Inject
    ModelsGenerator modelsGenerator;

    @Inject
    InboundTrackAddedReports inboundTrackAddedReports;

    @Test
    void shouldHasExpectedValues() throws Throwable {
        var expected = modelsGenerator.getInboundTrackModel();

        var promise = new CompletableFuture<List<CallEventReport>>();
        this.inboundTrackAddedReports.getOutput().subscribe(promise::complete);
        this.inboundTrackAddedReports.accept(List.of(expected));
        var actual = promise.get(10, TimeUnit.SECONDS).get(0);

        Assertions.assertEquals(expected.getServiceId(), actual.serviceId, "serviceId field");
        Assertions.assertEquals(expected.getMediaUnitId(), actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(expected.getMarker(), actual.marker, "marker field");
        Assertions.assertNotNull(actual.timestamp, "timestamp field");
        Assertions.assertEquals(expected.getCallId().toString(), actual.callId, "callId field");
        Assertions.assertEquals(expected.getRoomId(), actual.roomId, "roomId field");

        Assertions.assertEquals(expected.getClientId().toString(), actual.clientId, "clientId field");
        Assertions.assertEquals(expected.getUserId(), actual.userId, "userId field");
        Assertions.assertEquals(expected.getPeerConnectionId().toString(), actual.peerConnectionId, "peerConnectionId field");
        Assertions.assertEquals(expected.getTrackId().toString(), actual.mediaTrackId, "mediaTrackId field");
//        Assertions.assertEquals(expected.getSsrc(0),  actual.SSRC, "SSRC field");
        Assertions.assertEquals(null, actual.sampleTimestamp, "sampleTimestamp field");
        Assertions.assertEquals(null, actual.sampleSeq, "sampleSeq field");
        Assertions.assertEquals(CallEventType.MEDIA_TRACK_ADDED.name(), actual.name, "name field");
        Assertions.assertNotEquals(null, actual.message, "message field");
        Assertions.assertEquals(null, actual.value, "value field");
        Assertions.assertNotEquals(null, actual.attachments, "attachments field");
    }
}