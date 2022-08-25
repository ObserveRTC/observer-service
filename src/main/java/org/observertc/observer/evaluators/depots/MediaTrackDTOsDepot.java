package org.observertc.observer.evaluators.depots;

import org.observertc.observer.dto.MediaKind;
import org.observertc.observer.dto.MediaTrackDTO;
import org.observertc.observer.dto.StreamDirection;
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
    private ObservedClientSample observedClientSample;
    private UUID trackId = null;
    private UUID sfuSinkId = null;
    private UUID sfuStreamId = null;
    private StreamDirection direction = null;
    private Long SSRC = null;
    private UUID peerConnectionId = null;
    private MediaKind kind = null;


    public MediaTrackDTOsDepot setObservedClientSample(ObservedClientSample value) {
        if (Objects.isNull(value)) return this;
        this.observedClientSample = value;
        return this;
    }

    public MediaTrackDTOsDepot setTrackId(UUID value) {
        if (Objects.isNull(value)) return this;
        this.trackId = value;
        return this;
    }

    public MediaTrackDTOsDepot setSfuSinkId(UUID value) {
        if (Objects.isNull(value)) return this;
        this.sfuSinkId = value;
        return this;
    }

    public MediaTrackDTOsDepot setSfuStreamId(UUID value) {
        if (Objects.isNull(value)) return this;
        this.sfuStreamId = value;
        return this;
    }

    public MediaTrackDTOsDepot setStreamDirection(StreamDirection value) {
        if (Objects.isNull(value)) return this;
        this.direction = value;
        return this;
    }

    public MediaTrackDTOsDepot setMediaKind(MediaKind value) {
        if (Objects.isNull(value)) return this;
        this.kind = value;
        return this;
    }

    public MediaTrackDTOsDepot setSSRC(Long value) {
        if (Objects.isNull(value)) return this;
        this.SSRC = value;
        return this;
    }

    public MediaTrackDTOsDepot setPeerConnectionId(UUID value) {
        if (Objects.isNull(value)) return this;
        this.peerConnectionId = value;
        return this;
    }

    private void clean() {
        this.observedClientSample = null;
        this.trackId = null;
        this.sfuSinkId = null;
        this.sfuStreamId = null;
        this.direction = null;
        this.SSRC = null;
        this.peerConnectionId = null;
        this.kind = null;

    }

    public void assemble() {
        try {
            if (Objects.isNull(observedClientSample) || Objects.isNull(observedClientSample.getClientSample())) {
                logger.warn("No observed client sample");
                return;
            }
            if (Objects.isNull(this.trackId)) {
                logger.warn("Cannot create {} without trackId", MediaTrackDTO.class.getSimpleName());
                return;
            }
            if (Objects.isNull(this.direction)) {
                logger.warn("Cannot create {} without direction", MediaTrackDTO.class.getSimpleName());
                return;
            }
            if (Objects.isNull(this.kind)) {
                logger.warn("Cannot create {} without media kind", MediaTrackDTO.class.getSimpleName());
                return;
            }
            if (Objects.isNull(this.SSRC)) {
                logger.warn("Cannot create {} without SSRC", MediaTrackDTO.class.getSimpleName());
                return;
            }
            if (Objects.isNull(this.peerConnectionId)) {
                logger.warn("Cannot create {} without peerConnectionId", MediaTrackDTO.class.getSimpleName());
                return;
            }
            if (this.buffer.containsKey(trackId)) {
                return;
            }
            var clientSample = observedClientSample.getClientSample();

            var mediaTrackDTO = MediaTrackDTO.builder()
                    .withCallId(clientSample.callId)
                    .withServiceId(observedClientSample.getServiceId())
                    .withRoomId(clientSample.roomId)

                    .withClientId(clientSample.clientId)
                    .withUserId(clientSample.userId)
                    .withMediaUnitId(observedClientSample.getMediaUnitId())

                    .withTrackId(trackId)
                    .withSfuStreamId(sfuStreamId)
                    .withSfuSinkId(sfuSinkId)
                    .withDirection(this.direction)
                    .withPeerConnectionId(peerConnectionId)
                    .withSSRC(SSRC)
                    .withAddedTimestamp(clientSample.timestamp)
                    .withMarker(clientSample.marker)
                    .withMediaKind(kind)
                    .build();
            this.buffer.put(mediaTrackDTO.trackId, mediaTrackDTO);
        } catch (Exception ex) {
            logger.warn("Exception occurred while assembling {}", this.getClass().getSimpleName());
        } finally {
            this.clean();
        }
    }

    @Override
    public Map<UUID, MediaTrackDTO> get() {
        if (this.buffer.size() < 1) return Collections.EMPTY_MAP;
        var result = this.buffer;
        this.buffer = new HashMap<>();
        return result;
    }
}
