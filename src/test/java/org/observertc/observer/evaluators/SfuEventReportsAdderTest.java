package org.observertc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.observertc.observer.reports.Report;
import org.observertc.observer.utils.ModelsGenerator;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SfuEventReportsAdderTest {

    @Inject
    HamokStorages hazelcastMaps;

    @Inject
    SfuEventReportsAdder sfuEventReportsAdder;

    private static final ModelsGenerator MODELS_GENERATOR = new ModelsGenerator();

    private static final CallDTO callDTO = MODELS_GENERATOR.getCallDTO();
    private static final  ClientDTO clientDTO = MODELS_GENERATOR.getClientDTO();
    private static final  PeerConnectionDTO peerConnectionDTO = MODELS_GENERATOR.getPeerConnectionDTO();
    private static final  MediaTrackDTO mediaTrackDTO = MODELS_GENERATOR.getMediaTrackDTO();
    private static final  SfuDTO sfuDTO = MODELS_GENERATOR.getSfuDTO();
    private static final  SfuTransportDTO sfuTransportDTO = MODELS_GENERATOR.getSfuTransportDTO();
    private static final  SfuRtpPadDTO sfuRtpPadDTO = MODELS_GENERATOR.getSfuRtpPadDTO();

    @Test
    @Order(1)
    void shouldGenerateCallStartedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reports -> {
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

        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);
        this.hazelcastMaps.getCalls().remove(callDTO.callId);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(3)
    void shouldGenerateClientJoinedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getClients().put(clientDTO.clientId, clientDTO);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(4)
    void shouldGenerateClientLeftReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getClients().remove(clientDTO.clientId);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(5)
    void shouldGeneratePeerConnectionsOpenedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getPeerConnections().put(peerConnectionDTO.peerConnectionId, peerConnectionDTO);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(6)
    void shouldGeneratePeerConnectionsClosedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getPeerConnections().remove(peerConnectionDTO.peerConnectionId);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(7)
    void shouldGenerateMediaTrackAddedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getMediaTracks().put(mediaTrackDTO.trackId, mediaTrackDTO);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(8)
    void shouldGenerateMediaTrackRemovedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getMediaTracks().remove(mediaTrackDTO.trackId);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(9)
    void shouldGenerateSfuJoinedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getSFUs().put(sfuDTO.sfuId, sfuDTO);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(10)
    void shouldGenerateSfuLeftReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getSFUs().remove(sfuDTO.sfuId);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(11)
    void shouldGenerateSfuTransportAddedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getSFUTransports().put(sfuTransportDTO.transportId, sfuTransportDTO);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(12)
    void shouldGenerateSfuTransportRemovedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getSFUTransports().remove(sfuTransportDTO.transportId);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(13)
    void shouldGenerateSfuRtpPadAddedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getSFURtpPads().put(sfuRtpPadDTO.rtpPadId, sfuRtpPadDTO);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }

    @Test
    @Order(14)
    void shouldGenerateSfuRtpPadRemovedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);

        this.hazelcastMaps.getSFURtpPads().remove(sfuRtpPadDTO.rtpPadId);
        var reports = reportsPromise.get(30, TimeUnit.SECONDS);

        Assertions.assertEquals(1, reports.size());
        subscriber.dispose();
    }
}