package org.observertc.observer.components.eventreports;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.events.SfuEventType;
import org.observertc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.observer.utils.DTOGenerators;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;

@MicronautTest
class SfuTransportClosedReportsTest {

    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    SfuTransportClosedReports sfuTransportClosedReports;

    @Test
    void shouldHasExpectedValuesWhenRemoved() throws Throwable {
        var expected = dtoGenerators.getSfuTransportDTO();

        var reports = this.sfuTransportClosedReports.mapRemovedSfuTransport(List.of(expected));
        var actual = reports.get(0);

        Assertions.assertEquals(expected.serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(expected.mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(expected.marker, actual.marker, "marker field");
        Assertions.assertNotEquals(expected.opened, actual.timestamp, "timestamp field");
        Assertions.assertEquals(expected.sfuId.toString(), actual.sfuId, "sfuId field");
        Assertions.assertEquals(expected.toString(), actual.callId, "callId field");
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
        var expected = dtoGenerators.getSfuTransportDTO();
        var lastTouch = Instant.now().minusSeconds(1232).toEpochMilli();
        var expired = RepositoryExpiredEvent.make(expected, lastTouch);
        var reports = this.sfuTransportClosedReports.mapExpiredSfuTransport(List.of(expired));
        var actual = reports.get(0);

        Assertions.assertEquals(lastTouch, actual.timestamp, "timestamp field");
    }
}