package org.observertc.webrtc.observer.common;

import org.observertc.webrtc.observer.samples.ClientSamples;
import org.observertc.webrtc.observer.samples.ObservedSample;
import org.observertc.webrtc.schemas.reports.CallEventReport;

public class ReportBuilderSetter {
    enum CallEventTypes {
        CALL_STARTED,
    }

    public static CallEventReport.Builder withCallStarted(CallEventReport.Builder builder) {
        String eventName = CallEventTypes.CALL_STARTED.name();
        return builder.setName(eventName);
    }

    public static CallEventReport.Builder withObservedSample(CallEventReport.Builder builder, ObservedSample observedSample) {
        return builder
                .setMediaUnitId(observedSample.getMediaUnitId())
                .setClientId(observedSample.getClientId().toString())
                .setSampleTimestamp(observedSample.getTimestamp())
                .setRoomId(observedSample.getRoomId())
                .setServiceId(observedSample.getServiceId())
                ;
    }

    public static CallEventReport.Builder CallEventReportBuilderWithClientSample(CallEventReport.Builder builder, ClientSamples clientSamples) {
        return withObservedSample(builder, clientSamples);
    }


}
