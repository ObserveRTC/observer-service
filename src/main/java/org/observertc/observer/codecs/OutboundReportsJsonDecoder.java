package org.observertc.observer.codecs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.micronaut.context.annotation.Prototype;
import org.observertc.observer.common.OutboundReport;
import org.observertc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Prototype
public class OutboundReportsJsonDecoder implements Decoder{
    private static final Logger logger = LoggerFactory.getLogger(OutboundReportsJsonDecoder.class);

    private ObjectMapper mapper = new ObjectMapper();

    public OutboundReportsJsonDecoder() {
        SimpleModule module = new SimpleModule();
        try {
            module.addDeserializer(ObserverEventReport.Builder.class, new JsonToAvroBuilder<>(ObserverEventReport.Builder.class, ObserverEventReport.getClassSchema(), ObserverEventReport::newBuilder));
            module.addDeserializer(CallEventReport.Builder.class, new JsonToAvroBuilder<>(CallEventReport.Builder.class, CallEventReport.getClassSchema(), CallEventReport::newBuilder));
            module.addDeserializer(CallMetaReport.Builder.class, new JsonToAvroBuilder<>(CallMetaReport.Builder.class, CallMetaReport.getClassSchema(), CallMetaReport::newBuilder));
            module.addDeserializer(ClientExtensionReport.Builder.class, new JsonToAvroBuilder<>(ClientExtensionReport.Builder.class, ClientExtensionReport.getClassSchema(), ClientExtensionReport::newBuilder));
            module.addDeserializer(ClientTransportReport.Builder.class, new JsonToAvroBuilder<>(ClientTransportReport.Builder.class, ClientTransportReport.getClassSchema(), ClientTransportReport::newBuilder));
            module.addDeserializer(ClientDataChannelReport.Builder.class, new JsonToAvroBuilder<>(ClientDataChannelReport.Builder.class, ClientDataChannelReport.getClassSchema(), ClientDataChannelReport::newBuilder));
            module.addDeserializer(InboundAudioTrackReport.Builder.class, new JsonToAvroBuilder<>(InboundAudioTrackReport.Builder.class, InboundAudioTrackReport.getClassSchema(), InboundAudioTrackReport::newBuilder));
            module.addDeserializer(InboundVideoTrackReport.Builder.class, new JsonToAvroBuilder<>(InboundVideoTrackReport.Builder.class, InboundVideoTrackReport.getClassSchema(), InboundVideoTrackReport::newBuilder));
            module.addDeserializer(OutboundAudioTrackReport.Builder.class, new JsonToAvroBuilder<>(OutboundAudioTrackReport.Builder.class, OutboundAudioTrackReport.getClassSchema(), OutboundAudioTrackReport::newBuilder));
            module.addDeserializer(OutboundVideoTrackReport.Builder.class, new JsonToAvroBuilder<>(OutboundVideoTrackReport.Builder.class, OutboundVideoTrackReport.getClassSchema(), OutboundVideoTrackReport::newBuilder));
            module.addDeserializer(MediaTrackReport.Builder.class, new JsonToAvroBuilder<>(MediaTrackReport.Builder.class, MediaTrackReport.getClassSchema(), MediaTrackReport::newBuilder));
            module.addDeserializer(SfuEventReport.Builder.class, new JsonToAvroBuilder<>(SfuEventReport.Builder.class, SfuEventReport.getClassSchema(), SfuEventReport::newBuilder));
            module.addDeserializer(SfuMetaReport.Builder.class, new JsonToAvroBuilder<>(SfuMetaReport.Builder.class, SfuMetaReport.getClassSchema(), SfuMetaReport::newBuilder));
            module.addDeserializer(SFUTransportReport.Builder.class, new JsonToAvroBuilder<>(SFUTransportReport.Builder.class, SFUTransportReport.getClassSchema(), SFUTransportReport::newBuilder));
            module.addDeserializer(SfuInboundRtpPadReport.Builder.class, new JsonToAvroBuilder<>(SfuInboundRtpPadReport.Builder.class, SfuInboundRtpPadReport.getClassSchema(), SfuInboundRtpPadReport::newBuilder));
            module.addDeserializer(SfuOutboundRtpPadReport.Builder.class, new JsonToAvroBuilder<>(SfuOutboundRtpPadReport.Builder.class, SfuOutboundRtpPadReport.getClassSchema(), SfuOutboundRtpPadReport::newBuilder));
            module.addDeserializer(SfuSctpStreamReport.Builder.class, new JsonToAvroBuilder<>(SfuSctpStreamReport.Builder.class, SfuSctpStreamReport.getClassSchema(), SfuSctpStreamReport::newBuilder));
        } catch (Exception ex) {
            logger.error("Error occurred during initialization", ex);
        }
        mapper.registerModule(module);
    }

    public ObserverEventReport decodeObserverEventReports(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(ObserverEventReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    public CallEventReport decodeCallEventReports(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(CallEventReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    public CallMetaReport decodeCallMetaReports(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(CallMetaReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    public ClientExtensionReport decodeClientExtensionReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(ClientExtensionReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    public ClientTransportReport decodeClientTransportReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(ClientTransportReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    public ClientDataChannelReport decodeClientDataChannelReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(ClientDataChannelReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    public InboundAudioTrackReport decodeInboundAudioTrackReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(InboundAudioTrackReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    public InboundVideoTrackReport decodeInboundVideoTrackReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(InboundVideoTrackReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    public OutboundAudioTrackReport decodeOutboundAudioTrackReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(OutboundAudioTrackReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    public OutboundVideoTrackReport decodeOutboundVideoTrackReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(OutboundVideoTrackReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    public MediaTrackReport decodeMediaTrackReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(MediaTrackReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    public SfuEventReport decodeSfuEventReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(SfuEventReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    public SfuMetaReport decodeSfuMetaReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(SfuMetaReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    public SFUTransportReport decodeSfuTransportReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(SFUTransportReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    public SfuInboundRtpPadReport decodeSfuInboundRtpPadReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(SfuInboundRtpPadReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    public SfuOutboundRtpPadReport decodeSfuOutboundRtpPadReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(SfuOutboundRtpPadReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    public SfuSctpStreamReport decodeSfuSctpStreamReport(OutboundReport outboundReport) {
        if (Objects.isNull(outboundReport)) {
            return null;
        }
        var builder = this.decodeOrNull(SfuSctpStreamReport.Builder.class, outboundReport);
        if (Objects.isNull(builder)) {
            return null;
        }
        return builder.build();
    }

    private<T> T decodeOrNull(Class<T> klass, OutboundReport outboundReport) {
        try {
            byte[] bytes = outboundReport.getBytes();
            return this.mapper.readValue(bytes, klass);
        } catch (Throwable thr) {
            // show must go on
            logger.error("Unexpected error occurred during decoding process", thr);
            return null;
        }
    }
}
