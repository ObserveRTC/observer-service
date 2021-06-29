package org.observertc.webrtc.observer.samples;

import java.util.*;
import java.util.stream.Stream;

/**
 * Call assigned and organized ObservedClientSamples
 * Intermediate class organize ClientSamples, and help
 * creating:
 *  - {@link org.observertc.webrtc.observer.dto.CallDTO}
 *  - {@link org.observertc.webrtc.observer.dto.ClientDTO}
 *  - {@link org.observertc.webrtc.observer.dto.PeerConnectionDTO}
 *  - {@link org.observertc.webrtc.observer.dto.MediaTrackDTO}
 *
 *  Stream MediaTracks, PCTransports
 */
public class CallSamples implements Iterable<ClientSamples>, ObservedCall {

    public static CallSamplesBuilder builderFrom(UUID callId, ServiceRoomId serviceRoomId) {
        return new CallSamplesBuilder().withCallId(callId).withServiceRoomId(serviceRoomId);
    }

    UUID callId;
    ServiceRoomId serviceRoomId;
    final Map<UUID, ClientSamples> clientSamples = new HashMap<>();
    final Map<UUID, UUID> peerConnectionIdsToClientIds = new HashMap<>();
    final Map<MediaTrackId, UUID> inboundAudioTrackKeysToPeerConnectionIds = new HashMap<>();
    final Map<MediaTrackId, UUID> inboundVideoTrackKeysToPeerConnectionIds = new HashMap<>();
    final Map<MediaTrackId, UUID> outboundAudioTrackKeysToPeerConnectionIds = new HashMap<>();
    final Map<MediaTrackId, UUID> outboundVideoTrackKeysToPeerConnectionIds = new HashMap<>();


    CallSamples() {
    }

    @Override
    public ServiceRoomId getServiceRoomId() {
        return this.serviceRoomId;
    }

    @Override
    public Iterator<ClientSamples> iterator() {
        return this.clientSamples.values().iterator();
    }

    public Stream<ClientSamples> stream() {
        return this.clientSamples.values().stream();
    }

    public UUID getCallId() {
        return this.callId;
    }

    public Set<UUID> getClientIds() {
        return this.clientSamples.keySet();
    }

    public Set<UUID> getPeerConnectionIds() {
        return this.peerConnectionIdsToClientIds.keySet();
    }

    public Set<MediaTrackId> getInboundAudioTrackKeys() {
        return this.inboundAudioTrackKeysToPeerConnectionIds.keySet();
    }

    public Set<MediaTrackId> getInboundVideoTrackKeys() {
        return this.inboundVideoTrackKeysToPeerConnectionIds.keySet();
    }

    public Set<MediaTrackId> getOutboundAudioTrackKeys() {
        return this.outboundAudioTrackKeysToPeerConnectionIds.keySet();
    }

    public Set<MediaTrackId> getOutboundVideoTrackKeys() {
        return this.outboundVideoTrackKeysToPeerConnectionIds.keySet();
    }


}
