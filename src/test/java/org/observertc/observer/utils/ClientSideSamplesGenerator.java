package org.observertc.observer.utils;

import org.observertc.observer.common.JsonUtils;
import org.observertc.schemas.samples.Samples;

import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.observertc.observer.utils.TestUtils.arrayOrNullFromList;
import static org.observertc.observer.utils.TestUtils.arrayOrNullFromQueue;

public class ClientSideSamplesGenerator implements Supplier<Samples> {

    private RandomGenerators randomGenerator = new RandomGenerators();
    private String callId = null;
    private String clientId = UUID.randomUUID().toString();
    private int samplesSeq = 0;
    private String roomId = null;
    private String userId = null;
    private String marker = null;
    private String remoteClientId = null;
    private Integer timeZoneOffsetInHours = null;
    private Set<String> peerConnectionIds = new HashSet<>();
    private Map<String, RtpSession> inboundAudioTracks = new HashMap<>();
    private Map<String, RtpSession> inboundVideoTracks = new HashMap<>();
    private Map<String, RtpSession> outboundAudioTracks = new HashMap<>();
    private Map<String, RtpSession> outboundVideoTracks = new HashMap<>();
    private Map<String, DataChannelSession> dataChannels = new HashMap<>();

    private Queue<Samples.ClientSample.Engine> addedEngines = new LinkedList<>();
    private Queue<Samples.ClientSample.Platform> addedPlatforms = new LinkedList<>();
    private Queue<Samples.ClientSample.OperationSystem> addedOperationSystems = new LinkedList<>();
    private Queue<Samples.ClientSample.MediaDevice> addedMediaDevices = new LinkedList<>();
    private Queue<Samples.ClientSample.ExtensionStat> addedExtensionStats = new LinkedList<>();
    private Queue<String> addedUserMediaErrors = new LinkedList<>();
    private Queue<String> addedLocalSDP = new LinkedList<>();
    private Queue<Samples.ClientSample.Browser> addedBrowsers = new LinkedList<>();
    private Queue<Samples.ClientSample.MediaSourceStat> mediaSources = new LinkedList<>();
    private Queue<String> addedMediaConstraints = new LinkedList<>();
    private Queue<String> addedIceServers = new LinkedList<>();
    private Queue<Samples.ClientSample.MediaCodecStats> addedMediaCodecStats = new LinkedList<>();
    private Queue<Samples.ClientSample.Certificate> addedCertificates = new LinkedList<>();
    private Queue<Samples.ClientSample.IceLocalCandidate> addedIceLocalCandidates = new LinkedList<>();
    private Queue<Samples.ClientSample.IceRemoteCandidate> addedIceRemoteCandidates = new LinkedList<>();


    public ClientSideSamplesGenerator setClientId(String value) {
        this.clientId = value;
        return this;
    }

    public ClientSideSamplesGenerator setCallId(String value) {
        this.clientId = value;
        return this;
    }

    public ClientSideSamplesGenerator setRoomId(String value) {
        this.roomId = value;
        return this;
    }

    public ClientSideSamplesGenerator setRemoteClientId(String value) {
        this.remoteClientId = value;
        return this;
    }

    public ClientSideSamplesGenerator setUserId(String value) {
        this.userId = value;
        return this;
    }

    public ClientSideSamplesGenerator setMarker(String value) {
        this.marker = value;
        return this;
    }

    public ClientSideSamplesGenerator setTimeZoneOffsetInHours(Integer value) {
        this.timeZoneOffsetInHours = value;
        return this;
    }

    public ClientSideSamplesGenerator addBrowser() {
        var browser = new Samples.ClientSample.Browser();
        browser.name = this.randomGenerator.getRandomBrowserNames();
        browser.version = this.randomGenerator.getRandomVersionNumber();
        this.addedBrowsers.add(browser);
        return this;
    }

    public ClientSideSamplesGenerator addEngine() {
        var engine = new Samples.ClientSample.Engine();
        engine.name = this.randomGenerator.getRandomString();
        engine.version = this.randomGenerator.getRandomVersionNumber();
        this.addedEngines.add(engine);
        return this;
    }

    public ClientSideSamplesGenerator addPlatform() {
        var platform = new Samples.ClientSample.Platform();
        platform.type = this.randomGenerator.getRandomString();
        platform.vendor = this.randomGenerator.getRandomString();
        platform.model = this.randomGenerator.getRandomString();
        this.addedPlatforms.add(platform);
        return this;
    }

