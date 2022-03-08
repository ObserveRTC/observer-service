package org.observertc.observer.evaluators.listeners;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.utils.DTOGenerators;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.schemas.reports.CallEventReport;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
class CallStartedTest {

    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    CallStarted callStarted;

    @Test
    void reportFieldsShouldHaveTheRightValues() throws InterruptedException, ExecutionException, TimeoutException {
        var callEventReportPromise = new CompletableFuture<CallEventReport>();
        var callDTO = dtoGenerators.getCallDTO();
        this.callStarted.getObservableReports().subscribe(callEventReportPromise::complete);

        this.hazelcastMaps.getCalls().put(callDTO.callId, callDTO);
        var callEventReport = callEventReportPromise.get(5, TimeUnit.SECONDS);

        Assertions.assertEquals(CallEventType.CALL_STARTED.name(),callEventReport.getName());
        Assertions.assertEquals(callDTO.callId.toString(), callEventReport.getCallId());
        Assertions.assertEquals(callDTO.roomId, callEventReport.getRoomId());
        Assertions.assertEquals(callDTO.serviceId, callEventReport.getServiceId());
        Assertions.assertEquals(callDTO.started, callEventReport.getTimestamp());
    }
}