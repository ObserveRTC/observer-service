package org.observertc.observer.sinks;

import io.reactivex.rxjava3.functions.Consumer;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.metrics.SinkMetrics;
import org.observertc.observer.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

@Singleton
public class ReportSinks implements Consumer<List<Report>> {

    private static Logger logger = LoggerFactory.getLogger(ReportSinks.class);

    private final Map<String, Sink> sinks = new HashMap<>();

    @Inject
    ObserverConfig observerConfig;

    @Inject
    SinkMetrics sinkMetrics;

    @PostConstruct
    void setup() {
        this.fetchSinksConfig();
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

    public void accept(List<Report> reports) throws Throwable {
        synchronized (this) {
            Iterator<Map.Entry<String, Sink>> it = this.sinks.entrySet().iterator();
            while(it.hasNext()) {
                var entry = it.next();
                var sinkId = entry.getKey();
                var sink = entry.getValue();
                try {
                    int processedReports = sink.apply(reports);
                    if (this.sinkMetrics.isEnabled()) {
                        this.sinkMetrics.incrementReportsNum(processedReports, sinkId);
                    }
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

    private void fetchSinksConfig() {
        Map<String, Object> configs = this.observerConfig.sinks;
        if (Objects.isNull(configs)) {
            logger.warn("No Sinks has been configured");
            return;
        }
        configs.forEach((sinkId, sinkConfig) -> {
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
                if (observerConfig.security.printConfigs) {
                    logger.info("Sink {} with config {} has been initiated", sink.getClass().getSimpleName(), JsonUtils.objectToString(sinkConfigValue));
                }
                this.sinks.put(sinkId, sink);
            } catch (Exception ex) {
                logger.error("Error occurred while setting up a Sink {} with config {}",
                        sinkId,
                        JsonUtils.objectToString(sinkConfig),
                        ex
                );
            }
        });
        if (this.sinks.size() < 1) {
            logger.info("No sink has been set, the default (loggerSink) will be added");
            final String sinkId = "defaultLogger";
            var sink = new LoggerSink()
//                    .withPrintReports(true)
                    .withPrintReports(false)
                    .withPrintTypeSummary(true);

            String sinkLoggerName = String.format("Sink-%s:", sinkId);
            var logger = LoggerFactory.getLogger(sinkLoggerName);
            sink.withLogger(logger);
            this.sinks.put(sinkId, sink);
        }
    }

    private Sink buildSink(String sinkId, Map<String, Object> config) {
        SinkBuilder sinkBuilder = new SinkBuilder();
        sinkBuilder.withConfiguration(config);
        Sink result = sinkBuilder.build();
        if (Objects.isNull(result)) {
            logger.warn("Sink for {} has not been built", sinkId, JsonUtils.objectToString(config));
            return null;
        }
        String sinkLoggerName = String.format("Sink-%s:", sinkId);
        var logger = LoggerFactory.getLogger(sinkLoggerName);
        result.withLogger(logger);
        return result;
    }
}
