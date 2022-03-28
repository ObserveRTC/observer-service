package org.observertc.observer.simulator;

import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.utils.RandomGenerators;
import org.observertc.observer.utils.TestUtils;
import org.observertc.schemas.samples.Samples;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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

    private ClientSideSamplesGenerator samplesGenerator;

    public ClientSurrogate(String roomId) {
        this.roomId = roomId;
        this.randomGenerators = new RandomGenerators();
        this.userId = this.randomGenerators.getRandomTestUserIds();
        this.clientId = UUID.randomUUID();
        this.samplesGenerator = new ClientSideSamplesGenerator()
                .setClientId(this.clientId)
                .setRoomId(this.roomId)
                .setUserId(this.userId)
                ;
    }

    public Samples getSamples() {
        var result = this.samplesGenerator.get();
        return result;
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

}
