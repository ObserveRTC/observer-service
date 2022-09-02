package org.observertc.observer.samples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.ObservedSamplesGenerator;
import org.observertc.observer.utils.TestUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

class ObservedSfuSamplesTest {

    final ObservedSamplesGenerator observedSamplesGenerator = new ObservedSamplesGenerator();

    @Test
    void getTransportIds() {
        var observedSample = observedSamplesGenerator.generateObservedSfuSample();
        var expectedIds = SfuSampleVisitor
                .streamTransports(observedSample.getSfuSample())
                .map(transport -> transport.transportId)
                .collect(Collectors.toSet());
        var observedSamples = ObservedSfuSamples.builder()
                .addObservedSfuSample(observedSample)
                .build();

        boolean equals = TestUtils.equalSets(expectedIds, observedSamples.getTransportIds());
        Assertions.assertTrue(equals);
    }

    @Test
    void getRtpPadIds() {
        var observedSample = observedSamplesGenerator.generateObservedSfuSample();
        var expectedIds = new HashSet<UUID>();
        SfuSampleVisitor.streamInboundRtpPads(observedSample.getSfuSample())
                .map(rtpPad -> rtpPad.padId)
                .forEach(expectedIds::add);
        SfuSampleVisitor.streamOutboundRtpPads(observedSample.getSfuSample())
                .map(rtpPad -> rtpPad.padId)
                .forEach(expectedIds::add);
        var observedSamples = ObservedSfuSamples.builder()
                .addObservedSfuSample(observedSample)
                .build();

        boolean equals = TestUtils.equalSets(expectedIds, observedSamples.getRtpPadIds());
        Assertions.assertTrue(equals);
    }

    @Test
    void getChannelIds() {
        var observedSample = observedSamplesGenerator.generateObservedSfuSample();
        var expectedIds = new HashSet<UUID>();
        SfuSampleVisitor.streamSctpStreams(observedSample.getSfuSample())
                .map(rtpPad -> rtpPad.channelId)
                .forEach(expectedIds::add);
        var observedSamples = ObservedSfuSamples.builder()
                .addObservedSfuSample(observedSample)
                .build();

        boolean equals = TestUtils.equalSets(expectedIds, observedSamples.getSctpStreamIds());
        Assertions.assertTrue(equals);
    }

    @Test
    void getSfuIds() {
        var observedSample = observedSamplesGenerator.generateObservedSfuSample();
        var expectedIds = Set.of(observedSample.getSfuSample().sfuId);
        var observedSamples = ObservedSfuSamples.builder()
                .addObservedSfuSample(observedSample)
                .build();

        boolean equals = TestUtils.equalSets(expectedIds, observedSamples.getSfuIds());
        Assertions.assertTrue(equals);
    }
}