package org.observertc.observer.evaluators.listeners;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOGenerators;
import org.observertc.schemas.reports.CallEventReport;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
class CallEndedTest {

    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    CallEnded callEnded;

    @Test
    void reportFieldsShouldHaveTheRightValues() throws InterruptedException, ExecutionException, TimeoutException {
        var callEventReportPromise = new CompletableFuture<List<CallEventReport>>();
        var callDTO = dtoGenerators.getCallDTO();
        this.callEnded.getObservableReports().subscribe(callEventReportPromise::complete);

        this.hazelcastMaps.getCalls().put(callDTO.callId, callDTO);
        this.hazelcastMaps.getCalls().remove(callDTO.callId);
        var callEventReports = callEventReportPromise.get(5, TimeUnit.SECONDS);
        var callEventReport = callEventReports.get(0);
        var timestampThreshold = Instant.now().plusSeconds(5).toEpochMilli();

        Assertions.assertEquals(CallEventType.CALL_ENDED.name(), callEventReport.name);
        Assertions.assertEquals(callDTO.callId.toString(), callEventReport.callId);
        Assertions.assertEquals(callDTO.roomId, callEventReport.roomId);
        Assertions.assertEquals(callDTO.serviceId, callEventReport.serviceId);
        Assertions.assertTrue(callEventReport.timestamp < timestampThreshold);
    }


}