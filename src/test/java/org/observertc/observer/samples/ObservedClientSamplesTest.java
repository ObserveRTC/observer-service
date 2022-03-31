package org.observertc.observer.samples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.TestUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

class ObservedClientSamplesTest {

    final ObservedSamplesGenerator observedSamplesGenerator = new ObservedSamplesGenerator();

    @Test
    void getClientIds() {
        var observedSample = observedSamplesGenerator.generateObservedClientSample();
        var expectedIds = Set.of(observedSample.getClientSample().clientId);
        var observedSamples = ObservedClientSamples.builder()
                .addObservedClientSample(observedSample)
                .build();

        boolean equals = TestUtils.equalSets(expectedIds, observedSamples.getClientIds());
        Assertions.assertTrue(equals);
    }

    @Test
    void getServiceRoomIds() {
        var observedSample = observedSamplesGenerator.generateObservedClientSample();
        var expectedIds = Set.of(observedSample.getServiceRoomId());
        var observedSamples = ObservedClientSamples.builder()
                .addObservedClientSample(observedSample)
                .build();

        boolean equals = TestUtils.equalSets(expectedIds, observedSamples.getServiceRoomIds());
        Assertions.assertTrue(equals);
    }

    @Test
    void getPeerConnectionIds() {
        var observedSample = observedSamplesGenerator.generateObservedClientSample();
        var expectedIds = ClientSampleVisitor
                .streamPeerConnectionTransports(observedSample.getClientSample())
                .map(transport -> transport.peerConnectionId)
                .collect(Collectors.toSet());
        var observedSamples = ObservedClientSamples.builder()
                .addObservedClientSample(observedSample)
                .build();

        boolean equals = TestUtils.equalSets(expectedIds, observedSamples.getPeerConnectionIds());
        Assertions.assertTrue(equals);
    }

    @Test
    void getMediaTrackIds() {
        var observedSample = observedSamplesGenerator.generateObservedClientSample();
        var expectedIds = new HashSet<UUID>();
        ClientSampleVisitor.streamInboundAudioTracks(observedSample.getClientSample())
                .map(track -> track.trackId)
                .forEach(expectedIds::add);
        ClientSampleVisitor.streamInboundVideoTracks(observedSample.getClientSample())
                .map(track -> track.trackId)
                .forEach(expectedIds::add);
        ClientSampleVisitor.streamOutboundAudioTracks(observedSample.getClientSample())
                .map(track -> track.trackId)
                .forEach(expectedIds::add);
        ClientSampleVisitor.streamOutboundVideoTracks(observedSample.getClientSample())
                .map(track -> track.trackId)
                .forEach(expectedIds::add);
        var observedSamples = ObservedClientSamples.builder()
                .addObservedClientSample(observedSample)
                .build();

        boolean equals = TestUtils.equalSets(expectedIds, observedSamples.getMediaTrackIds());
        Assertions.assertTrue(equals);
    }


}