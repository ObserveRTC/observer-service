package org.observertc.webrtc.observer.dto;

import io.micronaut.context.annotation.Prototype;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Prototype
public class SfuDTOGenerator implements Supplier<SfuDTO> {

    private EasyRandom generator;
    private UUID sfuId;

    @PostConstruct
    void setup() {
        EasyRandomParameters parameters = new EasyRandomParameters();
        parameters.randomize(UUID.class, () -> UUID.randomUUID());
        this.generator = new EasyRandom(parameters);
    }

    @Override
    public SfuDTO get() {
        var result = this.generator.nextObject(SfuDTO.class);
        if (Objects.nonNull(this.sfuId)) {
            result.sfuId = this.sfuId;
        }
        return result;
    }

    public SfuDTOGenerator withSfuId(UUID value) {
        this.sfuId = value;
        return this;
    }
}
