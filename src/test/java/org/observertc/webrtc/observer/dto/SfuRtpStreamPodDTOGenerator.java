package org.observertc.webrtc.observer.dto;

import io.micronaut.context.annotation.Prototype;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Prototype
public class SfuRtpStreamPodDTOGenerator implements Supplier<SfuRtpPadDTO> {

    private EasyRandom generator;
    private UUID sfuId;
    private UUID streamId;

    @PostConstruct
    void setup() {
        EasyRandomParameters parameters = new EasyRandomParameters();
        parameters.randomize(UUID.class, () -> UUID.randomUUID());
        this.generator = new EasyRandom(parameters);
    }

    @Override
    public SfuRtpPadDTO get() {
        var result = this.generator.nextObject(SfuRtpPadDTO.class);
        if (Objects.nonNull(this.sfuId)) {
            result.sfuId = this.sfuId;
        }
        if (Objects.nonNull(this.streamId)) {
            result.rtpStreamId = this.streamId;
        }
        return result;
    }

    public SfuRtpStreamPodDTOGenerator withSfuId(UUID value) {
        this.sfuId = value;
        return this;
    }

    public SfuRtpStreamPodDTOGenerator withStreamId(UUID value) {
        this.streamId = value;
        return this;
    }
}
