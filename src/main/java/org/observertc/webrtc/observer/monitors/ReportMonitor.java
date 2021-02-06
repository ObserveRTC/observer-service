package org.observertc.webrtc.observer.monitors;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.reactivex.rxjava3.functions.Consumer;
import org.observertc.webrtc.schemas.reports.Report;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

@Singleton
public class ReportMonitor implements Consumer<Report> {
    private static final String METRIC_NAME = "generated_reports";
    private static final String SERVICE_UUID_TAG_NAME = "serviceUUID";
    private static final String SERVICE_NAME_TAG_NAME = "serviceName";
    private static final String REPORT_TYPE_TAG_NAME = "serviceName";
    private final BiConsumer<Report, List<Tag>> tagsResolver;

    @Inject
    MeterRegistry meterRegistry;

    public ReportMonitor(ReportMonitorConfig config) {
        this.tagsResolver = this.makeTagsResolver(config);
    }

    @Override
    public void accept(Report report) throws Throwable {
        if (Objects.nonNull(report)) {
            return;
        }
        List<Tag> tags = new ArrayList<>();
        this.tagsResolver.accept(report, tags);
        this.meterRegistry.counter(METRIC_NAME, tags).increment();
    }

    private BiConsumer<Report, List<Tag>>  makeTagsResolver(ReportMonitorConfig config) {
        BiConsumer<Report, List<Tag>> result = (report, tags) -> {

        };

        if (config.tagByServiceUUID) {
            result = result.andThen((report, tags) -> {
                if (Objects.nonNull(report.getServiceUUID())) {
                    Tag tag = Tag.of(SERVICE_UUID_TAG_NAME, report.getServiceUUID());
                    tags.add(tag);
                }
            });
        }

        if (config.tagByServiceName) {
            result = result.andThen((report, tags) -> {
                if (Objects.nonNull(report.getServiceName())) {
                    Tag tag = Tag.of(SERVICE_NAME_TAG_NAME, report.getServiceName());
                    tags.add(tag);
                }
            });
        }

        if (config.tagByType) {
            result = result.andThen((report, tags) -> {
                if (Objects.nonNull(report.getType())) {
                    Tag tag = Tag.of(REPORT_TYPE_TAG_NAME, report.getType().name());
                    tags.add(tag);
                }
            });
        }

        return result;
    }
}
