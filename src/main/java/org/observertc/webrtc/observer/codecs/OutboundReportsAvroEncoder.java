package org.observertc.webrtc.observer.codecs;

import io.micronaut.context.annotation.Prototype;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.MessageEncoder;
import org.apache.avro.specific.SpecificData;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.evaluators.OutboundReportEncoder;
import org.observertc.webrtc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class OutboundReportsAvroEncoder {
    private static final Logger logger = LoggerFactory.getLogger(OutboundReportEncoder.class);

    private final BinaryMessageEncoder<ObserverEventReport> observerEventEncoder;
    private final BinaryMessageEncoder<CallEventReport> callEventEncoder;
    private final BinaryMessageEncoder<CallMetaReport> callMetaEncoder;
    private final BinaryMessageEncoder<ClientExtensionReport> clientExtensionEncoder;
    private final BinaryMessageEncoder<ClientTransportReport> clientTransportEncoder;
    private final BinaryMessageEncoder<ClientDataChannelReport> clientDataChannelEncoder;
    private final BinaryMessageEncoder<InboundAudioTrackReport> inboundAudioTrackEncoder;
    private final BinaryMessageEncoder<InboundVideoTrackReport> inboundVideoTrackEncoder;
    private final BinaryMessageEncoder<OutboundAudioTrackReport> outboundAudioTrackEncoder;
    private final BinaryMessageEncoder<OutboundVideoTrackReport> outboundVideoTrackEncoder;
    private final BinaryMessageEncoder<MediaTrackReport> mediaTrackEncoder;

    private final BinaryMessageEncoder<SfuEventReport> sfuEventEncoder;
    private final BinaryMessageEncoder<SfuMetaReport> sfuMetaEncoder;
    private final BinaryMessageEncoder<SFUTransportReport> sfuTransportEncoder;
    private final BinaryMessageEncoder<SfuInboundRTPStreamReport> sfuInboundRTPStreamEncoder;
    private final BinaryMessageEncoder<SfuOutboundRTPStreamReport> sfuOutboundRTPStreamEncoder;
    private final BinaryMessageEncoder<SfuSctpStreamReport> sfuSctpStreamReportEncoder;

    public OutboundReportsAvroEncoder() {
        this.observerEventEncoder = new BinaryMessageEncoder<ObserverEventReport>(SpecificData.get(), ObserverEventReport.getClassSchema());
        this.callEventEncoder = new BinaryMessageEncoder<CallEventReport>(SpecificData.get(), CallEventReport.getClassSchema());
        this.callMetaEncoder = new BinaryMessageEncoder<CallMetaReport>(SpecificData.get(), CallMetaReport.getClassSchema());
        this.clientExtensionEncoder = new BinaryMessageEncoder<ClientExtensionReport>(SpecificData.get(), ClientExtensionReport.getClassSchema());
        this.clientTransportEncoder = new BinaryMessageEncoder<ClientTransportReport>(SpecificData.get(), ClientTransportReport.getClassSchema());
        this.clientDataChannelEncoder = new BinaryMessageEncoder<ClientDataChannelReport>(SpecificData.get(), ClientDataChannelReport.getClassSchema());
        this.inboundAudioTrackEncoder = new BinaryMessageEncoder<InboundAudioTrackReport>(SpecificData.get(), InboundAudioTrackReport.getClassSchema());
        this.inboundVideoTrackEncoder = new BinaryMessageEncoder<InboundVideoTrackReport>(SpecificData.get(), InboundVideoTrackReport.getClassSchema());
        this.outboundAudioTrackEncoder = new BinaryMessageEncoder<OutboundAudioTrackReport>(SpecificData.get(), OutboundAudioTrackReport.getClassSchema());
        this.outboundVideoTrackEncoder = new BinaryMessageEncoder<OutboundVideoTrackReport>(SpecificData.get(), OutboundVideoTrackReport.getClassSchema());
        this.mediaTrackEncoder = new BinaryMessageEncoder<MediaTrackReport>(SpecificData.get(), MediaTrackReport.getClassSchema());

        this.sfuEventEncoder = new BinaryMessageEncoder<SfuEventReport>(SpecificData.get(), SfuEventReport.getClassSchema());
        this.sfuMetaEncoder = new BinaryMessageEncoder<SfuMetaReport>(SpecificData.get(), SfuMetaReport.getClassSchema());
        this.sfuTransportEncoder = new BinaryMessageEncoder<SFUTransportReport>(SpecificData.get(), SFUTransportReport.getClassSchema());
        this.sfuInboundRTPStreamEncoder = new BinaryMessageEncoder<SfuInboundRTPStreamReport>(SpecificData.get(), SfuInboundRTPStreamReport.getClassSchema());
        this.sfuOutboundRTPStreamEncoder = new BinaryMessageEncoder<SfuOutboundRTPStreamReport>(SpecificData.get(), SfuOutboundRTPStreamReport.getClassSchema());
        this.sfuSctpStreamReportEncoder = new BinaryMessageEncoder<SfuSctpStreamReport>(SpecificData.get(), SfuSctpStreamReport.getClassSchema());
    }


    public OutboundReport.ObserverEventReport encodeObserverEventReport(ObserverEventReport observerEventReport) {
        return () -> encodeOrNull(this.observerEventEncoder, observerEventReport);
    }

    public OutboundReport.CallMetaOutboundReport encodeCallMetaReport(CallMetaReport callMetaReport) {
        return () -> encodeOrNull(this.callMetaEncoder, callMetaReport);
    }

    public OutboundReport.CallEventOutboundReport encodeCallEventReport(CallEventReport callEventReport) {
        return () -> encodeOrNull(this.callEventEncoder, callEventReport);
    }

    public OutboundReport.ClientExtensionReport encodeClientExtensionReport(ClientExtensionReport clientExtensionEncoder) {
        return () -> encodeOrNull(this.clientExtensionEncoder, clientExtensionEncoder);
    }

    public OutboundReport.ClientTransportReport encodeClientTransportReport(ClientTransportReport clientTransportReport) {
        return () -> encodeOrNull(this.clientTransportEncoder, clientTransportReport);
    }

    public OutboundReport.ClientDataChannelReport encodeClientDataChannelReport(ClientDataChannelReport clientDataChannelReport) {
        return () -> encodeOrNull(this.clientDataChannelEncoder, clientDataChannelReport);
    }

    public OutboundReport.InboundAudioTrackReport encodeInboundAudioTrackReport(InboundAudioTrackReport inboundAudioTrackReport) {
        return () -> encodeOrNull(this.inboundAudioTrackEncoder, inboundAudioTrackReport);
    }

    public OutboundReport.InboundVideoTrackReport encodeInboundVideoTrackReport(InboundVideoTrackReport inboundVideoTrackReport) {
        return () -> encodeOrNull(this.inboundVideoTrackEncoder, inboundVideoTrackReport);
    }

    public OutboundReport.OutboundAudioTrackReport encodeOutboundAudioTrackReport(OutboundAudioTrackReport outboundAudioTrackReport) {
        return () -> encodeOrNull(this.outboundAudioTrackEncoder, outboundAudioTrackReport);
    }

    public OutboundReport.OutboundVideoTrackReport encodeOutboundVideoTrackReport(OutboundVideoTrackReport outboundVideoTrackReport) {
        return () -> encodeOrNull(this.outboundVideoTrackEncoder, outboundVideoTrackReport);
    }

    public OutboundReport.MediaTrackReport encodeMediaTrackReport(MediaTrackReport mediaTrackReport) {
        return () -> encodeOrNull(this.mediaTrackEncoder, mediaTrackReport);
    }

    public OutboundReport.SfuEventReport encodeSfuEventReport(SfuEventReport sfuEventReport) {
        return () -> encodeOrNull(this.sfuEventEncoder, sfuEventReport);
    }

    public OutboundReport.SfuMetaReport encodeSfuMetaReport(SfuMetaReport sfuMetaReport) {
        return () -> encodeOrNull(this.sfuMetaEncoder, sfuMetaReport);
    }

    public OutboundReport.SfuTransportReport encodeSfuTransportReport(SFUTransportReport sfuTransportReport) {
        return () -> encodeOrNull(this.sfuTransportEncoder, sfuTransportReport);
    }

    public OutboundReport.SfuInboundRtpStreamReport encodeSfuInboundRtpStreamReport(SfuInboundRTPStreamReport sfuInboundRTPStreamReport) {
        return () -> encodeOrNull(this.sfuInboundRTPStreamEncoder, sfuInboundRTPStreamReport);
    }

    public OutboundReport.SfuOutboundRtpStreamReport encodeSfuOutboundRtpStreamReport(SfuOutboundRTPStreamReport sfuOutboundRTPStreamReport) {
        return () -> encodeOrNull(this.sfuOutboundRTPStreamEncoder, sfuOutboundRTPStreamReport);
    }

    public OutboundReport.SfuSctpStreamReport encodeSfuSctpStreamReport(SfuSctpStreamReport sfuSctpStreamReport) {
        return () -> encodeOrNull(this.sfuSctpStreamReportEncoder, sfuSctpStreamReport);
    }

    private<T> byte[] encodeOrNull(MessageEncoder<T> messageEncoder, T datum) {
        try {
            var encodedDatum = messageEncoder.encode(datum);
            return encodedDatum.array();
        } catch (Throwable thr) {
            // show must go on
            logger.error("Unexpected error occurred during decoding process", thr);
            return null;
        }
    }
}
