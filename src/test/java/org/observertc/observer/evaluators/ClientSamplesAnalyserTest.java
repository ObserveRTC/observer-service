package org.observertc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.reports.Report;
import org.observertc.observer.reports.ReportType;
import org.observertc.observer.repositories.HamokStorages;
import org.observertc.observer.samples.ClientSampleVisitor;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.utils.ObservedSamplesGenerator;
import org.observertc.observer.utils.SamplesGeneratorForSingleSfu;
import org.observertc.schemas.reports.InboundAudioTrackReport;
import org.observertc.schemas.reports.InboundVideoTrackReport;
import org.observertc.schemas.reports.OutboundAudioTrackReport;
import org.observertc.schemas.reports.OutboundVideoTrackReport;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@MicronautTest
class ClientSamplesAnalyserTest {

    @Inject
    ClientSamplesAnalyser clientSamplesAnalyser;

    @Inject
    HamokStorages hamokStorages;

    ObservedSamplesGenerator observedSamplesGenerator = new ObservedSamplesGenerator();

    @Test
    void shouldCreateReports() throws ExecutionException, InterruptedException, TimeoutException {
        this.clientSamplesAnalyser.config.dropUnmatchedReports = false;
        var observedClientSample = observedSamplesGenerator.generateObservedClientSample();
        var observedClientSamples = ObservedClientSamples.builder().addObservedClientSample(observedClientSample).build();
        var reportsPromise = new CompletableFuture<List<Report>>();
        var clientSample = observedClientSample.getClientSample();
        clientSample.callId = UUID.randomUUID().toString();

        int expectedNumberOfReports = getOneIfNotNull(clientSample.engine) +
                getOneIfNotNull(clientSample.platform) +
                getOneIfNotNull(clientSample.browser) +
                getOneIfNotNull(clientSample.os) +
                getArrayLength(clientSample.mediaConstraints) +
                getArrayLength(clientSample.mediaDevices) +
                getArrayLength(clientSample.userMediaErrors) +
                getArrayLength(clientSample.localSDPs) +
                getArrayLength(clientSample.extensionStats) +
                getArrayLength(clientSample.iceServers) +
                getArrayLength(clientSample.pcTransports) +
                getArrayLength(clientSample.iceCandidatePairs) +
                getArrayLength(clientSample.mediaSources) +
                getArrayLength(clientSample.codecs) +
                getArrayLength(clientSample.certificates) +
                getArrayLength(clientSample.inboundAudioTracks) +
                getArrayLength(clientSample.inboundVideoTracks) +
                getArrayLength(clientSample.outboundAudioTracks) +
                getArrayLength(clientSample.outboundVideoTracks) +
                getArrayLength(clientSample.iceLocalCandidates) +
                getArrayLength(clientSample.iceRemoteCandidates) +
                getArrayLength(clientSample.dataChannels) +
                0
                ;

        this.clientSamplesAnalyser.observableReports().subscribe(reportsPromise::complete);
        this.clientSamplesAnalyser.accept(observedClientSamples);
        var reports = reportsPromise.get(5, TimeUnit.SECONDS);

        Assertions.assertEquals(expectedNumberOfReports, reports.size(), "Number of reports");
    }


