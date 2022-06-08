package org.observertc.observer.sinks.firehose;

// https://docs.aws.amazon.com/code-samples/latest/catalog/javav2-firehose-src-main-java-com-example-firehose-PutBatchRecords.java.html

import org.observertc.observer.mappings.Mapper;
import org.observertc.observer.reports.Report;
import org.observertc.observer.sinks.Sink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.firehose.FirehoseClient;
import software.amazon.awssdk.services.firehose.model.Record;
import software.amazon.awssdk.services.firehose.model.*;

import java.util.List;
import java.util.function.Supplier;

public class FirehoseSink extends Sink {
    private static final Logger logger = LoggerFactory.getLogger(FirehoseSink.class);


    String streamName;
//    Mapper<Report, byte[]> encoder;
    Mapper<List<Report>, List<Record>> encoder;
    Supplier<FirehoseClient> clientSupplier;
    private FirehoseClient client;

    FirehoseSink() {
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
                PutRecordBatchRequest recordBatchRequest = PutRecordBatchRequest.builder()
                        .deliveryStreamName(this.streamName)
                        .records(records)
                        .build();

                PutRecordBatchResponse recordResponse = this.client.putRecordBatch(recordBatchRequest);
                logger.info("{} records are forwarded to stream {}", recordResponse.requestResponses().size(), this.streamName);

                List<PutRecordBatchResponseEntry> results = recordResponse.requestResponses();
                for (PutRecordBatchResponseEntry result: results) {
                    if (result.errorCode() == null) {
                        continue;
                    }
                    logger.warn("Error indicated adding record {}. error code: {}, error message: {}", result.recordId(), result.errorCode(), result.errorMessage());
                }
                break;
            } catch (FirehoseException firehoseException) {
                logger.warn("Firehose error occurred while sending records to firehose", firehoseException.getLocalizedMessage());
                this.client = null;
            } catch (Throwable t) {
                logger.warn("Error occurred while sending records to firehose, sink will be closed ", t);
                this.close();
            }
        }
        if (2 < retried) {
            logger.warn("Maximum retries for Firehose client has been reached, sink will be closed");
            this.close();
        }
    }
}
