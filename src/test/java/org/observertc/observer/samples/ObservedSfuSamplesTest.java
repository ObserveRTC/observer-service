package org.observertc.observer.samples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.observer.utils.ObservedSamplesGenerator;
import org.observertc.observer.utils.TestUtils;

import java.util.HashSet;
import java.util.Set;
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
        var expectedInboundPadIds = new HashSet<String>();
        var expectedOutboundPadIds = new HashSet<String>();
        SfuSampleVisitor.streamInboundRtpPads(observedSample.getSfuSample())
                .map(rtpPad -> rtpPad.padId)
                .forEach(expectedInboundPadIds::add);
        SfuSampleVisitor.streamOutboundRtpPads(observedSample.getSfuSample())
                .map(rtpPad -> rtpPad.padId)
                .forEach(expectedOutboundPadIds::add);
        var observedSamples = ObservedSfuSamples.builder()
                .addObservedSfuSample(observedSample)
                .build();

        boolean equals_inbound = TestUtils.equalSets(expectedInboundPadIds, observedSamples.getInboundRtpPadIds());
        Assertions.assertTrue(equals_inbound);

        boolean equals_outbound = TestUtils.equalSets(expectedOutboundPadIds, observedSamples.getOutboundRtpPadIds());
        Assertions.assertTrue(equals_outbound);
    }

    @Test
    void getChannelIds() {
        var observedSample = observedSamplesGenerator.generateObservedSfuSample();
        var expectedIds = new HashSet<String>();
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