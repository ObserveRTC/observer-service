package org.observertc.webrtc.observer.monitors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Singleton
public class SentinelMonitor {


    private Map<String, SentinelMetrics> sentinelMetrics = new HashMap<>();

    @Inject
    Provider<SentinelMetrics> sentinelMetricsProvider;

    @PostConstruct
    void setup() {

    }

    public SentinelMetrics getSentinelMetrics(String sentinelName) {
        SentinelMetrics result = this.sentinelMetrics.get(sentinelName);
        if (Objects.isNull(result)) {
            result = sentinelMetricsProvider.get().initialize(sentinelName);
            this.sentinelMetrics.put(sentinelName, result);
        }
        return result;
    }
}
