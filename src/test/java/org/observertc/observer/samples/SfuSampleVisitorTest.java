package org.observertc.observer.samples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

class SfuSampleVisitorTest {
    final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();

    @Test
    void streamTransports() {
        var clientSample = generator.generateObservedSfuSample().getSfuSample();
        var invoked = new AtomicBoolean(false);
        SfuSampleVisitor.streamTransports(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamOutboundRtpPads() {
        var clientSample = generator.generateObservedSfuSample().getSfuSample();
        var invoked = new AtomicBoolean(false);
        SfuSampleVisitor.streamOutboundRtpPads(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamInboundRtpPads() {
        var clientSample = generator.generateObservedSfuSample().getSfuSample();
        var invoked = new AtomicBoolean(false);
        SfuSampleVisitor.streamInboundRtpPads(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamSctpStreams() {
        var clientSample = generator.generateObservedSfuSample().getSfuSample();
        var invoked = new AtomicBoolean(false);
        SfuSampleVisitor.streamSctpStreams(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamExtensionStats() {
        var clientSample = generator.generateObservedSfuSample().getSfuSample();
        var invoked = new AtomicBoolean(false);
        SfuSampleVisitor.streamExtensionStats(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    private<T> Consumer<T> createConsumer(AtomicBoolean atomicBoolean) {
        return input -> {
            atomicBoolean.set(true);
        };
    }
}