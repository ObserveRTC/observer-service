package org.observertc.observer.components;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.reports.Report;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.samples.ObservedSamplesGenerator;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
class ClientSamplesAnalyzerTest {

    @Inject
    ClientSamplesAnalyzer clientSamplesAnalyzer;

    ObservedSamplesGenerator observedSamplesGenerator = new ObservedSamplesGenerator();

    @Test
    void shouldCreateReports() throws ExecutionException, InterruptedException, TimeoutException {
        var observedClientSample = observedSamplesGenerator.generateObservedClientSample();
        var observedClientSamples = ObservedClientSamples.builder().addObservedClientSample(observedClientSample).build();
        var reportsPromise = new CompletableFuture<List<Report>>();
        var clientSample = observedClientSample.getClientSample();
        int expectedNumberOfReports = getOneIfNotNull(clientSample.engine) +
                getOneIfNotNull(clientSample.platform) +
                getOneIfNotNull(clientSample.browser) +
                getOneIfNotNull(clientSample.os) +
                getArrayLength(clientSample.mediaConstraints) +
                getArrayLength(clientSample.mediaDevices) +
                getArrayLength(clientSample.userMediaErrors) +
                getArrayLength(clientSample.extensionStats) +
                getArrayLength(clientSample.iceServers) +
                getArrayLength(clientSample.pcTransports) +
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

        this.clientSamplesAnalyzer.observableReports().subscribe(reportsPromise::complete);
        this.clientSamplesAnalyzer.accept(observedClientSamples);
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