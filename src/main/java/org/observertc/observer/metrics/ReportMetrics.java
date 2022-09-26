package org.observertc.observer.metrics;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.events.CallMetaType;
import org.observertc.observer.reports.Report;
import org.observertc.observer.reports.ReportTypeVisitor;
import org.observertc.schemas.reports.CallEventReport;
import org.observertc.schemas.reports.CallMetaReport;
import org.observertc.schemas.reports.SfuEventReport;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Locale;

@Singleton
public class ReportMetrics {
    private static final String REPORT_METRICS_PREFIX = "reports";

    @Inject
    Metrics metrics;

    @Inject
    ObserverConfig.MetricsConfig.ReportMetricsConfig config;

    private ReportTypeVisitor<Object, Void> processor;

    @PostConstruct
    void setup() {
        this.processor = this.createProcessor();
    }

    public void process(List<Report> reports) {
        if (!this.config.enabled || reports == null) {
            return;
        }
        reports.stream()
                .filter(report -> report != null && report.type != null && report.payload != null)
                .forEach(report -> this.processor.apply(report.payload, report.type));
    }

    public boolean isEnabled() {
        return this.config.enabled;
    }

    private String getMetricName(String name) {
        String metricName = String.format("%s_%s", REPORT_METRICS_PREFIX, name);
        return metrics.getMetricName(metricName);
    }

    private ReportTypeVisitor<Object, Void> createProcessor() {
        return new ReportTypeVisitor<Object, Void>() {
            @Override
            public Void visitObserverEventReport(Object payload) {
//        var reportPayload = ((ObserverEventReport) payload);
//        var metricName = this.getMetricName("observer_event");
//        this.meterRegistry.counter()
                return null;
            }


            @Override
            public Void visitCallEventReport(Object payload) {
                var reportPayload = ((CallEventReport) payload);
                var metricName = getMetricName(reportPayload.name.toLowerCase(Locale.ROOT));
                var serviceIdTagValue = metrics.getTagValue(reportPayload.serviceId);
                var mediaUnitTagValue = metrics.getTagValue(reportPayload.mediaUnitId);
                metrics.registry.counter(
                        metricName,
                        metrics.getServiceIdTagName(), serviceIdTagValue,
                        metrics.getMediaUnitIdTagName(), mediaUnitTagValue
                ).increment();
                return null;
            }

            @Override
            public Void visitCallMetaDataReport(Object payload) {
                var reportPayload = ((CallMetaReport) payload);
                var metaType = reportPayload.type;
                if (!CallMetaType.USER_MEDIA_ERROR.equals(metaType)) {
                    return null;
                }
                var metricName = getMetricName(metaType.toLowerCase(Locale.ROOT));
                var serviceIdTagValue = metrics.getTagValue(reportPayload.serviceId);
                var mediaUnitTagValue = metrics.getTagValue(reportPayload.mediaUnitId);
                metrics.registry.counter(
                        metricName,
                        metrics.getServiceIdTagName(), serviceIdTagValue,
                        metrics.getMediaUnitIdTagName(), mediaUnitTagValue
                ).increment();
                return null;

            }

            @Override
            public Void visitClientExtensionDataReport(Object payload) {
//        var reportPayload = ((ClientExtensionReport) payload);
                return null;

            }

            @Override
            public Void visitPeerConnectionTransportReport(Object payload) {
//        var reportPayload = ((ClientTransportReport) payload);
                return null;

            }

            @Override
            public Void visitIceCandidatePairReport(Object payload) {
//        var reportPayload = ((ClientTransportReport) payload);
                return null;

            }

            @Override
            public Void visitClientDataChannelReport(Object payload) {
//        var reportPayload = ((ClientDataChannelReport) payload);
                return null;

            }

            @Override
            public Void visitInboundAudioTrackReport(Object payload) {
//                var reportPayload = ((InboundAudioTrackReport) payload);
                return null;
            }

            @Override
            public Void visitInboundVideoTrackReport(Object payload) {
//                var reportPayload = ((InboundVideoTrackReport) payload);
                return null;
            }

            @Override
            public Void visitOutboundAudioTrackReport(Object payload) {
//                var reportPayload = ((OutboundAudioTrackReport) payload);
                return null;
            }

            @Override
            public Void visitOutboundVideoTrackReport(Object payload) {
//                var reportPayload = ((OutboundVideoTrackReport) payload);
                return null;
            }

            @Override
            public Void visitSfuEventReport(Object payload) {
                var reportPayload = ((SfuEventReport) payload);
                var reportName = reportPayload.name.toLowerCase(Locale.ROOT);
                var metricName = getMetricName(reportName);
                var serviceIdTagValue = metrics.getTagValue(reportPayload.serviceId);
                var mediaUnitTagValue = metrics.getTagValue(reportPayload.mediaUnitId);
                metrics.registry.counter(
                        metricName,
                        metrics.getServiceIdTagName(), serviceIdTagValue,
                        metrics.getMediaUnitIdTagName(), mediaUnitTagValue
                ).increment();
                return null;
            }

            @Override
            public Void visitSfuMetaReport(Object payload) {
//                var reportPayload = ((SfuMetaReport) payload);
                return null;
            }

            @Override
            public Void visitSfuExtensionReport(Object payload) {
//        var reportPayload = ((SfuExtensionReport) payload);
                return null;
            }

            @Override
            public Void visitSfuTransportReport(Object payload) {
//        var reportPayload = ((SFUTransportReport) payload);
                return null;
            }

            @Override
            public Void visitSfuInboundRtpPadReport(Object payload) {
//        var reportPayload = ((SfuInboundRtpPadReport) payload);
                return null;

            }

            @Override
            public Void visitSfuOutboundRtpPadReport(Object payload) {
//        var reportPayload = ((SfuOutboundRtpPadReport) payload);
                return null;
            }

            @Override
            public Void visitSctpStreamReport(Object payload) {
//        var reportPayload = ((SfuSctpStreamReport) payload);
                return null;
            }
        };
    }


}