    public ClientSideSamplesGenerator addMediaSource() {
        var mediaSourceStat = new Samples.ClientSample.MediaSourceStat();
        mediaSourceStat.trackIdentifier = this.randomGenerator.getRandomString();
        mediaSourceStat.kind = this.randomGenerator.getRandomMediaKind();
        mediaSourceStat.relayedSource = this.randomGenerator.getRandomBoolean();
        mediaSourceStat.audioLevel = this.randomGenerator.getRandomPositiveDouble();
        mediaSourceStat.totalAudioEnergy = this.randomGenerator.getRandomPositiveDouble();
        mediaSourceStat.totalSamplesDuration = this.randomGenerator.getRandomPositiveDouble();
        mediaSourceStat.echoReturnLoss = this.randomGenerator.getRandomPositiveDouble();
        mediaSourceStat.echoReturnLossEnhancement = this.randomGenerator.getRandomPositiveDouble();
        mediaSourceStat.width = this.randomGenerator.getRandomPositiveInteger();
        mediaSourceStat.height = this.randomGenerator.getRandomPositiveInteger();
        mediaSourceStat.frames = this.randomGenerator.getRandomPositiveInteger();
        mediaSourceStat.framesPerSecond = this.randomGenerator.getRandomPositiveDouble();
        this.mediaSources.add(mediaSourceStat);
        return this;
    }

    public ClientSideSamplesGenerator addOperationSystem() {
        var operationSystem = new Samples.ClientSample.OperationSystem();
        operationSystem.name = this.randomGenerator.getOperationSystemName();
        operationSystem.version = this.randomGenerator.getRandomVersionNumber();
        operationSystem.versionName = this.randomGenerator.getRandomString();
        this.addedOperationSystems.add(operationSystem);
        return this;
    }

    public ClientSideSamplesGenerator addMediaDevice() {
        var mediaDevice = new Samples.ClientSample.MediaDevice();
        mediaDevice.id = this.randomGenerator.getRandomString();
        mediaDevice.kind = this.randomGenerator.getRandomMediaKind();
        mediaDevice.label = this.randomGenerator.getRandomPeerConnectionLabels();
        this.addedMediaDevices.add(mediaDevice);
        return this;
    }

    public ClientSideSamplesGenerator addExtensionStat() {
        var extensionStat = new Samples.ClientSample.ExtensionStat();
        extensionStat.type = this.randomGenerator.getRandomString();
        extensionStat.payload = JsonUtils.objectToString(Map.of("key", "value"));
        this.addedExtensionStats.add(extensionStat);
        return this;
    }

    public ClientSideSamplesGenerator addMediaCodec() {
        var mediaCodecStats = new Samples.ClientSample.MediaCodecStats();
        mediaCodecStats.payloadType = this.randomGenerator.getRandomPayloadType().toString();
        mediaCodecStats.codecType = this.randomGenerator.getRandomCodecType();
        mediaCodecStats.mimeType = this.randomGenerator.getRandomString();
        mediaCodecStats.clockRate = this.randomGenerator.getRandomClockRate();
        mediaCodecStats.channels = this.randomGenerator.getRandomPositiveInteger();
        mediaCodecStats.sdpFmtpLine = this.randomGenerator.getRandomString();
        this.addedMediaCodecStats.add(mediaCodecStats);
        return this;
    }

    public ClientSideSamplesGenerator addCertificate() {
        var certificate = new Samples.ClientSample.Certificate();
        certificate.fingerprint = this.randomGenerator.getRandomString();
        certificate.fingerprintAlgorithm = this.randomGenerator.getRandomString();
        certificate.base64Certificate = this.randomGenerator.getRandomString();
        certificate.issuerCertificateId = this.randomGenerator.getRandomString();
        this.addedCertificates.add(certificate);
        return this;
    }

    public ClientSideSamplesGenerator addIceLocalCandidate() {
        var iceLocalCandidate = new Samples.ClientSample.IceLocalCandidate();
        iceLocalCandidate.peerConnectionId = this.randomGenerator.getRandomFromCollection(this.peerConnectionIds);
        iceLocalCandidate.id = this.randomGenerator.getRandomString();
        iceLocalCandidate.address = this.randomGenerator.getRandomIPv4Address();
        iceLocalCandidate.port = this.randomGenerator.getRandomPort();
        iceLocalCandidate.protocol = this.randomGenerator.getRandomNetworkTransportProtocols();
        iceLocalCandidate.candidateType = this.randomGenerator.getRandomICECandidateTypes();
        iceLocalCandidate.priority = this.randomGenerator.getRandomPositiveLong();
        iceLocalCandidate.url = this.randomGenerator.getRandomIceUrl();
        iceLocalCandidate.relayProtocol = this.randomGenerator.getRandomRelayProtocols();
        this.addedIceLocalCandidates.add(iceLocalCandidate);
        return this;
    }

