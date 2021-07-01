package org.observertc.webrtc.observer.dto;

import io.micronaut.context.annotation.Prototype;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Prototype
public class MediaTrackDTOGenerator implements Supplier<MediaTrackDTO> {

    private EasyRandom generator;
    private StreamDirection streamDirection = null;
    private UUID peerConnectionId = null;

    @PostConstruct
    void setup() {
        EasyRandomParameters parameters = new EasyRandomParameters();
        this.generator = new EasyRandom(parameters);
    }

    @Override
    public MediaTrackDTO get() {
        var result = this.generator.nextObject(MediaTrackDTO.class);
        if (Objects.nonNull(this.streamDirection)) {
            result.direction = this.streamDirection;
        }
        if (Objects.nonNull(this.peerConnectionId)) {
            result.peerConnectionId = this.peerConnectionId;
        }
        return result;
    }

    public MediaTrackDTOGenerator withPeerConnectionId(UUID value) {
        this.peerConnectionId = value;
        return this;
    }

    public MediaTrackDTOGenerator withStreamDirection(StreamDirection streamDirection) {
        this.streamDirection = streamDirection;
        return this;
    }
}
