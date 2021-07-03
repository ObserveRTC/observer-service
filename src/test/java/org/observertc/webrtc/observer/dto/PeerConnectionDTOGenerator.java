package org.observertc.webrtc.observer.dto;

import io.micronaut.context.annotation.Prototype;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Prototype
public class PeerConnectionDTOGenerator implements Supplier<PeerConnectionDTO> {

    private EasyRandom generator;
    private UUID clientId;

    @PostConstruct
    void setup() {
        EasyRandomParameters parameters = new EasyRandomParameters();
        parameters.randomize(UUID.class, () -> UUID.randomUUID());
        this.generator = new EasyRandom(parameters);
    }

    @Override
    public PeerConnectionDTO get() {
        var result = this.generator.nextObject(PeerConnectionDTO.class);
        if (Objects.nonNull(this.clientId)) {
            result.clientId = this.clientId;
        }
        return result;
    }

    public PeerConnectionDTOGenerator withClientId(UUID value) {
        this.clientId = value;
        return this;
    }
}
