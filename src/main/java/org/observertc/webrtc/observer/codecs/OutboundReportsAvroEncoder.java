package org.observertc.webrtc.observer.codecs;

import io.reactivex.rxjava3.core.Observer;
import org.apache.avro.generic.GenericData;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.MessageEncoder;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.evaluators.OutboundReportEncoder;
import org.observertc.webrtc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutboundReportsAvroEncoder {
    private static final Logger logger = LoggerFactory.getLogger(OutboundReportEncoder.class);

    private Observer<? super OutboundReport> observer;
    private final BinaryMessageEncoder<ObserverEventReport> observerEventEncoder;
    private final BinaryMessageEncoder<CallEventReport> callEventEncoder;
    private final BinaryMessageEncoder<CallMetaReport> callMetaEncoder;
    private final BinaryMessageEncoder<ClientExtensionReport> clientExtensionEncoder;
    private final BinaryMessageEncoder<PcTransportReport> pcTransportEncoder;
    private final BinaryMessageEncoder<PcDataChannelReport> pcDataChannelEncoder;
    private final BinaryMessageEncoder<InboundAudioTrackReport> inboundAudioTrackEncoder;
    private final BinaryMessageEncoder<InboundVideoTrackReport> inboundVideoTrackEncoder;
    private final BinaryMessageEncoder<OutboundAudioTrackReport> outboundAudioTrackEncoder;
    private final BinaryMessageEncoder<OutboundVideoTrackReport> outboundVideoTrackEncoder;
    private final BinaryMessageEncoder<MediaTrackReport> mediaTrackEncoder;

    public OutboundReportsAvroEncoder() {
        this.observerEventEncoder = new BinaryMessageEncoder<ObserverEventReport>(GenericData.get(), ObserverEventReport.getClassSchema());
        this.callEventEncoder = new BinaryMessageEncoder<CallEventReport>(GenericData.get(), CallEventReport.getClassSchema());
        this.callMetaEncoder = new BinaryMessageEncoder<CallMetaReport>(GenericData.get(), CallMetaReport.getClassSchema());
        this.clientExtensionEncoder = new BinaryMessageEncoder<ClientExtensionReport>(GenericData.get(), ClientExtensionReport.getClassSchema());
        this.pcTransportEncoder = new BinaryMessageEncoder<PcTransportReport>(GenericData.get(), PcTransportReport.getClassSchema());
        this.pcDataChannelEncoder = new BinaryMessageEncoder<PcDataChannelReport>(GenericData.get(), PcDataChannelReport.getClassSchema());
        this.inboundAudioTrackEncoder = new BinaryMessageEncoder<InboundAudioTrackReport>(GenericData.get(), InboundAudioTrackReport.getClassSchema());
        this.inboundVideoTrackEncoder = new BinaryMessageEncoder<InboundVideoTrackReport>(GenericData.get(), InboundVideoTrackReport.getClassSchema());
        this.outboundAudioTrackEncoder = new BinaryMessageEncoder<OutboundAudioTrackReport>(GenericData.get(), OutboundAudioTrackReport.getClassSchema());
        this.outboundVideoTrackEncoder = new BinaryMessageEncoder<OutboundVideoTrackReport>(GenericData.get(), OutboundVideoTrackReport.getClassSchema());
        this.mediaTrackEncoder = new BinaryMessageEncoder<MediaTrackReport>(GenericData.get(), MediaTrackReport.getClassSchema());
    }


    public OutboundReport.ObserverEventReport encodeObserverEventReport(ObserverEventReport observerEventReport) {
        return () -> encodeOrNull(this.observerEventEncoder, observerEventReport);
    }

    public OutboundReport.CallEventOutboundReport encodeCallMetaReport(CallMetaReport callMetaReport) {
        return () -> encodeOrNull(this.callMetaEncoder, callMetaReport);
    }

    public OutboundReport.CallEventOutboundReport encodeCallEventReport(CallEventReport callEventReport) {
        return () -> encodeOrNull(this.callEventEncoder, callEventReport);
    }

    public OutboundReport.ClientExtensionReport encodeClientExtensionReport(ClientExtensionReport clientExtensionEncoder) {
        return () -> encodeOrNull(this.clientExtensionEncoder, clientExtensionEncoder);
    }

    public OutboundReport.PcTransportReport encodePcTransportReport(PcTransportReport pcTransportReport) {
        return () -> encodeOrNull(this.pcTransportEncoder, pcTransportReport);
    }

    public OutboundReport.PcDataChannelReport encodePcDataChannelReport(PcDataChannelReport pcDataChannelReport) {
        return () -> encodeOrNull(this.pcDataChannelEncoder, pcDataChannelReport);
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
