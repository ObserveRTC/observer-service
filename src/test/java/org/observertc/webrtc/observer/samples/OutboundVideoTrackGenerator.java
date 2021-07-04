package org.observertc.webrtc.observer.samples;

import io.micronaut.context.annotation.Prototype;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.api.Randomizer;
import org.observertc.webrtc.observer.RandomGenerators;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Prototype
public class OutboundVideoTrackGenerator implements Supplier<ClientSample.OutboundVideoTrack> {

    @Inject
    RandomGenerators randomGenerators;

    private EasyRandom generator;
    private UUID peerConnectionId = UUID.randomUUID();

    @PostConstruct
    void setup() {
        this.generator = this.makeGenerator();
    }

    @Override
    public ClientSample.OutboundVideoTrack get() {
        var result = this.generator.nextObject(ClientSample.OutboundVideoTrack.class);
        return result;
    }

    public OutboundVideoTrackGenerator withPeerConnectionId(UUID value) {
        this.peerConnectionId = value;
        return this;
    }

    private EasyRandom makeGenerator() {
        EasyRandomParameters parameters = new EasyRandomParameters();
        for (Field field : ClientSample.OutboundVideoTrack.class.getFields()) {
            String fieldName = field.getName();
            Randomizer randomizer = this.getRandomizerForField(fieldName);
            if (Objects.isNull(randomizer)) {
                continue;
            }
            parameters.randomize(rField -> rField.getName().equals(fieldName), randomizer);
        }
        var result = new EasyRandom(parameters);
        return result;
    }

    private Randomizer getRandomizerForField(String fieldName) {
        switch (fieldName) {
            case "ssrc":
                return () -> this.randomGenerators.getRandomPositiveLong();
            case "packetsSent":
                return this.randomGenerators::getRandomPositiveInteger;
            case "bytesSent":
                return this.randomGenerators::getRandomPositiveLong;
            case "rtxSsrc":
                return this.randomGenerators::getRandomPositiveInteger;
//            case "rid":
            case "lastPacketSentTimestamp":
            case "headerBytesSent":
                return this.randomGenerators::getRandomPositiveLong;
            case "packetsDiscardedOnSend":
            case "fecPacketsSent":
            case "retransmittedPacketsSent":
                return this.randomGenerators::getRandomPositiveInteger;
            case "retransmittedBytesSent":
            case "targetBitrate":
            case "totalEncodedBytesTarget":
                return this.randomGenerators::getRandomPositiveLong;
            case "frameWidth":
            case "frameHeight":
            case "frameBitDepth":
                return this.randomGenerators::getRandomPositiveInteger;
            case "framesPerSecond":
                return this.randomGenerators::getRandomPositiveDouble;
            case "framesSent":
            case "hugeFramesSent":
            case "framesEncoded":
            case "keyFramesEncoded":
            case "framesDiscardedOnSend":
                return this.randomGenerators::getRandomPositiveInteger;
            case "qpSum":
                return this.randomGenerators::getRandomPositiveLong;
            case "totalEncodeTime":
            case "totalPacketSendDelay":
            case "averageRtcpInterval":
                return this.randomGenerators::getRandomPositiveDouble;
            case "qualityLimitationReason":
                return null;
            case "qualityLimitationDurations":
                return this.randomGenerators::getRandomPositiveDouble;
            case "qualityLimitationResolutionChanges":
                return this.randomGenerators::getRandomPositiveInteger;
            case "perDscpPacketsSent":
                return this.randomGenerators::getRandomPositiveDouble;
            case "nackCount":
            case "firCount":
            case "pliCount":
            case "sliCount":
                return this.randomGenerators::getRandomPositiveInteger;
            case "encoderImplementation":
                return null;
            case "packetsReceived":
            case "packetsLost":
                return this.randomGenerators::getRandomPositiveInteger;
            case "jitter":
                return this.randomGenerators::getRandomPositiveDouble;
            case "packetsDiscarded":
            case "packetsRepaired":
            case "burstPacketsLost":
            case "burstPacketsDiscarded":
            case "burstLossCount":
            case "burstDiscardCount":
                return this.randomGenerators::getRandomPositiveInteger;
            case "burstLossRate":
            case "burstDiscardRate":
            case "gapLossRate":
            case "gapDiscardRate":
            case "framesDropped":
                return this.randomGenerators::getRandomPositiveDouble;
            case "partialFramesList":
            case "fullFramesList":
                return this.randomGenerators::getRandomPositiveInteger;
            case "roundTripTime":
            case "totalRoundTripTime":
            case "fractionLost":
                return this.randomGenerators::getRandomPositiveDouble;
            case "reportsReceived":
            case "roundTripTimeMeasurements":
                return this.randomGenerators::getRandomPositiveInteger;
            case "relayedSource":
                return () -> 32000 < this.randomGenerators.getRandomPort();
            case "width":
            case "height":
            case "bitDepth":
            case "frames":
                return this.randomGenerators::getRandomPositiveInteger;
            case "ended":
                return () -> 32000 < this.randomGenerators.getRandomPort();
            case "payloadType":
                return this.randomGenerators::getRandomPositiveInteger;
            case "codecType":
            case "mimeType":
                return null;
            case "clockRate":
                return this.randomGenerators::getRandomPositiveLong;
            case "channels":
                return this.randomGenerators::getRandomPositiveInteger;
            case "sdpFmtpLine":
                return null;
            case "trackId":
                return UUID.randomUUID()::toString;
            case "peerConnectionId":
                return this.peerConnectionId::toString;
        }
        return null;
    }
}
