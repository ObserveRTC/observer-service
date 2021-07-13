package org.observertc.webrtc.observer.common;

import org.observertc.webrtc.schemas.reports.ReportType;

public interface OutboundReport {
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
            return ReportType.PEER_CONNECTION_TRANPORT;
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
}

