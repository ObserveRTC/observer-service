package org.observertc.observer.sources;

import org.observertc.observer.configs.TransportCodecType;
import org.observertc.observer.mappings.Decoder;
import org.observertc.observer.mappings.JsonMapper;
import org.observertc.observer.mappings.Mapper;
import org.observertc.schemas.protobuf.ProtobufSamples;
import org.observertc.schemas.samples.Samples;

import java.util.Objects;

public class SamplesDecoder implements Decoder<byte[], Samples> {

    public static Builder builder() {
        return new Builder();
    }

    private Mapper<byte[], Samples> decoder;

    private SamplesDecoder() {

    }

    @Override
    public Samples decode(byte[] data) {
        return this.decoder.map(data);
    }

    public static final class Builder {
        private Mapper<byte[], byte[]> decrypter = null;
        private Mapper<byte[], Samples> decoder = null;

        public Builder withCodecType(TransportCodecType codecType) {
            Mapper<byte[], ProtobufSamples.Samples> protobufReader;
            Mapper<ProtobufSamples.Samples, Samples> protobufMapper;
            switch (codecType) {
                case PROTOBUF:
                    protobufReader = Mapper.create(ProtobufSamples.Samples::parseFrom);
                    var func = new ProtobufSamplesMapper();
                    protobufMapper = Mapper.create(func::apply);
                    this.decoder = Mapper.link(protobufReader, protobufMapper);
                    break;
                case JSON:
                    this.decoder = JsonMapper.<Samples>createBytesToObjectMapper(Samples.class);
                    break;
                default:
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
            var result = new SamplesDecoder();
            result.decoder = decoder;
            return result;
        }
    }
}
