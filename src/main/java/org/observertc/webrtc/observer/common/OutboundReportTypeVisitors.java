package org.observertc.webrtc.observer.common;

import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import org.observertc.webrtc.observer.codecs.OutboundReportsAvroDecoder;
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
                () -> PcTransportReport.getClassSchema(),
                () -> PcDataChannelReport.getClassSchema(),
                () -> InboundAudioTrackReport.getClassSchema(),
                () -> InboundVideoTrackReport.getClassSchema(),
                () -> OutboundAudioTrackReport.getClassSchema(),
                () -> OutboundVideoTrackReport.getClassSchema(),
                () -> MediaTrackReport.getClassSchema()
        );
    }

    public static OutboundReportTypeVisitor<Void, Function<OutboundReport, SpecificRecordBase>> decoderProvider() {
        final OutboundReportsAvroDecoder decoder = new OutboundReportsAvroDecoder();
        return OutboundReportTypeVisitor.<Function<OutboundReport, SpecificRecordBase>>createSupplierVisitor(
                () -> decoder::decodeObserverEventReports,
                () -> decoder::decodeCallEventReports,
                () -> decoder::decodeCallMetaReports,
                () -> decoder::decodeClientExtensionReport,
                () -> decoder::decodePcTransportReport,
                () -> decoder::decodePcDataChannelReport,
                () -> decoder::decodeInboundAudioTrackReport,
                () -> decoder::decodeInboundVideoTrackReport,
                () -> decoder::decodeOutboundAudioTrackReport,
                () -> decoder::decodeOutboundVideoTrackReport,
                () -> decoder::decodeMediaTrackReport
        );
    }

    private OutboundReportTypeVisitors() {

    }
}
