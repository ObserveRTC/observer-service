package org.observertc.observer.evaluators.eventreports;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.events.SfuEventType;
import org.observertc.observer.utils.DTOGenerators;

import java.util.List;

@MicronautTest
class SfuJoinedReportsTest {

    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    SfuJoinedReports sfuJoinedReports;

    @Test
    void shouldHasExpectedValues() throws Throwable {
        var expected = dtoGenerators.getSfuDTO();

        var reports = this.sfuJoinedReports.mapSfuDTOs(List.of(expected));
        var actual = reports.get(0);

        Assertions.assertEquals(expected.serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(expected.mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(expected.marker, actual.marker, "marker field");
        Assertions.assertEquals(expected.joined, actual.timestamp, "timestamp field");
        Assertions.assertEquals(expected.sfuId.toString(), actual.sfuId, "sfuId field");
        Assertions.assertEquals(null, actual.callId, "callId field");
        Assertions.assertEquals(null, actual.transportId, "transportId field");
        Assertions.assertEquals(null, actual.mediaStreamId, "mediaStreamId field");
        Assertions.assertEquals(null, actual.mediaSinkId, "mediaSinkId field");
        Assertions.assertEquals(null, actual.sctpStreamId, "sctpStreamId field");
        Assertions.assertEquals(null, actual.rtpPadId, "rtpPadId field");
        Assertions.assertEquals(SfuEventType.SFU_JOINED.name(), actual.name, "name field");
        Assertions.assertNotEquals(null, actual.message, "message field");
        Assertions.assertEquals(null, actual.value, "value field");
        Assertions.assertEquals(null, actual.attachments, "attachments field");
    }
}