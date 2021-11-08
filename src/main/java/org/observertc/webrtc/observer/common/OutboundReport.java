package org.observertc.webrtc.observer.common;

public interface OutboundReport {
    // TODO: through the encoder we can define key assigning strategy
//    default UUID getKey() { return UUID.randomUUID(); }
    ReportType getType();
    byte[] getBytes();

    @FunctionalInterface
    interface ObserverEventReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.OBSERVER_EVENT;
        }
    }

    @FunctionalInterface
    interface CallEventOutboundReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.CALL_EVENT;
        }
    }

    @FunctionalInterface
    interface CallMetaOutboundReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.CALL_META_DATA;
        }
    }

    @FunctionalInterface
    interface ClientExtensionReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.CLIENT_EXTENSION_DATA;
        }
    }

    @FunctionalInterface
    interface ClientTransportReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.PEER_CONNECTION_TRANSPORT;
        }
    }

    @FunctionalInterface
    interface ClientDataChannelReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.PEER_CONNECTION_DATA_CHANNEL;
        }
    }

    @FunctionalInterface
    interface InboundAudioTrackReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.INBOUND_AUDIO_TRACK;
        }
    }

    @FunctionalInterface
    interface InboundVideoTrackReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.INBOUND_VIDEO_TRACK;
        }
    }

    @FunctionalInterface
    interface OutboundAudioTrackReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.OUTBOUND_AUDIO_TRACK;
        }
    }

    @FunctionalInterface
    interface OutboundVideoTrackReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.OUTBOUND_VIDEO_TRACK;
        }
    }

    @FunctionalInterface
    interface MediaTrackReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.MEDIA_TRACK;
        }
    }

    @FunctionalInterface
    interface SfuEventReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.SFU_EVENT;
        }
    }

    @FunctionalInterface
    interface SfuMetaReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.SFU_META_DATA;
        }
    }

    @FunctionalInterface
    interface SfuTransportReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.SFU_TRANSPORT;
        }
    }

    @FunctionalInterface
    interface SfuInboundRtpPadReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.SFU_INBOUND_RTP_PAD;
        }
    }

    @FunctionalInterface
    interface SfuOutboundRtpPadReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.SFU_OUTBOUND_RTP_PAD;
        }
    }

    @FunctionalInterface
    interface SfuSctpStreamReport extends OutboundReport {
        @Override
        default ReportType getType() {
            return ReportType.SFU_SCTP_STREAM;
        }
    }
}

