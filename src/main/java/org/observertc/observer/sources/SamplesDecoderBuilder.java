package org.observertc.observer.sources;

import org.observertc.observer.configs.TransportCodecType;
import org.observertc.observer.mappings.Decoder;
import org.observertc.observer.mappings.JsonMapper;
import org.observertc.observer.mappings.Mapper;
import org.observertc.schemas.samples.Samples;

import java.util.Objects;

public class SamplesDecoderBuilder {
    private Mapper<byte[], byte[]> decrypter = null;
    private Mapper<byte[], Samples> decoder;

    public SamplesDecoderBuilder withCodecType(TransportCodecType codecType) {
        switch (codecType) {
            default:
            case JSON:
                this.decoder = JsonMapper.<Samples>createBytesToObjectMapper(Samples.class);
                break;
            case NONE:
                this.decoder = data -> {
                    throw new RuntimeException("No format type is configured to deserialize");
                };
        }
        return this;
    }

    public Decoder<byte[], Samples> build() {
        Objects.requireNonNull(this.decoder, "No Decoder is configured to build a Transport");
        Mapper<byte[], Samples> decoder;
        if (Objects.nonNull(this.decrypter)) {
            decoder = Mapper.link(this.decrypter, this.decoder);
        } else {
            decoder = this.decoder;
        }
        return new Decoder<byte[], Samples>() {
            @Override
            public Samples decode(byte[] data) {
                return decoder.map(data);
            }
        };
    }
}
