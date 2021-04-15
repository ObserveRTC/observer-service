package org.observertc.webrtc.observer.evaluators.monitors;

import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.schemas.reports.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public abstract class RtpMonitorAbstract<T> implements Function<Report, Report> {
    private static final Logger logger = LoggerFactory.getLogger(RtpMonitorAbstract.class);

    private final Map<String, Instant> accessedKeys;
    private ObserverConfig.RtpMonitorConfig config;

    public static String getKey(UUID pcUUID, Long SSRC) {
        return getKey(pcUUID.toString(), SSRC);
    }
    public static String getKey(String pcUUID, Long SSRC) {
        return String.format("%s-%d", pcUUID, SSRC);
    }

    public RtpMonitorAbstract(ObserverConfig.RtpMonitorConfig config) {
        this.accessedKeys = new LinkedHashMap<>(16, .75f, true);
        this.config = config;
    }


    @Override
    public Report apply(Report report) throws Throwable {
        if (!config.enabled) {
            return report;
        }
        T payload = this.getPayload(report);
        if (Objects.isNull(payload)) {
            return report;
        }
        try {
            this.monitor(payload);
        } catch (Throwable t) {
            logger.warn("Exception occurred while monitoring process", t);
        }
        return report;
    }

    private void monitor(T payload) {
        String key = this.getPayloadKey(payload);
        if (Objects.isNull(key)) {
            return;
        }
        synchronized (this) {
            this.accessedKeys.put(key, Instant.now());
        }

        try {
            this.doMonitor(payload, key);
        } catch (Throwable t) {
            logger.warn("Exception occurred while monitoring process", t);
        } finally {
            synchronized (this) {
                this.removeUnusedItems();
            }
        }
    }

    protected abstract T getPayload(Report report);
    protected abstract String getPayloadKey(T payload);
    protected abstract void doMonitor(T payload, String payloadKey);
    protected abstract void remove(String key);

    private void removeUnusedItems() {
        Iterator<Map.Entry<String, Instant>> it = this.accessedKeys.entrySet().iterator();
        Instant now = Instant.now();
        while (it.hasNext()) {
            Map.Entry<String, Instant> entry = it.next();
            if (Duration.between(entry.getValue(), now).getSeconds() < config.retentionTimeInS) {
                // the first item, accessed less than 300s ago, then we stop the check because
                // we know all consecutive itmes are accessed less than this.
                return;
            }
            // no hard feelings
            this.remove(entry.getKey());
            it.remove();
        }
    }


}
