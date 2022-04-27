package org.observertc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.reports.Report;
import org.observertc.observer.samples.ObservedSfuSamples;
import org.observertc.observer.utils.ObservedSamplesGenerator;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest
class SfuSamplesAnalyserTest {

    @Inject
    SfuSamplesAnalyser sfuSamplesAnalyser;

    ObservedSamplesGenerator observedSamplesGenerator = new ObservedSamplesGenerator();

    @Test
    void shouldCreateReports() throws ExecutionException, InterruptedException, TimeoutException {
        this.sfuSamplesAnalyser.config.dropUnmatchedOutboundReports = false;
        this.sfuSamplesAnalyser.config.dropUnmatchedInboundReports = false;
        var observedSfuSample = observedSamplesGenerator.generateObservedSfuSample();
        var observedSfuSamples = ObservedSfuSamples.builder().addObservedSfuSample(observedSfuSample).build();
        var reportsPromise = new CompletableFuture<List<Report>>();
        var sfuSample = observedSfuSample.getSfuSample();
        int expectedNumberOfReports =
                getArrayLength(sfuSample.transports) +
                        getArrayLength(sfuSample.inboundRtpPads) +
                        getArrayLength(sfuSample.outboundRtpPads) +
                        getArrayLength(sfuSample.sctpChannels) +
                        getArrayLength(sfuSample.extensionStats) +
                        0
                ;

        this.sfuSamplesAnalyser.observableReports().subscribe(reportsPromise::complete);
        this.sfuSamplesAnalyser.accept(observedSfuSamples);
        var reports = reportsPromise.get(5, TimeUnit.SECONDS);

        Assertions.assertEquals(expectedNumberOfReports, reports.size(), "Number of reports");
    }


    @Test
    void shouldNotCreateUnmatchedReports() throws ExecutionException, InterruptedException, TimeoutException {
        this.sfuSamplesAnalyser.config.dropUnmatchedOutboundReports = true;
        this.sfuSamplesAnalyser.config.dropUnmatchedInboundReports = true;
        var observedSfuSample = observedSamplesGenerator.generateObservedSfuSample();
        var observedSfuSamples = ObservedSfuSamples.builder().addObservedSfuSample(observedSfuSample).build();
        var reportsPromise = new CompletableFuture<List<Report>>();
        var sfuSample = observedSfuSample.getSfuSample();
        int expectedNumberOfReports =
                getArrayLength(sfuSample.transports) +
//                        getArrayLength(sfuSample.inboundRtpPads) +
//                        getArrayLength(sfuSample.outboundRtpPads) +
                        getArrayLength(sfuSample.sctpChannels) +
                        getArrayLength(sfuSample.extensionStats) +
                        0
                ;

        this.sfuSamplesAnalyser.observableReports().subscribe(reportsPromise::complete);
        this.sfuSamplesAnalyser.accept(observedSfuSamples);
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