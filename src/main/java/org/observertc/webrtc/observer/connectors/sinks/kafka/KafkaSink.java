package org.observertc.webrtc.observer.connectors.sinks.kafka;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.observertc.webrtc.observer.connectors.EncodedRecord;
import org.observertc.webrtc.observer.connectors.sinks.Sink;
import org.observertc.webrtc.schemas.reports.Report;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

public class KafkaSink extends Sink {
    private String topic;
    private Properties properties;
    private boolean tryReconnectOnFailure = false;
    private KafkaProducer<UUID, Bytes> producer;
    private final BinaryMessageEncoder<Report> encoder;
    public KafkaSink() {
        this.encoder = Report.getEncoder();
    }

    protected void connect() {
        Objects.requireNonNull(this.properties);
        this.producer = new KafkaProducer<UUID, Bytes>(this.properties);
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        this.connect();
    }



    @Override
    public void onNext(@NonNull List<EncodedRecord> records) {
        if (records.size() < 1) {
            return;
        }
        try {
            for (EncodedRecord record : records) {
                ProducerRecord<UUID, Bytes> producerRecord = this.makeProducerRecord(record);
                this.producer.send(producerRecord);
            }
        } catch (Throwable t) {
            logger.error("Unexpected exception", t);
            if (this.tryReconnectOnFailure) {
                try {
                    this.connect();
                    for (EncodedRecord record : records) {
                        ProducerRecord<UUID, Bytes> producerRecord = this.makeProducerRecord(record);
                        this.producer.send(producerRecord);
                    }
                } catch (Throwable t2) {
                    logger.error("Error occurred while we tried to sent the messages at the second attempt after reconnect. this is bad, messages will be lost", t2);
                }
            }
        }
    }

    private ProducerRecord<UUID, Bytes> makeProducerRecord(EncodedRecord record) {
        UUID key = record.getKey();
        byte[] bytes = record.getMessage();
        Bytes message = new Bytes(bytes);
        ProducerRecord<UUID, Bytes> result = new ProducerRecord<UUID, Bytes>(this.topic, key, message);
        return result;
    }


    KafkaSink withProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

    KafkaSink forTopic(String topicName) {
        if (Objects.nonNull(this.topic)) {
            logger.warn("TopicName is overwritten from {} to {}", this.topic, topicName);
        }
        this.topic = topicName;
        return this;
    }

    KafkaSink byReconnectOnFailure(boolean value) {
        this.tryReconnectOnFailure = value;
        return this;
    }
}
