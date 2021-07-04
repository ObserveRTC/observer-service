package org.observertc.webrtc.observer.samples;

import io.micronaut.context.annotation.Prototype;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.api.Randomizer;
import org.observertc.webrtc.observer.RandomGenerators;
import org.observertc.webrtc.observer.TestUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Prototype
public class PeerConnectionTransportGenerator implements Supplier<ClientSample.PeerConnectionTransport> {

    @Inject
    RandomGenerators randomGenerators;

    private EasyRandom generator;
    private UUID peerConnectionId = UUID.randomUUID();

    @PostConstruct
    void setup() {
        this.generator = this.makeGenerator();
    }

    @Override
    public ClientSample.PeerConnectionTransport get() {
        return this.generator.nextObject(ClientSample.PeerConnectionTransport.class);
    }

    public PeerConnectionTransportGenerator withPeerConnectionId(UUID value) {
        this.peerConnectionId = value;
        return this;
    }

    private EasyRandom makeGenerator() {
        EasyRandomParameters parameters = new EasyRandomParameters();
        for (Field field : ClientSample.PeerConnectionTransport.class.getFields()) {
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
            case "peerConnectionId":
                return this.peerConnectionId::toString;
            case "label":
                return () -> this.randomGenerators.getRandomFromList(TestUtils.getLabels());
            case "dataChannelsOpened":
            case "dataChannelsClosed":
            case "dataChannelsRequested":
            case "dataChannelsAccepted":
            case "packetsSent":
            case "packetsReceived":
                return this.randomGenerators::getRandomPositiveInteger;
            case "bytesSent":
            case "bytesReceived":
                return this.randomGenerators::getRandomPositiveLong;
            case "iceRole":
                return () -> this.randomGenerators.getRandomFromList(TestUtils.getIceRole());
//            case "iceLocalUsernameFragment"
            case "dtlsState":
                return () -> this.randomGenerators.getRandomFromList(TestUtils.getDtlsState());
            case "iceState":
                return () -> this.randomGenerators.getRandomFromList(TestUtils.getIceState());
//            case "selectedCandidatePairId":
//            case "localCertificateId":
//            case "remoteCertificateId":
            case "tlsVersion":
                return () -> "2";
            case "dtlsCipher":
                return () -> this.randomGenerators.getRandomFromList(TestUtils.getDtlsCipher());
            case "srtpCipher":
                return () -> this.randomGenerators.getRandomFromList(TestUtils.getSrtpCipher());
//            case "tlsGroup":
            case "selectedCandidatePairChanges":
                return this.randomGenerators::getRandomPositiveInteger;
            case "sctpSmoothedRoundTripTime":
            case "sctpCongestionWindow":
            case "sctpReceiverWindow":
                return this.randomGenerators::getRandomPositiveDouble;
            case "sctpMtu":
            case "sctpUnackData":
                return this.randomGenerators::getRandomPositiveInteger;
            case "candidatePairState":
                return () -> this.randomGenerators.getRandomFromList(TestUtils.getCandidatePairState());
            case "candidatePairPacketsSent":
            case "candidatePairPacketsReceived":
                return this.randomGenerators::getRandomPositiveInteger;
            case "candidatePairBytesSent":
            case "candidatePairBytesReceived":
                return this.randomGenerators::getRandomPositiveLong;
            case "candidatePairLastPacketSentTimestamp":
            case "candidatePairLastPacketReceivedTimestamp":
            case "candidatePairFirstRequestTimestamp":
            case "candidatePairLastRequestTimestamp":
            case "candidatePairLastResponseTimestamp":
                return this.randomGenerators::getRandomTimestamp;
            case "candidatePairTotalRoundTripTime":
            case "candidatePairCurrentRoundTripTime":
            case "candidatePairAvailableOutgoingBitrate":
            case "candidatePairAvailableIncomingBitrate":
                return this.randomGenerators::getRandomPositiveDouble;
            case "candidatePairRequestsReceived":
            case "candidatePairRequestsSent":
            case "candidatePairResponsesReceived":
            case "candidatePairResponsesSent":
            case "candidatePairRetransmissionReceived":
            case "candidatePairRetransmissionSent":
            case "candidatePairConsentRequestsSent":
                return this.randomGenerators::getRandomPositiveInteger;
            case "candidatePairConsentExpiredTimestamp":
                return this.randomGenerators::getRandomTimestamp;
            case "candidatePairPacketsDiscardedOnSend":
                return this.randomGenerators::getRandomPositiveLong;
            case "candidatePairBytesDiscardedOnSend":
            case "candidatePairRequestBytesSent":
            case "candidatePairConsentRequestBytesSent":
            case "candidatePairResponseBytesSent":
                return this.randomGenerators::getRandomPositiveLong;
            case "localAddress":
                return this.randomGenerators::getRandomIpAddress;
            case "localPort":
                return this.randomGenerators::getRandomPort;
            case "localProtocol":
                return () -> this.randomGenerators.getRandomFromList(TestUtils.getNetworkTransportProtocols());
            case "localCandidateType":
                return () -> this.randomGenerators.getRandomFromList(TestUtils.getICECandidateTypes());
            case "localRelayProtocol":
                return () -> this.randomGenerators.getRandomFromList(TestUtils.getRelayProtocols());
            case "remoteAddress":
                return this.randomGenerators::getRandomIpAddress;
            case "remotePort":
                return this.randomGenerators::getRandomPort;
            case "remoteProtocol":
                return () -> this.randomGenerators.getRandomFromList(TestUtils.getNetworkTransportProtocols());
            case "remoteCandidateType":
                return () -> this.randomGenerators.getRandomFromList(TestUtils.getICECandidateTypes());
            case "remoteRelayProtocol":
                return () -> this.randomGenerators.getRandomFromList(TestUtils.getRelayProtocols());
            case "sentMediaPackets":
            case "receivedMediaPackets":
            case "lostMediaPackets":
            case "videoRttAvg":
            case "audioRttAvg":
                return this.randomGenerators::getRandomPositiveInteger;
        }
        return null;
    }
}
