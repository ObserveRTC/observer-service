package org.observertc.observer.samples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;


class ClientSampleVisitorTest {

    final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();

    @Test
    void streamCertificates() {
        var clientSample = generator.generateObservedClientSample().getClientSample();
        var invoked = new AtomicBoolean(false);
        ClientSampleVisitor.streamCertificates(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamCodecs() {
        var clientSample = generator.generateObservedClientSample().getClientSample();
        var invoked = new AtomicBoolean(false);
        ClientSampleVisitor.streamCodecs(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamDataChannels() {
        var clientSample = generator.generateObservedClientSample().getClientSample();
        var invoked = new AtomicBoolean(false);
        ClientSampleVisitor.streamDataChannels(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamExtensionStats() {
        var clientSample = generator.generateObservedClientSample().getClientSample();
        var invoked = new AtomicBoolean(false);
        ClientSampleVisitor.streamExtensionStats(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamIceLocalCandidates() {
        var clientSample = generator.generateObservedClientSample().getClientSample();
        var invoked = new AtomicBoolean(false);
        ClientSampleVisitor.streamIceLocalCandidates(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamIceRemoteCandidates() {
        var clientSample = generator.generateObservedClientSample().getClientSample();
        var invoked = new AtomicBoolean(false);
        ClientSampleVisitor.streamIceRemoteCandidates(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamIceServers() {
        var clientSample = generator.generateObservedClientSample().getClientSample();
        var invoked = new AtomicBoolean(false);
        ClientSampleVisitor.streamIceServers(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamInboundAudioTracks() {
        var clientSample = generator.generateObservedClientSample().getClientSample();
        var invoked = new AtomicBoolean(false);
        ClientSampleVisitor.streamInboundAudioTracks(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamInboundVideoTracks() {
        var clientSample = generator.generateObservedClientSample().getClientSample();
        var invoked = new AtomicBoolean(false);
        ClientSampleVisitor.streamInboundVideoTracks(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamOutboundAudioTracks() {
        var clientSample = generator.generateObservedClientSample().getClientSample();
        var invoked = new AtomicBoolean(false);
        ClientSampleVisitor.streamOutboundAudioTracks(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamOutboundVideoTracks() {
        var clientSample = generator.generateObservedClientSample().getClientSample();
        var invoked = new AtomicBoolean(false);
        ClientSampleVisitor.streamOutboundVideoTracks(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamMediaConstraints() {
        var clientSample = generator.generateObservedClientSample().getClientSample();
        var invoked = new AtomicBoolean(false);
        ClientSampleVisitor.streamMediaConstraints(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamMediaDevices() {
        var clientSample = generator.generateObservedClientSample().getClientSample();
        var invoked = new AtomicBoolean(false);
        ClientSampleVisitor.streamMediaDevices(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamMediaSources() {
        var clientSample = generator.generateObservedClientSample().getClientSample();
        var invoked = new AtomicBoolean(false);
        ClientSampleVisitor.streamMediaSources(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamPeerConnectionTransports() {
        var clientSample = generator.generateObservedClientSample().getClientSample();
        var invoked = new AtomicBoolean(false);
        ClientSampleVisitor.streamPeerConnectionTransports(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    @Test
    void streamUserMediaErrors() {
        var clientSample = generator.generateObservedClientSample().getClientSample();
        var invoked = new AtomicBoolean(false);
        ClientSampleVisitor.streamUserMediaErrors(clientSample)
                .forEach(this.createConsumer(invoked));

        Assertions.assertTrue(invoked.get());
    }

    private<T> Consumer<T> createConsumer(AtomicBoolean atomicBoolean) {
        return input -> {
            atomicBoolean.set(true);
        };
    }
}