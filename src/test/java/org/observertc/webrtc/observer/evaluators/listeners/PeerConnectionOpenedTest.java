package org.observertc.webrtc.observer.evaluators.listeners;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.common.CallEventType;
import org.observertc.webrtc.observer.repositories.HazelcastMaps;
import org.observertc.webrtc.observer.utils.DTOGenerators;
import org.observertc.webrtc.schemas.reports.CallEventReport;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
class PeerConnectionOpenedTest {

    @Inject
    DTOGenerators dtoGenerators;

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    PeerConnectionOpened peerConnectionOpened;

    @Test
    void reportFieldsShouldHaveTheRightValues() throws InterruptedException, ExecutionException, TimeoutException {
        var callEventReportPromise = new CompletableFuture<CallEventReport>();
        var peerConnectionDTO = dtoGenerators.getPeerConnectionDTO();

        this.peerConnectionOpened.getObservableReports().subscribe(callEventReportPromise::complete);
        this.hazelcastMaps.getPeerConnections().put(peerConnectionDTO.peerConnectionId, peerConnectionDTO);
        var callEventReport = callEventReportPromise.get(5, TimeUnit.SECONDS);

        Assertions.assertEquals(CallEventType.PEER_CONNECTION_OPENED.name(), callEventReport.getName());
        Assertions.assertEquals(peerConnectionDTO.serviceId, callEventReport.getServiceId());
        Assertions.assertEquals(peerConnectionDTO.roomId, callEventReport.getRoomId());
        Assertions.assertEquals(peerConnectionDTO.callId.toString(), callEventReport.getCallId());

        Assertions.assertEquals(peerConnectionDTO.mediaUnitId, callEventReport.getMediaUnitId());
        Assertions.assertEquals(peerConnectionDTO.userId, callEventReport.getUserId());

        Assertions.assertEquals(peerConnectionDTO.clientId.toString(), callEventReport.getClientId());
        Assertions.assertEquals(peerConnectionDTO.peerConnectionId.toString(), callEventReport.getPeerConnectionId());
        Assertions.assertEquals(peerConnectionDTO.created, callEventReport.getTimestamp());
    }
}