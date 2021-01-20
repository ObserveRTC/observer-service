package org.observertc.webrtc.observer.monitors;

import io.reactivex.rxjava3.core.ObservableOperator;
import org.observertc.webrtc.observer.configbuilders.ConfigConverter;
import org.observertc.webrtc.schemas.reports.Report;
import org.observertc.webrtc.schemas.reports.ReportType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Objects;

@Singleton
public class CounterMonitorProvider {
    private static final String SERVICE_UUID_TAG_NAME = "serviceUUID";
    private static final String SERVICE_NAME_TAG_NAME = "serviceName";
    private static final String REPORT_TYPE_TAG_NAME = "serviceName";

    @Inject
    Provider<CounterMonitorBuilder<Report>> counterMonitorBuilderProvider;

    public ObservableOperator<Report, Report> buildReportMonitor(String metricName, Map<String, Object> configMap) {
        ConfigConverter.forceKeysToBeCamelCase(configMap);
        ReportMonitorConfig config = ConfigConverter.convert(ReportMonitorConfig.class, configMap);
        return this.buildReportMonitor(metricName, config);
    }

    public ObservableOperator<Report, Report> buildReportMonitor(String metricName, ReportMonitorConfig config) {
        if (!config.enabled) {
            return report -> report;
        }
        CounterMonitorBuilder<Report> builder = this.counterMonitorBuilderProvider.get();
        if (config.tagByServiceUUID) {
            builder.withTagResolver(SERVICE_UUID_TAG_NAME, Report::getServiceUUID);
        }
        if (config.tagByServiceName) {
            builder.withTagResolver(SERVICE_NAME_TAG_NAME, Report::getServiceName);
        }
        if (config.tagByType) {
            builder.withTagResolver(REPORT_TYPE_TAG_NAME, r -> {
                ReportType result = r.getType();
                if (Objects.nonNull(result)) return result.name();
                else return ReportType.UNKNOWN.name();
            });
        }
        return builder.withName(metricName).build();
    }

}
