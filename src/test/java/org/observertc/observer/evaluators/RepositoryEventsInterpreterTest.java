package org.observertc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.observertc.observer.dto.*;
import org.observertc.observer.reports.Report;
import org.observertc.observer.repositories.HazelcastMaps;
import org.observertc.observer.utils.DTOGenerators;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RepositoryEventsInterpreterTest {

    @Inject
    HazelcastMaps hazelcastMaps;

    @Inject
    RepositoryEventsInterpreter repositoryEventsInterpreter;

    private static final DTOGenerators dtoGenerators = new DTOGenerators();

    private static final CallDTO callDTO = dtoGenerators.getCallDTO();
    private static final  ClientDTO clientDTO = dtoGenerators.getClientDTO();
    private static final  PeerConnectionDTO peerConnectionDTO = dtoGenerators.getPeerConnectionDTO();
    private static final  MediaTrackDTO mediaTrackDTO = dtoGenerators.getMediaTrackDTO();
    private static final  SfuDTO sfuDTO = dtoGenerators.getSfuDTO();
    private static final  SfuTransportDTO sfuTransportDTO = dtoGenerators.getSfuTransportDTO();
    private static final  SfuRtpPadDTO sfuRtpPadDTO = dtoGenerators.getSfuRtpPadDTO();

    @Test
    @Order(1)
    void shouldGenerateCallStartedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.repositoryEventsInterpreter.observableReports().subscribe(reports -> {
            reportsPromise.complete(reports);
        });

        this.hazelcastMaps.getCalls().put(callDTO.callId, callDTO);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(2)
    void shouldGenerateCallEndedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();

        var subscriber = this.repositoryEventsInterpreter.observableReports().subscribe(reportsPromise::complete);
        this.hazelcastMaps.getCalls().remove(callDTO.callId);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(3)
    void shouldGenerateClientJoinedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.repositoryEventsInterpreter.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getClients().put(clientDTO.clientId, clientDTO);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(4)
    void shouldGenerateClientLeftReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.repositoryEventsInterpreter.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getClients().remove(clientDTO.clientId);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(5)
    void shouldGeneratePeerConnectionsOpenedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.repositoryEventsInterpreter.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getPeerConnections().put(peerConnectionDTO.peerConnectionId, peerConnectionDTO);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(6)
    void shouldGeneratePeerConnectionsClosedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.repositoryEventsInterpreter.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getPeerConnections().remove(peerConnectionDTO.peerConnectionId);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(7)
    void shouldGenerateMediaTrackAddedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.repositoryEventsInterpreter.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getMediaTracks().put(mediaTrackDTO.trackId, mediaTrackDTO);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(8)
    void shouldGenerateMediaTrackRemovedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.repositoryEventsInterpreter.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getMediaTracks().remove(mediaTrackDTO.trackId);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(9)
    void shouldGenerateSfuJoinedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.repositoryEventsInterpreter.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getSFUs().put(sfuDTO.sfuId, sfuDTO);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(10)
    void shouldGenerateSfuLeftReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.repositoryEventsInterpreter.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getSFUs().remove(sfuDTO.sfuId);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(11)
    void shouldGenerateSfuTransportAddedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.repositoryEventsInterpreter.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getSFUTransports().put(sfuTransportDTO.transportId, sfuTransportDTO);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(12)
    void shouldGenerateSfuTransportRemovedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.repositoryEventsInterpreter.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getSFUTransports().remove(sfuTransportDTO.transportId);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(13)
    void shouldGenerateSfuRtpPadAddedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.repositoryEventsInterpreter.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getSFURtpPads().put(sfuRtpPadDTO.rtpPadId, sfuRtpPadDTO);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(14)
    void shouldGenerateSfuRtpPadRemovedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.repositoryEventsInterpreter.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getSFURtpPads().remove(sfuRtpPadDTO.rtpPadId);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }
}