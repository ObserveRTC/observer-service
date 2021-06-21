package org.observertc.webrtc.observer.evaluators;

import org.observertc.webrtc.schemas.reports.CallEventReport;

import javax.inject.Singleton;

@Singleton
public class CallEventsBuildersFactory {

    enum Types {
        CALL_STARTED,
    }

    public CallEventReport.Builder makeStartedCallEventReportBuilder() {
        String eventName = Types.CALL_STARTED.name();
        return CallEventReport.newBuilder()
                .setName(eventName);
    }

}
