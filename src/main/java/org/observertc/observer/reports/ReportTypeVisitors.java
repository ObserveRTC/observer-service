package org.observertc.observer.reports;

import org.observertc.observer.configs.ReportsConfig;
import org.observertc.schemas.reports.*;

import java.util.Objects;

/**
 * Collection of useful object creations for report processing
 */
public final class ReportTypeVisitors {


    public static ReportTypeVisitor<Object, Report> reportMuxer() {
        return ReportTypeVisitor.<Object, Report>createFunctionalVisitor(
                report -> Report.fromObserverEventReport((ObserverEventReport) report),
                report -> Report.fromCallEventReport((CallEventReport) report),
                report -> Report.fromCallMetaReport((CallMetaReport) report),
                report -> Report.fromClientExtensionReport((ClientExtensionReport) report),
                report -> Report.fromClientTransportReport((ClientTransportReport) report),
                report -> Report.fromClientDataChannelReport((ClientDataChannelReport) report),
                report -> Report.fromInboundAudioTrackReport((InboundAudioTrackReport) report),
                report -> Report.fromInboundVideoTrackReport((InboundVideoTrackReport) report),
                report -> Report.fromOutboundAudioTrackReport((OutboundAudioTrackReport) report),
                report -> Report.fromOutboundVideoTrackReport((OutboundVideoTrackReport) report),
                report -> Report.fromSfuEventReport((SfuEventReport) report),
                report -> Report.fromSfuMetaReport((SfuMetaReport) report),
                report -> Report.fromSfuExtensionReport((SfuExtensionReport) report),
                report -> Report.fromSfuTransportReport((SFUTransportReport) report),
                report -> Report.fromSfuInboundRtpPadReport((SfuInboundRtpPadReport) report),
                report -> Report.fromSfuOutboundRtpPadReport((SfuOutboundRtpPadReport) report),
                report -> Report.fromSfuSctpStreamReport((SfuSctpStreamReport) report)
        );
    }

    public static ReportTypeVisitor<Object, String> serviceIdGetter() {
        return ReportTypeVisitor.<Object, String>createFunctionalVisitor(
                payload -> Objects.nonNull(payload) ? ((ObserverEventReport) payload).serviceId : null,
                payload -> Objects.nonNull(payload) ? ((CallEventReport) payload).serviceId : null,
                payload -> Objects.nonNull(payload) ? ((CallMetaReport) payload).serviceId : null,
                payload -> Objects.nonNull(payload) ? ((ClientExtensionReport) payload).serviceId : null,
                payload -> Objects.nonNull(payload) ? ((ClientTransportReport) payload).serviceId : null,
                payload -> Objects.nonNull(payload) ? ((ClientDataChannelReport) payload).serviceId : null,
                payload -> Objects.nonNull(payload) ? ((InboundAudioTrackReport) payload).serviceId : null,
                payload -> Objects.nonNull(payload) ? ((InboundVideoTrackReport) payload).serviceId : null,
                payload -> Objects.nonNull(payload) ? ((OutboundAudioTrackReport) payload).serviceId : null,
                payload -> Objects.nonNull(payload) ? ((OutboundVideoTrackReport) payload).serviceId : null,
                payload -> Objects.nonNull(payload) ? ((SfuEventReport) payload).serviceId : null,
                payload -> Objects.nonNull(payload) ? ((SfuMetaReport) payload).serviceId : null,
                payload -> Objects.nonNull(payload) ? ((SfuExtensionReport) payload).serviceId : null,
                payload -> Objects.nonNull(payload) ? ((SFUTransportReport) payload).serviceId : null,
                payload -> Objects.nonNull(payload) ? ((SfuInboundRtpPadReport) payload).serviceId : null,
                payload -> Objects.nonNull(payload) ? ((SfuOutboundRtpPadReport) payload).serviceId : null,
                payload -> Objects.nonNull(payload) ? ((SfuSctpStreamReport) payload).serviceId : null
        );
    }

    public static ReportTypeVisitor<Void, Boolean> makeTypeFilter(ReportsConfig config) {
        return ReportTypeVisitor.<Boolean>createSupplierVisitor(
                () -> config.sendObserverEvents,
                () -> config.sendCallEvents,
                () -> config.sendCallMeta,
                () -> config.sendClientExtensions,
                () -> config.sendClientTransports,
                () -> config.sendClientDataChannels,
                () -> config.sendInboundAudioTracks,
                () -> config.sendInboundVideoTracks,
                () -> config.sendOutboundAudioTracks,
                () -> config.sendOutboundVideoTracks,
                () -> config.sendSfuEvents,
                () -> config.sendSfuMeta,
                () -> config.sendSfuExtensions,
                () -> config.sendSfuTransports,
                () -> config.sendSfuInboundRtpStreams,
                () -> config.sendSfuOutboundRtpStreams,
                () -> config.sendSfuSctpStreams
        );
    }

    private ReportTypeVisitors() {

    }
}
