package org.observertc.observer.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

class SfuRtpPadDTOTest {

    private final String SERVICE_ID = "serviceId";
    private final String MEDIA_UNIT_ID = "mediaUnitId";
    private final UUID SFU_ID = UUID.randomUUID();
    private final UUID SFU_TRANSPORT_ID = UUID.randomUUID();
    private final UUID SFU_STREAM_ID = UUID.randomUUID();
    private final UUID SFU_SINK_ID = UUID.randomUUID();
    private final UUID SFU_RTP_PAD_ID = UUID.randomUUID();
    private final StreamDirection DIRECTION = UUID.randomUUID().getLeastSignificantBits() % 2L == 0 ? StreamDirection.OUTBOUND : StreamDirection.INBOUND;
    private final boolean SFU_RTP_INTERNAL = true;
    private final Long TIMESTAMP = Instant.now().toEpochMilli();

    @Test
    void shouldNotBuildWithoutSfuId() {
        var builder = SfuRtpPadDTO.builder()
                .withSfuRtpPadId(SFU_RTP_PAD_ID)
                .withAddedTimestamp(TIMESTAMP)
                .withSfuTransportId(SFU_TRANSPORT_ID)
                ;

        Assertions.assertThrows(Exception.class, builder::build);
    }

    @Test
    void shouldNotBuildWithoutTransportId() {
        var builder = SfuRtpPadDTO.builder()
                .withSfuId(SFU_ID)
                .withSfuRtpPadId(SFU_RTP_PAD_ID)
                .withAddedTimestamp(TIMESTAMP)
                ;

        Assertions.assertThrows(Exception.class, builder::build);
    }

    @Test
    void shouldNotBuildWithoutRtpPadId() {
        var builder = SfuRtpPadDTO.builder()
                .withSfuId(SFU_ID)
                .withAddedTimestamp(TIMESTAMP)
                .withSfuTransportId(SFU_TRANSPORT_ID)
                ;

        Assertions.assertThrows(Exception.class, builder::build);
    }

    @Test
    void shouldNotBuildWithoutTimestamp() {
        var builder = SfuRtpPadDTO.builder()
                .withSfuId(SFU_ID)
                .withSfuRtpPadId(SFU_RTP_PAD_ID)
                .withSfuTransportId(SFU_TRANSPORT_ID)
                ;

        Assertions.assertThrows(Exception.class, builder::build);
    }

    @Test
    void shouldHasExpectedValues() {
        var subject = this.makeDTO();

        Assertions.assertEquals(subject.serviceId, SERVICE_ID);
        Assertions.assertEquals(subject.mediaUnitId, MEDIA_UNIT_ID);
        Assertions.assertEquals(subject.sfuId, SFU_ID);
        Assertions.assertEquals(subject.sinkId, SFU_SINK_ID);
        Assertions.assertEquals(subject.streamId, SFU_STREAM_ID);
        Assertions.assertEquals(subject.streamDirection, DIRECTION);
        Assertions.assertEquals(subject.rtpPadId, SFU_RTP_PAD_ID);
        Assertions.assertEquals(subject.transportId, SFU_TRANSPORT_ID);
        Assertions.assertEquals(subject.internal, SFU_RTP_INTERNAL);
        Assertions.assertEquals(subject.added, TIMESTAMP);
    }

    @Test
    void shouldBeEqual() {
        var source = this.makeDTO();
        var target = SfuRtpPadDTO.builder().from(source).build();

        boolean equals = source.equals(target);
        Assertions.assertTrue(equals);
    }


    private SfuRtpPadDTO.Builder makeBuilder() {
        return SfuRtpPadDTO.builder()
                .withServiceId(SERVICE_ID)
                .withMediaUnitId(MEDIA_UNIT_ID)
                .withSfuId(SFU_ID)
                .withSinkId(SFU_SINK_ID)
                .withStreamId(SFU_STREAM_ID)
                .withStreamDirection(DIRECTION)
                .withSfuRtpPadId(SFU_RTP_PAD_ID)
                .withSfuTransportId(SFU_TRANSPORT_ID)
                .withInternal(SFU_RTP_INTERNAL)
                .withAddedTimestamp(TIMESTAMP)
                ;
    }

    private SfuRtpPadDTO makeDTO() {
        return this.makeBuilder().build();
    }

}