package org.observertc.observer.metrics;

import io.micrometer.core.instrument.DistributionSummary;
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
    private static final String CALL_NUMBER_OF_PARTICIPANTS_METRIC_NAME = "calls_participants_number";
    private static final String CALL_DURATIONS_METRIC_NAME = "call_durations";

    @Inject
    Metrics metrics;

    @Inject
    ObserverConfig.MetricsConfig.ReportMetricsConfig config;

    private ReportTypeVisitor<Object, Void> processor;

    private DistributionSummary outboundAudioTrackRttMeasurements;
    private DistributionSummary outboundVideoTrackRttMeasurements;
    private DistributionSummary outboundAudioTargetBitrateMeasurements;
    private DistributionSummary outboundVideoTargetBitrateMeasurements;
    private DistributionSummary numberOfParticipants;
    private DistributionSummary callDurations;

    @PostConstruct
    void setup() {
        this.outboundAudioTrackRttMeasurements = DistributionSummary.builder(getMetricName(OUTBOUND_AUDIO_TRACKS_RTT_MEASUREMENT_METRIC_NAME))
                .description("RoundTrip Time Measurements for Outbound Audio Tracks")
                .publishPercentiles(0.05, 0.5, 0.95)
                .baseUnit("seconds")
                .publishPercentileHistogram()
                .register(this.metrics.registry);

        this.outboundVideoTrackRttMeasurements = DistributionSummary.builder(getMetricName(OUTBOUND_VIDEO_TRACKS_RTT_MEASUREMENT_METRIC_NAME))
                .description("RoundTrip Time Measurements for Outbound Video Tracks")
                .publishPercentiles(0.05, 0.5, 0.95)
                .baseUnit("seconds")
                .publishPercentileHistogram()
                .register(this.metrics.registry);

        this.outboundAudioTargetBitrateMeasurements = DistributionSummary.builder(getMetricName(OUTBOUND_AUDIO_TARGET_BITRATE_MEASUREMENT_METRIC_NAME))
                .description("Target Bitrate Measurements for Outbound Audio Tracks")
                .publishPercentiles(0.05, 0.5, 0.95)
                .baseUnit("bps")
                .publishPercentileHistogram()
                .register(this.metrics.registry);

        this.outboundVideoTargetBitrateMeasurements = DistributionSummary.builder(getMetricName(OUTBOUND_VIDEO_TARGET_BITRATE_MEASUREMENT_METRIC_NAME))
                .description("Target Bitrate Measurements for Outbound Video Tracks")
                .publishPercentiles(0.05, 0.5, 0.95)
                .baseUnit("bps")
                .publishPercentileHistogram()
                .register(this.metrics.registry);

        this.numberOfParticipants = DistributionSummary.builder(getMetricName(CALL_NUMBER_OF_PARTICIPANTS_METRIC_NAME))
                .description("Number of participants")
                .publishPercentiles(0.05, 0.5, 0.95)
                .publishPercentileHistogram()
                .register(this.metrics.registry);

        this.callDurations = DistributionSummary.builder(getMetricName(CALL_DURATIONS_METRIC_NAME))
                .description("Call Durations")
                .baseUnit("minutes")
                .publishPercentiles(0.05, 0.5, 0.95)
                .publishPercentileHistogram()
                .register(this.metrics.registry);

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

    public ReportMetrics addNumberOfParticipants(int numberOfParticipants) {
        this.numberOfParticipants.record(numberOfParticipants);
        return this;
    }

    public ReportMetrics addCallDurationInMinutes(long durationInMinutes) {
        this.callDurations.record(durationInMinutes);
        return this;
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
                    outboundAudioTrackRttMeasurements.record(reportPayload.roundTripTime);
                }
                if (reportPayload.targetBitrate != null) {
                    outboundAudioTargetBitrateMeasurements.record(reportPayload.targetBitrate);
                }
                return null;
            }

            @Override
            public Void visitOutboundVideoTrackReport(Object payload) {
                var reportPayload = ((OutboundVideoTrackReport) payload);
                if (reportPayload.roundTripTime != null) {
                    outboundVideoTrackRttMeasurements.record(reportPayload.roundTripTime);
                }
                if (reportPayload.targetBitrate != null) {
                    outboundVideoTargetBitrateMeasurements.record(reportPayload.targetBitrate);
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
