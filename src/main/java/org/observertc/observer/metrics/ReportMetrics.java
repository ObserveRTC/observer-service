package org.observertc.observer.metrics;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.events.CallMetaType;
import org.observertc.observer.reports.Report;
import org.observertc.observer.reports.ReportTypeVisitor;
import org.observertc.schemas.reports.*;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Locale;

@Singleton
public class ReportMetrics {
    private static final String REPORT_METRICS_PREFIX = "reports";
    private static final String OUTBOUND_AUDIO_TRACKS_RTT_MEASUREMENT_METRIC_NAME = "outbound_audio_tracks_rtt";
    private static final String OUTBOUND_VIDEO_TRACKS_RTT_MEASUREMENT_METRIC_NAME = "outbound_video_tracks_rtt";
    private static final String OUTBOUND_AUDIO_TARGET_BITRATE_MEASUREMENT_METRIC_NAME = "outbound_audio_target_bitrate";
    private static final String OUTBOUND_VIDEO_TARGET_BITRATE_MEASUREMENT_METRIC_NAME = "outbound_video_target_bitrate";

    @Inject
    Metrics metrics;

    @Inject
    ObserverConfig.MetricsConfig.ReportMetricsConfig config;

    private ReportTypeVisitor<Object, Void> processor;
    private String outboundAudioTrackRttMeasurementsMetricName;
    private String outboundVideoTrackRttMeasurementsMetricName;
    private String outboundAudioTargetBitrateMeasurementsMetricName;
    private String outboundVideoTargetBitrateMeasurementsMetricName;

    @PostConstruct
    void setup() {
        this.outboundAudioTrackRttMeasurementsMetricName = getMetricName(OUTBOUND_AUDIO_TRACKS_RTT_MEASUREMENT_METRIC_NAME);
        this.outboundVideoTrackRttMeasurementsMetricName = getMetricName(OUTBOUND_VIDEO_TRACKS_RTT_MEASUREMENT_METRIC_NAME);
        this.outboundAudioTargetBitrateMeasurementsMetricName = getMetricName(OUTBOUND_AUDIO_TARGET_BITRATE_MEASUREMENT_METRIC_NAME);
        this.outboundVideoTargetBitrateMeasurementsMetricName = getMetricName(OUTBOUND_VIDEO_TARGET_BITRATE_MEASUREMENT_METRIC_NAME);
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
        var USER_MEDIA_ERROR = CallMetaType.USER_MEDIA_ERROR.toString();
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
                metrics.registry.counter(
                        metricName
                ).increment();
                return null;
            }

            @Override
            public Void visitCallMetaDataReport(Object payload) {
                var reportPayload = ((CallMetaReport) payload);
                var metaType = reportPayload.type;
                if (!USER_MEDIA_ERROR.equals(metaType)) {
                    return null;
                }
                var metricName = getMetricName(metaType.toLowerCase(Locale.ROOT));
                metrics.registry.counter(
                        metricName
                ).increment();
                return null;

            }

            @Override
            public Void visitClientExtensionDataReport(Object payload) {
                var reportPayload = ((ClientExtensionReport) payload);
                var metricName = getMetricName(reportPayload.extensionType.toLowerCase(Locale.ROOT));
                metrics.registry.counter(
                        metricName
                ).increment();
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
                var reportPayload = ((InboundVideoTrackReport) payload);
                return null;
            }

            @Override
            public Void visitOutboundAudioTrackReport(Object payload) {
                var reportPayload = ((OutboundAudioTrackReport) payload);
                if (reportPayload.roundTripTime != null) {
                    metrics.registry.summary(outboundAudioTrackRttMeasurementsMetricName).record(reportPayload.roundTripTime);
                }
                if (reportPayload.targetBitrate != null) {
                    metrics.registry.summary(outboundAudioTargetBitrateMeasurementsMetricName).record(reportPayload.targetBitrate);
                }
                return null;
            }

            @Override
            public Void visitOutboundVideoTrackReport(Object payload) {
                var reportPayload = ((OutboundVideoTrackReport) payload);
                if (reportPayload.roundTripTime != null) {
                    metrics.registry.summary(outboundVideoTrackRttMeasurementsMetricName).record(reportPayload.roundTripTime);
                }
                if (reportPayload.targetBitrate != null) {
                    metrics.registry.summary(outboundVideoTargetBitrateMeasurementsMetricName).record(reportPayload.targetBitrate);
                }
                return null;
            }

            @Override
            public Void visitSfuEventReport(Object payload) {
                var reportPayload = ((SfuEventReport) payload);
                var reportName = reportPayload.name.toLowerCase(Locale.ROOT);
                var metricName = getMetricName(reportName);
                metrics.registry.counter(
                        metricName
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
