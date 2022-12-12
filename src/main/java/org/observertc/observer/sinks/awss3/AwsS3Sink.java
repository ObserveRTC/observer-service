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
import java.util.concurrent.*;
import java.util.function.Supplier;

public class AwsS3Sink extends Sink {
    private static final Logger logger = LoggerFactory.getLogger(AwsS3Sink.class);
    private static final String SCHEMA_VERSION = "schema_" + CallEventReport.VERSION.replace(".", "");

    FormatEncoder<String, byte[]> encoder;
    Supplier<S3Client> clientSupplier;
    private S3Client client;
    private final String bucketName;
    Map<String, String> metadata;
    private final Sender sender;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.systemDefault());
    boolean createIndexes = false;

    AwsS3Sink(
            int parallelism,
            String bucketName
    ) {

        this.bucketName = bucketName;
        this.sender = new Sender(parallelism);
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
                        var payloads = entry.getValue();
                        if (payloads == null || payloads.size() < 1) {
                            logger.warn("No encoded objects for prefix {}", prefix);
                            continue;
                        }
                        var counter = 0;
                        for (var payload : payloads) {
                            var objectKey = String.format("%s/%s-%d", prefix, nowInString, ++counter);
                            this.sender.submit(objectKey, RequestBody.fromBytes(payload));
//                            var putObBuilder = PutObjectRequest.builder()
//                                    .bucket(this.bucketName)
//                                    .key(objectKey)
//                                    .metadata(this.metadata)
//                                    ;
//                            var putOb = putObBuilder.build();
//                            try {
//                                var response = this.client.putObject(putOb, RequestBody.fromBytes(payload));
//                            } catch (Exception ex) {
//                                logger.warn("Exception while uploading to {}", prefix, ex);
//                            }
                        }
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
                return;
            }
            if (createIndexes) {
                this.makeIndexes(reports);
            }
            this.sender.await();
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
                            this.sender.submit(
                                    "__indexes/calls/" + callEventReport.callId,
                                    RequestBody.fromString(JsonUtils.objectToString(callEventReport))
                            );
                            var serviceRoomKey = "__indexes/service-rooms/" + callEventReport.serviceId.replace("/", "") + "/" + callEventReport.roomId.replace("/", "") + "/" + callEventReport.callId;
                            this.sender.submit(
                                    serviceRoomKey,
                                    RequestBody.fromString(JsonUtils.objectToString(callEventReport))
                            );
                        }
                        case "MEDIA_TRACK_ADDED" -> {
                            this.sender.submit(
                                    "__indexes/media-tracks/" + callEventReport.mediaTrackId,
                                    RequestBody.fromString(JsonUtils.objectToString(callEventReport))
                            );
                            this.sender.submit(
                                    "__indexes/media-tracks-by-clients/" + callEventReport.clientId + "/" + callEventReport.mediaTrackId,
                                    RequestBody.fromString(JsonUtils.objectToString(callEventReport))
                            );
                        }
                        case "PEER_CONNECTION_OPENED" -> {
                            this.sender.submit(
                                    "__indexes/peer-connections/" + callEventReport.peerConnectionId,
                                    RequestBody.fromString(JsonUtils.objectToString(callEventReport))
                            );
                        }
                        case "CLIENT_JOINED" -> {
                            this.sender.submit(
                                    "__indexes/clients/" + callEventReport.clientId,
                                    RequestBody.fromString(JsonUtils.objectToString(callEventReport))
                            );
                        }
                    }
                }
                case SFU_EVENT -> {
                    var sfuEventReport = (SfuEventReport) report.payload;
                    switch (sfuEventReport.name) {
                        case "SFU_RTP_PAD_ADDED" -> {
                            var payload = RequestBody.fromString(JsonUtils.objectToString(sfuEventReport));
                            this.sender.submit(
                                    "__indexes/sfu-rtp-pads/" + sfuEventReport.rtpPadId,
                                    payload
                            );
                            this.sender.submit(
                                    "__indexes/sfu-media-sinks/" + sfuEventReport.mediaSinkId,
                                    payload
                            );
                            this.sender.submit(
                                    "__indexes/sfu-media-streams/" + sfuEventReport.mediaStreamId,
                                    payload
                            );
                        }
                        case "SFU_TRANSPORT_OPENED" -> {
                            var payload = RequestBody.fromString(JsonUtils.objectToString(sfuEventReport));
                            this.sender.submit(
                                    "__indexes/sfu-transports/" + sfuEventReport.transportId,
                                    payload
                            );
                        }
                    }
                }
            }
        }
    }

    private class Sender {
        private final int executorsNum;
        private final ExecutorService executor;
        private final Map<String, Future<Boolean>> promises = new ConcurrentHashMap<>();
        private volatile int ongoing = 0;

        Sender(int executorsNum) {
            this.executorsNum = executorsNum;
            this.executor = Executors.newFixedThreadPool(executorsNum);
        }

        public void submit(String objectKey, RequestBody payload) {
            var task = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    var client = AwsS3Sink.this.client;
                    if (client == null) {
                        logger.warn("Cannot upload {} because there is no client", objectKey);
                        return false;
                    }
                    var putObBuilder = PutObjectRequest.builder()
                            .bucket(AwsS3Sink.this.bucketName)
                            .key(objectKey)
                            .metadata(AwsS3Sink.this.metadata)
                            ;
                    var putOb = putObBuilder.build();
                    try {
                        var response = client.putObject(putOb, payload);
                    } catch (Exception ex) {
                        logger.warn("Exception while uploading {}", objectKey, ex);
                        return false;
                    }
                    return true;
                }
            };
            this.promises.put(objectKey, this.executor.submit(task));
        }

        public void await() {
            if (this.promises.isEmpty()) {
                return;
            }
            var total = 0;
            var success = 0;
            for (var it = this.promises.entrySet().iterator(); it.hasNext(); ++total) {
                var entry = it.next();
                var objectKey = entry.getKey();
                var promise = entry.getValue();
                try {
                    if (promise.get(30000, TimeUnit.MILLISECONDS)) {
                        ++success;
                    }
                } catch (Exception e) {
                    logger.warn("Error occurred while uploading {}. ", objectKey, e);
                }
            }
            this.promises.clear();
            logger.info("Sending batch complete. Parallel workers: {}, successful sending: {}, failed: {}, total: {}",
                    this.executorsNum,
                    success,
                    total - success,
                    total
            );
        }
    }
}
