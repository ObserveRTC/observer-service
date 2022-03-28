package org.observertc.observer.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

class SfuDTOTest {

    private final String SERVICE_ID = UUID.randomUUID().toString();
    private final String MEDIA_UNIT_ID = UUID.randomUUID().toString();
    private final Long TIMESTAMP = Instant.now().toEpochMilli();
    private final UUID SFU_ID = UUID.randomUUID();
    private final String TIMEZONE_ID = ZoneId.systemDefault().getId();
    private final String MARKER = SerDeUtils.NULL_STRING;

    @Test
    void structureShouldHasNotChangedSinceLastTestFixed() {
        var fields = SfuDTOTest.class.getFields();
        Assertions.assertEquals(8, fields.length);
    }

    @Test
    void shouldNotBuildWithoutServiceId() {
        var builder = SfuDTO.builder()
                .withConnectedTimestamp(TIMESTAMP);


        Assertions.assertThrows(Exception.class, builder::build);
    }

    @Test
    void shouldNotBuildWithoutTimestamp() {
        var builder =  SfuDTO.builder()
                .withServiceId(SERVICE_ID);

        Assertions.assertThrows(Exception.class, builder::build);
    }

    @Test
    void shouldHasExpectedValues() {
        var subject = this.makeDTO();

        Assertions.assertEquals(subject.sfuId, SFU_ID);
        Assertions.assertEquals(subject.joined, TIMESTAMP);
        Assertions.assertEquals(subject.serviceId, SERVICE_ID);
        Assertions.assertEquals(subject.mediaUnitId, MEDIA_UNIT_ID);
        Assertions.assertEquals(subject.timeZoneId, TIMEZONE_ID);
        Assertions.assertEquals(subject.marker, MARKER);
    }

    @Test
    void shouldBeEqual() {
        var source = this.makeDTO();
        var target = SfuDTO.builder().from(source).build();

        boolean equals = source.equals(target);
        Assertions.assertTrue(equals);
    }


    private SfuDTO.Builder makeBuilder() {
        return SfuDTO.builder()
                .withServiceId(SERVICE_ID)
                .withMediaUnitId(MEDIA_UNIT_ID)
                .withSfuId(SFU_ID)
                .withTimeZoneId(TIMEZONE_ID)
                .withConnectedTimestamp(TIMESTAMP)
                .withMarker(MARKER)
                ;
    }

    private SfuDTO makeDTO() {
        return this.makeBuilder().build();
    }


}