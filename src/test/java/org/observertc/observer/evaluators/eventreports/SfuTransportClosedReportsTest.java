package org.observertc.observer.evaluators.eventreports;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.events.SfuEventType;
import org.observertc.observer.utils.ModelsGenerator;
import org.observertc.schemas.reports.SfuEventReport;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@MicronautTest
class SfuTransportClosedReportsTest {

    @Inject
    ModelsGenerator modelsGenerator;

    @Inject
    SfuTransportClosedReports sfuTransportClosedReports;

    @Test
    void shouldHasExpectedValuesWhenRemoved() throws Throwable {
        var expected = this.modelsGenerator.getSfuTransportModel();

        var promise = new CompletableFuture<List<SfuEventReport>>();
        this.sfuTransportClosedReports.getOutput().subscribe(promise::complete);
        this.sfuTransportClosedReports.accept(List.of(expected));
        var actual = promise.get(10, TimeUnit.SECONDS).get(0);

        Assertions.assertEquals(expected.getServiceId(), actual.serviceId, "serviceId field");
        Assertions.assertEquals(expected.getMediaUnitId(), actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(expected.getMarker(), actual.marker, "marker field");
        Assertions.assertNotNull(actual.timestamp, "timestamp field");
        Assertions.assertEquals(expected.getSfuId().toString(), actual.sfuId, "sfuId field");
        Assertions.assertEquals(null, actual.callId, "callId field");
        Assertions.assertEquals(expected.getTransportId().toString(), actual.transportId, "transportId field");

        Assertions.assertEquals(null, actual.mediaStreamId, "mediaStreamId field");
        Assertions.assertEquals(null, actual.mediaSinkId, "mediaSinkId field");
        Assertions.assertEquals(null, actual.sctpStreamId, "sctpStreamId field");
        Assertions.assertEquals(null, actual.rtpPadId, "rtpPadId field");
        Assertions.assertEquals(SfuEventType.SFU_TRANSPORT_CLOSED.name(), actual.name, "name field");
        Assertions.assertNotEquals(null, actual.message, "message field");
        Assertions.assertEquals(null, actual.value, "value field");
        Assertions.assertNotEquals(null, actual.attachments, "attachments field");

    }
}