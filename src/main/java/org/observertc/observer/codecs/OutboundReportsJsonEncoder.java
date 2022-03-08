package org.observertc.observer.codecs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.OutboundReport;
import org.observertc.schemas.reports.*;
import org.observertc.webrtc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class OutboundReportsJsonEncoder implements Encoder{
    private static final Logger logger = LoggerFactory.getLogger(OutboundReportsJsonEncoder.class);

    private ObjectMapper mapper = new ObjectMapper();

    public OutboundReportsJsonEncoder() {
        SimpleModule module = new SimpleModule();
        try {
            module.addSerializer(ObserverEventReport.class, new AvroBuilderToJson<ObserverEventReport>(ObserverEventReport.class, ObserverEventReport.getClassSchema()));
            module.addSerializer(CallEventReport.class, new AvroBuilderToJson<CallEventReport>(CallEventReport.class, CallEventReport.getClassSchema()));
            module.addSerializer(CallMetaReport.class, new AvroBuilderToJson<CallMetaReport>(CallMetaReport.class, CallMetaReport.getClassSchema()));
            module.addSerializer(ClientExtensionReport.class, new AvroBuilderToJson<ClientExtensionReport>(ClientExtensionReport.class, ClientExtensionReport.getClassSchema()));
            module.addSerializer(ClientTransportReport.class, new AvroBuilderToJson<ClientTransportReport>(ClientTransportReport.class, ClientTransportReport.getClassSchema()));
            module.addSerializer(ClientDataChannelReport.class, new AvroBuilderToJson<ClientDataChannelReport>(ClientDataChannelReport.class, ClientDataChannelReport.getClassSchema()));
            module.addSerializer(InboundAudioTrackReport.class, new AvroBuilderToJson<InboundAudioTrackReport>(InboundAudioTrackReport.class, InboundAudioTrackReport.getClassSchema()));
            module.addSerializer(InboundVideoTrackReport.class, new AvroBuilderToJson<InboundVideoTrackReport>(InboundVideoTrackReport.class, InboundVideoTrackReport.getClassSchema()));
            module.addSerializer(OutboundAudioTrackReport.class, new AvroBuilderToJson<OutboundAudioTrackReport>(OutboundAudioTrackReport.class, OutboundAudioTrackReport.getClassSchema()));
            module.addSerializer(OutboundVideoTrackReport.class, new AvroBuilderToJson<OutboundVideoTrackReport>(OutboundVideoTrackReport.class, OutboundVideoTrackReport.getClassSchema()));
            module.addSerializer(MediaTrackReport.class, new AvroBuilderToJson<MediaTrackReport>(MediaTrackReport.class, MediaTrackReport.getClassSchema()));
            module.addSerializer(SfuEventReport.class, new AvroBuilderToJson<SfuEventReport>(SfuEventReport.class, SfuEventReport.getClassSchema()));
            module.addSerializer(SfuMetaReport.class, new AvroBuilderToJson<SfuMetaReport>(SfuMetaReport.class, SfuMetaReport.getClassSchema()));
            module.addSerializer(SFUTransportReport.class, new AvroBuilderToJson<SFUTransportReport>(SFUTransportReport.class, SFUTransportReport.getClassSchema()));
            module.addSerializer(SfuInboundRtpPadReport.class, new AvroBuilderToJson<SfuInboundRtpPadReport>(SfuInboundRtpPadReport.class, SfuInboundRtpPadReport.getClassSchema()));
            module.addSerializer(SfuOutboundRtpPadReport.class, new AvroBuilderToJson<SfuOutboundRtpPadReport>(SfuOutboundRtpPadReport.class, SfuOutboundRtpPadReport.getClassSchema()));
            module.addSerializer(SfuSctpStreamReport.class, new AvroBuilderToJson<SfuSctpStreamReport>(SfuSctpStreamReport.class, SfuSctpStreamReport.getClassSchema()));
        } catch (Exception ex) {
            logger.error("Error occurred during initialization", ex);
        }
        mapper.registerModule(module);
    }


    public OutboundReport.ObserverEventReport encodeObserverEventReport(ObserverEventReport observerEventReport) {
        return () -> encodeOrNull(observerEventReport);
    }

    public OutboundReport.CallMetaOutboundReport encodeCallMetaReport(CallMetaReport callMetaReport) {
        return () -> encodeOrNull(callMetaReport);
    }

    public OutboundReport.CallEventOutboundReport encodeCallEventReport(CallEventReport callEventReport) {
        return () -> encodeOrNull(callEventReport);
    }

    public OutboundReport.ClientExtensionReport encodeClientExtensionReport(ClientExtensionReport clientExtensionEncoder) {
        return () -> encodeOrNull(clientExtensionEncoder);
    }

    public OutboundReport.ClientTransportReport encodeClientTransportReport(ClientTransportReport clientTransportReport) {
        return () -> encodeOrNull(clientTransportReport);
    }

    public OutboundReport.ClientDataChannelReport encodeClientDataChannelReport(ClientDataChannelReport clientDataChannelReport) {
        return () -> encodeOrNull(clientDataChannelReport);
    }

    public OutboundReport.InboundAudioTrackReport encodeInboundAudioTrackReport(InboundAudioTrackReport inboundAudioTrackReport) {
        return () -> encodeOrNull(inboundAudioTrackReport);
    }

    public OutboundReport.InboundVideoTrackReport encodeInboundVideoTrackReport(InboundVideoTrackReport inboundVideoTrackReport) {
        return () -> encodeOrNull(inboundVideoTrackReport);
    }

    public OutboundReport.OutboundAudioTrackReport encodeOutboundAudioTrackReport(OutboundAudioTrackReport outboundAudioTrackReport) {
        return () -> encodeOrNull(outboundAudioTrackReport);
    }

    public OutboundReport.OutboundVideoTrackReport encodeOutboundVideoTrackReport(OutboundVideoTrackReport outboundVideoTrackReport) {
        return () -> encodeOrNull(outboundVideoTrackReport);
    }

    public OutboundReport.MediaTrackReport encodeMediaTrackReport(MediaTrackReport mediaTrackReport) {
        return () -> encodeOrNull(mediaTrackReport);
    }

    public OutboundReport.SfuEventReport encodeSfuEventReport(SfuEventReport sfuEventReport) {
        return () -> encodeOrNull(sfuEventReport);
    }

    public OutboundReport.SfuMetaReport encodeSfuMetaReport(SfuMetaReport sfuMetaReport) {
        return () -> encodeOrNull(sfuMetaReport);
    }

    public OutboundReport.SfuTransportReport encodeSfuTransportReport(SFUTransportReport sfuTransportReport) {
        return () -> encodeOrNull(sfuTransportReport);
    }

    public OutboundReport.SfuInboundRtpPadReport encodeSfuInboundRtpPadReport(SfuInboundRtpPadReport sfuRtpSourceReport) {
        return () -> encodeOrNull(sfuRtpSourceReport);
    }

    public OutboundReport.SfuOutboundRtpPadReport encodeSfuOutboundRtpPadReport(SfuOutboundRtpPadReport sfuRtpSinkReport) {
        return () -> encodeOrNull(sfuRtpSinkReport);
    }

    public OutboundReport.SfuSctpStreamReport encodeSfuSctpStreamReport(SfuSctpStreamReport sfuSctpStreamReport) {
        return () -> encodeOrNull(sfuSctpStreamReport);
    }

    private<T> byte[] encodeOrNull(T datum) {
        try {
            var result = mapper.writeValueAsBytes(datum);
            return result;
        } catch (Exception ex) {
            logger.error("Unexpected error occurred in encoding process", ex);
            return null;
        }
    }
}
