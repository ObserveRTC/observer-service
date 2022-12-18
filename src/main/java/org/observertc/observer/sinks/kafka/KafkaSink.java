package org.observertc.observer.sinks.kafka;


import io.reactivex.rxjava3.annotations.NonNull;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.observertc.observer.reports.Report;
import org.observertc.observer.sinks.Sink;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;

public class KafkaSink extends Sink {
    private UUID key = UUID.randomUUID();
    private Properties properties;
    private boolean tryReconnectOnFailure;
    private int consecutiveEmptyLists = 0;
    private final Function<Report, ProducerRecord<UUID, Bytes>> recorder;
    private KafkaProducer<UUID, Bytes> producer;

    public KafkaSink(Properties properties,
                     boolean tryReconnectOnFailure,
                     Function<Report, ProducerRecord<UUID, Bytes>> recorder
    ) {
        this.properties = properties;
        this.tryReconnectOnFailure = tryReconnectOnFailure;
        this.recorder = recorder;
    }

    @Override
    public void open() {
        Objects.requireNonNull(this.properties);
        this.producer = new KafkaProducer<UUID, Bytes>(this.properties);
        super.open();
    }


    @Override
    public void process(@NonNull List<Report> reports) {
        if (reports.size() < 1) {
            if (3 < ++this.consecutiveEmptyLists) {
                // keep the connection alive
            }
            return;
        } else {
            this.consecutiveEmptyLists = 0;
        }

        int sent = 0;
        for (int tried = 0; tried < 3; ++tried) {
            try {
                int recordsCounter = 0;
                for (var report : reports) {
                    if (++recordsCounter < sent) {
                        continue;
                    }
                    var producerRecord = this.recorder.apply(report);
                    this.producer.send(producerRecord);
                    ++sent;
                }
                break;
            } catch (Exception ex) {
                logger.error("Unexpected exception while sending reports", ex);
                if (this.tryReconnectOnFailure) {
                    try {
                        this.open();
                        continue;
                    } catch (Exception ex2) {
                        logger.error("Error occurred while we tried to sent the messages at the second attempt after reconnect.", ex2);
                    }
                }
                break;
            }
        }
    }
}
