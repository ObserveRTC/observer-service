package org.observertc.webrtc.observer.samples;

import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

/**
 * Call Samples Builder
 */
public class CallSamplesBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CallSamplesBuilder.class);

    CallSamplesBuilder() {

    }

    private final CallSamples result = new CallSamples();

    public CallSamples build() {
        Objects.requireNonNull(this.result.callId);
        Objects.requireNonNull(this.result.serviceRoomId);
        return this.result;
    }

    public CallSamplesBuilder withCallId(UUID callId) {
        this.result.callId = callId;
        return this;
    }

    public CallSamplesBuilder withServiceRoomId(ServiceRoomId serviceRoomId) {
        this.result.serviceRoomId = serviceRoomId;
        return this;
    }

    public CallSamplesBuilder withClientSamples(ClientSamples clientSamples) {
        Objects.requireNonNull(clientSamples);
        UUID clientId = clientSamples.getClientId();
        ClientSamples prevClientSamples = this.result.clientSamples.get(clientId);
        if (Objects.nonNull(prevClientSamples)) {
            prevClientSamples.addAll(clientSamples);
        } else {
            this.result.clientSamples.put(clientId, clientSamples);
        }
        for (ClientSample clientSample : clientSamples) {
            this.collectPeerConnectionIds(clientId, clientSample);
            ClientSampleVisitor.streamInboundAudioTracks(clientSample)
                    .forEach(track -> {
                        UUID peerConnectionId = UUID.fromString(track.peerConnectionId);
                        UUID trackId = UUID.fromString(track.trackId);
                        this.result.inboundAudioTrackIdsToPeerConnectionIds.put(trackId, peerConnectionId);
                    });
            ClientSampleVisitor.streamInboundVideoTracks(clientSample)
                    .forEach(track -> {
                        UUID peerConnectionId = UUID.fromString(track.peerConnectionId);
                        UUID trackId = UUID.fromString(track.trackId);
                        this.result.inboundVideoTrackIdsToPeerConnectionIds.put(trackId, peerConnectionId);
                    });
            ClientSampleVisitor.streamOutboundAudioTracks(clientSample)
                    .forEach(track -> {
                        UUID peerConnectionId = UUID.fromString(track.peerConnectionId);
                        UUID trackId = UUID.fromString(track.trackId);
                        this.result.outboundAudioTrackIdsToPeerConnectionIds.put(trackId, peerConnectionId);
                    });
            ClientSampleVisitor.streamOutboundVideoTracks(clientSample)
                    .forEach(track -> {
                        UUID peerConnectionId = UUID.fromString(track.peerConnectionId);
                        UUID trackId = UUID.fromString(track.trackId);
                        this.result.outboundVideoTrackIdsToPeerConnectionIds.put(trackId, peerConnectionId);
                    });
        }
        return this;
    }

    private void collectPeerConnectionIds(UUID clientId, ClientSample clientSample) {
        ClientSampleVisitor
                .streamPeerConnectionTransports(clientSample)
                .forEach(peerConnectionTransport -> {
                    var peerConnectionIdStr = peerConnectionTransport.peerConnectionId;
                    var peerConnectionIdHolder = UUIDAdapter.tryParse(peerConnectionIdStr);
                    if (peerConnectionIdHolder.isEmpty()) {
                        return;
                    }
                    var peerConnectionId = peerConnectionIdHolder.get();
                    this.result.peerConnectionIdsToClientIds.put(peerConnectionId, clientId);
                });
    }
}
