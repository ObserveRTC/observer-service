package org.observertc.webrtc.observer.dto;

import io.micronaut.context.annotation.Prototype;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.function.Supplier;

@Prototype
public class CallDTOGenerator implements Supplier<CallDTO> {

    private EasyRandom generator;

    @PostConstruct
    void setup() {
        EasyRandomParameters parameters = new EasyRandomParameters();
        parameters.randomize(UUID.class, () -> UUID.randomUUID());
        this.generator = new EasyRandom(parameters);
    }

    @Override
    public CallDTO get() {
        return this.generator.nextObject(CallDTO.class);
    }
}
