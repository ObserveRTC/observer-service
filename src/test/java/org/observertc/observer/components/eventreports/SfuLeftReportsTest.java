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
class SfuLeftReportsTest {

    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    SfuLeftReports sfuLeftReports;

    @Test
    void shouldHasExpectedValuesWhenRemoved() throws Throwable {
        var expected = dtoGenerators.getSfuDTO();

        var reports = this.sfuLeftReports.mapRemovedSfuDTOs(List.of(expected));
        var actual = reports.get(0);

        Assertions.assertEquals(expected.serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(expected.mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(expected.marker, actual.marker, "marker field");
        Assertions.assertNotEquals(expected.joined, actual.timestamp, "timestamp field");
        Assertions.assertEquals(expected.sfuId.toString(), actual.sfuId, "sfuId field");
        Assertions.assertEquals(null, actual.callId, "callId field");
        Assertions.assertEquals(null, actual.transportId, "transportId field");
        Assertions.assertEquals(null, actual.mediaStreamId, "mediaStreamId field");
        Assertions.assertEquals(null, actual.mediaSinkId, "mediaSinkId field");
        Assertions.assertEquals(null, actual.sctpStreamId, "sctpStreamId field");
        Assertions.assertEquals(null, actual.rtpPadId, "rtpPadId field");
        Assertions.assertEquals(SfuEventType.SFU_LEFT.name(), actual.name, "name field");
        Assertions.assertNotEquals(null, actual.message, "message field");
        Assertions.assertEquals(null, actual.value, "value field");
        Assertions.assertEquals(null, actual.attachments, "attachments field");

    }


    @Test
    void shouldHasExpectedValuesWhenExpired() {
        var expected = dtoGenerators.getSfuDTO();

        var lastTouched = Instant.now().minusMillis(6000).toEpochMilli();
        var expired = RepositoryExpiredEvent.make(expected, lastTouched);
        var reports = this.sfuLeftReports.mapExpiredSfuDTOs(List.of(expired));
        var actual = reports.get(0);

        Assertions.assertEquals(lastTouched, actual.timestamp, "timestamp field");
    }

}