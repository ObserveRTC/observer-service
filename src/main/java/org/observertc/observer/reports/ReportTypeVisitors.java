package org.observertc.observer.reports;

import org.observertc.observer.configs.ObserverConfig;
import org.observertc.schemas.reports.*;

/**
 * Collection of useful object creations for report processing
 */
public final class ReportTypeVisitors {

//    public static OutboundReportTypeVisitor<Void, Schema> avroSchemaResolver() {
//        return OutboundReportTypeVisitor.<Schema>createSupplierVisitor(
//                () -> ObserverEventReport.getClassSchema(),
//                () -> CallEventReport.getClassSchema(),
//                () -> CallMetaReport.getClassSchema(),
//                () -> ClientExtensionReport.getClassSchema(),
//                () -> ClientTransportReport.getClassSchema(),
//                () -> ClientDataChannelReport.getClassSchema(),
//                () -> InboundAudioTrackReport.getClassSchema(),
//                () -> InboundVideoTrackReport.getClassSchema(),
//                () -> OutboundAudioTrackReport.getClassSchema(),
//                () -> OutboundVideoTrackReport.getClassSchema(),
//                () -> MediaTrackReport.getClassSchema(),
//                () -> SfuEventReport.getClassSchema(),
//                () -> SfuMetaReport.getClassSchema(),
//                () -> SFUTransportReport.getClassSchema(),
//                () -> SfuInboundRtpPadReport.getClassSchema(),
//                () -> SfuOutboundRtpPadReport.getClassSchema(),
//                () -> SfuSctpStreamReport.getClassSchema()
//        );
//    }

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
                report -> Report.fromSfuTransportReport((SFUTransportReport) report),
                report -> Report.fromSfuInboundRtpPadReport((SfuInboundRtpPadReport) report),
                report -> Report.fromSfuOutboundRtpPadReport((SfuOutboundRtpPadReport) report),
                report -> Report.fromSfuSctpStreamReport((SfuSctpStreamReport) report)
        );
    }
//
//    public static OutboundReportTypeVisitor<Void, Function<OutboundReport, SpecificRecordBase>> decoderProvider(Decoder decoder) {
//        return OutboundReportTypeVisitor.<Function<OutboundReport, SpecificRecordBase>>createSupplierVisitor(
//                () -> decoder::decodeObserverEventReports,
//                () -> decoder::decodeCallEventReports,
//                () -> decoder::decodeCallMetaReports,
//                () -> decoder::decodeClientExtensionReport,
//                () -> decoder::decodeClientTransportReport,
//                () -> decoder::decodeClientDataChannelReport,
//                () -> decoder::decodeInboundAudioTrackReport,
//                () -> decoder::decodeInboundVideoTrackReport,
//                () -> decoder::decodeOutboundAudioTrackReport,
//                () -> decoder::decodeOutboundVideoTrackReport,
//                () -> decoder::decodeMediaTrackReport,
//                () -> decoder::decodeSfuEventReport,
//                () -> decoder::decodeSfuMetaReport,
//                () -> decoder::decodeSfuTransportReport,
//                () -> decoder::decodeSfuInboundRtpPadReport,
//                () -> decoder::decodeSfuOutboundRtpPadReport,
//                () -> decoder::decodeSfuSctpStreamReport
//        );
//    }
//
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
                () -> config.sendSfuTransports,
                () -> config.sendSfuInboundRtpStreams,
                () -> config.sendSfuOutboundRtpStreams,
                () -> config.sendSfuSctpStreams
        );
    }

    private ReportTypeVisitors() {

    }
}
