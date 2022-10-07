//package org.observertc.observer.sinks.file;
//
//
//import io.micronaut.context.annotation.Prototype;
//import org.apache.commons.csv.CSVFormat;
//import org.observertc.observer.common.AwsUtils;
//import org.observertc.observer.common.JsonUtils;
//import org.observertc.observer.configbuilders.AbstractBuilder;
//import org.observertc.observer.configbuilders.Builder;
//import org.observertc.observer.mappings.JsonMapper;
//import org.observertc.observer.reports.Report;
//import org.observertc.observer.security.credentialbuilders.AwsCredentialsProviderBuilder;
//import org.observertc.observer.sinks.CsvFormatEncoder;
//import org.observertc.observer.sinks.FormatEncoder;
//import org.observertc.observer.sinks.JsonFormatEncoder;
//import org.observertc.observer.sinks.Sink;
//import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
//import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
//import software.amazon.awssdk.core.SdkBytes;
//import software.amazon.awssdk.services.firehose.FirehoseClient;
//import software.amazon.awssdk.services.firehose.model.Record;
//
//import java.util.Map;
//import java.util.function.Function;
//import java.util.function.Supplier;
//
//@Prototype
//public class FileSinkBuilder extends AbstractBuilder implements Builder<Sink> {
//
//    private static AwsCredentialsProvider getCredentialProvider(Map<String, Object> config) {
//        if (config == null) {
//            logger.info("Default AWS credential is used for sink");
//            return DefaultCredentialsProvider.create();
//        }
//        var providerBuilder = new AwsCredentialsProviderBuilder();
//        providerBuilder.withConfiguration(config);
//        return providerBuilder.build();
//    }
//
//    @Override
//    public Sink build() {
//        var config = this.convertAndValidate(Config.class);
//        Supplier<FirehoseClient> clientProvider = () -> {
//            var region = AwsUtils.getRegion(config.regionId);
//            var credentialsProvider = getCredentialProvider(config.credentials);
//            return FirehoseClient.builder()
//                    .credentialsProvider(credentialsProvider)
//                    .region(region)
//                    .build();
//        };
//        var result = new FileSinkBuilder();
//        return result;
//    }
//
//    private FormatEncoder<String, Record> makeJsonEncoder(Function<Report, String> getDeliveryStreamId) {
//        var mapper = JsonMapper.createObjectToBytesMapper();
//        return new JsonFormatEncoder<String, Record>(
//                getDeliveryStreamId,
//                report -> {
//                    byte[] bytes = mapper.map(report.payload);
//                    if (bytes == null) {
//                        logger.warn("Cannot map report {}", JsonUtils.objectToString(report));
//                        return null;
//                    }
//                    return Record.builder()
//                            .data(SdkBytes.fromByteArray(bytes))
//                            .build();
//                },
//                logger
//        );
//    }
//
//    private FormatEncoder<String, Record> makeCsvEncoder(Function<Report, String> getDeliveryStreamId, CSVFormat format, int maxChunkSize) {
//        return new CsvFormatEncoder<>(
//                maxChunkSize,
//                getDeliveryStreamId,
//                lines -> {
//                    var bytes = lines.getBytes();
//                    return Record.builder()
//                            .data(SdkBytes.fromByteArray(bytes))
//                            .build();
//                },
//                format,
//                logger
//        );
//    }
//
//    public static class Config {
//
//        public String filePrefix = "";
//
//        public String path = "./";
//
//    }
//}
