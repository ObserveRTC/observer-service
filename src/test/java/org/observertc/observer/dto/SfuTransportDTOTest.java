package org.observertc.observer.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

class SfuTransportDTOTest {

    private final String SERVICE_ID = UUID.randomUUID().toString();
    private final String MEDIA_UNIT_ID = UUID.randomUUID().toString();
    private final UUID SFU_ID = UUID.randomUUID();
    private final UUID SFU_TRANSPORT_ID = UUID.randomUUID();
    private final boolean INTERNAL = true;
    private final Long TIMESTAMP = Instant.now().toEpochMilli();
    private final String MARKER = SerDeUtils.NULL_STRING;

    @Test
    void structureShouldHasNotChangedSinceLastTestFixed() {
        var fields = SfuTransportDTO.class.getFields();
        Assertions.assertEquals(8, fields.length);
    }

    @Test
    void shouldNotBuildWithoutSfuId() {
        var builder =  SfuTransportDTO.builder()
                .withServiceId(SERVICE_ID)
                .withOpenedTimestamp(TIMESTAMP)
                ;

        Assertions.assertThrows(Exception.class, builder::build);
    }

    @Test
    void shouldNotBuildWithoutServiceId() {
        var builder =  SfuTransportDTO.builder()
                .withSfuId(SFU_ID)
                .withOpenedTimestamp(TIMESTAMP)
                ;

        Assertions.assertThrows(Exception.class, builder::build);
    }

    @Test
    void shouldNotBuildWithoutTimestamp() {
        var builder =  SfuTransportDTO.builder()
                .withServiceId(SERVICE_ID)
                .withSfuId(SFU_ID)
                ;

        Assertions.assertThrows(Exception.class, builder::build);
    }

    @Test
    void shouldHasExpectedValues() {
        var subject = this.makeDTO();

        Assertions.assertEquals(subject.serviceId, SERVICE_ID);
        Assertions.assertEquals(subject.mediaUnitId, MEDIA_UNIT_ID);
        Assertions.assertEquals(subject.sfuId, SFU_ID);
        Assertions.assertEquals(subject.transportId, SFU_TRANSPORT_ID);
        Assertions.assertEquals(subject.internal, INTERNAL);
        Assertions.assertEquals(subject.opened, TIMESTAMP);
        Assertions.assertEquals(subject.marker, MARKER);

    }

    @Test
    void shouldBeEqual() {
        var source = this.makeDTO();
        var target = SfuTransportDTO.builder().from(source).build();

        boolean equals = source.equals(target);
        Assertions.assertTrue(equals);
    }


    private SfuTransportDTO.Builder makeBuilder() {
        return SfuTransportDTO.builder()
                .withServiceId(SERVICE_ID)
                .withMediaUnitId(MEDIA_UNIT_ID)
                .withSfuId(SFU_ID)
                .withTransportId(SFU_TRANSPORT_ID)
                .withInternal(INTERNAL)
                .withOpenedTimestamp(TIMESTAMP)
                .withMarker(MARKER)
                ;
    }

    private SfuTransportDTO makeDTO() {
        return this.makeBuilder().build();
    }
}