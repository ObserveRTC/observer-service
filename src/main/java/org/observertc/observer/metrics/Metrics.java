package org.observertc.observer.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.configs.ObserverConfig;

import java.util.Arrays;

@Singleton
class Metrics {

    private static final String TAG_VALUE_UNKNOWN = "unknown";

    @Inject
    ObserverConfig.MetricsConfig config;

    @Inject
    MeterRegistry registry;

    String getMetricName(String name, String... appendixes) {
        if (appendixes == null || appendixes.length < 1) {
            return String.format("%s_%s", this.config.prefix, name);
        }
        var additions = Arrays.stream(appendixes).map(String::toLowerCase).reduce((t, u) -> t + "," + u).get();
        return String.format("%s_%s_%s",  this.config.prefix, name, additions);
    }

    String getMediaUnitIdTagName() {
        return this.config.mediaUnitTagName;
    }

    String getServiceIdTagName() {
        return this.config.serviceIdTagName;
    }

    String getTagValue(String value) {
        if (value == null) {
            return TAG_VALUE_UNKNOWN;
        }
        return value;
    }
}
