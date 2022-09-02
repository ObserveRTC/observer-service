package org.observertc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.observertc.observer.reports.Report;
import org.observertc.observer.utils.ModelsGenerator;
import org.observertc.schemas.dtos.Models;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SfuEventReportsAdderTest {

    @Inject
    SfuEventReportsAdder sfuEventReportsAdder;

    private static final ModelsGenerator MODELS_GENERATOR = new ModelsGenerator();

    private static final Models.Call callDTO = MODELS_GENERATOR.getCallDTO();
    private static final Models.Client clientDTO = MODELS_GENERATOR.getClientModel();
    private static final Models.PeerConnection peerConnectionDTO = MODELS_GENERATOR.getPeerConnectionModel();
    private static final Models.InboundTrack inboundTrack = MODELS_GENERATOR.getInboundTrackModel();
    private static final Models.Sfu sfuDTO = MODELS_GENERATOR.getSfuModel();
    private static final Models.SfuTransport sfuTransportDTO = MODELS_GENERATOR.getSfuTransportModel();
    private static final Models.SfuInboundRtpPad sfuInboundRtpPadDTO = MODELS_GENERATOR.getSfuInboundRtpPad();

    @Test
    @Order(1)
    void shouldGenerateCallStartedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reports -> {
            reportsPromise.complete(reports);
        });

//        this.hazelcastMaps.getCalls().put(callDTO.callId, callDTO);
//        var reports = reportsPromise.get(30, TimeUnit.SECONDS);
//
//        Assertions.assertEquals(1, reports.size());
//        subscriber.dispose();
    }

    @Test
    @Order(2)
    void shouldGenerateCallEndedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();

//        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);
//        this.hazelcastMaps.getCalls().remove(callDTO.callId);
//        var reports = reportsPromise.get(30, TimeUnit.SECONDS);
//
//        Assertions.assertEquals(1, reports.size());
//        subscriber.dispose();
    }

    @Test
    @Order(3)
    void shouldGenerateClientJoinedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);

//        this.hazelcastMaps.getClients().put(clientDTO.clientId, clientDTO);
//        var reports = reportsPromise.get(30, TimeUnit.SECONDS);
//
//        Assertions.assertEquals(1, reports.size());
//        subscriber.dispose();
    }

    @Test
    @Order(4)
    void shouldGenerateClientLeftReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);

//        this.hazelcastMaps.getClients().remove(clientDTO.clientId);
//        var reports = reportsPromise.get(30, TimeUnit.SECONDS);
//
//        Assertions.assertEquals(1, reports.size());
//        subscriber.dispose();
    }

    @Test
    @Order(5)
    void shouldGeneratePeerConnectionsOpenedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);

//        this.hazelcastMaps.getPeerConnections().put(peerConnectionDTO.peerConnectionId, peerConnectionDTO);
//        var reports = reportsPromise.get(30, TimeUnit.SECONDS);
//
//        Assertions.assertEquals(1, reports.size());
//        subscriber.dispose();
    }

    @Test
    @Order(6)
    void shouldGeneratePeerConnectionsClosedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);
//
//        this.hazelcastMaps.getPeerConnections().remove(peerConnectionDTO.peerConnectionId);
//        var reports = reportsPromise.get(30, TimeUnit.SECONDS);
//
//        Assertions.assertEquals(1, reports.size());
//        subscriber.dispose();
    }

    @Test
    @Order(7)
    void shouldGenerateMediaTrackAddedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);
//
//        this.hazelcastMaps.getMediaTracks().put(inboundTrack.trackId, inboundTrack);
//        var reports = reportsPromise.get(30, TimeUnit.SECONDS);
//
//        Assertions.assertEquals(1, reports.size());
//        subscriber.dispose();
    }

    @Test
    @Order(8)
    void shouldGenerateMediaTrackRemovedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);
//
//        this.hazelcastMaps.getMediaTracks().remove(inboundTrack.trackId);
//        var reports = reportsPromise.get(30, TimeUnit.SECONDS);
//
//        Assertions.assertEquals(1, reports.size());
//        subscriber.dispose();
    }

    @Test
    @Order(9)
    void shouldGenerateSfuJoinedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);
//
//        this.hazelcastMaps.getSFUs().put(sfuDTO.sfuId, sfuDTO);
//        var reports = reportsPromise.get(30, TimeUnit.SECONDS);
//
//        Assertions.assertEquals(1, reports.size());
//        subscriber.dispose();
    }

    @Test
    @Order(10)
    void shouldGenerateSfuLeftReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);
//
//        this.hazelcastMaps.getSFUs().remove(sfuDTO.sfuId);
//        var reports = reportsPromise.get(30, TimeUnit.SECONDS);
//
//        Assertions.assertEquals(1, reports.size());
//        subscriber.dispose();
    }

    @Test
    @Order(11)
    void shouldGenerateSfuTransportAddedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);
//
//        this.hazelcastMaps.getSFUTransports().put(sfuTransportDTO.transportId, sfuTransportDTO);
//        var reports = reportsPromise.get(30, TimeUnit.SECONDS);
//
//        Assertions.assertEquals(1, reports.size());
//        subscriber.dispose();
    }

    @Test
    @Order(12)
    void shouldGenerateSfuTransportRemovedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);
//
//        this.hazelcastMaps.getSFUTransports().remove(sfuTransportDTO.transportId);
//        var reports = reportsPromise.get(30, TimeUnit.SECONDS);
//
//        Assertions.assertEquals(1, reports.size());
//        subscriber.dispose();
    }

    @Test
    @Order(13)
    void shouldGenerateSfuRtpPadAddedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);
//
//        this.hazelcastMaps.getSFURtpPads().put(sfuInboundRtpPadDTO.rtpPadId, sfuInboundRtpPadDTO);
//        var reports = reportsPromise.get(30, TimeUnit.SECONDS);
//
//        Assertions.assertEquals(1, reports.size());
//        subscriber.dispose();
    }

    @Test
    @Order(14)
    void shouldGenerateSfuRtpPadRemovedReports() throws ExecutionException, InterruptedException, TimeoutException {
        var reportsPromise = new CompletableFuture<List<Report>>();
        var subscriber = this.sfuEventReportsAdder.observableReports().subscribe(reportsPromise::complete);
//
//        this.hazelcastMaps.getSFURtpPads().remove(sfuInboundRtpPadDTO.rtpPadId);
//        var reports = reportsPromise.get(30, TimeUnit.SECONDS);
//
//        Assertions.assertEquals(1, reports.size());
//        subscriber.dispose();
    }
}