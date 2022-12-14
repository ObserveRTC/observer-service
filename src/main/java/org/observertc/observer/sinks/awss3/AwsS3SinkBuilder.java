package org.observertc.observer.sinks.awss3;


import io.micronaut.context.annotation.Prototype;
import org.apache.commons.csv.CSVFormat;
import org.observertc.observer.common.AwsUtils;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configs.InvalidConfigurationException;
import org.observertc.observer.mappings.JsonMapper;
import org.observertc.observer.reports.Report;
import org.observertc.observer.reports.ReportType;
import org.observertc.observer.security.credentialbuilders.AwsCredentialsProviderBuilder;
import org.observertc.observer.sinks.*;
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
public class AwsS3SinkBuilder extends AbstractBuilder implements SinkBuilder {

    private Essentials essentials;

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
    public void setEssentials(Essentials essentials) {
        this.essentials = essentials;
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
        var result = new AwsS3Sink(
                config.parallelism,
                config.bucketName
        );

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

        Function<ReportType, String> reportTypePrefix;
        if (config.defaultPrefix != null && config.prefixes != null) {
            reportTypePrefix = type -> prefixMapper.getOrDefault(type, config.defaultPrefix);
        } else if (config.prefixes != null) {
            reportTypePrefix = type -> prefixMapper.get(type);
        } else {
            reportTypePrefix = type -> config.defaultPrefix;
        }

        Function<Report, String> s3prefix;
        if (config.structured) {
            var objKeyAssigner = new ObjectHierarchyKeyAssignerBuilder();
            s3prefix = objKeyAssigner.create(
                    reportTypePrefix
            );
        } else {
            s3prefix = report -> reportTypePrefix.apply(report.type);
        }

        result.clientSupplier = clientProvider;
        switch (config.encodingType) {
            case CSV -> {
                result.encoder = this.makeCsvEncoder(s3prefix, config.csvFormat, 450);
                result.metadata = Map.of("Content-Type", "text/csv");
            }
            case JSON -> {
                result.encoder = this.makeJsonEncoder(s3prefix);
                result.metadata = Map.of("Content-Type", "text/json");
            }
        }
        result.createIndexes = config.createIndexes;
        return result;
    }

    private FormatEncoder<String, byte[]> makeJsonEncoder(Function<Report, String> s3Prefix) {
        var mapper = JsonMapper.createObjectToBytesMapper(true);
        return new JsonFormatEncoder<String, byte[]>(
                s3Prefix,
                report -> mapper.map(report.payload),
                logger
        );
    }

    private FormatEncoder<String, byte[]> makeCsvEncoder(Function<Report, String> s3Prefix, CSVFormat format, int maxChunkSize) {
        return new CsvFormatEncoder<>(
                maxChunkSize,
                s3Prefix,
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

        public boolean structured = false;

        public Map<String, String> prefixes = null;

        public EncodingType encodingType = EncodingType.CSV;

        public Map<String, Object> credentials = null;

        public boolean createIndexes = false;

        public CSVFormat csvFormat = CSVFormat.DEFAULT;

        public int parallelism = 10;

    }
}