    public ClientSideSamplesGenerator addIceRemoteCandidate() {
        var iceRemoteCandidate = new Samples.ClientSample.IceRemoteCandidate();
        iceRemoteCandidate.peerConnectionId = this.randomGenerator.getRandomFromCollection(this.peerConnectionIds);
        iceRemoteCandidate.id = this.randomGenerator.getRandomString();
        iceRemoteCandidate.address =  this.randomGenerator.getRandomIPv4Address();
        iceRemoteCandidate.port = this.randomGenerator.getRandomPort();
        iceRemoteCandidate.protocol = this.randomGenerator.getRandomNetworkTransportProtocols();
        iceRemoteCandidate.candidateType =  this.randomGenerator.getRandomICECandidateTypes();
        iceRemoteCandidate.priority = this.randomGenerator.getRandomPositiveLong();
        iceRemoteCandidate.url = this.randomGenerator.getRandomIceUrl();
        iceRemoteCandidate.relayProtocol = this.randomGenerator.getRandomRelayProtocols();
        this.addedIceRemoteCandidates.add(iceRemoteCandidate);
        return this;
    }

    public ClientSideSamplesGenerator addUserMediaError(String value) {
        this.addedUserMediaErrors.add(value);
        return this;
    }

    public ClientSideSamplesGenerator addLocalSdp(String value) {
        this.addedLocalSDP.add(value);
        return this;
    }

    public ClientSideSamplesGenerator addMediaConstraint(String value) {
        this.addedMediaConstraints.add(value);
        return this;
    }

    public ClientSideSamplesGenerator addIceServer(String value) {
        this.addedIceServers.add(value);
        return this;
    }

    public ClientSideSamplesGenerator addPeerConnection(String value) {
        this.peerConnectionIds.add(value);
        return this;
    }

    public ClientSideSamplesGenerator removePeerConnection(UUID value) {
        if (!this.peerConnectionIds.contains(value)) return this;
        this.inboundAudioTracks.entrySet().stream()
                .filter(entry -> entry.getValue().peerConnectionId.equals(value))
                .map(entry -> entry.getKey())
                .collect(Collectors.toList())
                .stream()
                .forEach(this.inboundAudioTracks::remove);

        this.inboundVideoTracks.entrySet().stream()
                .filter(entry -> entry.getValue().peerConnectionId.equals(value))
                .map(entry -> entry.getKey())
                .collect(Collectors.toList())
                .stream()
                .forEach(this.inboundVideoTracks::remove);

        this.outboundAudioTracks.entrySet().stream()
                .filter(entry -> entry.getValue().peerConnectionId.equals(value))
                .map(entry -> entry.getKey())
                .collect(Collectors.toList())
                .stream()
                .forEach(this.outboundAudioTracks::remove);

        this.outboundVideoTracks.entrySet().stream()
                .filter(entry -> entry.getValue().peerConnectionId.equals(value))
                .map(entry -> entry.getKey())
                .collect(Collectors.toList())
                .stream()
                .forEach(this.outboundVideoTracks::remove);

        this.dataChannels.entrySet().stream()
                .filter(entry -> entry.getValue().peerConnectionId.equals(value))
                .map(entry -> entry.getKey())
                .collect(Collectors.toList())
                .stream()
                .forEach(this.dataChannels::remove);
        this.peerConnectionIds.remove(value);
        return this;
    }

    public ClientSideSamplesGenerator addDataChannel(String peerConnectionId, String channelId) {
        var session = new DataChannelSession(peerConnectionId);
        this.dataChannels.put(channelId, session);
        return this;
    }


    public ClientSideSamplesGenerator addInboundAudioTrack(String peerConnectionId, String trackId, Long SSRC) {
        return this.addInboundAudioTrack(peerConnectionId, trackId, SSRC, null, null);
    }

    public ClientSideSamplesGenerator addInboundAudioTrack(String peerConnectionId, String trackId, Long SSRC, String sfuStreamId, String sfuSinkId) {
        if (!this.peerConnectionIds.contains(peerConnectionId)) {
            throw new RuntimeException("Add the peer connection id to the generator before you add any session related to it");
        }
        var rtpSession = new RtpSession(sfuStreamId, sfuSinkId, SSRC, peerConnectionId);
        this.inboundAudioTracks.put(trackId, rtpSession);
        return this;
    }

    public ClientSideSamplesGenerator removeInboundAudioTrack(String trackId) {
        this.inboundAudioTracks.remove(trackId);
        return this;
    }

    public ClientSideSamplesGenerator addInboundVideoTrack(String peerConnectionId, String trackId, Long SSRC) {
        return this.addInboundVideoTrack(peerConnectionId, trackId, SSRC, null, null);
    }

    public ClientSideSamplesGenerator addInboundVideoTrack(String peerConnectionId, String trackId, Long SSRC, String sfuStreamId, String sfuSinkId) {
        if (!this.peerConnectionIds.contains(peerConnectionId)) {
            throw new RuntimeException("Add the peer connection id to the generator before you add any session related to it");
        }
        var rtpSession = new RtpSession(sfuStreamId, sfuSinkId, SSRC, peerConnectionId);
        this.inboundVideoTracks.put(trackId, rtpSession);
        return this;
    }

