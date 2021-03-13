package org.observertc.webrtc.observer.evaluators.monitors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.schemas.reports.Report;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

@Singleton
public class CounterMonitorBuilder {
    private static final String UNKNOWN_TAG_VALUE = "Unknown";
    private static final String SERVICE_UUID_TAG_NAME = "serviceUUID";
    private static final String SERVICE_NAME_TAG_NAME = "serviceName";
    private static final String REPORT_TYPE_TAG_NAME = "reportType";

    @Inject
    MeterRegistry meterRegistry;

    public Function<Report, Report> build(String metricName, ObserverConfig.ReportCounterMonitorConfig config) {
        if (Objects.isNull(config) || !config.enabled) {
            return report -> report;
        }
        final BiConsumer<Report, List<Tag>> tagsResolver = this.makeTagsResolver(config);
        return report -> {
            List<Tag> tags = new LinkedList<>();
            tagsResolver.accept(report, tags);
            this.meterRegistry.counter(metricName, tags).increment();
            return report;
        };
    }

    private BiConsumer<Report, List<Tag>>  makeTagsResolver(ObserverConfig.ReportCounterMonitorConfig config) {
        BiConsumer<Report, List<Tag>> result = null;

        if (config.tagByServiceUUID) {
            result = (report, tags) -> {
                String serviceUUID;
                if (Objects.nonNull(report.getServiceUUID())) {
                    serviceUUID =  report.getServiceUUID();
                } else {
                    serviceUUID = UNKNOWN_TAG_VALUE;
                }
                Tag tag = Tag.of(SERVICE_UUID_TAG_NAME, serviceUUID);
                tags.add(tag);
            };
        }

        if (config.tagByServiceName) {
            BiConsumer<Report, List<Tag>> tagResolver = (report, tags) -> {
                String serviceName;
                if (Objects.nonNull(report.getServiceName())) {
                    serviceName = report.getServiceName();
                } else {
                    serviceName = UNKNOWN_TAG_VALUE;
                }
                Tag tag = Tag.of(SERVICE_NAME_TAG_NAME, serviceName);
                tags.add(tag);
            };
            if (Objects.nonNull(result)) {
                result = result.andThen(tagResolver);
            } else {
                result = tagResolver;
            }
        }

        if (config.tagByType) {
            BiConsumer<Report, List<Tag>> tagResolver = (report, tags) -> {
                String reportType;
                if (Objects.nonNull(report.getType())) {
                    reportType = report.getType().name();
                } else {
                    reportType = UNKNOWN_TAG_VALUE;
                }
                Tag tag = Tag.of(REPORT_TYPE_TAG_NAME, reportType);
                tags.add(tag);
            };
            if (Objects.nonNull(result)) {
                result = result.andThen(tagResolver);
            } else {
                result = tagResolver;
            }
        }

        return result;
    }
}
