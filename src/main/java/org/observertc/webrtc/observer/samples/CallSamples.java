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
    final Map<UUID, UUID> inboundAudioTrackIdsToPeerConnectionIds = new HashMap<>();
    final Map<UUID, UUID> inboundVideoTrackIdsToPeerConnectionIds = new HashMap<>();
    final Map<UUID, UUID> outboundAudioTrackIdsToPeerConnectionIds = new HashMap<>();
    final Map<UUID, UUID> outboundVideoTrackIdsToPeerConnectionIds = new HashMap<>();


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

    public Set<UUID> getInboundAudioTrackIds() {
        return this.inboundAudioTrackIdsToPeerConnectionIds.keySet();
    }

    public Set<UUID> getInboundVideoTrackIds() {
        return this.inboundVideoTrackIdsToPeerConnectionIds.keySet();
    }

    public Set<UUID> getOutboundAudioTrackIds() {
        return this.outboundAudioTrackIdsToPeerConnectionIds.keySet();
    }

    public Set<UUID> getOutboundVideoTrackIds() {
        return this.outboundVideoTrackIdsToPeerConnectionIds.keySet();
    }


}