    public ClientSideSamplesGenerator removeInboundVideoTrack(String trackId) {
        this.outboundVideoTracks.remove(trackId);
        return this;
    }

    public ClientSideSamplesGenerator addOutboundAudioTrack(String peerConnectionId, String trackId, Long SSRC) {
        return this.addOutboundAudioTrack(peerConnectionId, trackId, SSRC, null);
    }

    public ClientSideSamplesGenerator addOutboundAudioTrack(String peerConnectionId, String trackId, Long SSRC, String sfuStreamId) {
        if (!this.peerConnectionIds.contains(peerConnectionId)) {
            throw new RuntimeException("Add the peer connection id to the generator before you add any session related to it");
        }
        var rtpSession = new RtpSession(sfuStreamId, null, SSRC, peerConnectionId);
        this.outboundAudioTracks.put(trackId, rtpSession);
        return this;
    }

    public ClientSideSamplesGenerator removeOutboundAudioTrack(String trackId) {
        this.outboundAudioTracks.remove(trackId);
        return this;
    }

    public ClientSideSamplesGenerator addOutboundVideoTrack(String peerConnectionId, String trackId, Long SSRC) {
        return this.addOutboundVideoTrack(peerConnectionId, trackId, SSRC, null);
    }

    public ClientSideSamplesGenerator addOutboundVideoTrack(String peerConnectionId, String trackId, Long SSRC, String sfuStreamId) {
        if (!this.peerConnectionIds.contains(peerConnectionId)) {
            throw new RuntimeException("Add the peer connection id to the generator before you add any session related to it");
        }
        var rtpSession = new RtpSession(sfuStreamId, null, SSRC, peerConnectionId);
        this.outboundVideoTracks.put(trackId, rtpSession);
        return this;
    }

    public ClientSideSamplesGenerator removeOutboundVideoTrack(UUID trackId) {
        this.outboundVideoTracks.remove(trackId);
        return this;
    }

