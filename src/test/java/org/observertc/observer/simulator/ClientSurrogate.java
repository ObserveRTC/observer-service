package org.observertc.observer.simulator;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.utils.RandomGenerators;
import org.observertc.observer.utils.TestUtils;
import org.observertc.schemas.samples.Samples;
import org.observertc.schemas.samples.Samples.ClientSample;

import java.time.Instant;
import java.util.*;

@Prototype
public class ClientSurrogate implements NetworkLinkProvider {

    public final String roomId;
    public final String userId;
    public final UUID clientId;
    private NetworkLink link;
    private final RandomGenerators randomGenerators;
    private PeerConnection peerConnection;
    private Map<UUID, TrackSurrogate> inboundAudioTracks = new HashMap<>();
    private Map<UUID, TrackSurrogate> inboundVideoTracks = new HashMap<>();
    private Map<UUID, TrackSurrogate> outboundAudioTracks = new HashMap<>();
    private Map<UUID, TrackSurrogate> outboundVideoTracks = new HashMap<>();

    private ClientSample.MediaDevice videoDevice;
    private ClientSample.MediaDevice audioDevice;
    private int samplesSeq = 0;
    private List<String> userMediaErrors;
    private String marker = null;
    private Integer timeZoneOffsetInHour;
    private List<ClientSample.ExtensionStat> extensionStats;
    private ClientSample.OperationSystem operationSystem;
    private ClientSample.Platform platform;
    private ClientSample.Browser browser;
    private ClientSample.Engine engine;
    private ClientSample.Certificate[] certificates;
    private ClientSample.MediaCodecStats[] codecs;
    private ClientSample.IceRemoteCandidate[] iceRemoteCandidates;
    private ClientSample.IceLocalCandidate[] iceLocalCandidates;

    public ClientSurrogate(String roomId) {
        this.roomId = roomId;
        this.randomGenerators = new RandomGenerators();
        this.userId = this.randomGenerators.getRandomTestUserIds();
        this.clientId = UUID.randomUUID();
    }

    public void connect(NetworkLinkProvider linkProvider) {
        if (Objects.nonNull(this.link)) {
            throw new RuntimeException("Link already exists");
        }
        this.link = linkProvider.provideNetworkLink();
        this.createPeerConnection();
    }

    public NetworkLink provideNetworkLink() {
        if (Objects.isNull(this.link)) {
            this.link = new NetworkLink();
            this.createPeerConnection();
        }
        return this.link;
    }

    private void createPeerConnection() {
        if (Objects.nonNull(this.peerConnection)) {
            throw new RuntimeException("Peer Connection is already created");
        }
        this.peerConnection = this.link.createPeerConnection(new NetworkLinkEvents() {
            @Override
            public void onRtpSessionAdded(RtpSessionSurrogate session) {
                var peerConnectionId = peerConnection.getId();
                var track = TrackSurrogate.createFromSession(peerConnectionId, session);
                var inboundTracks = session.kind.equals(TestUtils.AUDIO_KIND) ? inboundAudioTracks : inboundVideoTracks;
                inboundTracks.put(track.trackId, track);
            }

            @Override
            public void onRtpSessionRemoved(RtpSessionSurrogate session) {
                var peerConnectionId = peerConnection.getId();
                var inboundTracks = session.kind.equals(TestUtils.AUDIO_KIND) ? inboundAudioTracks : inboundVideoTracks;
                var foundTrackId = inboundTracks.values().stream()
                        .filter(track -> track.peerConnectionId == peerConnectionId && track.SSRC == session.SSRC)
                        .map(track -> track.trackId)
                        .findFirst();
                if (foundTrackId.isPresent()) {
                    inboundTracks.remove(foundTrackId.get());
                }
            }
        });
    }

