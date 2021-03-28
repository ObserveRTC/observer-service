package org.observertc.webrtc.observer.connectors.sinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.webrtc.observer.connectors.EncodedRecord;
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
            switch (record.getFormat()) {
                case JSON:
                    this.perceiveJsonFormat(record, typeSummary);
                    break;
                case AVRO:
                default:
                    this.perceiveAvroFormat(record, typeSummary);
                    break;
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

    private void perceiveAvroFormat (EncodedRecord record, Map<ReportType, Integer> typeSummary) {
        Report report;
        try {
            report = Report.getDecoder().decode(record.getMessage());
        } catch (IOException e) {
            logger.warn("Cannot decode input bytes. Is it in the right Avro format?");
            return;
        }
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

    private void perceiveJsonFormat (EncodedRecord record, Map<ReportType, Integer> typeSummary) {
        Map<String, Object> map;
        try {
            map = OBJECT_MAPPER.readValue(record.getMessage(), Map.class);
        } catch (Throwable e) {
            logger.warn("Cannot decode input bytes. Is it in the right format?");
            return;
        }
        String type = (String) map.get("type");

        ReportType reportType = ReportType.valueOf(type);
        typeSummary.put(reportType, typeSummary.getOrDefault(reportType, 0) + 1);
        if (this.printReports) {
            try {
                String message = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(map);
                message = String.format("Received report: %s", message);
                this.sink.accept(message);
            } catch (Throwable throwable) {
                DEFAULT_LOGGER.error("Unexpected error occurred", throwable);
            }
        }
    }
}
