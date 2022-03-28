package org.observertc.observer.components.eventreports;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.events.SfuEventType;
import org.observertc.observer.repositories.SfuRtpPadEvents;
import org.observertc.observer.utils.DTOGenerators;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@MicronautTest
class SfuRtpPadAddedReportsTest {
    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    SfuRtpPadAddedReports sfuRtpPadAddedReports;

    @Test
    void shouldHasExpectedValues_1() throws Throwable {
        var streamId = UUID.randomUUID();
        var trackId = UUID.randomUUID();
        var callId = UUID.randomUUID();
        var clientId = UUID.randomUUID();
        var sfuRtpPadDTO = this.dtoGenerators.getSfuRtpPadDTOBuilder()
                .withStreamId(streamId)
                .withStreamDirection(StreamDirection.INBOUND)
                .build();
        var sfuStreamDTO = this.dtoGenerators.getSfuStreamDTOBuilder()
                .withStreamId(streamId)
                .withSfuTransportId(sfuRtpPadDTO.transportId)
                .withSfuId(sfuRtpPadDTO.rtpPadId)
                .withTrackId(trackId)
                .withCallId(callId)
                .withClientId(clientId)
                .build();
        var expected = new SfuRtpPadEvents.Payload();
        expected.timestamp = Instant.now().toEpochMilli();
        expected.sfuStreamDTO = sfuStreamDTO;
        expected.sfuRtpPadDTO = sfuRtpPadDTO;

        var reports = this.sfuRtpPadAddedReports.mapCompletedSfuRtpPads(List.of(expected));
        var actual = reports.get(0);

        Assertions.assertEquals(sfuRtpPadDTO.serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(sfuRtpPadDTO.mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(sfuRtpPadDTO.marker, actual.marker, "marker field");
        Assertions.assertNotEquals(sfuRtpPadDTO.added, actual.timestamp, "timestamp field");
        Assertions.assertEquals(sfuRtpPadDTO.sfuId.toString(), actual.sfuId, "sfuId field");
        Assertions.assertEquals(callId.toString(), actual.callId, "callId field");
        Assertions.assertEquals(sfuRtpPadDTO.transportId.toString(), actual.transportId, "transportId field");
        Assertions.assertEquals(streamId.toString(), actual.mediaStreamId, "mediaStreamId field");
        Assertions.assertEquals(null, actual.mediaSinkId, "mediaSinkId field");
        Assertions.assertEquals(null, actual.sctpStreamId, "sctpStreamId field");
        Assertions.assertEquals(sfuRtpPadDTO.rtpPadId.toString(), actual.rtpPadId, "rtpPadId field");
        Assertions.assertEquals(SfuEventType.SFU_LEFT.name(), actual.name, "name field");
        Assertions.assertNotEquals(null, actual.message, "message field");
        Assertions.assertEquals(null, actual.value, "value field");
        Assertions.assertEquals(null, actual.attachments, "attachments field");

    }

    @Test
    void shouldHasExpectedValues_2() throws Throwable {
        var streamId = UUID.randomUUID();
        var sinkId = UUID.randomUUID();
        var trackId = UUID.randomUUID();
        var callId = UUID.randomUUID();
        var clientId = UUID.randomUUID();
        var sfuRtpPadDTO = this.dtoGenerators.getSfuRtpPadDTOBuilder()
                .withStreamId(streamId)
                .withStreamDirection(StreamDirection.INBOUND)
                .build();
        var sfuSinkDTO = this.dtoGenerators.getSfuSinkDTOBuilder()
                .withStreamId(streamId)
                .withSinkId(sinkId)
                .withSfuTransportId(sfuRtpPadDTO.transportId)
                .withSfuId(sfuRtpPadDTO.rtpPadId)
                .withTrackId(trackId)
                .withCallId(callId)
                .withClientId(clientId)
                .build();
        var expected = new SfuRtpPadEvents.Payload();
        expected.timestamp = Instant.now().toEpochMilli();
        expected.sfuSinkDTO = sfuSinkDTO;
        expected.sfuRtpPadDTO = sfuRtpPadDTO;

        var reports = this.sfuRtpPadAddedReports.mapCompletedSfuRtpPads(List.of(expected));
        var actual = reports.get(0);

        Assertions.assertEquals(sfuRtpPadDTO.serviceId, actual.serviceId, "serviceId field");
        Assertions.assertEquals(sfuRtpPadDTO.mediaUnitId, actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(sfuRtpPadDTO.marker, actual.marker, "marker field");
        Assertions.assertNotEquals(sfuRtpPadDTO.added, actual.timestamp, "timestamp field");
        Assertions.assertEquals(sfuRtpPadDTO.sfuId.toString(), actual.sfuId, "sfuId field");
        Assertions.assertEquals(callId.toString(), actual.callId, "callId field");
        Assertions.assertEquals(sfuRtpPadDTO.transportId.toString(), actual.transportId, "transportId field");
        Assertions.assertEquals(streamId.toString(), actual.mediaStreamId, "mediaStreamId field");
        Assertions.assertEquals(sinkId.toString(), actual.mediaSinkId, "mediaSinkId field");
        Assertions.assertEquals(null, actual.sctpStreamId, "sctpStreamId field");
        Assertions.assertEquals(sfuRtpPadDTO.rtpPadId.toString(), actual.rtpPadId, "rtpPadId field");
        Assertions.assertEquals(SfuEventType.SFU_RTP_PAD_ADDED.name(), actual.name, "name field");
        Assertions.assertNotEquals(null, actual.message, "message field");
        Assertions.assertEquals(null, actual.value, "value field");
        Assertions.assertNotEquals(null, actual.attachments, "attachments field");

    }
}