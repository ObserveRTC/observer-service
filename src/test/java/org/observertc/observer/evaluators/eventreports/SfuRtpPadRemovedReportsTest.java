package org.observertc.observer.evaluators.eventreports;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.events.SfuEventType;
import org.observertc.observer.repositories.RepositoryExpiredEvent;
import org.observertc.observer.utils.DTOGenerators;

import java.time.Instant;
import java.util.List;

@MicronautTest
class SfuRtpPadRemovedReportsTest {

    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    SfuRtpPadRemovedReports sfuRtpPadRemovedReports;

    @Test
    void shouldHasExpectedValuesWhenRemoved() throws Throwable {
        var sfuRtpPadDTO = this.dtoGenerators.getSfuRtpPadDTO();

        var reports = this.sfuRtpPadRemovedReports.mapRemovedSfuRtpPad(List.of(sfuRtpPadDTO));
        var actual = reports.get(0);
        var streamId = UUIDAdapter.toStringOrNull(sfuRtpPadDTO.streamId);
        var sinkId = UUIDAdapter.toStringOrNull(sfuRtpPadDTO.sinkId);

        Assertions.assertEquals(sfuRtpPadDTO.serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(sfuRtpPadDTO.mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(sfuRtpPadDTO.marker, actual.marker, "marker field");
        Assertions.assertNotEquals(sfuRtpPadDTO.added, actual.timestamp, "timestamp field");
        Assertions.assertEquals(sfuRtpPadDTO.sfuId.toString(), actual.sfuId, "sfuId field");
        Assertions.assertEquals(null, actual.callId, "callId field");
        Assertions.assertEquals(sfuRtpPadDTO.transportId.toString(), actual.transportId, "transportId field");
        Assertions.assertEquals(streamId, actual.mediaStreamId, "mediaStreamId field");
        Assertions.assertEquals(sinkId, actual.mediaSinkId, "mediaSinkId field");
        Assertions.assertEquals(null, actual.sctpStreamId, "sctpStreamId field");
        Assertions.assertEquals(sfuRtpPadDTO.rtpPadId.toString(), actual.rtpPadId, "rtpPadId field");
        Assertions.assertEquals(SfuEventType.SFU_RTP_PAD_REMOVED.name(), actual.name, "name field");
        Assertions.assertNotEquals(null, actual.message, "message field");
        Assertions.assertEquals(null, actual.value, "value field");
        Assertions.assertNotEquals(null, actual.attachments, "attachments field");

    }

    @Test
    void shouldHasExpectedValuesWhenExpired() throws Throwable {
        var sfuRtpPadDTO = this.dtoGenerators.getSfuRtpPadDTO();
        var lastTouch = Instant.now().toEpochMilli();

        var expiredEvent = RepositoryExpiredEvent.make(sfuRtpPadDTO, lastTouch);
        var reports = this.sfuRtpPadRemovedReports.mapExpiredSfuRtpPad(List.of(expiredEvent));
        var actual = reports.get(0);
        var streamId = UUIDAdapter.toStringOrNull(sfuRtpPadDTO.streamId);
        var sinkId = UUIDAdapter.toStringOrNull(sfuRtpPadDTO.sinkId);

        Assertions.assertEquals(sfuRtpPadDTO.serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(sfuRtpPadDTO.mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(sfuRtpPadDTO.marker, actual.marker, "marker field");
        Assertions.assertNotEquals(sfuRtpPadDTO.added, actual.timestamp, "timestamp field");
        Assertions.assertEquals(sfuRtpPadDTO.sfuId.toString(), actual.sfuId, "sfuId field");
        Assertions.assertEquals(null, actual.callId, "callId field");
        Assertions.assertEquals(sfuRtpPadDTO.transportId.toString(), actual.transportId, "transportId field");
        Assertions.assertEquals(streamId, actual.mediaStreamId, "mediaStreamId field");
        Assertions.assertEquals(sinkId, actual.mediaSinkId, "mediaSinkId field");
        Assertions.assertEquals(null, actual.sctpStreamId, "sctpStreamId field");
        Assertions.assertEquals(sfuRtpPadDTO.rtpPadId.toString(), actual.rtpPadId, "rtpPadId field");
        Assertions.assertEquals(SfuEventType.SFU_RTP_PAD_REMOVED.name(), actual.name, "name field");
        Assertions.assertNotEquals(null, actual.message, "message field");
        Assertions.assertEquals(null, actual.value, "value field");
        Assertions.assertNotEquals(null, actual.attachments, "attachments field");

    }

}