package org.observertc.webrtc.observer.dto;

import io.micronaut.context.annotation.Prototype;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Prototype
public class SfuTransportDTOGenerator implements Supplier<SfuTransportDTO> {

    private EasyRandom generator;
    private UUID sfuId;
    private UUID transportId;

    @PostConstruct
    void setup() {
        EasyRandomParameters parameters = new EasyRandomParameters();
        parameters.randomize(UUID.class, () -> UUID.randomUUID());
        this.generator = new EasyRandom(parameters);
    }

    @Override
    public SfuTransportDTO get() {
        var result = this.generator.nextObject(SfuTransportDTO.class);
        if (Objects.nonNull(this.sfuId)) {
            result.sfuId = this.sfuId;
        }
        if (Objects.nonNull(this.transportId)) {
            result.transportId = this.transportId;
        }
        return result;
    }

    public SfuTransportDTOGenerator withSfuId(UUID value) {
        this.sfuId = value;
        return this;
    }

    public SfuTransportDTOGenerator withTransportId(UUID value) {
        this.transportId = value;
        return this;
    }
}
