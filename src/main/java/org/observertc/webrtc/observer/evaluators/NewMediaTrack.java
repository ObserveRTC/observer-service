package org.observertc.webrtc.observer.evaluators;

import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.dto.MediaTrackDTO;
import org.observertc.webrtc.schemas.reports.CallEventReport;

import java.util.UUID;

class NewMediaTrack extends MediaTrackDTO.Builder {
    private CallEventReport.Builder callEventReportBuilder = CallEventReport.newBuilder();

    NewMediaTrack(UUID clientId) {
        this.withClientId(clientId);
        this.callEventReportBuilder.setClientId(clientId.toString());
    }

    @Override
    public ClientDTO build() {
        var result = super.build();
        this.callEventReportBuilder
                .setTimestamp(result.connected)
                .setClientId(result.clientId.toString());
        return result;
    }
    
    public NewMediaTrack setServiceId(String value) {
        this.callEventReportBuilder.setServiceId(value);
        return this;
    }

    public NewMediaTrack setServiceName(String value) {
        this.callEventReportBuilder.setServiceName(value);
        return this;
    }

    public NewMediaTrack setMediaUnitId(String value) {
        this.callEventReportBuilder.setMediaUnitId(value);
        return this;
    }

    public NewMediaTrack setMarker(String value) {
        this.callEventReportBuilder.setMarker(value);
        return this;
    }

    public NewMediaTrack setTimestamp(long value) {
        this.callEventReportBuilder.setTimestamp(value);
        return this;
    }

    public NewMediaTrack setCallId(String value) {
        this.callEventReportBuilder.setCallId(value);
        return this;
    }

    public NewMediaTrack setRoomId(String value) {
        this.callEventReportBuilder.setRoomId(value);
        return this;
    }

    public NewMediaTrack setClientId(String value) {
        this.callEventReportBuilder.setClientId(value);
        return this;
    }

    public NewMediaTrack setUserId(String value) {
        this.callEventReportBuilder.setUserId(value);
        return this;
    }

    public NewMediaTrack setPeerConnectionId(String value) {
        this.callEventReportBuilder.setPeerConnectionId(value);
        return this;
    }

    public NewMediaTrack setSampleTimestamp(Long value) {
        this.callEventReportBuilder.setSampleTimestamp(value);
        return this;
    }

    public NewMediaTrack setSampleSeq(Integer value) {
        this.callEventReportBuilder.setSampleSeq(value);
        return this;
    }

    public NewMediaTrack setMessage(String value) {
        this.callEventReportBuilder.setMessage(value);
        return this;
    }

    public NewMediaTrack setValue(String value) {
        this.callEventReportBuilder.setValue(value);
        return this;
    }


    public NewMediaTrack setAttachments(String value) {
        this.callEventReportBuilder.setAttachments(value);
        return this;
    }

}