package org.observertc.webrtc.observer.sinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.webrtc.observer.codecs.OutboundReportsAvroDecoder;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.common.OutboundReportTypeVisitor;
import org.observertc.webrtc.observer.common.OutboundReports;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class LoggerSink extends Sink {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(LoggerSink.class);
    private boolean printReports = true;
    private Consumer<String> sink = logger::info;
    private Level level = Level.INFO;
    private boolean typeSummary = false;

    private java.util.function.Consumer<OutboundReport> printer;

    LoggerSink() {
        this.printer = report -> {};
    }

    @Override
    public void accept(@NonNull OutboundReports outboundReports) {
        logger.info("Number of reports are: {}", outboundReports.getReportsNum());
        Map<ReportType, Integer> receivedTypes = new HashMap<>();
        for (OutboundReport outboundReport : outboundReports) {
            ReportType reportType = outboundReport.getType();
            receivedTypes.put(reportType, receivedTypes.getOrDefault(reportType, 0) + 1);
            this.printer.accept(outboundReport);
        }

        if (this.typeSummary) {
            receivedTypes.forEach((reportType, numberOfReports) -> {
                String message = String.format("Received number of reports of %s: %d", reportType, numberOfReports);
                try {
                    this.sink.accept(message);
                } catch (Throwable throwable) {
                    DEFAULT_LOGGER.error("Unexpected error occurred", throwable);
                }
            });
        }
    }

    LoggerSink withPrintTypeSummary(boolean value) {
        this.typeSummary = value;
        return this;
    }

    LoggerSink withPrintReports(boolean value) {
        this.printReports = value;
        if (this.printReports) {
            this.printer = this.makePrinter();
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

    private java.util.function.Consumer<OutboundReport> makePrinter() {
        final OutboundReportsAvroDecoder decoder = new OutboundReportsAvroDecoder();
        var decodeAndPrint = OutboundReportTypeVisitor.createConsumerVisitor(
                this.makeDecodeAndPrintConsumer(decoder::decodeObserverEventReports),
                this.makeDecodeAndPrintConsumer(decoder::decodeCallEventReports),
                this.makeDecodeAndPrintConsumer(decoder::decodeCallMetaReports),
                this.makeDecodeAndPrintConsumer(decoder::decodeClientExtensionReport),
                this.makeDecodeAndPrintConsumer(decoder::decodeClientTransportReport),
                this.makeDecodeAndPrintConsumer(decoder::decodeClientDataChannelReport),
                this.makeDecodeAndPrintConsumer(decoder::decodeInboundAudioTrackReport),
                this.makeDecodeAndPrintConsumer(decoder::decodeInboundVideoTrackReport),
                this.makeDecodeAndPrintConsumer(decoder::decodeOutboundAudioTrackReport),
                this.makeDecodeAndPrintConsumer(decoder::decodeOutboundVideoTrackReport),
                this.makeDecodeAndPrintConsumer(decoder::decodeMediaTrackReport)
        );
        return outboundReport -> {
            if (Objects.isNull(outboundReport)) {
                return;
            }
            decodeAndPrint.apply(outboundReport, outboundReport.getType());
        };
    }

    private<T extends org.apache.avro.specific.SpecificRecordBase> java.util.function.Consumer<OutboundReport> makeDecodeAndPrintConsumer(Function<OutboundReport, T> decoder) {
        return outboundReport -> {
            if (Objects.isNull(outboundReport)) {
                return;
            }
            org.apache.avro.specific.SpecificRecordBase recordBase = decoder.apply(outboundReport);
            if (Objects.isNull(recordBase)) {
                logger.warn("Cannot decoding {}", ObjectToString.toString(outboundReport));
                return;
            }
            logger.info("Decoded Report type {}, value: {}", outboundReport.getType(), recordBase.toString());
        };
    }
}