    public void turnMicOn() {
        if (0 < this.outboundAudioTracks.size()) {
            return;
        }
        var session = RtpSessionSurrogate.createAudioSession();
        this.peerConnection.addRtpSession(session);
        var track = TrackSurrogate.createFromSession(this.peerConnection.getId(), session);
        this.outboundAudioTracks.put(track.trackId, track);
    }

    public void turnMicOff() {
        if (this.outboundAudioTracks.size() < 1) {
            return;
        }
        this.outboundAudioTracks.forEach((trackId, track) -> {
            this.peerConnection.closeRtpSession(track.SSRC);
        });
        this.outboundAudioTracks.clear();
    }

    public void turnCamOn() {
        if (0 < this.outboundVideoTracks.size()) {
            return;
        }
        var session = RtpSessionSurrogate.createVideoSession();
        this.peerConnection.addRtpSession(session);
        var track = TrackSurrogate.createFromSession(this.peerConnection.getId(), session);
        this.outboundVideoTracks.put(track.trackId, track);
    }

    public void turnCamOff() {
        if (this.outboundVideoTracks.size() < 1) {
            return;
        }
        this.outboundVideoTracks.forEach((trackId, track) -> {
            this.peerConnection.closeRtpSession(track.SSRC);
        });
        this.outboundVideoTracks.clear();
    }

