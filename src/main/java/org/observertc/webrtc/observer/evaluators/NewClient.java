package org.observertc.webrtc.observer.evaluators;

import org.observertc.webrtc.observer.dto.ClientDTO;
import org.observertc.webrtc.observer.samples.ClientSamples;
import org.observertc.webrtc.observer.samples.ObservedSample;
import org.observertc.webrtc.schemas.reports.CallEventReport;

import java.time.Instant;
import java.util.UUID;

class NewClient extends ClientDTO.Builder {
    private CallEventReport.Builder callEventReportBuilder = CallEventReport.newBuilder();

    NewClient(UUID clientId) {
        Instant created = Instant.now();
        this.withClientId(clientId);
        this.callEventReportBuilder
                .setClientId(clientId.toString())
                .setTimestamp(created.toEpochMilli());
    }

    @Override
    public ClientDTO build() {
        var result = super.build();
        this.callEventReportBuilder
                .setClientId(result.clientId.toString())
                .setMediaUnitId(result.mediaUnitId)
                .setSampleTimestamp(result.connected)
                .setMessage("Client is connected to the call");
        return result;
    }

    public NewClient withObservedSample(ObservedSample observedSample) {
        this.callEventReportBuilder
                .setServiceId(observedSample.getServiceId())
                .setMediaUnitId(observedSample.getMediaUnitId())
                .setRoomId(observedSample.getRoomId())
                .setClientId(observedSample.getClientId().toString())
//                .setTimeZoneId(observedSample.getTimeZoneId())
                ;
        this.withMediaUnitId(observedSample.getMediaUnitId());
        return this;
    }
    
    public NewClient withMarker(String value) {
        this.callEventReportBuilder.setMarker(value);
        return this;
    }

    public NewClient withUserId(String value) {
        this.callEventReportBuilder.setUserId(value);
        return this;
    }

    public NewClient withSampleSeq(Integer value) {
        this.callEventReportBuilder.setSampleSeq(value);
        return this;
    }

    public NewClient withClientSamples(ClientSamples clientSamples) {

        return null;
    }
}