package org.observertc.webrtc.observer.connectors.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import org.bson.Document;
import org.observertc.webrtc.observer.connectors.EncodedRecord;
import org.observertc.webrtc.observer.connectors.MessageFormat;
import org.observertc.webrtc.observer.connectors.encoders.avro.AvroEncoder;
import org.observertc.webrtc.observer.connectors.encoders.bson.BsonEncoder;
import org.observertc.webrtc.observer.connectors.encoders.json.JsonEncoder;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;
import java.util.*;

public class LoggerSink extends Sink {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(LoggerSink.class);
    private boolean printReports = true;
    private Consumer<String> sink = logger::info;
    private Level level = Level.INFO;
    private boolean typeSummary = false;

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        super.onSubscribe(d);
        switch (this.level) {
            case WARN:
                this.sink = this.logger::warn;
                break;
            case DEBUG:
                this.sink = this.logger::debug;
                break;
            case ERROR:
                this.sink = this.logger::error;
                break;
            case TRACE:
                this.sink = this.logger::trace;
                break;
            default:
            case INFO:
                this.sink = this.logger::info;
                break;
        }
    }

    @Override
    public void onNext(@NonNull List<EncodedRecord> records) {
        logger.info("Number of reports are: {}", records.size());
        Map<ReportType, Integer> typeSummary = new HashMap<>();
        for (EncodedRecord record : records) {
            Class encoderType = record.getEncoderType();
            if (Objects.isNull(encoderType)) {
                logger.warn("No encoder type is attached to record", record);
                continue;
            }
            MessageFormat format = record.getFormat();
            if (encoderType.equals(AvroEncoder.class)) {
                switch (format) {
                    case BYTES:
                        this.perceiveAvroBytesFormat(record, typeSummary);
                        break;
                    case OBJECT:
                        this.perceiveAvroObjectFormat(record, typeSummary);
                        break;
                    default:
                        logger.warn("Not implemented logger format interpreter for {} encodertype, {} messageformat", encoderType, format);
                }
            } else if (encoderType.equals(JsonEncoder.class)) {
                switch (format) {
                    case BYTES:
                        this.perceiveJsonBytesFormat(record, typeSummary);
                        break;
                    case OBJECT:
                        this.perceiveJsonObjectFormat(record, typeSummary);
                        break;
                    default:
                        logger.warn("Not implemented logger format interpreter for {} encodertype, {} messageformat", encoderType, format);
                }
            } else if(encoderType.equals(BsonEncoder.class)) {
                switch (format) {
                    case BYTES:
                        this.perceiveBsonBytesFormat(record, typeSummary);
                        break;
                    case OBJECT:
                        this.perceiveBsonObjectFormat(record, typeSummary);
                        break;
                    default:
                        logger.warn("Not implemented logger format interpreter for {} encodertype, {} messageformat", encoderType, format);
                }
            } else {
                logger.warn("Not implemented logger format interpreter for {} encodertype, {} messageformat", encoderType, format);
            }
        }

        if (this.typeSummary) {
            Iterator<Map.Entry<ReportType, Integer>> it = typeSummary.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<ReportType, Integer> entry = it.next();
                String message = String.format("Received number of reports of %s: %d", entry.getKey(), entry.getValue());
                try {
                    this.sink.accept(message);
                } catch (Throwable throwable) {
                    DEFAULT_LOGGER.error("Unexpected error occurred", throwable);
                }
            }
        }
    }

    LoggerSink withPrintTypeSummary(boolean value) {
        this.typeSummary = value;
        return this;
    }

    LoggerSink withPrintReports(boolean value) {
        this.printReports = value;
        return this;
    }

    LoggerSink witLogLevel(Level level) {
        Objects.requireNonNull(level);
        this.level = level;
        return this;
    }

    private void perceiveAvroBytesFormat(EncodedRecord record, Map<ReportType, Integer> typeSummary) {
        Report report;
        try {
            byte[] bytes = record.getMessage();
            report = Report.getDecoder().decode(bytes);
        } catch (IOException e) {
            logger.warn("Cannot decode input bytes. Is it in the right Avro format?");
            return;
        }
        this.processAvroReport(report, typeSummary);
    }

    private void perceiveAvroObjectFormat(EncodedRecord record, Map<ReportType, Integer> typeSummary) {
        Report report = record.getMessage();
        this.processAvroReport(report, typeSummary);
    }

    private void processAvroReport(Report report, Map<ReportType, Integer> typeSummary) {
        typeSummary.put(report.getType(), typeSummary.getOrDefault(report.getType(), 0) + 1);
        if (this.printReports) {
            String message = String.format("Received report: %s", report.toString());
            try {
                this.sink.accept(message);
            } catch (Throwable throwable) {
                DEFAULT_LOGGER.error("Unexpected error occurred", throwable);
            }
        }
    }

    private void perceiveJsonBytesFormat(EncodedRecord record, Map<ReportType, Integer> typeSummary) {
        JsonNode jsonNode;
        try {
            byte[] bytes = record.getMessage();
            jsonNode = OBJECT_MAPPER.readTree(bytes);
        } catch (Throwable e) {
            logger.warn("Cannot decode input bytes. Is it in the right format?");
            return;
        }
        this.processJsonNode(jsonNode, typeSummary);
    }
    private void perceiveJsonObjectFormat(EncodedRecord record, Map<ReportType, Integer> typeSummary) {
        ObjectNode objectNode;
        try {
            objectNode = record.getMessage();
        } catch (Throwable e) {
            logger.warn("Cannot decode input bytes. Is it in the right format?");
            return;
        }
        this.processJsonNode(objectNode, typeSummary);
    }

    private void processJsonNode(JsonNode jsonNode, Map<ReportType, Integer> typeSummary) {
        String type = jsonNode.get("type").asText();

        ReportType reportType = ReportType.valueOf(type);
        typeSummary.put(reportType, typeSummary.getOrDefault(reportType, 0) + 1);
        if (this.printReports) {
            try {
                String message = jsonNode.toString();
                message = String.format("Received report: %s", message);
                this.sink.accept(message);
            } catch (Throwable throwable) {
                DEFAULT_LOGGER.error("Unexpected error occurred", throwable);
            }
        }
    }

    private void perceiveBsonBytesFormat(EncodedRecord record, Map<ReportType, Integer> typeSummary) {
        Document document;
        try {
            byte[] message = record.getMessage();
            document = Document.parse(new String(message));
        } catch (Throwable e) {
            logger.warn("Cannot decode input bytes. Is it in the right format?");
            return;
        }
        ReportType reportType = record.getReportType();
        this.processBsonDocument(document, reportType, typeSummary);
    }

    private void perceiveBsonObjectFormat(EncodedRecord record, Map<ReportType, Integer> typeSummary) {
        Document document;
        try {
            document = record.getMessage();
        } catch (Throwable e) {
            logger.warn("Cannot decode input bytes. Is it in the right format?");
            return;
        }
        ReportType reportType = record.getReportType();
        this.processBsonDocument(document, reportType, typeSummary);
    }

    private void processBsonDocument(Document document, ReportType reportType, Map<ReportType, Integer> typeSummary) {
        typeSummary.put(reportType, typeSummary.getOrDefault(reportType, 0) + 1);
        if (this.printReports) {
            try {
                String message = document.toString();
                message = String.format("Received report: %s", message);
                this.sink.accept(message);
            } catch (Throwable throwable) {
                DEFAULT_LOGGER.error("Unexpected error occurred", throwable);
            }
        }
    }
}
