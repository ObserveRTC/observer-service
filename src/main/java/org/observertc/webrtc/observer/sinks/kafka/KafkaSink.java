package org.observertc.webrtc.observer.sinks.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.annotations.NonNull;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.observertc.webrtc.observer.common.MuxedReport;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.common.OutboundReports;
import org.observertc.webrtc.observer.common.ReportType;
import org.observertc.webrtc.observer.sinks.Sink;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;

public class KafkaSink extends Sink {
    private UUID key = UUID.randomUUID();
    private String muxTopic;
    private String demuxTopicPrefix;
    private Properties properties;
    private boolean tryReconnectOnFailure = false;
    private int consecutiveEmptyLists = 0;
    private final Function<OutboundReport, ProducerRecord<UUID, Bytes>> createRecord;
    private KafkaProducer<UUID, Bytes> producer;

    public KafkaSink(Properties properties, boolean tryReconnectOnFailure, boolean multiplex, String demuxTopicPrefix, String muxTopic) {
        this.properties = properties;
        this.tryReconnectOnFailure = tryReconnectOnFailure;
        this.muxTopic = muxTopic;
        this.demuxTopicPrefix = demuxTopicPrefix;
        if (multiplex) {
            this.createRecord = this.createMuxedRecordFunc();
        } else {
            this.createRecord = this.createDemuxedRecordFunc();
        }
    }

    @Override
    public void open() {
        Objects.requireNonNull(this.properties);
        this.producer = new KafkaProducer<UUID, Bytes>(this.properties);
    }


    @Override
    public void accept(@NonNull OutboundReports outboundReports) {
        if (outboundReports.getReportsNum() < 1) {
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
                for (OutboundReport outboundReport : outboundReports) {
                    if (++recordsCounter < sent) {
                        continue;
                    }
                    ProducerRecord<UUID, Bytes> producerRecord = this.createRecord.apply(outboundReport);
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

    private Function<OutboundReport, ProducerRecord<UUID, Bytes>> createDemuxedRecordFunc() {
        final String prefix = Objects.isNull(this.demuxTopicPrefix) ? "" : this.demuxTopicPrefix;
        return outboundReport -> {
            String topic = prefix + outboundReport.getType().toString();
            Bytes message = new Bytes(outboundReport.getBytes());
            ProducerRecord<UUID, Bytes> result = new ProducerRecord<UUID, Bytes>(topic, this.key, message);
            return result;
        };
    }

    private Function<OutboundReport, ProducerRecord<UUID, Bytes>> createMuxedRecordFunc() {
//        SpecificData specificData = SpecificData.getForSchema(Report.getClassSchema());
        ObjectMapper mapper = new ObjectMapper();
        return inputReport -> {
            MuxedReport muxedReport = new MuxedReport();
            try {
                ReportType reportType = inputReport.getType();
                muxedReport.type = reportType.name();
                muxedReport.payload = inputReport.getBytes();
            } catch (Exception ex) {
                logger.warn("Exception while creating mux report", ex);
                return null;
            }
            ProducerRecord<UUID, Bytes> result;
            try {
                var encodedReport = mapper.writeValueAsBytes(muxedReport);
                var message = Bytes.wrap(encodedReport);
                result = new ProducerRecord<UUID, Bytes>(this.muxTopic, this.key, message);
            } catch (IOException ex) {
                logger.warn("Exception while creating mux message", ex);
                return null;
            }
            return result;
        };

    }
}