    @Override
    public Samples get() {
        var pcTransports = this.peerConnectionIds.stream().map(peerConnectionId -> {
            var peerConnectionTransport = new Samples.ClientSample.PeerConnectionTransport();
            peerConnectionTransport.peerConnectionId = peerConnectionId;
            peerConnectionTransport.label = this.randomGenerator.getRandomPeerConnectionLabels();
            peerConnectionTransport.packetsSent = this.randomGenerator.getRandomPositiveInteger();
            peerConnectionTransport.packetsReceived = this.randomGenerator.getRandomPositiveInteger();
            peerConnectionTransport.bytesSent = this.randomGenerator.getRandomPositiveLong();
            peerConnectionTransport.bytesReceived = this.randomGenerator.getRandomPositiveLong();
            peerConnectionTransport.iceRole = this.randomGenerator.getRandomIceRole();
            peerConnectionTransport.iceLocalUsernameFragment = this.randomGenerator.getRandomString();
            peerConnectionTransport.dtlsState = this.randomGenerator.getRandomDtlsState();
            peerConnectionTransport.iceState = this.randomGenerator.getRandomIceState();
            peerConnectionTransport.tlsVersion = this.randomGenerator.getRandomVersionNumber();
            peerConnectionTransport.dtlsCipher = this.randomGenerator.getRandomDtlsCipher();
            peerConnectionTransport.srtpCipher = this.randomGenerator.getRandomSrtpCipher();
            peerConnectionTransport.tlsGroup = this.randomGenerator.getRandomString();
            peerConnectionTransport.selectedCandidatePairChanges = this.randomGenerator.getRandomPositiveInteger();
            return peerConnectionTransport;
        }).collect(Collectors.toList());

        var iceCandidatePairs = this.peerConnectionIds.stream().map(peerConnectionId -> {
            var iceCandidatePair = new Samples.ClientSample.IceCandidatePair();
            iceCandidatePair.peerConnectionId = peerConnectionId;
            iceCandidatePair.label = this.randomGenerator.getRandomPeerConnectionLabels();
            iceCandidatePair.transportId = UUID.randomUUID().toString();
            iceCandidatePair.localCandidateId = UUID.randomUUID().toString();
            iceCandidatePair.remoteCandidateId = UUID.randomUUID().toString();
            iceCandidatePair.state = this.randomGenerator.getRandomIceState();
            iceCandidatePair.nominated = this.randomGenerator.getRandomBoolean();
            iceCandidatePair.packetsSent = this.randomGenerator.getRandomPositiveInteger();
            iceCandidatePair.packetsReceived = this.randomGenerator.getRandomPositiveInteger();
            iceCandidatePair.bytesSent = this.randomGenerator.getRandomPositiveLong();
            iceCandidatePair.bytesReceived = this.randomGenerator.getRandomPositiveLong();
            iceCandidatePair.lastPacketSentTimestamp = this.randomGenerator.getRandomTimestamp();
            iceCandidatePair.lastPacketReceivedTimestamp = this.randomGenerator.getRandomTimestamp();
            iceCandidatePair.totalRoundTripTime = this.randomGenerator.getRandomPositiveDouble();
            iceCandidatePair.currentRoundTripTime = this.randomGenerator.getRandomPositiveDouble();
            iceCandidatePair.availableOutgoingBitrate = this.randomGenerator.getRandomPositiveDouble();
            iceCandidatePair.availableIncomingBitrate = this.randomGenerator.getRandomPositiveDouble();
            iceCandidatePair.requestsReceived = this.randomGenerator.getRandomPositiveInteger();
            iceCandidatePair.requestsSent = this.randomGenerator.getRandomPositiveInteger();
            iceCandidatePair.responsesReceived = this.randomGenerator.getRandomPositiveInteger();
            iceCandidatePair.responsesSent = this.randomGenerator.getRandomPositiveInteger();
            iceCandidatePair.consentRequestsSent = this.randomGenerator.getRandomPositiveInteger();
            iceCandidatePair.packetsDiscardedOnSend = this.randomGenerator.getRandomPositiveInteger();
            iceCandidatePair.bytesDiscardedOnSend = this.randomGenerator.getRandomPositiveLong();
            return iceCandidatePair;
        }).collect(Collectors.toList());

        var inboundAudioTracks = this.inboundAudioTracks.entrySet().stream().map(entry -> {
            var trackId = entry.getKey();
            var session = entry.getValue();
            var inboundAudioTrack = new Samples.ClientSample.InboundAudioTrack();
            inboundAudioTrack.trackId = trackId;
            inboundAudioTrack.peerConnectionId = session.peerConnectionId;
            inboundAudioTrack.remoteClientId = this.remoteClientId;
            inboundAudioTrack.sfuStreamId = session.sfuStreamId;
            inboundAudioTrack.sfuSinkId = session.sfuSinkId;
            inboundAudioTrack.ssrc = session.SSRC;
            inboundAudioTrack.packetsReceived = this.randomGenerator.getRandomPositiveInteger();
            inboundAudioTrack.packetsLost = this.randomGenerator.getRandomPositiveInteger();
            inboundAudioTrack.jitter = this.randomGenerator.getRandomPositiveDouble();
            inboundAudioTrack.packetsDiscarded = this.randomGenerator.getRandomPositiveInteger();
            inboundAudioTrack.lastPacketReceivedTimestamp = this.randomGenerator.getRandomTimestamp();
            inboundAudioTrack.headerBytesReceived =  this.randomGenerator.getRandomPositiveLong();
            inboundAudioTrack.fecPacketsReceived = this.randomGenerator.getRandomPositiveInteger();
            inboundAudioTrack.fecPacketsDiscarded = this.randomGenerator.getRandomPositiveInteger();
            inboundAudioTrack.bytesReceived = this.randomGenerator.getRandomPositiveLong();
            inboundAudioTrack.nackCount = this.randomGenerator.getRandomPositiveInteger();
            inboundAudioTrack.totalProcessingDelay = this.randomGenerator.getRandomPositiveDouble();
            inboundAudioTrack.estimatedPlayoutTimestamp = this.randomGenerator.getRandomTimestamp();
            inboundAudioTrack.jitterBufferDelay = this.randomGenerator.getRandomPositiveDouble();
            inboundAudioTrack.jitterBufferEmittedCount = this.randomGenerator.getRandomPositiveInteger();
            inboundAudioTrack.decoderImplementation = this.randomGenerator.getRandomCodecType();
            inboundAudioTrack.totalSamplesReceived = this.randomGenerator.getRandomPositiveInteger();
            inboundAudioTrack.concealedSamples = this.randomGenerator.getRandomPositiveInteger();
            inboundAudioTrack.silentConcealedSamples = this.randomGenerator.getRandomPositiveInteger();
            inboundAudioTrack.concealmentEvents = this.randomGenerator.getRandomPositiveInteger();
            inboundAudioTrack.insertedSamplesForDeceleration = this.randomGenerator.getRandomPositiveInteger();
            inboundAudioTrack.removedSamplesForAcceleration = this.randomGenerator.getRandomPositiveInteger();
            inboundAudioTrack.packetsSent = this.randomGenerator.getRandomPositiveInteger();
            inboundAudioTrack.bytesSent = this.randomGenerator.getRandomPositiveLong();
            inboundAudioTrack.remoteTimestamp = this.randomGenerator.getRandomTimestamp();
            inboundAudioTrack.reportsSent = this.randomGenerator.getRandomPositiveInteger();
            inboundAudioTrack.roundTripTime = this.randomGenerator.getRandomPositiveDouble();
            inboundAudioTrack.totalRoundTripTime = this.randomGenerator.getRandomPositiveDouble();
            inboundAudioTrack.roundTripTimeMeasurements = this.randomGenerator.getRandomPositiveInteger();
            return inboundAudioTrack;
        }).collect(Collectors.toList());

        var inboundVideoTracks = this.inboundVideoTracks.entrySet().stream().map(entry -> {
            var trackId = entry.getKey();
            var session = entry.getValue();
            var inboundVideoTrack = new Samples.ClientSample.InboundVideoTrack();
            inboundVideoTrack.trackId = trackId;
            inboundVideoTrack.peerConnectionId = session.peerConnectionId;
            inboundVideoTrack.remoteClientId = this.remoteClientId;
            inboundVideoTrack.sfuStreamId = session.sfuStreamId;
            inboundVideoTrack.sfuSinkId = session.sfuSinkId;
            inboundVideoTrack.ssrc = session.SSRC;
            inboundVideoTrack.packetsReceived = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.packetsLost = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.jitter = this.randomGenerator.getRandomPositiveDouble();
            inboundVideoTrack.packetsDiscarded = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.lastPacketReceivedTimestamp = this.randomGenerator.getRandomTimestamp();
            inboundVideoTrack.headerBytesReceived =  this.randomGenerator.getRandomPositiveLong();
            inboundVideoTrack.fecPacketsReceived = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.fecPacketsDiscarded = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.bytesReceived = this.randomGenerator.getRandomPositiveLong();
            inboundVideoTrack.nackCount = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.totalProcessingDelay = this.randomGenerator.getRandomPositiveDouble();
            inboundVideoTrack.estimatedPlayoutTimestamp = this.randomGenerator.getRandomTimestamp();
            inboundVideoTrack.jitterBufferDelay = this.randomGenerator.getRandomPositiveDouble();
            inboundVideoTrack.jitterBufferEmittedCount = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.decoderImplementation = this.randomGenerator.getRandomCodecType();
            inboundVideoTrack.framesDropped = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.framesDecoded = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.keyFramesDecoded = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.frameWidth = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.frameHeight = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.framesPerSecond = this.randomGenerator.getRandomPositiveDouble();
            inboundVideoTrack.qpSum = this.randomGenerator.getRandomPositiveLong();
            inboundVideoTrack.totalDecodeTime = this.randomGenerator.getRandomPositiveDouble();
            inboundVideoTrack.totalInterFrameDelay = this.randomGenerator.getRandomPositiveDouble();
            inboundVideoTrack.totalSquaredInterFrameDelay = this.randomGenerator.getRandomPositiveDouble();
            inboundVideoTrack.firCount = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.pliCount = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.framesReceived = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.packetsSent = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.bytesSent = this.randomGenerator.getRandomPositiveLong();
            inboundVideoTrack.remoteTimestamp = this.randomGenerator.getRandomTimestamp();
            inboundVideoTrack.reportsSent = this.randomGenerator.getRandomPositiveInteger();
            inboundVideoTrack.roundTripTime = this.randomGenerator.getRandomPositiveDouble();
            inboundVideoTrack.totalRoundTripTime = this.randomGenerator.getRandomPositiveDouble();
            inboundVideoTrack.roundTripTimeMeasurements = this.randomGenerator.getRandomPositiveInteger();
            return inboundVideoTrack;
        }).collect(Collectors.toList());

        var outboundAudioTracks = this.outboundAudioTracks.entrySet().stream().map(entry -> {
            var trackId = entry.getKey();
            var session = entry.getValue();
            var outboundAudioTrack = new Samples.ClientSample.OutboundAudioTrack();
            outboundAudioTrack.trackId = trackId;
            outboundAudioTrack.peerConnectionId = session.peerConnectionId;
            outboundAudioTrack.sfuStreamId = session.sfuStreamId;
            outboundAudioTrack.ssrc = session.SSRC;
            outboundAudioTrack.packetsSent = this.randomGenerator.getRandomPositiveInteger();
            outboundAudioTrack.bytesSent = this.randomGenerator.getRandomPositiveLong();
            outboundAudioTrack.rid = this.randomGenerator.getRandomString();
            outboundAudioTrack.headerBytesSent = this.randomGenerator.getRandomPositiveLong();
            outboundAudioTrack.retransmittedPacketsSent = this.randomGenerator.getRandomPositiveInteger();
            outboundAudioTrack.retransmittedBytesSent =  this.randomGenerator.getRandomPositiveLong();
            outboundAudioTrack.targetBitrate = this.randomGenerator.getRandomPositiveInteger();
            outboundAudioTrack.totalEncodedBytesTarget = this.randomGenerator.getRandomPositiveLong();
            outboundAudioTrack.totalPacketSendDelay = this.randomGenerator.getRandomPositiveDouble();
            outboundAudioTrack.averageRtcpInterval = this.randomGenerator.getRandomPositiveDouble();
            outboundAudioTrack.nackCount = this.randomGenerator.getRandomPositiveInteger();
            outboundAudioTrack.encoderImplementation = this.randomGenerator.getRandomCodecType();
            outboundAudioTrack.packetsReceived = this.randomGenerator.getRandomPositiveInteger();
            outboundAudioTrack.packetsLost = this.randomGenerator.getRandomPositiveInteger();
            outboundAudioTrack.jitter = this.randomGenerator.getRandomPositiveDouble();
            outboundAudioTrack.roundTripTime = this.randomGenerator.getRandomPositiveDouble();
            outboundAudioTrack.totalRoundTripTime = this.randomGenerator.getRandomPositiveDouble();
            outboundAudioTrack.fractionLost = this.randomGenerator.getRandomPositiveDouble();
            outboundAudioTrack.roundTripTimeMeasurements = this.randomGenerator.getRandomPositiveInteger();
            outboundAudioTrack.relayedSource = this.randomGenerator.getRandomBoolean();
            outboundAudioTrack.audioLevel = this.randomGenerator.getRandomPositiveDouble();
            outboundAudioTrack.totalAudioEnergy = this.randomGenerator.getRandomPositiveDouble();
            outboundAudioTrack.totalSamplesDuration = this.randomGenerator.getRandomPositiveDouble();
            outboundAudioTrack.echoReturnLoss = this.randomGenerator.getRandomPositiveDouble();
            outboundAudioTrack.echoReturnLossEnhancement = this.randomGenerator.getRandomPositiveDouble();
            return outboundAudioTrack;
        }).collect(Collectors.toList());

        var outboundVideoTracks = this.outboundVideoTracks.entrySet().stream().map(entry -> {
            var trackId = entry.getKey();
            var session = entry.getValue();
            var outboundVideoTrack = new Samples.ClientSample.OutboundVideoTrack();
            outboundVideoTrack.trackId = trackId;
            outboundVideoTrack.peerConnectionId = session.peerConnectionId;
            outboundVideoTrack.sfuStreamId = session.sfuStreamId;
            outboundVideoTrack.ssrc = session.SSRC;
            outboundVideoTrack.packetsSent = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.bytesSent =  this.randomGenerator.getRandomPositiveLong();
            outboundVideoTrack.rid = this.randomGenerator.getRandomString();
            outboundVideoTrack.headerBytesSent =  this.randomGenerator.getRandomPositiveLong();
            outboundVideoTrack.retransmittedPacketsSent = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.retransmittedBytesSent =  this.randomGenerator.getRandomPositiveLong();
            outboundVideoTrack.targetBitrate = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.totalEncodedBytesTarget =  this.randomGenerator.getRandomPositiveLong();
            outboundVideoTrack.totalPacketSendDelay = this.randomGenerator.getRandomPositiveDouble();
            outboundVideoTrack.averageRtcpInterval = this.randomGenerator.getRandomPositiveDouble();
            outboundVideoTrack.nackCount = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.firCount = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.pliCount = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.encoderImplementation = this.randomGenerator.getRandomCodecType();
            outboundVideoTrack.frameWidth = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.frameHeight = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.framesPerSecond = this.randomGenerator.getRandomPositiveDouble();
            outboundVideoTrack.framesSent = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.hugeFramesSent = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.framesEncoded = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.keyFramesEncoded = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.qpSum = this.randomGenerator.getRandomPositiveLong();
            outboundVideoTrack.totalEncodeTime = this.randomGenerator.getRandomPositiveDouble();
            outboundVideoTrack.qualityLimitationDurationNone = this.randomGenerator.getRandomPositiveDouble();
            outboundVideoTrack.qualityLimitationDurationCPU = this.randomGenerator.getRandomPositiveDouble();
            outboundVideoTrack.qualityLimitationDurationBandwidth = this.randomGenerator.getRandomPositiveDouble();
            outboundVideoTrack.qualityLimitationDurationOther = this.randomGenerator.getRandomPositiveDouble();
            outboundVideoTrack.qualityLimitationReason = this.randomGenerator.getRandomQualityLimitationReason();
            outboundVideoTrack.qualityLimitationResolutionChanges = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.packetsReceived = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.packetsLost = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.jitter = this.randomGenerator.getRandomPositiveDouble();
            outboundVideoTrack.roundTripTime = this.randomGenerator.getRandomPositiveDouble();
            outboundVideoTrack.totalRoundTripTime = this.randomGenerator.getRandomPositiveDouble();
            outboundVideoTrack.fractionLost = this.randomGenerator.getRandomPositiveDouble();
            outboundVideoTrack.roundTripTimeMeasurements = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.framesDropped = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.relayedSource = this.randomGenerator.getRandomBoolean();
            outboundVideoTrack.width = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.height = this.randomGenerator.getRandomPositiveInteger();
            outboundVideoTrack.frames = this.randomGenerator.getRandomPositiveInteger();
            return outboundVideoTrack;
        }).collect(Collectors.toList());

        var dataChannels = this.dataChannels.entrySet().stream().map(entry -> {
            var channelId = entry.getKey();
            var session = entry.getValue();
            var dataChannel = new Samples.ClientSample.DataChannel();
            dataChannel.peerConnectionId = session.peerConnectionId;
            dataChannel.label = this.randomGenerator.getRandomString();
            dataChannel.protocol = this.randomGenerator.getRandomNetworkTransportProtocols();
            dataChannel.dataChannelIdentifier = this.randomGenerator.getRandomPositiveInteger();
            dataChannel.state = this.randomGenerator.getRandomDataChannelState();
            dataChannel.bytesSent =  this.randomGenerator.getRandomPositiveLong();
            dataChannel.bytesReceived = this.randomGenerator.getRandomPositiveLong();
            return dataChannel;
        }).collect(Collectors.toList());

        var clientSample = new Samples.ClientSample();
        clientSample.callId = this.callId;
        clientSample.clientId = this.clientId;
        clientSample.sampleSeq = ++this.samplesSeq;
        clientSample.roomId = this.roomId;
        clientSample.userId = this.userId;
        clientSample.engine = this.addedEngines.poll();
        clientSample.platform = this.addedPlatforms.poll();
        clientSample.browser = this.addedBrowsers.poll();
        clientSample.os = this.addedOperationSystems.poll();
        clientSample.mediaConstraints = arrayOrNullFromQueue(String.class, this.addedMediaConstraints);
        clientSample.mediaDevices = arrayOrNullFromQueue(Samples.ClientSample.MediaDevice.class, this.addedMediaDevices);
        clientSample.userMediaErrors = arrayOrNullFromQueue(String.class, this.addedUserMediaErrors);
        clientSample.localSDPs = arrayOrNullFromQueue(String.class, this.addedLocalSDP);
        clientSample.extensionStats = arrayOrNullFromQueue(Samples.ClientSample.ExtensionStat.class, this.addedExtensionStats);
        clientSample.iceServers = arrayOrNullFromQueue(String.class, this.addedIceServers);
        clientSample.pcTransports = arrayOrNullFromList(Samples.ClientSample.PeerConnectionTransport.class, pcTransports);
        clientSample.iceCandidatePairs = arrayOrNullFromList(Samples.ClientSample.IceCandidatePair.class, iceCandidatePairs);
        clientSample.mediaSources = arrayOrNullFromQueue(Samples.ClientSample.MediaSourceStat.class, this.mediaSources);
        clientSample.codecs = arrayOrNullFromQueue(Samples.ClientSample.MediaCodecStats.class, this.addedMediaCodecStats);
        clientSample.certificates = arrayOrNullFromQueue(Samples.ClientSample.Certificate.class, this.addedCertificates);
        clientSample.inboundAudioTracks = arrayOrNullFromList(Samples.ClientSample.InboundAudioTrack.class, inboundAudioTracks);
        clientSample.inboundVideoTracks = arrayOrNullFromList(Samples.ClientSample.InboundVideoTrack.class, inboundVideoTracks);;
        clientSample.outboundAudioTracks = arrayOrNullFromList(Samples.ClientSample.OutboundAudioTrack.class, outboundAudioTracks);;
        clientSample.outboundVideoTracks = arrayOrNullFromList(Samples.ClientSample.OutboundVideoTrack.class, outboundVideoTracks);;
        clientSample.iceLocalCandidates = arrayOrNullFromQueue(Samples.ClientSample.IceLocalCandidate.class, this.addedIceLocalCandidates);
        clientSample.iceRemoteCandidates = arrayOrNullFromQueue(Samples.ClientSample.IceRemoteCandidate.class, this.addedIceRemoteCandidates);
        clientSample.dataChannels = arrayOrNullFromList(Samples.ClientSample.DataChannel.class, dataChannels);
        clientSample.timestamp = Instant.now().toEpochMilli();
        clientSample.timeZoneOffsetInHours = this.timeZoneOffsetInHours;
        clientSample.marker = this.marker;

        var controls = new Samples.Controls();
        controls.close = false;

        var samples = new Samples();
        samples.controls = controls;
        samples.clientSamples = new Samples.ClientSample[]{ clientSample };
        samples.sfuSamples = null;
        return samples;
    }

    private class RtpSession {
        final String sfuStreamId;
        final String sfuSinkId;
        final Long SSRC;
        final String peerConnectionId;

        RtpSession(String sfuStreamId, String sfuSinkId, Long ssrc, String peerConnectionId) {
            this.sfuStreamId = sfuStreamId;
            this.sfuSinkId = sfuSinkId;
            SSRC = ssrc;
            this.peerConnectionId = peerConnectionId;
        }
    }

    private class DataChannelSession {

        public final String peerConnectionId;

        private DataChannelSession(String peerConnectionId) {
            this.peerConnectionId = peerConnectionId;
        }
    }
}
