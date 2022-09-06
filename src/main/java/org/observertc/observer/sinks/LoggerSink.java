package org.observertc.observer.sinks;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.groupingBy;

public class LoggerSink extends Sink {
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(LoggerSink.class);
    private boolean printReports = true;
    private Consumer<String> sink = logger::info;
    private Level level = Level.INFO;
    private boolean typeSummary = false;

    private java.util.function.Consumer<Report> printer;

    LoggerSink() {
        this.printer = report -> {};
    }

    @Override
    public void process(@NonNull List<Report> reports) {
        logger.info("Number of reports are: {}", reports.size());
        var reportsByTypes = reports.stream()
                .collect(groupingBy(r -> r.type));
        for (var entry : reportsByTypes.entrySet()) {
            var reportType = entry.getKey();
            var typeReports = entry.getValue();
            if (this.typeSummary) {
                String message = String.format("Received number of reports of %s: %d", reportType, typeReports.size());
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
        if (this.printReports) {
            this.printer = report -> {
                var jsonStr = JsonUtils.objectToString(report);
                logger.info("Report: {}", jsonStr);
            };
        }
        return this;
    }

    LoggerSink witLogLevel(Level level) {
        Objects.requireNonNull(level);
        this.level = level;
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
        return this;
    }
}
