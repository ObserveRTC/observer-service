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
class SfuJoinedTest {

    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    SfuJoined sfuJoined;

    @Test
    void reportFieldsShouldHaveTheRightValues() throws InterruptedException, ExecutionException, TimeoutException {
        var callEventReportPromise = new CompletableFuture<SfuEventReport>();
        var sfuDTO = dtoGenerators.getSfuDTO();
        this.sfuJoined.getObservableReports().subscribe(callEventReportPromise::complete);

        this.hazelcastMaps.getSFUs().put(sfuDTO.sfuId, sfuDTO);
        var sfuEventReport = callEventReportPromise.get(5, TimeUnit.SECONDS);

        Assertions.assertEquals(SfuEventType.SFU_JOINED.name(),sfuEventReport.getName());
        Assertions.assertEquals(sfuDTO.serviceId, sfuEventReport.getServiceId());
        Assertions.assertEquals(sfuDTO.mediaUnitId, sfuEventReport.getMediaUnitId());
        Assertions.assertEquals(sfuDTO.sfuId.toString(), sfuEventReport.getSfuId());
//        Assertions.assertEquals(sfuDTO.timeZoneId, callEventReport.);
        Assertions.assertEquals(sfuDTO.joined, sfuEventReport.getTimestamp());
    }
}