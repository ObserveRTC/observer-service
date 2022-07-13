package org.observertc.observer.sinks.awss3;


import io.micronaut.context.annotation.Prototype;
import org.apache.commons.csv.CSVFormat;
import org.observertc.observer.common.AwsUtils;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.observertc.observer.configs.InvalidConfigurationException;
import org.observertc.observer.mappings.JsonMapper;
import org.observertc.observer.reports.ReportType;
import org.observertc.observer.security.credentialbuilders.AwsCredentialsProviderBuilder;
import org.observertc.observer.sinks.CsvFormatEncoder;
import org.observertc.observer.sinks.FormatEncoder;
import org.observertc.observer.sinks.JsonFormatEncoder;
import org.observertc.observer.sinks.Sink;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Prototype
public class AwsS3SinkBuilder extends AbstractBuilder implements Builder<Sink> {

    private static AwsCredentialsProvider getCredentialProvider(Map<String, Object> config) {
        if (config == null) {
            logger.info("Default AWS credential is used for sink");
            return DefaultCredentialsProvider.create();
        }
        var providerBuilder = new AwsCredentialsProviderBuilder();
        providerBuilder.withConfiguration(config);
        return providerBuilder.build();
    }

    @Override
    public Sink build() {
        var config = this.convertAndValidate(Config.class);
        Supplier<S3Client> clientProvider = () -> {
            var region = AwsUtils.getRegion(config.regionId);
            var credentialsProvider = getCredentialProvider(config.credentials);

            return S3Client.builder()
                    .credentialsProvider(credentialsProvider)
                    .region(region)
                    .build();
        };
        var result = new AwsS3Sink(config.bucketName);

        if (config.defaultPrefix == null && config.prefixes == null) {
            throw new InvalidConfigurationException("Either the default delivery stream id or delivery stream ids must be provided to configure the sink");
        }
        final Map<ReportType, String> prefixMapper;
        if (config.prefixes != null) {
            prefixMapper = config.prefixes.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> ReportType.valueOf(Utils.camelCaseToSnakeCase(entry.getKey()).toUpperCase()),
                            Map.Entry::getValue
                    ));
            logger.info("Mappings: {}", JsonUtils.objectToString(prefixMapper));
        } else {
            prefixMapper = Collections.EMPTY_MAP;
        }

        Function<ReportType, String> getDeliveryStreamId;
        if (config.defaultPrefix != null && config.prefixes != null) {
            getDeliveryStreamId = type -> prefixMapper.getOrDefault(type, config.defaultPrefix);
        } else if (config.prefixes != null) {
            getDeliveryStreamId = type -> prefixMapper.get(type);
        } else {
            getDeliveryStreamId = type -> config.defaultPrefix;
        }
        result.clientSupplier = clientProvider;
        switch (config.encodingType) {
            case CSV -> {
                result.encoder = this.makeCsvEncoder(getDeliveryStreamId, config.csvFormat, 450);
                result.metadata = Map.of("Content-Type", "text/csv");
            }
            case JSON -> {
                result.encoder = this.makeJsonEncoder(getDeliveryStreamId);
                result.metadata = Map.of("Content-Type", "text/plain");
            }
        }
        return result;
    }

    private FormatEncoder<String, byte[]> makeJsonEncoder(Function<ReportType, String> getDeliveryStreamId) {
        var mapper = JsonMapper.createObjectToBytesMapper();
        return new JsonFormatEncoder<String, byte[]>(
                getDeliveryStreamId,
                report -> mapper.map(report.payload),
                logger
        );
    }

    private FormatEncoder<String, byte[]> makeCsvEncoder(Function<ReportType, String> getDeliveryStreamId, CSVFormat format, int maxChunkSize) {
        return new CsvFormatEncoder<>(
                maxChunkSize,
                getDeliveryStreamId,
                String::getBytes,
                format,
                logger
        );
    }

    public static class Config {

        public enum EncodingType {
            JSON,
            CSV
        }

        @NotNull
        public String regionId;

        @NotNull
        public String bucketName;

        public String defaultPrefix;

        public Map<String, String> prefixes = null;

        public EncodingType encodingType = EncodingType.CSV;

        public Map<String, Object> credentials = null;

        public CSVFormat csvFormat = CSVFormat.DEFAULT;

    }
}
