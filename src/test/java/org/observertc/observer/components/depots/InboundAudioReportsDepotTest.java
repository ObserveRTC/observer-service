package org.observertc.observer.components.depots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.observertc.observer.simulator.ObservedSamplesGenerator;
import org.observertc.observer.utils.RandomGenerators;

import java.util.UUID;

class InboundAudioReportsDepotTest {
    private final RandomGenerators randomGenerators = new RandomGenerators();
    private final ObservedSamplesGenerator generator = new ObservedSamplesGenerator();
    private InboundAudioReportsDepot depot = new InboundAudioReportsDepot();

    @Test
    @Order(1)
    void shouldMakeDTO() {
        var callId = UUID.randomUUID();
        var observedClientSample = this.generator.generateObservedClientSample(callId);
        var clientSample = observedClientSample.getClientSample();
        var remoteClientId = UUID.randomUUID();
        var remotePeerConnectionId = UUID.randomUUID();
        var remoteTrackId = UUID.randomUUID();
        var remoteUserId = randomGenerators.getRandomTestUserIds();
        var inboundAudioTrack = clientSample.inboundAudioTracks[0];
        this.depot
                .setInboundAudioTrack(inboundAudioTrack)
                .setObservedClientSample(observedClientSample)
                .setRemoteClientId(remoteClientId)
                .setRemotePeerConnectionId(remotePeerConnectionId)
                .setRemoteTrackId(remoteTrackId)
                .setRemoteUserId(remoteUserId)
                .assemble();
        var inboundAudioTrackReport = depot.get().get(0);

        Assertions.assertEquals(inboundAudioTrackReport.serviceId, observedClientSample.getServiceId());
        Assertions.assertEquals(inboundAudioTrackReport.mediaUnitId, observedClientSample.getMediaUnitId());
        Assertions.assertEquals(inboundAudioTrackReport.marker, clientSample.marker);
        Assertions.assertEquals(inboundAudioTrackReport.timestamp, clientSample.timestamp);

        Assertions.assertEquals(inboundAudioTrackReport.callId, callId);
        Assertions.assertEquals(inboundAudioTrackReport.roomId, clientSample.roomId);
        Assertions.assertEquals(inboundAudioTrackReport.clientId, clientSample.clientId.toString());
        Assertions.assertEquals(inboundAudioTrackReport.userId, clientSample.userId);
        Assertions.assertEquals(inboundAudioTrackReport.peerConnectionId, inboundAudioTrack.peerConnectionId.toString());
//                       .setLabel(inboundAudioTrack.)
        Assertions.assertEquals(inboundAudioTrackReport.trackId, inboundAudioTrack.trackId.toString());

        Assertions.assertEquals(inboundAudioTrackReport.remoteClientId, remoteClientId);
        Assertions.assertEquals(inboundAudioTrackReport.remoteUserId, remoteUserId);
        Assertions.assertEquals(inboundAudioTrackReport.remotePeerConnectionId, remotePeerConnectionId);
        Assertions.assertEquals(inboundAudioTrackReport.remoteTrackId, remoteTrackId);

        Assertions.assertEquals(inboundAudioTrackReport.sampleSeq, clientSample.sampleSeq);


        Assertions.assertEquals(inboundAudioTrackReport.ssrc, inboundAudioTrack.ssrc);
        Assertions.assertEquals(inboundAudioTrackReport.packetsReceived, inboundAudioTrack.packetsReceived);
        Assertions.assertEquals(inboundAudioTrackReport.packetsSent, inboundAudioTrack.packetsSent);
        Assertions.assertEquals(inboundAudioTrackReport.packetsLost, inboundAudioTrack.packetsLost);
        Assertions.assertEquals(inboundAudioTrackReport.jitter, inboundAudioTrack.jitter);
        Assertions.assertEquals(inboundAudioTrackReport.packetsDiscarded, inboundAudioTrack.packetsDiscarded);
        Assertions.assertEquals(inboundAudioTrackReport.packetsRepaired, inboundAudioTrack.packetsRepaired);
        Assertions.assertEquals(inboundAudioTrackReport.burstPacketsLost, inboundAudioTrack.burstPacketsLost);
        Assertions.assertEquals(inboundAudioTrackReport.burstPacketsDiscarded, inboundAudioTrack.burstPacketsDiscarded);
        Assertions.assertEquals(inboundAudioTrackReport.burstLossCount, inboundAudioTrack.burstLossCount);
        Assertions.assertEquals(inboundAudioTrackReport.burstDiscardCount, inboundAudioTrack.burstDiscardCount);
        Assertions.assertEquals(inboundAudioTrackReport.burstLossRate, inboundAudioTrack.burstLossRate);
        Assertions.assertEquals(inboundAudioTrackReport.burstDiscardRate, inboundAudioTrack.burstDiscardRate);
        Assertions.assertEquals(inboundAudioTrackReport.gapLossRate, inboundAudioTrack.gapLossRate);
        Assertions.assertEquals(inboundAudioTrackReport.gapLossRate, inboundAudioTrack.gapLossRate);
        Assertions.assertEquals(inboundAudioTrackReport.gapDiscardRate, inboundAudioTrack.gapDiscardRate);
        Assertions.assertEquals(inboundAudioTrackReport.voiceActivityFlag, inboundAudioTrack.voiceActivityFlag);
        Assertions.assertEquals(inboundAudioTrackReport.lastPacketReceivedTimestamp, inboundAudioTrack.lastPacketReceivedTimestamp);
        Assertions.assertEquals(inboundAudioTrackReport.averageRtcpInterval, inboundAudioTrack.averageRtcpInterval);
        Assertions.assertEquals(inboundAudioTrackReport.headerBytesReceived, inboundAudioTrack.headerBytesReceived);
        Assertions.assertEquals(inboundAudioTrackReport.fecPacketsReceived, inboundAudioTrack.fecPacketsReceived);
        Assertions.assertEquals(inboundAudioTrackReport.fecPacketsDiscarded, inboundAudioTrack.fecPacketsDiscarded);
        Assertions.assertEquals(inboundAudioTrackReport.bytesReceived, inboundAudioTrack.bytesReceived);
        Assertions.assertEquals(inboundAudioTrackReport.packetsFailedDecryption, inboundAudioTrack.packetsFailedDecryption);
        Assertions.assertEquals(inboundAudioTrackReport.packetsDuplicated, inboundAudioTrack.packetsDuplicated);
        Assertions.assertEquals(inboundAudioTrackReport.perDscpPacketsReceived, inboundAudioTrack.perDscpPacketsReceived);
        Assertions.assertEquals(inboundAudioTrackReport.nackCount, inboundAudioTrack.nackCount);
        Assertions.assertEquals(inboundAudioTrackReport.totalProcessingDelay, inboundAudioTrack.totalProcessingDelay);
        Assertions.assertEquals(inboundAudioTrackReport.estimatedPlayoutTimestamp, inboundAudioTrack.estimatedPlayoutTimestamp);
        Assertions.assertEquals(inboundAudioTrackReport.jitterBufferDelay, inboundAudioTrack.jitterBufferDelay);
        Assertions.assertEquals(inboundAudioTrackReport.jitterBufferEmittedCount, inboundAudioTrack.jitterBufferEmittedCount);
        Assertions.assertEquals(inboundAudioTrackReport.decoderImplementation, inboundAudioTrack.decoderImplementation);


        Assertions.assertEquals(inboundAudioTrackReport.packetsSent, inboundAudioTrack.packetsSent);
        Assertions.assertEquals(inboundAudioTrackReport.bytesSent, inboundAudioTrack.bytesSent);
        Assertions.assertEquals(inboundAudioTrackReport.remoteTimestamp, inboundAudioTrack.remoteTimestamp);
        Assertions.assertEquals(inboundAudioTrackReport.reportsSent, inboundAudioTrack.reportsSent);

        Assertions.assertEquals(inboundAudioTrackReport.ended, inboundAudioTrack.ended);

        Assertions.assertEquals(inboundAudioTrackReport.payloadType, inboundAudioTrack.payloadType);
        Assertions.assertEquals(inboundAudioTrackReport.mimeType, inboundAudioTrack.mimeType);
        Assertions.assertEquals(inboundAudioTrackReport.clockRate, inboundAudioTrack.clockRate);
        Assertions.assertEquals(inboundAudioTrackReport.channels, inboundAudioTrack.channels);
        Assertions.assertEquals(inboundAudioTrackReport.sdpFmtpLine, inboundAudioTrack.sdpFmtpLine);
    }

    @Test
    @Order(2)
    void shouldBeEmpty() {
        Assertions.assertEquals(0, this.depot.get().size());
    }
}