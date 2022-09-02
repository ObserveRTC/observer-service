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
class SfuInboundRtpPadAddedReportsTest {
    @Inject
    ModelsGenerator modelsGenerator;

    @Inject
    SfuInboundRtpPadAddedReports sfuInboundRtpPadAddedReports;

    @Test
    void shouldHasExpectedValues() throws Throwable {
        var sfuRtpPadDTO = this.modelsGenerator.getSfuInboundRtpPad();

        var promise = new CompletableFuture<List<SfuEventReport>>();
        this.sfuInboundRtpPadAddedReports.getOutput().subscribe(promise::complete);
        this.sfuInboundRtpPadAddedReports.accept(List.of(sfuRtpPadDTO));
        var actual = promise.get(10, TimeUnit.SECONDS).get(0);

        Assertions.assertEquals(sfuRtpPadDTO.getServiceId(), actual.serviceId, "serviceId field");
        Assertions.assertEquals(sfuRtpPadDTO.getMediaUnitId(), actual.mediaUnitId, "mediaUnitId field");
        Assertions.assertEquals(sfuRtpPadDTO.getMarker(), actual.marker, "marker field");
        Assertions.assertEquals(sfuRtpPadDTO.getAdded(), actual.timestamp, "timestamp field");
        Assertions.assertEquals(sfuRtpPadDTO.getSfuId().toString(), actual.sfuId, "sfuId field");
        Assertions.assertEquals(null, actual.callId, "callId field");
        Assertions.assertEquals(sfuRtpPadDTO.getSfuTransportId().toString(), actual.transportId, "transportId field");
        Assertions.assertEquals(sfuRtpPadDTO.getSfuStreamId(), actual.mediaStreamId, "mediaStreamId field");
        Assertions.assertEquals(null, actual.sctpStreamId, "sctpStreamId field");
        Assertions.assertEquals(sfuRtpPadDTO.getRtpPadId().toString(), actual.rtpPadId, "rtpPadId field");
        Assertions.assertEquals(SfuEventType.SFU_RTP_PAD_ADDED.name(), actual.name, "name field");
        Assertions.assertNotEquals(null, actual.message, "message field");
        Assertions.assertEquals(null, actual.value, "value field");
        Assertions.assertNotEquals(null, actual.attachments, "attachments field");
    }
}