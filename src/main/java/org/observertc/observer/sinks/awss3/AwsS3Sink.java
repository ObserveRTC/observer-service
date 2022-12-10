package org.observertc.observer.sinks.awss3;

// https://docs.aws.amazon.com/code-samples/latest/catalog/javav2-firehose-src-main-java-com-example-firehose-PutBatchRecords.java.html

import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.reports.Report;
import org.observertc.observer.sinks.FormatEncoder;
import org.observertc.observer.sinks.Sink;
import org.observertc.schemas.reports.CallEventReport;
import org.observertc.schemas.reports.SfuEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class AwsS3Sink extends Sink {
    private static final Logger logger = LoggerFactory.getLogger(AwsS3Sink.class);
    private static final String SCHEMA_VERSION = "schema_" + CallEventReport.VERSION.replace(".", "");

    FormatEncoder<String, byte[]> encoder;
    Supplier<S3Client> clientSupplier;
    private S3Client client;
    private final String bucketName;
    Map<String, String> metadata;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.systemDefault());
    boolean createIndexes = false;

    AwsS3Sink(String bucketName) {
        this.bucketName = bucketName;
    }


    @Override
    protected void process(List<Report> reports) {
        if (reports == null || reports.size() < 1) {
            return;
        }
        var records = this.encoder.map(reports);
        if (records == null || records.size() < 1) {
            return;
        }
        logger.info("start processing {} reports", reports.size());
        Instant now = Instant.now();
        try {
            String nowInString = dateFormatter.format(now);
            int retried = 0;
            for (; retried < 3; ++retried) {
                if (this.client == null) {
                    this.client = clientSupplier.get();
                }
                try {
                    for (var it = records.entrySet().iterator(); it.hasNext(); ) {
                        var entry = it.next();
                        var prefix = entry.getKey();
                        var objects = entry.getValue();
                        if (objects == null || objects.size() < 1) {
                            logger.warn("No encoded objects for prefix {}", prefix);
                            continue;
                        }

                        int counter = 0;
                        for (var object : objects) {
                            var objectKey = String.format("%s/%s-%d", prefix, nowInString, ++counter);
                            var putObBuilder = PutObjectRequest.builder()
                                    .bucket(this.bucketName)
                                    .key(objectKey)
                                    .metadata(this.metadata)
                                    ;
                            var putOb = putObBuilder.build();
                            try {
                                var response = this.client.putObject(putOb, RequestBody.fromBytes(object));
                            } catch (Exception ex) {
                                logger.warn("Exception while uploading to {}", prefix, ex);
                            }
                        }
                        logger.info("Uploaded {} objects to a bucket prefixed with {}", counter, prefix);
                    }
                    break;
                } catch (S3Exception s3Exception) {
                    logger.warn("S3 error occurred while sending records to S3 {}", s3Exception.getLocalizedMessage(), s3Exception);
                    this.client = null;
                } catch (Exception ex) {
                    logger.warn("Error occurred while sending records to S3, sink will be closed. {}", ex.getLocalizedMessage(), ex);
                    this.close();
                }
            }
            if (2 < retried) {
                logger.warn("Maximum retries for Firehose client has been reached, sink will be closed");
                this.close();
            } else if (createIndexes) {
                this.makeIndexes(reports);
            }
        } finally {
            logger.info("end processing {} reports. Elapsed time in ms: {}", reports.size(), Instant.now().toEpochMilli() - now.toEpochMilli());
        }
    }

    private void makeIndexes(List<Report> reports) {
        for (var report : reports) {
            switch (report.type) {
                case CALL_EVENT -> {
                    var callEventReport = (CallEventReport) report.payload;
                    switch (callEventReport.name) {
                        case "CALL_STARTED" -> {
                            this.uploadIndex(
                                    "calls/" + callEventReport.callId,
                                    JsonUtils.objectToString(callEventReport)
                            );
                            var serviceRoomKey = "service-rooms/" + callEventReport.serviceId.replace("/", "") + "/" + callEventReport.roomId.replace("/", "") + "/" + callEventReport.callId;
                            this.uploadIndex(
                                    serviceRoomKey,
                                    JsonUtils.objectToString(callEventReport)
                            );
                        }
                        case "MEDIA_TRACK_ADDED" -> {
                            this.uploadIndex(
                                    "media-tracks/" + callEventReport.mediaTrackId,
                                    JsonUtils.objectToString(callEventReport)
                            );
                        }
                        case "PEER_CONNECTION_OPENED" -> {
                            this.uploadIndex(
                                    "peer-connections/" + callEventReport.peerConnectionId,
                                    JsonUtils.objectToString(callEventReport)
                            );
                        }
                        case "CLIENT_JOINED" -> {
                            this.uploadIndex(
                                    "clients/" + callEventReport.clientId,
                                    JsonUtils.objectToString(callEventReport)
                            );
                        }
                    }
                }
                case SFU_EVENT -> {
                    var sfuEventReport = (SfuEventReport) report.payload;
                    switch (sfuEventReport.name) {
                        case "SFU_RTP_PAD_ADDED" -> {
                            var payload = JsonUtils.objectToString(sfuEventReport);
                            this.uploadIndex(
                                    "sfu-rtp-pads/" + sfuEventReport.rtpPadId,
                                    payload
                            );
                            if (sfuEventReport.mediaSinkId != null) {
                                this.uploadIndex(
                                        "sfu-media-sinks/" + sfuEventReport.mediaSinkId,
                                        payload
                                );
                            }
                            if (sfuEventReport.mediaStreamId != null) {
                                this.uploadIndex(
                                        "sfu-media-streams/" + sfuEventReport.mediaStreamId,
                                        payload
                                );
                            }
                        }
                        case "SFU_TRANSPORT_OPENED" -> {
                            var payload = JsonUtils.objectToString(sfuEventReport);
                            this.uploadIndex(
                                    "sfu-transports/" + sfuEventReport.transportId,
                                    payload
                            );
                        }
                    }
                }
            }
        }
    }

    private void uploadIndex(String key, String payload) {
        if (key == null || payload == null) {
            logger.warn("Cannot upload null key ({}) or value ({}) as index", key, payload);
            return;
        }
        try {
            var objectKey = "__indexes/" + key;
            var putObBuilder = PutObjectRequest.builder()
                    .bucket(this.bucketName)
                    .key(objectKey)
                    .metadata(this.metadata)
                    ;
            var putOb = putObBuilder.build();
            try {
                var response = this.client.putObject(putOb, RequestBody.fromString(payload));
            } catch (Exception ex) {
                logger.warn("Exception while uploading to {}", objectKey, ex);
            }
        } catch (S3Exception s3Exception) {
            logger.warn("S3 error occurred while sending records to S3 {}", s3Exception.getLocalizedMessage(), s3Exception);
            this.client = null;
        } catch (Exception ex) {
            logger.warn("Error occurred while sending records to S3, sink will be closed. {}", ex.getLocalizedMessage(), ex);
            this.close();
        }
    }
}
