package org.observertc.webrtc.observer.dto;

import io.micronaut.context.annotation.Prototype;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

@Prototype
public class MediaTrackDTOGenerator implements Supplier<MediaTrackDTO> {

    private EasyRandom generator;
    private StreamDirection streamDirection = null;
    private UUID peerConnectionId = null;
    private UUID clientId = null;
    private UUID callId = null;

    @PostConstruct
    void setup() {
        var random = new Random();
        EasyRandomParameters parameters = new EasyRandomParameters();
        parameters.randomize(UUID.class, () -> UUID.randomUUID());
        parameters.randomize(field -> field.getName().equals("ssrc"), () -> Long.valueOf(Math.abs(random.nextInt())));
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
        if (Objects.nonNull(this.clientId)) {
            result.clientId = this.clientId;
        }
        if (Objects.nonNull(this.callId)) {
            result.callId = this.callId;
        }

        return result;
    }

    public MediaTrackDTOGenerator withCallId(UUID value) {
        this.callId = value;
        return this;
    }

    public MediaTrackDTOGenerator withClientId(UUID value) {
        this.clientId = value;
        return this;
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
