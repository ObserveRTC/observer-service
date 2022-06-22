package org.observertc.observer.sinks.firehose;


import io.micronaut.context.annotation.Prototype;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.observertc.observer.common.AwsUtils;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.configbuilders.AbstractBuilder;
import org.observertc.observer.configbuilders.Builder;
import org.observertc.observer.mappings.JsonMapper;
import org.observertc.observer.mappings.Mapper;
import org.observertc.observer.reports.Report;
import org.observertc.observer.reports.ReportTypeVisitor;
import org.observertc.observer.security.credentialbuilders.AwsCredentialsProviderBuilder;
import org.observertc.observer.sinks.Sink;
import org.observertc.schemas.reports.csvsupport.*;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.firehose.FirehoseClient;
import software.amazon.awssdk.services.firehose.model.Record;

import javax.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Prototype
public class FirehoseSinkBuilder extends AbstractBuilder implements Builder<Sink> {

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
        Supplier<FirehoseClient> clientProvider = () -> {
            var region = AwsUtils.getRegion(config.regionId);
            var credentialsProvider = getCredentialProvider(config.credentials);
            return FirehoseClient.builder()
                    .credentialsProvider(credentialsProvider)
                    .region(region)
                    .build();
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

        public Map<String, Object> credentials = null;

        public CSVFormat csvFormat = CSVFormat.DEFAULT;

        public int csvChunkSize = 100;

    }
}
