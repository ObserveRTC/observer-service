package org.observertc.webrtc.observer.codecs;

import io.micronaut.context.annotation.Prototype;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.MessageDecoder;
import org.apache.avro.specific.SpecificData;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.observer.evaluators.OutboundReportEncoder;
import org.observertc.webrtc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Prototype
public class OutboundReportsAvroDecoder {
    private static final Logger logger = LoggerFactory.getLogger(OutboundReportEncoder.class);

    private final BinaryMessageDecoder<ObserverEventReport> observerEventDecoder;
    private final BinaryMessageDecoder<CallEventReport> callEventDecoder;
    private final BinaryMessageDecoder<CallMetaReport> callMetaDecoder;
    private final BinaryMessageDecoder<ClientExtensionReport> clientExtensionDecoder;
    private final BinaryMessageDecoder<ClientTransportReport> clientTransportDecoder;
    private final BinaryMessageDecoder<ClientDataChannelReport> clientDataChannelDecoder;
    private final BinaryMessageDecoder<InboundAudioTrackReport> inboundAudioTrackDecoder;
    private final BinaryMessageDecoder<InboundVideoTrackReport> inboundVideoTrackDecoder;
    private final BinaryMessageDecoder<OutboundAudioTrackReport> outboundAudioTrackDecoder;
    private final BinaryMessageDecoder<OutboundVideoTrackReport> outboundVideoTrackDecoder;
    private final BinaryMessageDecoder<MediaTrackReport> mediaTrackDecoder;

    public OutboundReportsAvroDecoder() {

        this.observerEventDecoder = new BinaryMessageDecoder<ObserverEventReport>(SpecificData.get(), ObserverEventReport.getClassSchema());
        this.callEventDecoder = new BinaryMessageDecoder<CallEventReport>(SpecificData.get(), CallEventReport.getClassSchema());
        this.callMetaDecoder = new BinaryMessageDecoder<CallMetaReport>(SpecificData.get(), CallMetaReport.getClassSchema());
        this.clientExtensionDecoder = new BinaryMessageDecoder<ClientExtensionReport>(SpecificData.get(), ClientExtensionReport.getClassSchema());
        this.clientTransportDecoder = new BinaryMessageDecoder<ClientTransportReport>(SpecificData.get(), ClientTransportReport.getClassSchema());
        this.clientDataChannelDecoder = new BinaryMessageDecoder<ClientDataChannelReport>(SpecificData.get(), ClientDataChannelReport.getClassSchema());
        this.inboundAudioTrackDecoder = new BinaryMessageDecoder<InboundAudioTrackReport>(SpecificData.get(), InboundAudioTrackReport.getClassSchema());
        this.inboundVideoTrackDecoder = new BinaryMessageDecoder<InboundVideoTrackReport>(SpecificData.get(), InboundVideoTrackReport.getClassSchema());
        this.outboundAudioTrackDecoder = new BinaryMessageDecoder<OutboundAudioTrackReport>(SpecificData.get(), OutboundAudioTrackReport.getClassSchema());
        this.outboundVideoTrackDecoder = new BinaryMessageDecoder<OutboundVideoTrackReport>(SpecificData.get(), OutboundVideoTrackReport.getClassSchema());
        this.mediaTrackDecoder = new BinaryMessageDecoder<MediaTrackReport>(SpecificData.get(), MediaTrackReport.getClassSchema());
    }

    public ObserverEventReport decodeObserverEventReports(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        return this.decodeOrNull(this.observerEventDecoder, outboundReport);
    }

    public CallEventReport decodeCallEventReports(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        return this.decodeOrNull(this.callEventDecoder, outboundReport);
    }

    public CallMetaReport decodeCallMetaReports(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        return this.decodeOrNull(this.callMetaDecoder, outboundReport);
    }

    public ClientExtensionReport decodeClientExtensionReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        return this.decodeOrNull(this.clientExtensionDecoder, outboundReport);
    }

    public ClientTransportReport decodeClientTransportReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        return this.decodeOrNull(this.clientTransportDecoder, outboundReport);
    }

    public ClientDataChannelReport decodeClientDataChannelReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        return this.decodeOrNull(this.clientDataChannelDecoder, outboundReport);
    }

    public InboundAudioTrackReport decodeInboundAudioTrackReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        return this.decodeOrNull(this.inboundAudioTrackDecoder, outboundReport);
    }

    public InboundVideoTrackReport decodeInboundVideoTrackReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        return this.decodeOrNull(this.inboundVideoTrackDecoder, outboundReport);
    }

    public OutboundAudioTrackReport decodeOutboundAudioTrackReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        return this.decodeOrNull(this.outboundAudioTrackDecoder, outboundReport);
    }

    public OutboundVideoTrackReport decodeOutboundVideoTrackReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        return this.decodeOrNull(this.outboundVideoTrackDecoder, outboundReport);
    }

    public MediaTrackReport decodeMediaTrackReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        return this.decodeOrNull(this.mediaTrackDecoder, outboundReport);
    }

    private<T> T decodeOrNull(MessageDecoder<T> messageDecoder, OutboundReport outboundReport) {
        try {
            byte[] bytes = outboundReport.getBytes();
            return messageDecoder.decode(bytes);
        } catch (Throwable thr) {
            // show must go on
            logger.error("Unexpected error occurred during decoding process", thr);
            return null;
        }
    }
}
