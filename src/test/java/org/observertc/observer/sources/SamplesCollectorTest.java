package org.observertc.observer.sources;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.samples.ObservedClientSamples;
import org.observertc.observer.samples.ObservedSfuSamples;
import org.observertc.observer.utils.ClientSideSamplesGenerator;
import org.observertc.observer.utils.SfuSideSamplesGenerator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MicronautTest(environments={"test"})
class SamplesCollectorTest {

    @Inject
    SamplesCollector samplesCollector;

    ClientSideSamplesGenerator clientSideSamplesGenerator = new ClientSideSamplesGenerator();
    SfuSideSamplesGenerator sfuSideSamplesGenerator = new SfuSideSamplesGenerator();

    @Test
    void observableClientSamples() throws ExecutionException, InterruptedException, TimeoutException {
        var receivedSamples = ReceivedSamples.of("service", "mediaUnit", clientSideSamplesGenerator.get());
        var emitted = new CompletableFuture<ObservedClientSamples>();
        samplesCollector.observableClientSamples().subscribe(emitted::complete);
        samplesCollector.accept(receivedSamples);
        samplesCollector.teardown();
        var actual = emitted.get(60, TimeUnit.SECONDS).stream().findFirst().get();
        Assertions.assertEquals(receivedSamples.samples.clientSamples[0], actual.getClientSample());
    }

    @Test
    void observableSfuSamples() throws ExecutionException, InterruptedException, TimeoutException {
        var receivedSamples = ReceivedSamples.of("service", "mediaUnit", sfuSideSamplesGenerator.get());
        var emitted = new CompletableFuture<ObservedSfuSamples>();
        samplesCollector.observableSfuSamples().subscribe(emitted::complete);
        samplesCollector.accept(receivedSamples);
        samplesCollector.teardown();
        var actual = emitted.get(60, TimeUnit.SECONDS).stream().findFirst().get();
        Assertions.assertEquals(receivedSamples.samples.sfuSamples[0], actual.getSfuSample());
    }
}