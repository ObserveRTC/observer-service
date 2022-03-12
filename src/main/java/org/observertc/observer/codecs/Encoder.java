package org.observertc.observer.codecs;

import org.observertc.observer.common.OutboundReport;
import org.observertc.schemas.reports.*;

public interface Encoder {


    OutboundReport.ObserverEventReport encodeObserverEventReport(ObserverEventReport observerEventReport);

    OutboundReport.CallMetaOutboundReport encodeCallMetaReport(CallMetaReport callMetaReport);

    OutboundReport.CallEventOutboundReport encodeCallEventReport(CallEventReport callEventReport);

    OutboundReport.ClientExtensionReport encodeClientExtensionReport(ClientExtensionReport clientExtensionEncoder);

    OutboundReport.ClientTransportReport encodeClientTransportReport(ClientTransportReport clientTransportReport);

    OutboundReport.ClientDataChannelReport encodeClientDataChannelReport(ClientDataChannelReport clientDataChannelReport);

    OutboundReport.InboundAudioTrackReport encodeInboundAudioTrackReport(InboundAudioTrackReport inboundAudioTrackReport);

    OutboundReport.InboundVideoTrackReport encodeInboundVideoTrackReport(InboundVideoTrackReport inboundVideoTrackReport);

    OutboundReport.OutboundAudioTrackReport encodeOutboundAudioTrackReport(OutboundAudioTrackReport outboundAudioTrackReport);

    OutboundReport.OutboundVideoTrackReport encodeOutboundVideoTrackReport(OutboundVideoTrackReport outboundVideoTrackReport);

    OutboundReport.MediaTrackReport encodeMediaTrackReport(MediaTrackReport mediaTrackReport);

    OutboundReport.SfuEventReport encodeSfuEventReport(SfuEventReport sfuEventReport);

    OutboundReport.SfuMetaReport encodeSfuMetaReport(SfuMetaReport sfuMetaReport);

    OutboundReport.SfuTransportReport encodeSfuTransportReport(SFUTransportReport sfuTransportReport);

    OutboundReport.SfuInboundRtpPadReport encodeSfuInboundRtpPadReport(SfuInboundRtpPadReport sfuInboundRtpPadReport);

    OutboundReport.SfuOutboundRtpPadReport encodeSfuOutboundRtpPadReport(SfuOutboundRtpPadReport sfuOutboundRtpPadReport);

    OutboundReport.SfuSctpStreamReport encodeSfuSctpStreamReport(SfuSctpStreamReport sfuSctpStreamReport);

}
