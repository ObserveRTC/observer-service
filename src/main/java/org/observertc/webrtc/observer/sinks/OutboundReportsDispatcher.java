package org.observertc.webrtc.observer.sinks;

import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.webrtc.observer.codecs.OutboundReportsCodec;
import org.observertc.webrtc.observer.common.JsonUtils;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class OutboundReportsDispatcher implements Consumer<List<OutboundReport>> {

    private static Logger logger = LoggerFactory.getLogger(OutboundReportsDispatcher.class);

    private final Map<String, Sink> sinks = new HashMap<>();

    @Inject
    OutboundReportsCodec outboundReportsCodec;

    @Inject
    ObserverConfig observerConfig;

    @PostConstruct
    void setup() {
        this.fetchSinksConfig(observerConfig.sinks);
        if (this.sinks.size() < 1) {
            logger.info("No sink has been set, the default (loggerSink) will be added");
            var decoder = this.outboundReportsCodec.getDecoder();
            var sink = new LoggerSink()
                    .withDecoder(decoder)
//                    .withPrintReports(true)
                    .withPrintReports(false)
                    .withPrintTypeSummary(true);
            final String sinkId = "defaultLogger";
            String sinkLoggerName = String.format("Sink-%s:", sinkId);
            var logger = LoggerFactory.getLogger(sinkLoggerName);
            sink.withLogger(logger);
            this.sinks.put(sinkId, sink);
        }
    }

    @PreDestroy
    void teardown() {
        this.sinks.forEach((sinkId, sink) -> {
            if (!sink.isEnabled()) {
                logger.info("Sink {} is not enabled", sinkId);
                return;
            }
            try {
                sink.close();
                logger.info("Sink {} is closed", sinkId);
            } catch (Exception ex) {
                logger.warn("Error occurred while closing sink {}", sinkId, ex);
            }
        });
    }

    @Override
    public void accept(List<OutboundReport> outboundReports) throws Throwable {
        synchronized (this) {
            Iterator<Map.Entry<String, Sink>> it = this.sinks.entrySet().iterator();
            while(it.hasNext()) {
                var entry = it.next();
                var sinkId = entry.getKey();
                var sink = entry.getValue();
                try {
                    sink.accept(outboundReports);
                } catch (Throwable ex) {
                    logger.error("Unexpected error occurred on sink {}. Sink will be closed", sinkId, ex);
                    try {
                        sink.close();
                    } catch (Exception ex2) {
                        logger.error("Error occurred while shutting down sink {}", sinkId, ex2);
                    }
                    it.remove();
                }
            }
        }
    }

    private void fetchSinksConfig(Map<String, Object> config) {
        if (Objects.isNull(config)) {
            return;
        }
        config.forEach((sinkId, sinkConfig) -> {
            try {
                Map<String, Object> sinkConfigValue = (Map<String, Object>) sinkConfig;
                var sink = this.buildSink(sinkId, sinkConfigValue);
                if (Objects.isNull(sink)) {
                    logger.warn("{} : {} has not been built");
                    return;
                }
                if (!sink.isEnabled()) {
                    logger.info("{} is disabled", sinkId);
                    return;
                }
                sink.open();
                this.sinks.put(sinkId, sink);
            } catch (Exception ex) {
                logger.error("Error occurred while setting up a Sink {} with config {}",
                        sinkId,
                        JsonUtils.objectToString(sinkConfig),
                        ex
                );
            }
        });
    }

    private Sink buildSink(String sinkId, Map<String, Object> config) {
        SinkBuilder sinkBuilder = new SinkBuilder();
        sinkBuilder.withConfiguration(config);
        var decoder = this.outboundReportsCodec.getDecoder();
        sinkBuilder.setDecoder(decoder);
        Sink result = sinkBuilder.build();
        String sinkLoggerName = String.format("Sink-%s:", sinkId);
        var logger = LoggerFactory.getLogger(sinkLoggerName);
        result.withLogger(logger);
        return result;
    }
}
