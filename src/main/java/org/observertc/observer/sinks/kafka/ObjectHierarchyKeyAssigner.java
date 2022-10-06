package org.observertc.observer.sinks.kafka;

import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.common.Utils;
import org.observertc.observer.reports.Report;
import org.observertc.observer.reports.ReportTypeVisitor;
import org.observertc.schemas.reports.*;

import java.util.UUID;

class ObjectHierarchyKeyAssigner implements ReportTypeVisitor<Report, UUID> {

    private final UUID defaultValue = UUID.randomUUID();

    @Override
    public UUID visitObserverEventReport(Report obj) {
        var payload = (ObserverEventReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.clientId,
                payload.peerConnectionId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitCallEventReport(Report obj) {
        var payload = (CallEventReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.clientId,
                payload.peerConnectionId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitCallMetaDataReport(Report obj) {
        var payload = (CallMetaReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.clientId,
                payload.peerConnectionId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitClientExtensionDataReport(Report obj) {
        var payload = (ClientExtensionReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.clientId,
                payload.peerConnectionId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitPeerConnectionTransportReport(Report obj) {
        var payload = (PeerConnectionTransportReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.clientId,
                payload.peerConnectionId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitIceCandidatePairReport(Report obj) {
        var payload = (IceCandidatePairReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.clientId,
                payload.peerConnectionId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitClientDataChannelReport(Report obj) {
        var payload = (ClientDataChannelReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.clientId,
                payload.peerConnectionId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitInboundAudioTrackReport(Report obj) {
        var payload = (InboundAudioTrackReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.clientId,
                payload.peerConnectionId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitInboundVideoTrackReport(Report obj) {
        var payload = (InboundVideoTrackReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.clientId,
                payload.peerConnectionId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitOutboundAudioTrackReport(Report obj) {
        var payload = (OutboundAudioTrackReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.clientId,
                payload.peerConnectionId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitOutboundVideoTrackReport(Report obj) {
        var payload = (OutboundVideoTrackReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.clientId,
                payload.peerConnectionId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitSfuEventReport(Report obj) {
        var payload = (SfuEventReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.sfuId,
                payload.transportId,
                payload.rtpPadId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitSfuMetaReport(Report obj) {
        var payload = (SfuMetaReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.sfuId,
                payload.transportId,
                payload.rtpPadId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitSfuExtensionReport(Report obj) {
        var payload = (SfuExtensionReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.sfuId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitSfuTransportReport(Report obj) {
        var payload = (SFUTransportReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.sfuId,
                payload.transportId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitSfuInboundRtpPadReport(Report obj) {
        var payload = (SfuInboundRtpPadReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.sfuId,
                payload.transportId,
                payload.rtpPadId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitSfuOutboundRtpPadReport(Report obj) {
        var payload = (SfuOutboundRtpPadReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.sfuId,
                payload.transportId,
                payload.rtpPadId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }

    @Override
    public UUID visitSctpStreamReport(Report obj) {
        var payload = (SfuSctpStreamReport) obj.payload;
        var value = Utils.<String>firstNotNull(
                payload.callId,
                payload.sfuId,
                payload.transportId,
                payload.streamId
        );
        return UUIDAdapter.tryParseOrDefault(value, defaultValue);
    }
}
