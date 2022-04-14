package org.observertc.observer.sources;

import org.observertc.observer.configs.TransportCodecType;
import org.observertc.observer.mappings.Decoder;
import org.observertc.observer.mappings.JsonMapper;
import org.observertc.observer.mappings.Mapper;
import org.observertc.schemas.protobuf.ProtobufSamples;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

public class SamplesDecoder implements Decoder<byte[], Samples> {

    public static Builder builder(Logger logger) {
        return new Builder(logger);
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
        private final Logger logger;

        public Builder(Logger logger) {
            this.logger = logger;
        }

        public Builder withCodecType(TransportCodecType codecType) {
            switch (codecType) {
                case PROTOBUF:
                    this.decoder = this.createProtobufCodec();
                    break;
                case JSON:
                    this.decoder = this.createJsonCodec();
                    break;
                default:
                case NONE:
                    this.decoder = data -> {
                        throw new RuntimeException("No format type is configured to deserialize");
                    };
            }
            return this;
        }

        private Mapper<byte[], Samples> createProtobufCodec() {
            logger.info("Suppoerted schema versions: {}", Samples.VERSION);

            Mapper<byte[], ProtobufSamples.Samples> protobufReader;
            Mapper<ProtobufSamples.Samples, Samples> protobufMapper;
            protobufReader = Mapper.create(ProtobufSamples.Samples::parseFrom);
            var func = new ProtobufSamplesMapper();
            protobufMapper = Mapper.create(func::apply);
            var result = Mapper.link(protobufReader, protobufMapper);;
            return result;
        }

        private Mapper<byte[], Samples> createJsonCodec() {
            var supportedSchemaVersions = List.of(
                    Samples.VERSION
            );
            logger.info("Suppoerted schema versions: {}", String.join(", ", supportedSchemaVersions));

            var result = JsonMapper.<Samples>createBytesToObjectMapper(Samples.class);
            return result;
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
