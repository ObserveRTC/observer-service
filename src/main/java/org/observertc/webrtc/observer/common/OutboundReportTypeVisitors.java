package org.observertc.webrtc.observer.common;

import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import org.observertc.webrtc.observer.codecs.Decoder;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.schemas.reports.*;

import java.util.function.Function;

/**
 * Collection of useful object creations for report processing
 */
public final class OutboundReportTypeVisitors {

    public static OutboundReportTypeVisitor<Void, Schema> avroSchemaResolver() {
        return OutboundReportTypeVisitor.<Schema>createSupplierVisitor(
                () -> ObserverEventReport.getClassSchema(),
                () -> CallEventReport.getClassSchema(),
                () -> CallMetaReport.getClassSchema(),
                () -> ClientExtensionReport.getClassSchema(),
                () -> ClientTransportReport.getClassSchema(),
                () -> ClientDataChannelReport.getClassSchema(),
                () -> InboundAudioTrackReport.getClassSchema(),
                () -> InboundVideoTrackReport.getClassSchema(),
                () -> OutboundAudioTrackReport.getClassSchema(),
                () -> OutboundVideoTrackReport.getClassSchema(),
                () -> MediaTrackReport.getClassSchema(),
                () -> SfuEventReport.getClassSchema(),
                () -> SfuMetaReport.getClassSchema(),
                () -> SFUTransportReport.getClassSchema(),
                () -> SfuInboundRtpPadReport.getClassSchema(),
                () -> SfuOutboundRtpPadReport.getClassSchema(),
                () -> SfuSctpStreamReport.getClassSchema()
        );
    }

    public static OutboundReportTypeVisitor<Void, Function<OutboundReport, SpecificRecordBase>> decoderProvider(Decoder decoder) {
        return OutboundReportTypeVisitor.<Function<OutboundReport, SpecificRecordBase>>createSupplierVisitor(
                () -> decoder::decodeObserverEventReports,
                () -> decoder::decodeCallEventReports,
                () -> decoder::decodeCallMetaReports,
                () -> decoder::decodeClientExtensionReport,
                () -> decoder::decodeClientTransportReport,
                () -> decoder::decodeClientDataChannelReport,
                () -> decoder::decodeInboundAudioTrackReport,
                () -> decoder::decodeInboundVideoTrackReport,
                () -> decoder::decodeOutboundAudioTrackReport,
                () -> decoder::decodeOutboundVideoTrackReport,
                () -> decoder::decodeMediaTrackReport,
                () -> decoder::decodeSfuEventReport,
                () -> decoder::decodeSfuMetaReport,
                () -> decoder::decodeSfuTransportReport,
                () -> decoder::decodeSfuInboundRtpPadReport,
                () -> decoder::decodeSfuOutboundRtpPadReport,
                () -> decoder::decodeSfuSctpStreamReport
        );
    }

    public static OutboundReportTypeVisitor<Void, Boolean> makeTypeFilter(ObserverConfig.ReportsConfig config) {
        return OutboundReportTypeVisitor.<Boolean>createSupplierVisitor(
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
                () -> config.sendMediaTracks,
                () -> config.sendSfuEvents,
                () -> config.sendSfuMeta,
                () -> config.sendSfuTransports,
                () -> config.sendSfuInboundRtpStreams,
                () -> config.sendSfuOutboundRtpStreams,
                () -> config.sendSfuSctpStreams
        );
    }

    private OutboundReportTypeVisitors() {

    }
}
