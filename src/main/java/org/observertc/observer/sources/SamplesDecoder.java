package org.observertc.observer.sources;

import io.reactivex.rxjava3.functions.Function;
import org.observertc.observer.configs.TransportFormatType;
import org.observertc.observer.mappings.Decoder;
import org.observertc.observer.mappings.JsonMapper;
import org.observertc.observer.mappings.Mapper;
import org.observertc.schemas.protobuf.ProtobufSamples;
import org.observertc.schemas.protobuf.ProtobufSamplesMapper;
import org.observertc.schemas.samples.Samples;
import org.observertc.schemas.v210.samples.Fromv210ToLatestConverter;
import org.slf4j.Logger;

import java.util.Objects;

class SamplesDecoder implements Decoder<byte[], Samples> {

    public static Builder builder(Logger logger) {
        return new Builder(logger);
    }

    private Function<byte[], Samples> decoder;

    private SamplesDecoder() {

    }

    @Override
    public Samples decode(byte[] data) throws Throwable {
        return this.decoder.apply(data);
    }

    public static final class Builder {
        private TransportFormatType format;
        private String version;
        private Mapper<byte[], Samples> decoder = null;
        private final Logger logger;
        public Builder(Logger logger) {
            this.logger = logger;
        }

        public Builder withVersion(String version) {
            if (!SamplesVersionVisitor.isVersionValid(version)) {
                throw new RuntimeException("Not valid or not supported schema version: " + version);
            }
            this.version = version;
            return this;
        }

        public Builder withFormatType(TransportFormatType formatType) {
            this.format = formatType;
            return this;
        }

        public SamplesDecoder build() {
            Objects.requireNonNull(this.format, "No format is configured to build a Decoder");
            Objects.requireNonNull(this.version, "No version is configured to build a Decoder");
            var result = new SamplesDecoder();
            switch (this.format) {
                case PROTOBUF:
                    result.decoder = this.createProtobufDecoder();
                    break;
                case JSON:
                    result.decoder = this.createJsonDecoder();
                    break;
                default:
                case NONE:
                    result.decoder = data -> {
                        throw new RuntimeException("No format type is configured to deserialize");
                    };
            }
            return result;
        }


        private Function<byte[], Samples> createProtobufDecoder() {
            var result = SamplesVersionVisitor.<Function<byte[], Samples>>createSupplierVisitor(
                    () -> { // latest
                        var samplerMapper = new ProtobufSamplesMapper();
                        return message -> {
                            var protobufSamples = ProtobufSamples.Samples.parseFrom(message);
                            var samples = samplerMapper.apply(protobufSamples);
                            return samples;
                        };
                    },
                    () -> { // v2.1.0
                        var samplerMapper = new org.observertc.schemas.v210.protobuf.ProtobufSamplesMapper();
                        return message -> {
                            var protobufSamples = org.observertc.schemas.v210.protobuf.ProtobufSamples.Samples.parseFrom(message);
                            var samples = samplerMapper.apply(protobufSamples);
                            return samples;
                        };
                    },
                    () -> { // not recognized
                        throw new RuntimeException("Not recognized version" + this.version);
                    }
            ).apply(null, this.version);
            return result;
        }

        private Function<byte[], Samples> createJsonDecoder() {
            var result = SamplesVersionVisitor.<Function<byte[], Samples>>createSupplierVisitor(
                    () -> { // latest
                        var decoder = JsonMapper.<Samples>createBytesToObjectMapper(Samples.class);
                        return message -> {
                            var samples = decoder.map(message);
                            if (samples == null) {
                                throw new RuntimeException("Failed to decode Samples");
                            }
                            return samples;
                        };
                    },
                    () -> { // v2.1.0
                        var decoder = JsonMapper.<org.observertc.schemas.v210.samples.Samples>createBytesToObjectMapper(org.observertc.schemas.v210.samples.Samples.class);
                        Mapper<org.observertc.schemas.v210.samples.Samples, Samples> samplesVersionAligner;
                        var from210LatestConverter = new Fromv210ToLatestConverter();
                        return message -> {
                            var samplesV210 = decoder.map(message);
                            if (samplesV210 == null) {
                                throw new RuntimeException("Failed to decode Samples");
                            }
                            var samples = from210LatestConverter.apply(samplesV210);
                            return samples;
                        };
                    },
                    () -> {
                        throw new RuntimeException("Not recognized version" + this.version);
                    }
            ).apply(null, this.version);
            return result;
        }
    }
}
