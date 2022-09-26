package org.observertc.observer.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.observertc.schemas.reports.*;

import java.util.Objects;

public class Report {

    public static Builder builder() {
        return new Builder();
    }

    public static Report fromObserverEventReport(ObserverEventReport report) {

        return builder()
                .setType(ReportType.OBSERVER_EVENT)
                .setSchemaVersion(ObserverEventReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromCallEventReport(CallEventReport report) {
        return builder()
                .setType(ReportType.CALL_EVENT)
                .setSchemaVersion(CallEventReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromCallMetaReport(CallMetaReport report) {
        return builder()
                .setType(ReportType.CALL_META_DATA)
                .setSchemaVersion(CallEventReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromClientExtensionReport(ClientExtensionReport report) {
        return builder()
                .setType(ReportType.CLIENT_EXTENSION_DATA)
                .setSchemaVersion(ClientExtensionReport.VERSION)
                .setPayload(report)
                .build();
    }

    @Deprecated
    public static Report fromClientTransportReport(ClientTransportReport report) {
        return builder()
                .setType(ReportType.PEER_CONNECTION_TRANSPORT)
                .setSchemaVersion(ClientTransportReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromPeerConnectionTransportReport(PeerConnectionTransportReport report) {
        return builder()
                .setType(ReportType.PEER_CONNECTION_TRANSPORT)
                .setSchemaVersion(ClientTransportReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromIceCandidatePairReport(IceCandidatePairReport report) {
        return builder()
                .setType(ReportType.ICE_CANDIDATE_PAIR)
                .setSchemaVersion(ClientTransportReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromClientDataChannelReport(ClientDataChannelReport report) {
        return builder()
                .setType(ReportType.PEER_CONNECTION_DATA_CHANNEL)
                .setSchemaVersion(ClientDataChannelReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromInboundAudioTrackReport(InboundAudioTrackReport report) {
        return builder()
                .setType(ReportType.INBOUND_AUDIO_TRACK)
                .setSchemaVersion(InboundAudioTrackReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromInboundVideoTrackReport(InboundVideoTrackReport report) {
        return builder()
                .setType(ReportType.INBOUND_VIDEO_TRACK)
                .setSchemaVersion(InboundVideoTrackReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromOutboundAudioTrackReport(OutboundAudioTrackReport report) {
        return builder()
                .setType(ReportType.OUTBOUND_AUDIO_TRACK)
                .setSchemaVersion(OutboundAudioTrackReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromOutboundVideoTrackReport(OutboundVideoTrackReport report) {
        return builder()
                .setType(ReportType.OUTBOUND_VIDEO_TRACK)
                .setSchemaVersion(OutboundVideoTrackReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromSfuEventReport(SfuEventReport report) {
        return builder()
                .setType(ReportType.SFU_EVENT)
                .setSchemaVersion(SfuEventReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromSfuMetaReport(SfuMetaReport report) {
        return builder()
                .setType(ReportType.SFU_META_DATA)
                .setSchemaVersion(SfuMetaReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromSfuExtensionReport(SfuExtensionReport report) {
        return builder()
                .setType(ReportType.SFU_EXTENSION_DATA)
                .setSchemaVersion(SfuMetaReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromSfuTransportReport(SFUTransportReport report) {
        return builder()
                .setType(ReportType.SFU_TRANSPORT)
                .setSchemaVersion(SFUTransportReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromSfuInboundRtpPadReport(SfuInboundRtpPadReport report) {
        return builder()
                .setType(ReportType.SFU_INBOUND_RTP_PAD)
                .setSchemaVersion(SfuInboundRtpPadReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromSfuOutboundRtpPadReport(SfuOutboundRtpPadReport report) {
        return builder()
                .setType(ReportType.SFU_OUTBOUND_RTP_PAD)
                .setSchemaVersion(SfuOutboundRtpPadReport.VERSION)
                .setPayload(report)
                .build();
    }

    public static Report fromSfuSctpStreamReport(SfuSctpStreamReport report) {
        return builder()
                .setType(ReportType.SFU_SCTP_STREAM)
                .setSchemaVersion(SfuSctpStreamReport.VERSION)
                .setPayload(report)
                .build();
    }


    @JsonProperty("type")
    public ReportType type;

    @JsonProperty("schemaVersion")
    public String schemaVersion;

    @JsonProperty("payload")
    public Object payload;

    public static class Builder {
        private Report result = new Report();
        public Builder setType(ReportType value) {
            this.result.type = value;
            return this;
        }

        public Builder setSchemaVersion(String value) {
            this.result.schemaVersion = value;
            return this;
        }

        public Builder setPayload(Object value) {
            this.result.payload = value;
            return this;
        }

        public Report build() {
            Objects.requireNonNull(this.result.type);
            Objects.requireNonNull(this.result.payload);
            return this.result;
        }
    }
}
