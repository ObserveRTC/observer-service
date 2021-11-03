package org.observertc.webrtc.observer.codecs;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.observer.common.OutboundReport;
import org.observertc.webrtc.schemas.reports.*;

@Prototype
public interface Decoder {

    ObserverEventReport decodeObserverEventReports(OutboundReport outboundReport);

    CallEventReport decodeCallEventReports(OutboundReport outboundReport);

    CallMetaReport decodeCallMetaReports(OutboundReport outboundReport);

    ClientExtensionReport decodeClientExtensionReport(OutboundReport outboundReport);

    ClientTransportReport decodeClientTransportReport(OutboundReport outboundReport);

    ClientDataChannelReport decodeClientDataChannelReport(OutboundReport outboundReport);

    InboundAudioTrackReport decodeInboundAudioTrackReport(OutboundReport outboundReport);

    InboundVideoTrackReport decodeInboundVideoTrackReport(OutboundReport outboundReport);

    OutboundAudioTrackReport decodeOutboundAudioTrackReport(OutboundReport outboundReport);

    OutboundVideoTrackReport decodeOutboundVideoTrackReport(OutboundReport outboundReport);

    MediaTrackReport decodeMediaTrackReport(OutboundReport outboundReport);

    SfuEventReport decodeSfuEventReport(OutboundReport outboundReport);

    SfuMetaReport decodeSfuMetaReport(OutboundReport outboundReport);

    SFUTransportReport decodeSfuTransportReport(OutboundReport outboundReport);

    SfuInboundRtpPadReport decodeSfuInboundRtpPadReport(OutboundReport outboundReport);

    SfuOutboundRtpPadReport decodeSfuOutboundRtpPadReport(OutboundReport outboundReport);

    SfuSctpStreamReport decodeSfuSctpStreamReport(OutboundReport outboundReport);

}
