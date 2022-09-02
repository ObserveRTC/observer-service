package org.observertc.observer.evaluators.eventreports;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.events.SfuEventType;
import org.observertc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.observer.utils.ModelsGenerator;

import java.time.Instant;
import java.util.List;

@MicronautTest
class SfuTransportClosedReportsTest {

    @Inject
    ModelsGenerator modelsGenerator;

    @Inject
    SfuTransportClosedReports sfuTransportClosedReports;

    @Test
    void shouldHasExpectedValuesWhenRemoved() throws Throwable {
        var expected = modelsGenerator.getSfuTransportDTO();

        var reports = this.sfuTransportClosedReports.mapRemovedSfuTransport(List.of(expected));
        var actual = reports.get(0);

        Assertions.assertEquals(expected.serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(expected.mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(expected.marker, actual.marker, "marker field");
        Assertions.assertNotEquals(expected.opened, actual.timestamp, "timestamp field");
        Assertions.assertEquals(expected.sfuId.toString(), actual.sfuId, "sfuId field");
        Assertions.assertEquals(null, actual.callId, "callId field");
        Assertions.assertEquals(expected.transportId.toString(), actual.transportId, "transportId field");
        Assertions.assertEquals(null, actual.mediaStreamId, "mediaStreamId field");
        Assertions.assertEquals(null, actual.mediaSinkId, "mediaSinkId field");
        Assertions.assertEquals(null, actual.sctpStreamId, "sctpStreamId field");
        Assertions.assertEquals(null, actual.rtpPadId, "rtpPadId field");
        Assertions.assertEquals(SfuEventType.SFU_TRANSPORT_CLOSED.name(), actual.name, "name field");
        Assertions.assertNotEquals(null, actual.message, "message field");
        Assertions.assertEquals(null, actual.value, "value field");
        Assertions.assertNotEquals(null, actual.attachments, "attachments field");

    }

    @Test
    void shouldHasExpectedValuesWhenExpired() throws Throwable {
        var expected = modelsGenerator.getSfuTransportDTO();
        var lastTouch = Instant.now().minusSeconds(1232).toEpochMilli();
        var expired = RepositoryExpiredEvent.make(expected, lastTouch);
        var reports = this.sfuTransportClosedReports.mapExpiredSfuTransport(List.of(expired));
        var actual = reports.get(0);

        Assertions.assertEquals(lastTouch, actual.timestamp, "timestamp field");
    }
}