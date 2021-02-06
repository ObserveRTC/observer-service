package org.observertc.webrtc.observer.connectors.sinks;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.*;

public class LoggerSink extends Sink {
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
    public void onNext(@NonNull List<Report> reports) {
        logger.info("Number of reports are: {}", reports.size());
        Map<ReportType, Integer> typeSummary = new HashMap<>();
        for (Report report : reports) {
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

}
