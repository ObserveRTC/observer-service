package org.observertc.observer.evaluators.listeners;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.events.SfuEventType;
import org.observertc.observer.utils.DTOGenerators;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.schemas.reports.SfuEventReport;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
class SfuTransportOpenedTestCodec {

    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    SfuTransportOpened sfuTransportOpened;

    @Test
    void reportFieldsShouldHaveTheRightValues() throws InterruptedException, ExecutionException, TimeoutException {
        var callEventReportPromise = new CompletableFuture<SfuEventReport>();
        var sfuTransportDTO = dtoGenerators.getSfuTransportDTO();
        this.sfuTransportOpened.getObservableReports().subscribe(callEventReportPromise::complete);

        this.hazelcastMaps.getSFUTransports().put(sfuTransportDTO.transportId, sfuTransportDTO);
        var sfuEventReport = callEventReportPromise.get(5, TimeUnit.SECONDS);

        Assertions.assertEquals(SfuEventType.SFU_TRANSPORT_OPENED.name(),sfuEventReport.getName());
        Assertions.assertEquals(sfuTransportDTO.serviceId, sfuEventReport.getServiceId());
        Assertions.assertEquals(sfuTransportDTO.mediaUnitId, sfuEventReport.getMediaUnitId());
        Assertions.assertEquals(sfuTransportDTO.sfuId.toString(), sfuEventReport.getSfuId());
//        Assertions.assertEquals(sfuDTO.timeZoneId, callEventReport.);
        Assertions.assertEquals(sfuTransportDTO.opened, sfuEventReport.getTimestamp());
    }
}