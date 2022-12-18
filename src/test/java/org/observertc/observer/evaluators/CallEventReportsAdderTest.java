package org.observertc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.events.CallEventType;
import org.observertc.observer.reports.Report;
import org.observertc.observer.reports.ReportType;
import org.observertc.observer.repositories.ClientsRepository;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.utils.ObservedSamplesGenerator;
import org.observertc.observer.utils.ReportGenerators;
import org.observertc.schemas.reports.CallEventReport;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@MicronautTest(environments = "test")
class CallEventReportsAdderTest {

    @Inject
    CallEntitiesUpdater callEntitiesUpdater;

    @Inject
    CallEventReportsAdder callEventReportsAdder;

    @Inject
    ClientSamplesAnalyser clientSamplesAnalyser;

    @Inject
    ObserverConfig config;

    @Inject
    ClientsRepository clientsRepository;

    private ObservedSamplesGenerator aliceObservedSamplesGenerator;
    private ObservedSamplesGenerator bobObservedSamplesGenerator;

    @BeforeEach
    void setup() {
        this.aliceObservedSamplesGenerator = new ObservedSamplesGenerator();
        this.bobObservedSamplesGenerator = ObservedSamplesGenerator.createSharedRoomGenerator(this.aliceObservedSamplesGenerator);

        this.callEntitiesUpdater.observableClientSamples()
                .subscribe(this.clientSamplesAnalyser::accept);

        this.clientSamplesAnalyser.observableReports()
                .subscribe(this.callEventReportsAdder.reportsObserver());

    }

    @AfterEach
    void teardown() {
        this.clientsRepository.clearStorage();
    }

    @Test
    void shouldForwardCorrectNumberOfReports() throws ExecutionException, InterruptedException, TimeoutException {
        var generatedReport = Report.fromCallMetaReport(new ReportGenerators().generateCallMetaReport());

        var forwardedReports = new LinkedList<Report>();
        this.callEventReportsAdder.observableReports().subscribe(forwardedReports::addAll);
        this.callEventReportsAdder.reportsObserver().onNext(List.of(generatedReport));

        Assertions.assertEquals(1, forwardedReports.size());
    }

    @Test
    void shouldReportStartedCalls() throws ExecutionException, InterruptedException, TimeoutException {
        var observedClientSamples = this.generateObservedClientSamples();
        var reports = new LinkedList<Report>();

        this.callEventReportsAdder.observableReports().subscribe(reports::addAll);
        this.callEntitiesUpdater.accept(observedClientSamples);

        // send samples twice, and only one time it should create new entries
        this.callEntitiesUpdater.accept(observedClientSamples);

        var actualNumberOfReports = reports.stream()
                .filter(r -> ReportType.CALL_EVENT.equals(r.type))
                .map(r -> (CallEventReport) r.payload)
                .filter(r -> CallEventType.CALL_STARTED.name().equals(r.name))
                .count();
        Assertions.assertEquals(1, actualNumberOfReports);
    }

//    @Test
//    void shouldReportStoppedCalls_1() throws ExecutionException, InterruptedException, TimeoutException {
//        var observedClientSamples = this.generateObservedClientSamples();
//        var promise = new CompletableFuture<List<ModifiedStorageEntry<String, Models.Client>>>();
//        var reports = new LinkedList<Report>();
//        this.callEventReportsAdder.observableReports().subscribe(reports::addAll);
//        this.clientsRepository.observableExpiredEntries().subscribe(promise::complete);
//
//        this.callEntitiesUpdater.accept(observedClientSamples);
//        promise.get(config.repository.clientMaxIdleTimeInS * 10, TimeUnit.SECONDS);
//
//        this.callEventReportsAdder.flush();
//
//        var actualNumberOfReports = reports.stream()
//                .filter(r -> ReportType.CALL_EVENT.equals(r.type))
//                .map(r -> (CallEventReport) r.payload)
//                .filter(r -> CallEventType.CALL_ENDED.name().equals(r.name))
//                .count();
//        Assertions.assertEquals(1, actualNumberOfReports);
//    }

    @Test
    void shouldReportJoinedClients() throws ExecutionException, InterruptedException, TimeoutException {
        var observedClientSamples = this.generateObservedClientSamples();
        var reports = new LinkedList<Report>();
        this.callEventReportsAdder.observableReports().subscribe(reports::addAll);

        this.callEntitiesUpdater.accept(observedClientSamples);

        // send samples twice, and only one time it should create new entries
        this.callEntitiesUpdater.accept(observedClientSamples);

        var actualNumberOfReports = reports.stream()
                .filter(r -> ReportType.CALL_EVENT.equals(r.type))
                .map(r -> (CallEventReport) r.payload)
                .filter(r -> CallEventType.CLIENT_JOINED.name().equals(r.name))
                .count();
        Assertions.assertEquals(observedClientSamples.getClientIds().size(), actualNumberOfReports);
    }

//    @Test
//    void shouldReportDetachedClient() throws ExecutionException, InterruptedException, TimeoutException {
//        var observedClientSamples = this.generateObservedClientSamples();
//        var promise = new CompletableFuture<List<ModifiedStorageEntry<String, Models.Client>>>();
//        var reports = new LinkedList<Report>();
//        this.callEventReportsAdder.observableReports().subscribe(reports::addAll);
//        this.clientsRepository.observableDeletedEntries().subscribe(promise::complete);
//
//        this.callEntitiesUpdater.accept(observedClientSamples);
//        promise.get(config.repository.callMaxIdleTimeInS * 10, TimeUnit.SECONDS);
//
//        Thread.sleep(5000);
//
//        this.callEventReportsAdder.flush();
//
//        var actualNumberOfReports = reports.stream()
//                .filter(r -> ReportType.CALL_EVENT.equals(r.type))
//                .map(r -> (CallEventReport) r.payload)
//                .filter(r -> CallEventType.CLIENT_LEFT.name().equals(r.name))
//                .count();
//        Assertions.assertEquals(observedClientSamples.getClientIds().size(), actualNumberOfReports);
//    }

