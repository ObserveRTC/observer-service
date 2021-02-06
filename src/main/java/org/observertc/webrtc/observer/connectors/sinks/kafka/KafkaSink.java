package org.observertc.webrtc.observer.connectors.sinks.kafka;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.observertc.webrtc.observer.common.ReportVisitor;
import org.observertc.webrtc.observer.connectors.sinks.Sink;
import org.observertc.webrtc.schemas.reports.*;

import java.nio.ByteBuffer;
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
    private final ReportVisitor<UUID> keyConverter;
    public KafkaSink() {
        this.encoder = Report.getEncoder();
        this.keyConverter = this.makeKeyConverter();
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
    public void onNext(@NonNull List<Report> reports) {
        if (reports.size() < 1) {
            return;
        }
        try {
            for (Report report : reports) {
                ProducerRecord<UUID, Bytes> record = this.makeProducerRecord(report);
                this.producer.send(record);
            }
        } catch (Throwable t) {
            logger.error("Unexpected exception", t);
            if (this.tryReconnectOnFailure) {
                try {
                    this.connect();
                    for (Report report : reports) {
                        ProducerRecord<UUID, Bytes> record = this.makeProducerRecord(report);
                        this.producer.send(record);
                    }
                } catch (Throwable t2) {
                    logger.error("Error occurred while we tried to sent the messages at the second attempt after reconnect. this is bad, messages will be lost", t2);
                }
            }
        }
    }

    private ProducerRecord<UUID, Bytes> makeProducerRecord(Report report) {
        UUID key = this.keyConverter.apply(report);
        ByteBuffer message;
        try {
            message = this.encoder.encode(report);
        } catch (Throwable t) {
            this.logger.warn("Exception by serializing report " + report.toString(), t);
            return null;
        }
        Bytes bytes = new Bytes(message.array());
        ProducerRecord<UUID, Bytes> record = new ProducerRecord<UUID, Bytes>(this.topic, key, bytes);
        return record;
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

    private ReportVisitor<UUID> makeKeyConverter() {
        return new ReportVisitor<UUID>() {
            @Override
            public UUID visitTrackReport(Report report, Track payload) {
                return UUID.fromString(payload.getPeerConnectionUUID());
            }

            @Override
            public UUID visitFinishedCallReport(Report report, FinishedCall payload) {
                return UUID.fromString(report.getServiceUUID());
            }

            @Override
            public UUID visitInitiatedCallReport(Report report, InitiatedCall payload) {
                return UUID.fromString(report.getServiceUUID());
            }

            @Override
            public UUID visitJoinedPeerConnectionReport(Report report, JoinedPeerConnection payload) {
                return UUID.fromString(payload.getPeerConnectionUUID());
            }

            @Override
            public UUID visitDetachedPeerConnectionReport(Report report, DetachedPeerConnection payload) {
                return UUID.fromString(payload.getPeerConnectionUUID());
            }

            @Override
            public UUID visitInboundRTPReport(Report report, InboundRTP payload) {
                return UUID.fromString(payload.getPeerConnectionUUID());
            }

            @Override
            public UUID visitOutboundRTPReport(Report report, OutboundRTP payload) {
                return UUID.fromString(payload.getPeerConnectionUUID());
            }

            @Override
            public UUID visitRemoteInboundRTPReport(Report report, RemoteInboundRTP payload) {
                return UUID.fromString(payload.getPeerConnectionUUID());
            }

            @Override
            public UUID visitMediaSourceReport(Report report, MediaSource payload) {
                return UUID.fromString(payload.getPeerConnectionUUID());
            }

            @Override
            public UUID visitObserverReport(Report report, ObserverEventReport payload) {
                return UUID.fromString(report.getServiceUUID());
            }

            @Override
            public UUID visitUserMediaErrorReport(Report report, UserMediaError payload) {
                return UUID.fromString(report.getServiceUUID());
            }

            @Override
            public UUID visitICECandidatePairReport(Report report, ICECandidatePair payload) {
                return UUID.fromString(payload.getPeerConnectionUUID());
            }

            @Override
            public UUID visitICELocalCandidateReport(Report report, ICELocalCandidate payload) {
                return UUID.fromString(payload.getPeerConnectionUUID());
            }

            @Override
            public UUID visitICERemoteCandidateReport(Report report, ICERemoteCandidate payload) {
                return UUID.fromString(payload.getPeerConnectionUUID());
            }

            @Override
            public UUID visitUnrecognizedReport(Report report) {
                return UUID.fromString(report.getServiceUUID());
            }

            @Override
            public UUID visitExtensionReport(Report report, ExtensionReport payload) {
                return UUID.fromString(report.getServiceUUID());
            }

            @Override
            public UUID visitUnknownType(Report report) {
                return UUID.fromString(report.getServiceUUID());
            }
        };
    }
}
