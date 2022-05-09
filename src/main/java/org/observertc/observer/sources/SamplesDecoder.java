package org.observertc.observer.sources;

import io.reactivex.rxjava3.functions.Function;
import org.observertc.observer.configs.TransportFormatType;
import org.observertc.observer.mappings.Decoder;
import org.observertc.observer.mappings.JsonMapper;
import org.observertc.observer.mappings.Mapper;
import org.observertc.schemas.protobuf.ProtobufSamples;
import org.observertc.schemas.protobuf.ProtobufSamplesMapper;
import org.observertc.schemas.samples.Samples;
import org.observertc.schemas.v200.samples.Fromv200ToLatestConverter;
import org.observertc.schemas.v200beta59.samples.Fromv200beta59ToLatestConverter;
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
                    () -> { // <= 2.0.0
                        var samplerMapper = new org.observertc.schemas.v200.protobuf.ProtobufSamplesMapper();
                        return message -> {
                            var protobufSamples = org.observertc.schemas.v200.protobuf.ProtobufSamples.Samples.parseFrom(message);
                            var samples = samplerMapper.apply(protobufSamples);
                            return samples;
                        };
                    },
                    () -> { // <= 2.0.0-beta59
                        var samplerMapper = new org.observertc.schemas.v200beta59.protobuf.ProtobufSamplesMapper();
                        return message -> {
                            var protobufSamples = org.observertc.schemas.v200beta59.protobuf.ProtobufSamples.Samples.parseFrom(message);
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
                    () -> { // <= 2.0.0
                        var samplesV200beta64Mapper = JsonMapper.<org.observertc.schemas.v200.samples.Samples>createBytesToObjectMapper(org.observertc.schemas.v200.samples.Samples.class);
                        Mapper<org.observertc.schemas.v200.samples.Samples, Samples> samplesVersionAligner;
                        var from200ToLatestConverter = new Fromv200ToLatestConverter();
                        return message -> {
                            var samplesV200 = samplesV200beta64Mapper.map(message);
                            if (samplesV200 == null) {
                                throw new RuntimeException("Failed to decode Samples");
                            }
                            var samples = from200ToLatestConverter.apply(samplesV200);
                            return samples;
                        };
                    },
                    () -> { // <= 2.0.0-beta-59
                        var samplesV200beta59Mapper = JsonMapper.<org.observertc.schemas.v200beta59.samples.Samples>createBytesToObjectMapper(org.observertc.schemas.v200beta59.samples.Samples.class);
                        Mapper<org.observertc.schemas.v200beta59.samples.Samples, Samples> samplesVersionAligner;
                        var from200beta59ToLatestConverter = new Fromv200beta59ToLatestConverter();
                        return message -> {
                            var samplesV200beta59 = samplesV200beta59Mapper.map(message);
                            if (samplesV200beta59 == null) {
                                throw new RuntimeException("Failed to decode Samples");
                            }
                            var samples = from200beta59ToLatestConverter.apply(samplesV200beta59);
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
