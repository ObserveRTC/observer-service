package org.observertc.observer.sinks.firehose;


import io.micronaut.context.annotation.Prototype;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.observertc.observer.configs.InvalidConfigurationException;
import org.observertc.observer.mappings.JsonMapper;
import org.observertc.observer.mappings.Mapper;
import org.observertc.observer.reports.Report;
import org.observertc.observer.reports.ReportTypeVisitor;
import org.observertc.observer.sinks.Sink;
import org.observertc.schemas.reports.csvsupport.*;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.profiles.ProfileFile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.firehose.FirehoseClient;
import software.amazon.awssdk.services.firehose.model.Record;

import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Prototype
public class FirehoseSinkBuilder extends AbstractBuilder implements Builder<Sink> {

    @Override
    public Sink build() {
        var config = this.convertAndValidate(Config.class);
        AtomicReference<FirehoseClient> clientHolder = new AtomicReference<>(null);
        Supplier<FirehoseClient> clientProvider = () -> {
            var region = getRegion(config.regionId);
            var credentialsProvider = getCredentialProvider(config);
            var builder = FirehoseClient.builder()
                    .region(region);

            if (credentialsProvider != null) {
                builder.credentialsProvider(credentialsProvider);
            }
            return builder.build();
        };
        var result = new FirehoseSink();
        result.deliveryStreamId = config.deliveryStreamId;
        result.clientSupplier = clientProvider;
        switch (config.encodingType) {
            case CSV -> result.encoder = this.makeCsvEncoder(config.csvFormat, config.csvChunkSize);
            case JSON -> result.encoder = this.makeJsonEncoder();
        }
        return result;
    }

    private static ProfileCredentialsProvider getCredentialProvider(Config config) {
        if (config.profileFilePath == null && config.profileName == null) {
            return null;
//            throw new InvalidConfigurationException("profileFile or profileId, must be given");
        }
        var builder = ProfileCredentialsProvider.builder();
        if (config.profileName != null) {
            builder.profileName(config.profileName);
        }
        if (config.profileFilePath != null) {
            var path = Path.of(config.profileFilePath);
            var type = ProfileFile.Type.valueOf(config.profileFileType);
            var profileFile = ProfileFile.builder().content(path).type(type).build();
            builder.profileFile(profileFile);
        }

        return builder.build();
    }

    private Mapper<List<Report>, List<Record>> makeJsonEncoder() {
        var mapper = JsonMapper.createObjectToBytesMapper();
        return Mapper.create(reports -> {
            List<Record> records = new LinkedList<>();
            for (var report : reports) {
                byte[] bytes = mapper.map(report);
                if (bytes == null) {
                    logger.warn("Cannot map report {}", JsonUtils.objectToString(report));
                    continue;
                }

                Record myRecord = Record.builder()
                        .data(SdkBytes.fromByteArray(bytes))
                        .build();

                records.add(myRecord);
            }
            return records;
        });


    }

    private Mapper<List<Report>, List<Record>> makeCsvEncoder(CSVFormat format, int maxChunkSize) {
        var mapper = ReportTypeVisitor.<Report, Iterable<?>>createFunctionalVisitor(
                new ObserverEventReportToIterable(),
                new CallEventReportToIterable(),
                new CallMetaReportToIterable(),
                new ClientExtensionReportToIterable(),
                new ClientTransportReportToIterable(),
                new ClientDataChannelReportToIterable(),
                new InboundAudioTrackReportToIterable(),
                new InboundVideoTrackReportToIterable(),
                new OutboundAudioTrackReportToIterable(),
                new OutboundVideoTrackReportToIterable(),
                new SfuEventReportToIterable(),
                new SfuMetaReportToIterable(),
                new SfuExtensionReportToIterable(),
                new SFUTransportReportToIterable(),
                new SfuInboundRtpPadReportToIterable(),
                new SfuOutboundRtpPadReportToIterable(),
                new SfuSctpStreamReportToIterable()
        );
        return Mapper.<List<Report>, List<Record>>create(reports -> {
            var records = new LinkedList<Record>();
            var stringBuilder = new StringBuffer();
            var csvPrinter = new CSVPrinter(stringBuilder, format);
            var chunkSize = 0;
            for (var report : reports) {
                var iterable = mapper.apply(report, report.type);
                csvPrinter.printRecord(iterable);
                if (++chunkSize < maxChunkSize) {
                    continue;
                }
                csvPrinter.flush();
                var lines = stringBuilder.toString();
                var bytes = lines.getBytes();
                Record myRecord = Record.builder()
                        .data(SdkBytes.fromByteArray(bytes))
                        .build();

                records.add(myRecord);
                stringBuilder = new StringBuffer();
                csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.DEFAULT);
                chunkSize = 0;
            }
            if (0 < chunkSize) {
                var lines = stringBuilder.toString();
                var bytes = lines.getBytes();
                Record myRecord = Record.builder()
                        .data(SdkBytes.fromByteArray(bytes))
                        .build();

                records.add(myRecord);
            }
            return records;
        });
    }

    private static Region getRegion(String configuredRegion) {
        var foundRegion = Region.regions().stream().filter(awsRegion -> awsRegion.id().equals(configuredRegion)).findFirst();
        if (foundRegion.isEmpty()) {
            String availableRegions = Region.regions().stream().map(region -> region.id()).collect(Collectors.joining(", "));
            throw new InvalidConfigurationException("Invalid AWS region: " + configuredRegion + ": " + availableRegions);
        }
        return foundRegion.get();
    }

    public static class Config {

        public enum EncodingType {
            JSON,
            CSV
        }

        @NotNull
        public String regionId;

        @NotNull
        public String deliveryStreamId;

        public EncodingType encodingType = EncodingType.CSV;

        public String profileFilePath;
        public String profileFileType;

        public String profileName;

        public CSVFormat csvFormat = CSVFormat.DEFAULT;

        public int csvChunkSize = 100;

    }
}
