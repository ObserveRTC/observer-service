package org.observertc.webrtc.observer.evaluators.listeners;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.common.SfuEventType;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.utils.DTOGenerators;
import org.observertc.webrtc.schemas.reports.SfuEventReport;

import javax.inject.Inject;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
class SfuTransportClosedTest {

    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    SfuTransportClosed sfuTransportClosed;

    @Test
    void reportFieldsShouldHaveTheRightValues() throws InterruptedException, ExecutionException, TimeoutException {
        var callEventReportPromise = new CompletableFuture<SfuEventReport>();
        var sfuTransportDTO = dtoGenerators.getSfuTransportDTO();
        this.sfuTransportClosed.getObservableReports().subscribe(callEventReportPromise::complete);

        this.hazelcastMaps.getSFUTransports().put(sfuTransportDTO.transportId, sfuTransportDTO);
        this.hazelcastMaps.getSFUTransports().remove(sfuTransportDTO.transportId);
        var sfuEventReport = callEventReportPromise.get(5, TimeUnit.SECONDS);
        var timestampThreshold = Instant.now().plusSeconds(5).toEpochMilli();

        Assertions.assertEquals(SfuEventType.SFU_TRANSPORT_CLOSED.name(),sfuEventReport.getName());
        Assertions.assertEquals(sfuTransportDTO.serviceId, sfuEventReport.getServiceId());
        Assertions.assertEquals(sfuTransportDTO.mediaUnitId, sfuEventReport.getMediaUnitId());
        Assertions.assertEquals(sfuTransportDTO.sfuId.toString(), sfuEventReport.getSfuId());
//        Assertions.assertEquals(sfuDTO.timeZoneId, callEventReport.);
        Assertions.assertTrue(sfuEventReport.getTimestamp() < timestampThreshold);
    }
}