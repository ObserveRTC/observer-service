package org.observertc.webrtc.observer.dto;

import io.micronaut.context.annotation.Prototype;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Prototype
public class SfuRtpStreamDTOGenerator implements Supplier<SfuRtpStreamDTO> {

    private EasyRandom generator;
    private UUID sfuId;
    private UUID transportId;
    private UUID streamId;

    @PostConstruct
    void setup() {
        EasyRandomParameters parameters = new EasyRandomParameters();
        parameters.randomize(UUID.class, () -> UUID.randomUUID());
        this.generator = new EasyRandom(parameters);
    }

    @Override
    public SfuRtpStreamDTO get() {
        var result = this.generator.nextObject(SfuRtpStreamDTO.class);
        if (Objects.nonNull(this.sfuId)) {
            result.sfuId = this.sfuId;
        }
        if (Objects.nonNull(this.transportId)) {
            result.transportId = this.transportId;
        }
        if (Objects.nonNull(this.streamId)) {
            result.streamId = this.streamId;
        }
        return result;
    }

    public SfuRtpStreamDTOGenerator withSfuId(UUID value) {
        this.sfuId = value;
        return this;
    }

    public SfuRtpStreamDTOGenerator withTransportId(UUID value) {
        this.transportId = value;
        return this;
    }

    public SfuRtpStreamDTOGenerator withStreamId(UUID value) {
        this.streamId = value;
        return this;
    }
}