    @Test
    void shouldMatchReports() throws ExecutionException, InterruptedException, TimeoutException {
        this.clientSamplesAnalyser.config.dropUnmatchedReports = false;
        var samplesGeneratorForSingleSfu = new SamplesGeneratorForSingleSfu();
        samplesGeneratorForSingleSfu.saveTo(this.hamokStorages);
        var observedClientSamples = samplesGeneratorForSingleSfu.getObservedClientSamples();

        var reports = new LinkedList<Report>();
        this.clientSamplesAnalyser.observableReports().subscribe(reports::addAll);
        this.clientSamplesAnalyser.accept(observedClientSamples);

        var inboundAudioReports = reports.stream()
                .filter(report -> ReportType.INBOUND_AUDIO_TRACK.equals(report.type))
                .map(report -> (InboundAudioTrackReport) report.payload)
                .collect(Collectors.toList());
        var inboundVideoReports = reports.stream()
                .filter(report -> ReportType.INBOUND_VIDEO_TRACK.equals(report.type))
                .map(report -> (InboundVideoTrackReport) report.payload)
                .collect(Collectors.toList());
        var outboundAudioReports = reports.stream()
                .filter(report -> ReportType.OUTBOUND_AUDIO_TRACK.equals(report.type))
                .map(report -> (OutboundAudioTrackReport) report.payload)
                .collect(Collectors.toList());
        var outboundVideoReports = reports.stream()
                .filter(report -> ReportType.OUTBOUND_VIDEO_TRACK.equals(report.type))
                .map(report -> (OutboundVideoTrackReport) report.payload)
                .collect(Collectors.toList());

        for (var inboundAudioReport : inboundAudioReports) {
            var found = false;
            for (var outboundAudioReport : outboundAudioReports) {
                if (outboundAudioReport.sfuStreamId == inboundAudioReport.sfuStreamId) {
                    found = true;
                }
            }
            Assertions.assertTrue(found);
        }
        for (var outboundVideoReport : outboundVideoReports) {
            var found = false;
            for (var inboundVideoReport : inboundVideoReports) {
                if (inboundVideoReport.sfuStreamId == outboundVideoReport.sfuStreamId) {
                    found = true;
                }
            }
            Assertions.assertTrue(found);
        }
    }

    @Test
    void shouldNotCreateUnMatchedReports() throws ExecutionException, InterruptedException, TimeoutException {
        this.clientSamplesAnalyser.config.dropUnmatchedReports = true;
        var observedClientSample = observedSamplesGenerator.generateObservedClientSample();
        var observedClientSamples = ObservedClientSamples.builder().addObservedClientSample(observedClientSample).build();
        var reportsPromise = new CompletableFuture<List<Report>>();
        var clientSample = observedClientSample.getClientSample();

        clientSample.callId = UUID.randomUUID().toString();
        ClientSampleVisitor.streamInboundAudioTracks(observedClientSample.getClientSample()).forEach(track -> track.sfuSinkId = UUID.randomUUID().toString());
        ClientSampleVisitor.streamInboundVideoTracks(observedClientSample.getClientSample()).forEach(track -> track.sfuSinkId = UUID.randomUUID().toString());
        int expectedNumberOfReports = getOneIfNotNull(clientSample.engine) +
                getOneIfNotNull(clientSample.platform) +
                getOneIfNotNull(clientSample.browser) +
                getOneIfNotNull(clientSample.os) +
                getArrayLength(clientSample.mediaConstraints) +
                getArrayLength(clientSample.mediaDevices) +
                getArrayLength(clientSample.userMediaErrors) +
                getArrayLength(clientSample.localSDPs) +
                getArrayLength(clientSample.extensionStats) +
                getArrayLength(clientSample.iceServers) +
                getArrayLength(clientSample.pcTransports) +
                getArrayLength(clientSample.iceCandidatePairs) +
                getArrayLength(clientSample.mediaSources) +
                getArrayLength(clientSample.codecs) +
                getArrayLength(clientSample.certificates) +
//                getArrayLength(clientSample.inboundAudioTracks) +
//                getArrayLength(clientSample.inboundVideoTracks) +
                getArrayLength(clientSample.outboundAudioTracks) +
                getArrayLength(clientSample.outboundVideoTracks) +
                getArrayLength(clientSample.iceLocalCandidates) +
                getArrayLength(clientSample.iceRemoteCandidates) +
                getArrayLength(clientSample.dataChannels) +
                0
                ;

        this.clientSamplesAnalyser.observableReports().subscribe(reportsPromise::complete);
        this.clientSamplesAnalyser.accept(observedClientSamples);
        var reports = reportsPromise.get(5, TimeUnit.SECONDS);

        Assertions.assertEquals(expectedNumberOfReports, reports.size(), "Number of reports");
    }

    static <T> int getArrayLength(T[] array) {
        if (Objects.isNull(array)) return 0;
        return array.length;
    }

    static<T> int getOneIfNotNull(T obj) {
        return Objects.isNull(obj) ? 0 : 1;
    }
}