package org.observertc.observer.sinks.firehose;

// https://docs.aws.amazon.com/code-samples/latest/catalog/javav2-firehose-src-main-java-com-example-firehose-PutBatchRecords.java.html

import org.observertc.observer.common.CollectionChunker;
import org.observertc.observer.common.Utils;
import org.observertc.observer.reports.Report;
import org.observertc.observer.sinks.FormatEncoder;
import org.observertc.observer.sinks.Sink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.firehose.FirehoseClient;
import software.amazon.awssdk.services.firehose.model.Record;
import software.amazon.awssdk.services.firehose.model.*;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class FirehoseSink extends Sink {
    private static final Logger logger = LoggerFactory.getLogger(FirehoseSink.class);

    FormatEncoder<String, Record> encoder;
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
        var chunker = CollectionChunker.<Record>builder()
                .setLimit(4 * 1000 * 1000) // 4MByte because of Firehose limitation
                .setSizeFn(record -> record.data().asByteArrayUnsafe().length)
                .setCanOverflowFlag(false)
                .build();

        int retried = 0;
        for (; retried < 3; ++retried) {
            if (this.client == null) {
                this.client = clientSupplier.get();
            }
            try {
                for (var it = records.entrySet().iterator(); it.hasNext(); ) {
                    var entry = it.next();
                    var deliveryStreamId = entry.getKey();
                    for (var jt = chunker.iterate(entry.getValue()); jt.hasNext(); ) {
                        var deliveryRecords = jt.next();
                        PutRecordBatchRequest recordBatchRequest = PutRecordBatchRequest.builder()
                                .deliveryStreamName(deliveryStreamId)
                                .records(deliveryRecords)
                                .build();

                        PutRecordBatchResponse recordResponse = this.client.putRecordBatch(recordBatchRequest);
                        logger.info("{} batch ({} records) are forwarded to stream {}",
                                recordResponse.requestResponses().size(),
                                Utils.firstNotNull(deliveryRecords, Collections.EMPTY_LIST).size(),
                                deliveryStreamId);

                        List<PutRecordBatchResponseEntry> results = recordResponse.requestResponses();
                        for (PutRecordBatchResponseEntry result: results) {
                            if (result.errorCode() == null) {
                                continue;
                            }
                            logger.warn("Error indicated adding record {}. error code: {}, error message: {}", result.recordId(), result.errorCode(), result.errorMessage());
                        }
                    }
                }
                break;
            } catch (ServiceUnavailableException serviceUnavailableException) {
                logger.warn("Service is unavailable error occurred. Records attempted to send will be dropped, but the sink keeps open", serviceUnavailableException);
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
