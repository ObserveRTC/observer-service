package org.observertc.observer.sinks.firehose;

// https://docs.aws.amazon.com/code-samples/latest/catalog/javav2-firehose-src-main-java-com-example-firehose-PutBatchRecords.java.html

import software.amazon.awssdk.services.firehose.FirehoseClient;

public class FirehoseSink {
    FirehoseSink() {
        var client = FirehoseClient.builder()
                .build();
//        Record record = Record.builder().data()
    }
}
