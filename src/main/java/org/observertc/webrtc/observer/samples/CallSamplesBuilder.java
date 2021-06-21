package org.observertc.webrtc.observer.samples;

import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

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
        if (Objects.isNull(prevClientSamples)) {
            prevClientSamples.addAll(clientSamples);
        } else {
            this.result.clientSamples.put(clientId, clientSamples);
        }
        for (ClientSample clientSample : clientSamples) {
            this.collectPeerConnectionIds(clientId, clientSample);
            this.collectInboundAudioTrackKeys(clientId, clientSample);
            this.collectInboundVideoTrackKeys(clientId, clientSample);
            this.collectOutboundAudioTrackKeys(clientId, clientSample);
            this.collectOutboundVideoTrackKeys(clientId, clientSample);
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

    private void collectInboundAudioTrackKeys(UUID clientId, ClientSample clientSample) {
        var stream = ClientSampleVisitor.streamInboundAudioTracks(clientSample);
        collectMediaTrack(stream,
                inboundAudioTrack -> inboundAudioTrack.peerConnectionId,
                inboundAudioTrack -> inboundAudioTrack.ssrc,
                this.result.inboundAudioTrackKeysToPeerConnectionIds
        );
    }

    private void collectInboundVideoTrackKeys(UUID clientId, ClientSample clientSample) {
        var stream = ClientSampleVisitor.streamInboundVideoTracks(clientSample);
        collectMediaTrack(stream,
                inboundVideoTrack -> inboundVideoTrack.peerConnectionId,
                inboundVideoTrack -> inboundVideoTrack.ssrc,
                this.result.inboundVideoTrackKeysToPeerConnectionIds
        );
    }

    private void collectOutboundAudioTrackKeys(UUID clientId, ClientSample clientSample) {
        var stream = ClientSampleVisitor.streamOutboundAudioTracks(clientSample);
        collectMediaTrack(stream,
                outboundAudioTrack -> outboundAudioTrack.peerConnectionId,
                outboundAudioTrack -> outboundAudioTrack.ssrc,
                this.result.outboundAudioTrackKeysToPeerConnectionIds
        );
    }

    private void collectOutboundVideoTrackKeys(UUID clientId, ClientSample clientSample) {
        var stream = ClientSampleVisitor.streamOutboundVideoTracks(clientSample);
        collectMediaTrack(stream,
                outboundVideoTrack -> outboundVideoTrack.peerConnectionId,
                outboundVideoTrack -> outboundVideoTrack.ssrc,
                this.result.outboundVideoTrackKeysToPeerConnectionIds
        );
    }


    private<T> void collectMediaTrack(Stream<T> stream, Function<T, String> getPeerConnectionId, Function<T, Long> getSSRC, Map<MediaTrackId, UUID> mediaTracksMap) {
        stream.forEach(item -> {
            var peerConnectionString = getPeerConnectionId.apply(item);
            var peerConnectionIdHolder = UUIDAdapter.tryParse(peerConnectionString);
            if (peerConnectionIdHolder.isEmpty()) {
                logger.warn("Peer Connection Id {} cannot be parsed for PeerConnectionTransport: {}", peerConnectionString, ObjectToString.toString(item));
            }
            var peerConnectionId = peerConnectionIdHolder.get();
            var ssrc = getSSRC.apply(item);
            var mediaTrackId = MediaTrackId.make(peerConnectionId, ssrc);
            mediaTracksMap.put(mediaTrackId, peerConnectionId);
        });
    }

}