    @Test
    void shouldReportOpenedPeerConnections() throws ExecutionException, InterruptedException, TimeoutException {
        var observedClientSamples = this.generateObservedClientSamples();
        var reports = new LinkedList<Report>();
        this.callEventReportsAdder.observableReports().subscribe(reports::addAll);

        this.callEntitiesUpdater.accept(observedClientSamples);

        // send samples twice, and only one time it should create new entries
        this.callEntitiesUpdater.accept(observedClientSamples);

        var actualNumberOfReports = reports.stream()
                .filter(r -> ReportType.CALL_EVENT.equals(r.type))
                .map(r -> (CallEventReport) r.payload)
                .filter(r -> CallEventType.PEER_CONNECTION_OPENED.name().equals(r.name))
                .count();
        Assertions.assertEquals(observedClientSamples.getPeerConnectionIds().size(), actualNumberOfReports);
    }

//    @Test
//    void shouldReportClosedPeerConnection() throws ExecutionException, InterruptedException, TimeoutException {
//        var observedClientSamples = this.generateObservedClientSamples();
//        var promise = new CompletableFuture<List<ModifiedStorageEntry<String, Models.Client>>>();
//        var reports = new LinkedList<Report>();
//        this.callEventReportsAdder.observableReports().subscribe(reports::addAll);
//        this.clientsRepository.observableDeletedEntries().subscribe(promise::complete);
//
//        this.callEntitiesUpdater.accept(observedClientSamples);
//        promise.get(config.repository.callMaxIdleTimeInS * 10, TimeUnit.SECONDS);
//
//        Thread.sleep(5000);
//
//        this.callEventReportsAdder.flush();
//        var callEventReports = reports.stream()
//                .filter(r -> ReportType.CALL_EVENT.equals(r.type))
//                .map(r -> (CallEventReport) r.payload)
//                .collect(Collectors.toList());
//
//        var actualNumberOfReports = callEventReports.stream()
//                .filter(r -> CallEventType.PEER_CONNECTION_CLOSED.name().equals(r.name))
//                .count();
//        Assertions.assertEquals(observedClientSamples.getPeerConnectionIds().size(), actualNumberOfReports);
//    }

    @Test
    void shouldReportAddedTracks() throws ExecutionException, InterruptedException, TimeoutException {
        var observedClientSamples = this.generateObservedClientSamples();
        var reports = new LinkedList<Report>();
        this.callEventReportsAdder.observableReports().subscribe(reports::addAll);

        this.callEntitiesUpdater.accept(observedClientSamples);

        // send samples twice, and only one time it should create new entries
        this.callEntitiesUpdater.accept(observedClientSamples);

        var actualNumberOfReports = reports.stream()
                .filter(r -> ReportType.CALL_EVENT.equals(r.type))
                .map(r -> (CallEventReport) r.payload)
                .filter(r -> CallEventType.MEDIA_TRACK_ADDED.name().equals(r.name))
                .count();
        Assertions.assertEquals(
                observedClientSamples.getInboundTrackIds().size() +
                        observedClientSamples.getOutboundTrackIds().size(),
                actualNumberOfReports
        );
    }

//    @Test
//    void shouldReportRemovedTracks() throws ExecutionException, InterruptedException, TimeoutException {
//        var observedClientSamples = this.generateObservedClientSamples();
//        var promise = new CompletableFuture<List<ModifiedStorageEntry<String, Models.Client>>>();
//        var reports = new LinkedList<Report>();
//        this.callEventReportsAdder.observableReports().subscribe(reports::addAll);
//        this.clientsRepository.observableDeletedEntries().subscribe(promise::complete);
//
//        this.callEntitiesUpdater.accept(observedClientSamples);
//        promise.get(config.repository.callMaxIdleTimeInS * 10, TimeUnit.SECONDS);
//
//        Thread.sleep(5000);
//
//        this.callEventReportsAdder.flush();
//
//        var actualNumberOfReports = reports.stream()
//                .filter(r -> ReportType.CALL_EVENT.equals(r.type))
//                .map(r -> (CallEventReport) r.payload)
//                .filter(r -> CallEventType.MEDIA_TRACK_REMOVED.name().equals(r.name))
//                .count();
//        Assertions.assertEquals(
//                observedClientSamples.getInboundTrackIds().size() +
//                        observedClientSamples.getOutboundTrackIds().size(),
//                actualNumberOfReports
//        );
//    }

    private ObservedClientSamples generateObservedClientSamples() {
        var callId = UUID.randomUUID().toString();
        var observedAliceSample = aliceObservedSamplesGenerator.generateObservedClientSample(callId);
        var observedBobSample = bobObservedSamplesGenerator.generateObservedClientSample(callId);
        return ObservedClientSamples.builder()
                .addObservedClientSample(observedAliceSample)
                .addObservedClientSample(observedBobSample)
                .build();
    }

}