    public void addUserMediaError(String errorMessage) {
        if (Objects.isNull(this.userMediaErrors)) this.userMediaErrors = new LinkedList<>();
        this.userMediaErrors.add(errorMessage);
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public void setTimeZoneOffsetInHour(int value) {
        this.timeZoneOffsetInHour = value;
    }

    public void addExtensionStat(ClientSample.ExtensionStat value) {
        if (Objects.isNull(this.extensionStats)) this.extensionStats = new LinkedList<>();
        this.extensionStats.add(value);
    }


    public Samples generateSamples() {
        var result = new Samples();
        var meta = new Samples.SamplesMeta();
        meta.schemaVersion = Samples.VERSION;
        result.meta = meta;
        result.clientSamples = this.generateClientSample();
        return result;
    }

    private ClientSample[] generateClientSample() {
        var result = new ClientSample();
        result.roomId = this.roomId;
        result.userId = this.userId;
        result.clientId = this.clientId;
        result.callId = null;


        result.mediaDevices = TestUtils.arrayOrNull(this.generateAudioDevice(), this.generateVideoDevice());
        result.sampleSeq = ++this.samplesSeq;
        result.timestamp = Instant.now().toEpochMilli();
        result.marker = this.marker;
        result.timeZoneOffsetInHours = this.timeZoneOffsetInHour;
        result.userMediaErrors = (String[]) this.userMediaErrors.toArray();
        result.browser = this.generateBrowser();
        result.certificates = this.generateCertificates();
        result.codecs = this.generateCodecs();
//        result.dataChannels =
        result.os = this.generateOs();
        result.engine = this.generateEngine();
        result.extensionStats = (ClientSample.ExtensionStat[]) this.extensionStats.toArray();
        result.iceLocalCandidates = this.generateIceLocalCandidates();
        result.iceRemoteCandidates = this.generateIceRemoteCandidates();
        result.iceServers = this.generateIceServer();
        result.platform = this.generatePlatform();
        result.outboundAudioTracks = this.generateOutboundAudioTracks();
        result.outboundVideoTracks = this.generateOutboundVideoTracks();
        result.inboundAudioTracks = this.generateInboundAudioTracks();
        result.inboundVideoTracks = this.generateInboundVideoTracks();
        result.pcTransports = this.generatePcTransports();
        this.extensionStats = null;
        this.userMediaErrors = null;
        return (ClientSample[]) List.of(result).toArray();
    }

    private String[] generateIceServer() {
        var result = new String[1];
        result[0] = "https://myPreciousIceServer.com";
        return result;
    }

    private ClientSample.PeerConnectionTransport[] generatePcTransports() {
        var result = new ClientSample.PeerConnectionTransport[1];
        result[0] = ClientSample.PeerConnectionTransport.newBuilder()
                .setPeerConnectionId(this.peerConnection.getId())
                .build();
        return result;
    }

    private ClientSample.IceLocalCandidate[] generateIceLocalCandidates() {
        if (Objects.nonNull(this.iceLocalCandidates)) {
            return null;
        }
        this.iceLocalCandidates = new ClientSample.IceLocalCandidate[1];
        var candidate = new ClientSample.IceLocalCandidate();
        candidate.candidateType = this.randomGenerators.getRandomICECandidateTypes();
        candidate.id = UUID.randomUUID().toString();
        candidate.peerConnectionId = this.peerConnection.getId();
        candidate.address = this.randomGenerators.getRandomIPv4Address();
        candidate.port = this.randomGenerators.getRandomPort();
        candidate.priority = this.randomGenerators.getRandomPositiveLong();
        candidate.protocol = this.randomGenerators.getRandomNetworkTransportProtocols();
        candidate.relayProtocol = this.randomGenerators.getRandomRelayProtocols();
        candidate.url = "https://something.com";
        candidate.transportId = UUID.randomUUID();
        this.iceLocalCandidates[0] = candidate;
        return this.iceLocalCandidates;
    }

    private ClientSample.IceRemoteCandidate[] generateIceRemoteCandidates() {
        if (Objects.nonNull(this.iceRemoteCandidates)) {
            return null;
        }
        this.iceRemoteCandidates = new ClientSample.IceRemoteCandidate[1];
        var candidate = new ClientSample.IceRemoteCandidate();
        candidate.candidateType = this.randomGenerators.getRandomICECandidateTypes();
        candidate.id = UUID.randomUUID().toString();
        candidate.peerConnectionId = this.peerConnection.getId();
        candidate.address = this.randomGenerators.getRandomIPv4Address();
        candidate.port = this.randomGenerators.getRandomPort();
        candidate.priority = this.randomGenerators.getRandomPositiveLong();
        candidate.protocol = this.randomGenerators.getRandomNetworkTransportProtocols();
        candidate.relayProtocol = this.randomGenerators.getRandomRelayProtocols();
        candidate.url = "https://something.com";
        candidate.transportId = UUID.randomUUID();
        this.iceRemoteCandidates[0] = candidate;
        return this.iceRemoteCandidates;
    }

    private ClientSample.MediaCodecStats[] generateCodecs() {
        if (Objects.nonNull(this.codecs)) {
            return null;
        }
        this.codecs = new ClientSample.MediaCodecStats[1];
        var codec = new ClientSample.MediaCodecStats();
        codec.codecType = "encoder";
        codec.channels = 2;
        codec.clockRate = 48000;
        codec.mimeType = "audio/opus";
        codec.payloadType = "111";
        codec.sdpFmtpLine = "audio/opus";
        this.codecs[0] = codec;
        return this.codecs;
    }

    private ClientSample.Certificate[] generateCertificates() {
        if (Objects.nonNull(this.certificates)) {
            return null;
        }
        this.certificates = new ClientSample.Certificate[1];
        var certificate = new ClientSample.Certificate();
        certificate.base64Certificate = UUID.randomUUID().toString();
        certificate.issuerCertificateId = UUID.randomUUID().toString();
        certificate.fingerprint = UUID.randomUUID().toString();
        certificate.fingerprintAlgorithm = "NONE";
        this.certificates[0] = certificate;
        return this.certificates;
    }

    private ClientSample.Browser generateBrowser() {
        if (Objects.nonNull(this.browser)) {
            // already generated and provided
            return null;
        }
        var result = new ClientSample.Browser();
        result.name = "Chrome";
        result.version = "99.0.4844.51";
        this.browser = result;
        return this.browser;
    }

    private ClientSample.Engine generateEngine() {
        if (Objects.nonNull(this.engine)) {
            // already generated and provided
            return null;
        }
        var result = new ClientSample.Engine();
        result.name = "Chrome";
        result.version = "99.0.4844.51";
        this.engine = result;
        return this.engine;
    }

    private ClientSample.Platform generatePlatform() {
        if (Objects.nonNull(this.platform)) {
            // already generated and provided
            return null;
        }
        var result = new ClientSample.Platform();
        result.model = "Mac";
        result.type = "BigSur";
        result.vendor = "Apple";
        this.platform = result;
        return this.platform;
    }

    private ClientSample.OperationSystem generateOs() {
        if (Objects.nonNull(this.operationSystem)) {
            // already generated and provided
            return null;
        }
        var result = new ClientSample.OperationSystem();
        result.name = "Windows";
        result.versionName = "Chicago";
        result.version = "11.1.23.5.312";
        this.operationSystem = result;
        return this.operationSystem;
    }

    private ClientSample.MediaDevice generateVideoDevice() {
        if (Objects.nonNull(this.videoDevice)) {
            // already generated and provided
            return null;
        }

        var result = new ClientSample.MediaDevice();
        result.id = UUID.randomUUID().toString();
        result.kind = TestUtils.VIDEO_KIND;
        result.label = UUID.randomUUID().toString();
        this.videoDevice = result;
        return this.videoDevice;
    }

    private ClientSample.MediaDevice generateAudioDevice() {
        if (Objects.nonNull(this.audioDevice)) {
            // already generated and provided
            return null;
        }
        var result = new ClientSample.MediaDevice();
        result.id = UUID.randomUUID().toString();
        result.kind = TestUtils.AUDIO_KIND;
        result.label = UUID.randomUUID().toString();
        this.audioDevice = result;
        return result;
    }

    private ClientSample.OutboundAudioTrack[] generateOutboundAudioTracks() {
        var result = new LinkedList<ClientSample.OutboundAudioTrack>();
        for (var track : this.outboundVideoTracks.values()) {
            var entry = new ClientSample.OutboundAudioTrack();
            entry.peerConnectionId = track.peerConnectionId;
            entry.trackId = track.trackId;
            entry.ssrc = track.SSRC;
            // random values
        }
        if (result.size() < 1) return null;
        return (ClientSample.OutboundAudioTrack[]) result.toArray();
    }

    private ClientSample.OutboundVideoTrack[] generateOutboundVideoTracks() {
        var result = new LinkedList<ClientSample.OutboundVideoTrack>();
        for (var track : this.outboundVideoTracks.values()) {
            var entry = new ClientSample.OutboundVideoTrack();
            entry.peerConnectionId = track.peerConnectionId;
            entry.trackId = track.trackId;
            entry.ssrc = track.SSRC;
            // random values
        }
        if (result.size() < 1) return null;
        return (ClientSample.OutboundVideoTrack[]) result.toArray();
    }

    private ClientSample.InboundAudioTrack[] generateInboundAudioTracks() {
        var result = new LinkedList<ClientSample.InboundAudioTrack>();
        for (var track : this.inboundVideoTracks.values()) {
            var entry = new ClientSample.InboundAudioTrack();
            entry.peerConnectionId = track.peerConnectionId;
            entry.trackId = track.trackId;
            entry.ssrc = track.SSRC;
            // random values
        }
        if (result.size() < 1) return null;
        return (ClientSample.InboundAudioTrack[]) result.toArray();
    }

    private ClientSample.InboundVideoTrack[] generateInboundVideoTracks() {
        var result = new LinkedList<ClientSample.InboundVideoTrack>();
        for (var track : this.inboundVideoTracks.values()) {
            var entry = new ClientSample.InboundVideoTrack();
            entry.peerConnectionId = track.peerConnectionId;
            entry.trackId = track.trackId;
            entry.ssrc = track.SSRC;
            // random values
        }
        if (result.size() < 1) return null;
        return (ClientSample.InboundVideoTrack[]) result.toArray();
    }
}
