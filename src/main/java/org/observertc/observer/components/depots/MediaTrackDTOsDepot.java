package org.observertc.observer.components.depots;

import org.observertc.observer.dto.MediaTrackDTO;
import org.observertc.observer.dto.StreamDirection;
import org.observertc.observer.samples.ClientSampleVisitor;
import org.observertc.observer.samples.ObservedClientSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

/**
 * For single thread only!
 */
public class MediaTrackDTOsDepot implements Supplier<Map<UUID, MediaTrackDTO>> {

    private static final Logger logger = LoggerFactory.getLogger(MediaTrackDTOsDepot.class);

    private Map<UUID, MediaTrackDTO> buffer = new HashMap<>();

    public MediaTrackDTOsDepot addFromObservedClientSample(ObservedClientSample observedClientSample) {
        if (Objects.isNull(observedClientSample) || Objects.isNull(observedClientSample.getClientSample())) {
            logger.warn("No observed client sample");
        }
        var clientSample = observedClientSample.getClientSample();
        ClientSampleVisitor.streamInboundAudioTracks(clientSample)
            .forEach(track -> {
                if (this.buffer.containsKey(track.trackId)) {
                    return;
                }
                var mediaTrackDTO = MediaTrackDTO.builder()
                        .withCallId(clientSample.callId)
                        .withServiceId(observedClientSample.getServiceId())
                        .withRoomId(clientSample.roomId)

                        .withClientId(clientSample.clientId)
                        .withUserId(clientSample.userId)
                        .withMediaUnitId(observedClientSample.getMediaUnitId())

                        .withTrackId(track.trackId)
//                        .withSfuStreamId(track.sfuStreamId)
                        .withSfuSinkId(track.sfuSinkId)
                        .withDirection(StreamDirection.INBOUND)
                        .withPeerConnectionId(track.peerConnectionId)
                        .withSSRC(track.ssrc)
                        .withAddedTimestamp(clientSample.timestamp)
                        .build();
                this.buffer.put(track.trackId, mediaTrackDTO);
        });

        ClientSampleVisitor.streamInboundVideoTracks(clientSample)
            .forEach(track -> {
                var mediaTrackDTO = MediaTrackDTO.builder()
                        .withCallId(clientSample.callId)
                        .withServiceId(observedClientSample.getServiceId())
                        .withRoomId(clientSample.roomId)

                        .withClientId(clientSample.clientId)
                        .withUserId(clientSample.userId)
                        .withMediaUnitId(observedClientSample.getMediaUnitId())

                        .withTrackId(track.trackId)
//                        .withSfuStreamId(track.sfuStreamId)
                        .withSfuSinkId(track.sfuSinkId)
                        .withDirection(StreamDirection.INBOUND)
                        .withPeerConnectionId(track.peerConnectionId)
                        .withSSRC(track.ssrc)
                        .withAddedTimestamp(clientSample.timestamp)
                        .build();
                this.buffer.put(track.trackId, mediaTrackDTO);
        });

        ClientSampleVisitor.streamOutboundAudioTracks(clientSample)
            .forEach(track -> {
                var mediaTrackDTO = MediaTrackDTO.builder()
                        .withCallId(clientSample.callId)
                        .withServiceId(observedClientSample.getServiceId())
                        .withRoomId(clientSample.roomId)

                        .withClientId(clientSample.clientId)
                        .withUserId(clientSample.userId)
                        .withMediaUnitId(observedClientSample.getMediaUnitId())

                        .withTrackId(track.trackId)
                        .withSfuStreamId(track.sfuStreamId)
//                        .withSfuSinkId(track.sfuSinkId)
                        .withDirection(StreamDirection.OUTBOUND)
                        .withPeerConnectionId(track.peerConnectionId)
                        .withSSRC(track.ssrc)
                        .withAddedTimestamp(clientSample.timestamp)
                        .build();
                this.buffer.put(track.trackId, mediaTrackDTO);
        });

        ClientSampleVisitor.streamOutboundVideoTracks(clientSample)
            .forEach(track -> {
                var mediaTrackDTO = MediaTrackDTO.builder()
                        .withCallId(clientSample.callId)
                        .withServiceId(observedClientSample.getServiceId())
                        .withRoomId(clientSample.roomId)

                        .withClientId(clientSample.clientId)
                        .withUserId(clientSample.userId)
                        .withMediaUnitId(observedClientSample.getMediaUnitId())

                        .withTrackId(track.trackId)
                        .withSfuStreamId(track.sfuStreamId)
//                        .withSfuSinkId(track.sfuSinkId)
                        .withDirection(StreamDirection.OUTBOUND)
                        .withPeerConnectionId(track.peerConnectionId)
                        .withSSRC(track.ssrc)
                        .withAddedTimestamp(clientSample.timestamp)
                        .build();
                this.buffer.put(track.trackId, mediaTrackDTO);
        });
        return this;
    }

    @Override
    public Map<UUID, MediaTrackDTO> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_MAP;
        var result = this.buffer;
        this.buffer = new HashMap<>();
        return result;
    }
}
