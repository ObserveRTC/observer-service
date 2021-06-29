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
public class InboundAudioTrackGenerator implements Supplier<ClientSample.InboundAudioTrack> {

    @Inject
    RandomGenerators randomGenerators;

    private EasyRandom generator;
    private UUID peerConnectionId = UUID.randomUUID();

    @PostConstruct
    void setup() {
        this.generator = this.makeGenerator();
    }

    @Override
    public ClientSample.InboundAudioTrack get() {
        var result = this.generator.nextObject(ClientSample.InboundAudioTrack.class);
        return result;
    }

    public InboundAudioTrackGenerator withPeerConnectionId(UUID value) {
        this.peerConnectionId = value;
        return this;
    }

    private EasyRandom makeGenerator() {
        EasyRandomParameters parameters = new EasyRandomParameters();
        for (Field field : ClientSample.InboundAudioTrack.class.getFields()) {
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
            case "voiceActivityFlag":
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
            case "nackCount":
                return this.randomGenerators::getRandomPositiveInteger;
            case "totalProcessingDelay":
            case "estimatedPlayoutTimestamp":
            case "jitterBufferDelay":
                return this.randomGenerators::getRandomPositiveDouble;
            case "jitterBufferEmittedCount":
            case "totalSamplesReceived":
            case "totalSamplesDecoded":
            case "samplesDecodedWithSilk":
            case "samplesDecodedWithCelt":
            case "concealedSamples":
            case "silentConcealedSamples":
            case "concealmentEvents":
            case "insertedSamplesForDeceleration":
            case "removedSamplesForAcceleration":
                return this.randomGenerators::getRandomPositiveInteger;
            case "audioLevel":
            case "totalAudioEnergy":
            case "totalSamplesDuration":
                return this.randomGenerators::getRandomPositiveDouble;
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
