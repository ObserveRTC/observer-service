package org.observertc.webrtc.observer.dto;

import io.micronaut.context.annotation.Prototype;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Prototype
public class ClientDTOGenerator implements Supplier<ClientDTO> {

    private EasyRandom generator;
    private UUID callId;

    @PostConstruct
    void setup() {
        EasyRandomParameters parameters = new EasyRandomParameters();
        parameters.randomize(UUID.class, () -> UUID.randomUUID());
        this.generator = new EasyRandom(parameters);
    }

    @Override
    public ClientDTO get() {
        var result = this.generator.nextObject(ClientDTO.class);
        if (Objects.nonNull(this.callId)) {
            result.callId = this.callId;
        }
        return result;
    }

    public ClientDTOGenerator withCallId(UUID value) {
        this.callId = value;
        return this;
    }
}
