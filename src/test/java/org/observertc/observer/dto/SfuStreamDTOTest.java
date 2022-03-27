package org.observertc.observer.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class SfuStreamDTOTest {

    private final UUID SFU_ID = UUID.randomUUID();
    private final UUID SFU_TRANSPORT_ID = UUID.randomUUID();
    private final UUID SFU_STREAM_ID = UUID.randomUUID();
    private final UUID TRACK_ID = UUID.randomUUID();
    private final UUID CLIENT_ID = UUID.randomUUID();
    private final UUID CALL_ID = UUID.randomUUID();

    @Test
    void shouldHasExpectedValues() {
        var subject = this.makeDTO();

        Assertions.assertEquals(subject.sfuId, SFU_ID);
        Assertions.assertEquals(subject.sfuStreamId, SFU_STREAM_ID);
        Assertions.assertEquals(subject.sfuTransportId, SFU_TRANSPORT_ID);
        Assertions.assertEquals(subject.trackId, TRACK_ID);
        Assertions.assertEquals(subject.callId, CALL_ID);
        Assertions.assertEquals(subject.clientId, CLIENT_ID);

    }

    @Test
    void shouldBeEqual() {
        var source = this.makeDTO();
        var target = SfuStreamDTO.builder().from(source).build();

        boolean equals = source.equals(target);
        Assertions.assertTrue(equals);
    }


    private SfuStreamDTO.Builder makeBuilder() {
        return SfuStreamDTO.builder()
                .withSfuId(SFU_ID)
                .withStreamId(SFU_STREAM_ID)
                .withSfuTransportId(SFU_TRANSPORT_ID)
                .withTrackId(TRACK_ID)
                .withCallId(CALL_ID)
                .withClientId(CLIENT_ID)
                ;
    }

    private SfuStreamDTO makeDTO() {
        return this.makeBuilder().build();
    }

}