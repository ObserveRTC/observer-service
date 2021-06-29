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
public class InboundVideoTrackGenerator implements Supplier<ClientSample.InboundVideoTrack> {

    @Inject
    RandomGenerators randomGenerators;

    private EasyRandom generator;
    private UUID peerConnectionId = UUID.randomUUID();

    @PostConstruct
    void setup() {
        this.generator = this.makeGenerator();
    }

    @Override
    public ClientSample.InboundVideoTrack get() {
        var result = this.generator.nextObject(ClientSample.InboundVideoTrack.class);
        return result;
    }

    public InboundVideoTrackGenerator withPeerConnectionId(UUID value) {
        this.peerConnectionId = value;
        return this;
    }

    private EasyRandom makeGenerator() {
        EasyRandomParameters parameters = new EasyRandomParameters();
        for (Field field : ClientSample.InboundVideoTrack.class.getFields()) {
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
                return this.randomGenerators::getRandomPositiveDouble;
//            case "framesDropped":
            case "fullFramesLost":
            case "framesDecoded":
            case "keyFramesDecoded":
            case "frameWidth":
            case "frameHeight":
            case "frameBitDepth":
                return this.randomGenerators::getRandomPositiveInteger;
            case "framesPerSecond":
                return this.randomGenerators::getRandomPositiveDouble;
            case "qpSum":
            case "totalDecodeTime":
            case "totalInterFrameDelay":
            case "totalSquaredInterFrameDelay":
                return this.randomGenerators::getRandomPositiveLong;
            case "lastPacketReceivedTimestamp":
                return () -> 32000 < this.randomGenerators.getRandomPort();
            case "averageRtcpInterval":
                return this.randomGenerators::getRandomPositiveDouble;
            case "headerBytesReceived":
                return () -> this.randomGenerators.getRandomPositiveLong();
            case "fecPacketsReceived":
            case "fecPacketsDiscarded":
                return this.randomGenerators::getRandomPositiveInteger;
            case "bytesReceived":
                return this.randomGenerators::getRandomPositiveLong;
            case "packetsFailedDecryption":
            case "packetsDuplicated":
                return this.randomGenerators::getRandomPositiveInteger;
            case "perDscpPacketsReceived":
                return this.randomGenerators::getRandomPositiveDouble;
            case "firCount":
            case "pliCount":
            case "nackCount":
            case "sliCount":
                return this.randomGenerators::getRandomPositiveInteger;
            case "totalProcessingDelay":
            case "estimatedPlayoutTimestamp":
            case "jitterBufferDelay":
                return this.randomGenerators::getRandomPositiveDouble;
            case "jitterBufferEmittedCount":
            case "framesReceived":
                return this.randomGenerators::getRandomPositiveInteger;
            case "decoderImplementation":
                return null;
            case "packetsSent":
                return this.randomGenerators::getRandomPositiveInteger;
            case "bytesSent":
                return this.randomGenerators::getRandomPositiveLong;
            case "remoteTimestamp":
                return this.randomGenerators::getRandomPositiveDouble;
            case "reportsSent":
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
