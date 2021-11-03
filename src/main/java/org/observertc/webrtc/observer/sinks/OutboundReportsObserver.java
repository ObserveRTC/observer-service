package org.observertc.webrtc.observer.sinks;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import org.observertc.webrtc.observer.codecs.OutboundReportsCodec;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.common.OutboundReports;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.*;

@Singleton
public class OutboundReportsObserver implements Observer<OutboundReports> {

    private static Logger logger = LoggerFactory.getLogger(OutboundReportsObserver.class);

    private Disposable upstream;
    private final Map<String, Sink> sinks = new HashMap<>();
    private OutboundReportsCodec outboundReportsCodec;
    public OutboundReportsObserver(ObserverConfig config, OutboundReportsCodec outboundReportsCodec) {
        this.outboundReportsCodec = outboundReportsCodec;
        if (Objects.nonNull(config.sinks)) {
            config.sinks.forEach((sinkId, sinkConfig) -> {
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
                            ObjectToString.toString(sinkConfig),
                            ex
                    );
                }
            });
        }
        if (this.sinks.size() < 1) {
            logger.info("No sink has been set, the default (loggerSink) will be added");
            var decoder = this.outboundReportsCodec.getDecoder();
            var sink = new LoggerSink()
                    .withDecoder(decoder)
                    .withPrintReports(true)
                    .withPrintTypeSummary(true);
            this.sinks.put("defaultLogger", sink);
        }
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

    @Override
    public void onNext(@NonNull OutboundReports outboundReports) {
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

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        this.upstream = d;
    }

    @Override
    public void onError(@NonNull Throwable e) {
        if (Objects.nonNull(this.upstream)) {
            if (!upstream.isDisposed()) {
                this.upstream.dispose();
            }
        }
        logger.warn("Error occurred in pipeline ", e);
    }

    @Override
    public void onComplete() {
        if (Objects.nonNull(this.upstream)) {
            if (!upstream.isDisposed()) {
                this.upstream.dispose();
            }
        }
        logger.info("Pipeline is completed");
    }
}
