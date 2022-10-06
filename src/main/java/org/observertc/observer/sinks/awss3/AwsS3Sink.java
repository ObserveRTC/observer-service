package org.observertc.observer.sinks.awss3;

// https://docs.aws.amazon.com/code-samples/latest/catalog/javav2-firehose-src-main-java-com-example-firehose-PutBatchRecords.java.html

import org.observertc.observer.reports.Report;
import org.observertc.observer.sinks.FormatEncoder;
import org.observertc.observer.sinks.Sink;
import org.observertc.schemas.reports.CallEventReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.firehose.model.FirehoseException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class AwsS3Sink extends Sink {
    private static final Logger logger = LoggerFactory.getLogger(AwsS3Sink.class);
    private static final String SCHEMA_VERSION = "schema_" + CallEventReport.VERSION.replace(".", "");

    FormatEncoder<String, byte[]> encoder;
    Supplier<S3Client> clientSupplier;
    private S3Client client;
    private final String bucketName;
    Map<String, String> metadata;

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
                    for (var object : objects) {
                        var objectKey = String.format("%s/%s/%s", prefix, SCHEMA_VERSION, UUID.randomUUID());
                        PutObjectRequest putOb = PutObjectRequest.builder()
                                .bucket(this.bucketName)
                                .key(objectKey)
                                .metadata(this.metadata)
                                .build();
                        try {
                            var response = this.client.putObject(putOb, RequestBody.fromBytes(object));
                        } catch (Exception ex) {
                            logger.warn("Exception while uploading to {}", prefix, ex);
                        }
                    }
                }
                break;
            } catch (FirehoseException firehoseException) {
                logger.warn("Firehose error occurred while sending records to firehose {}", firehoseException.getLocalizedMessage(), firehoseException);
                this.client = null;
            } catch (Exception ex) {
                logger.warn("Error occurred while sending records to firehose, sink will be closed. {}", ex.getLocalizedMessage(), ex);
                this.close();
            }
        }
        if (2 < retried) {
            logger.warn("Maximum retries for Firehose client has been reached, sink will be closed");
            this.close();
        }
    }
}
