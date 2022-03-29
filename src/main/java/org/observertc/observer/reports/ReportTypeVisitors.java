package org.observertc.observer.reports;

import org.observertc.observer.configs.ObserverConfig;
import org.observertc.schemas.reports.*;

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

    public static ReportTypeVisitor<Void, Boolean> makeTypeFilter(ObserverConfig.ReportsConfig config) {
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
