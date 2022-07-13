package org.observertc.observer.sinks.firehose;


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
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.firehose.FirehoseClient;
import software.amazon.awssdk.services.firehose.model.Record;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
        if (config.defaultDeliveryStreamId == null && config.streams == null) {
            throw new InvalidConfigurationException("Either the default delivery stream id or delivery stream ids must be provided to configure the sink");
        }
        final Map<ReportType, String> deliveryStreamIds;
        if (config.streams != null) {
            deliveryStreamIds = config.streams.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> ReportType.valueOf(Utils.camelCaseToSnakeCase(entry.getKey()).toUpperCase()),
                            Map.Entry::getValue
                    ));
            logger.info("Mappings: {}", JsonUtils.objectToString(deliveryStreamIds));
        } else {
            deliveryStreamIds = Collections.EMPTY_MAP;
        }

        Function<ReportType, String> getDeliveryStreamId;
        if (config.defaultDeliveryStreamId != null && config.streams != null) {
            getDeliveryStreamId = type -> deliveryStreamIds.getOrDefault(type, config.defaultDeliveryStreamId);
        } else if (config.streams != null) {
            getDeliveryStreamId = type -> deliveryStreamIds.get(type);
        } else {
            getDeliveryStreamId = type -> config.defaultDeliveryStreamId;
        }
        result.clientSupplier = clientProvider;
        switch (config.encodingType) {
            case CSV -> result.encoder = this.makeCsvEncoder(getDeliveryStreamId, config.csvFormat, config.csvChunkSize);
            case JSON -> result.encoder = this.makeJsonEncoder(getDeliveryStreamId);
        }
        return result;
    }

    private FormatEncoder<String, Record> makeJsonEncoder(Function<ReportType, String> getDeliveryStreamId) {
        var mapper = JsonMapper.createObjectToBytesMapper();
        return new JsonFormatEncoder<String, Record>(
                getDeliveryStreamId,
                report -> {
                    byte[] bytes = mapper.map(report.payload);
                    if (bytes == null) {
                        logger.warn("Cannot map report {}", JsonUtils.objectToString(report));
                        return null;
                    }
                    return Record.builder()
                            .data(SdkBytes.fromByteArray(bytes))
                            .build();
                },
                logger
        );
    }

    private FormatEncoder<String, Record> makeCsvEncoder(Function<ReportType, String> getDeliveryStreamId, CSVFormat format, int maxChunkSize) {
        return new CsvFormatEncoder<>(
                maxChunkSize,
                getDeliveryStreamId,
                lines -> {
                    var bytes = lines.getBytes();
                    return Record.builder()
                            .data(SdkBytes.fromByteArray(bytes))
                            .build();
                },
                format,
                logger
        );
    }

//    private Mapper<List<Report>, Map<String, List<Record>>> makeCsvEncoder(Function<ReportType, String> getDeliveryStreamId, CSVFormat format, int maxChunkSize) {
//        var mapper = ReportTypeVisitor.<Report, Iterable<?>>createFunctionalVisitor(
//                new ObserverEventReportToIterable(),
//                new CallEventReportToIterable(),
//                new CallMetaReportToIterable(),
//                new ClientExtensionReportToIterable(),
//                new ClientTransportReportToIterable(),
//                new ClientDataChannelReportToIterable(),
//                new InboundAudioTrackReportToIterable(),
//                new InboundVideoTrackReportToIterable(),
//                new OutboundAudioTrackReportToIterable(),
//                new OutboundVideoTrackReportToIterable(),
//                new SfuEventReportToIterable(),
//                new SfuMetaReportToIterable(),
//                new SfuExtensionReportToIterable(),
//                new SFUTransportReportToIterable(),
//                new SfuInboundRtpPadReportToIterable(),
//                new SfuOutboundRtpPadReportToIterable(),
//                new SfuSctpStreamReportToIterable()
//        );
//        return Mapper.create(reports -> {
//            var records = new HashMap<String, List<Record>>();
//            var stringBuilder = new StringBuffer();
//            var csvPrinter = new CSVPrinter(stringBuilder, format);
//            var chunkSize = 0;
//            var reportsByTypes = reports.stream().collect(groupingBy(r -> r.type));
//            for (var it = reportsByTypes.entrySet().iterator(); it.hasNext(); ) {
//                var entry = it.next();
//                var type = entry.getKey();
//                var groupedReports = entry.getValue();
//                for (var jt = groupedReports.iterator(); jt.hasNext(); ) {
//                    var report = jt.next();
//                    var iterable = mapper.apply(report, type);
//                    csvPrinter.printRecord(iterable);
//                    if (++chunkSize < maxChunkSize && jt.hasNext()) {
//                        continue;
//                    }
//                    csvPrinter.flush();
//                    var lines = stringBuilder.toString();
//                    var bytes = lines.getBytes();
//                    Record myRecord = Record.builder()
//                            .data(SdkBytes.fromByteArray(bytes))
//                            .build();
//
//                    String deliveryStreamId = getDeliveryStreamId.apply(report.type);
//                    if (deliveryStreamId == null) {
//                        continue;
//                    }
//                    var deliveryRecords = records.get(deliveryStreamId);
//                    if (deliveryRecords == null) {
//                        deliveryRecords = new LinkedList<>();
//                        records.put(deliveryStreamId, deliveryRecords);
//                    }
//
//                    deliveryRecords.add(myRecord);
//                    stringBuilder = new StringBuffer();
//                    csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.DEFAULT);
//                    chunkSize = 0;
//                }
//            }
//            logger.info("Received {} reports ({} types) mapped to {} different type of records", reports.size(), reportsByTypes.size(), records.size());
//            return records;
//        });
//    }

    public static class Config {

        public enum EncodingType {
            JSON,
            CSV
        }

        @NotNull
        public String regionId;

        public String defaultDeliveryStreamId;

        public Map<String, String> streams = null;

        public EncodingType encodingType = EncodingType.CSV;

        public Map<String, Object> credentials = null;

        public CSVFormat csvFormat = CSVFormat.DEFAULT;

        public int csvChunkSize = 100;

    }
}
