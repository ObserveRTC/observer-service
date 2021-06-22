package org.observertc.webrtc.observer.evaluators;

import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
import org.observertc.webrtc.schemas.reports.CallEventReport;

import java.util.UUID;

class NewPeerConnection extends PeerConnectionDTO.Builder {
    private CallEventReport.Builder callEventReportBuilder = CallEventReport.newBuilder();

    NewPeerConnection(UUID peerConnectionId) {
        this.withPeerConnectionId(peerConnectionId);
        this.callEventReportBuilder.setPeerConnectionId(peerConnectionId.toString());
    }

    @Override
    public PeerConnectionDTO build() {
        var result = super.build();
        this.callEventReportBuilder
                .setTimestamp(result.added)
                .setClientId(result.clientId.toString());
        return result;
    }

    public NewPeerConnection setServiceId(String value) {
        this.callEventReportBuilder.setServiceId(value);
        return this;
    }

    public NewPeerConnection setServiceName(String value) {
        this.callEventReportBuilder.setServiceName(value);
        return this;
    }

    public NewPeerConnection setMediaUnitId(String value) {
        this.callEventReportBuilder.setMediaUnitId(value);
        return this;
    }

    public NewPeerConnection setMarker(String value) {
        this.callEventReportBuilder.setMarker(value);
        return this;
    }

    public NewPeerConnection setTimestamp(long value) {
        this.callEventReportBuilder.setTimestamp(value);
        return this;
    }

    public NewPeerConnection setCallId(String value) {
        this.callEventReportBuilder.setCallId(value);
        return this;
    }

    public NewPeerConnection setRoomId(String value) {
        this.callEventReportBuilder.setRoomId(value);
        return this;
    }

    public NewPeerConnection setClientId(String value) {
        this.callEventReportBuilder.setClientId(value);
        return this;
    }

    public NewPeerConnection setUserId(String value) {
        this.callEventReportBuilder.setUserId(value);
        return this;
    }

    public NewPeerConnection setPeerConnectionId(String value) {
        this.callEventReportBuilder.setPeerConnectionId(value);
        return this;
    }

    public NewPeerConnection setSampleTimestamp(Long value) {
        this.callEventReportBuilder.setSampleTimestamp(value);
        return this;
    }

    public NewPeerConnection setSampleSeq(Integer value) {
        this.callEventReportBuilder.setSampleSeq(value);
        return this;
    }

    public NewPeerConnection setMessage(String value) {
        this.callEventReportBuilder.setMessage(value);
        return this;
    }

    public NewPeerConnection setValue(String value) {
        this.callEventReportBuilder.setValue(value);
        return this;
    }


    public NewPeerConnection setAttachments(String value) {
        this.callEventReportBuilder.setAttachments(value);
        return this;
    }

}