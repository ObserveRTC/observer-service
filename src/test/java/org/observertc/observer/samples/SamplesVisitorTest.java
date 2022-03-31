package org.observertc.observer.samples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.ClientSideSamplesGenerator;
import org.observertc.observer.utils.SfuSideSamplesGenerator;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

class SamplesVisitorTest {
    final ClientSideSamplesGenerator clientSideSamplesGenerator = new ClientSideSamplesGenerator();
    final SfuSideSamplesGenerator sfuSideSamplesGenerator = new SfuSideSamplesGenerator();

    @Test
    void streamSfuSamples() {
        var samples = sfuSideSamplesGenerator.get();
        var invoked = new AtomicBoolean(false);
        SamplesVisitor.streamSfuSamples(samples)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamClientSamples() {
        var samples = clientSideSamplesGenerator.get();
        var invoked = new AtomicBoolean(false);
        SamplesVisitor.streamClientSamples(samples)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    private<T> Consumer<T> createConsumer(AtomicBoolean atomicBoolean) {
        return input -> {
            atomicBoolean.set(true);
        };
    }
}