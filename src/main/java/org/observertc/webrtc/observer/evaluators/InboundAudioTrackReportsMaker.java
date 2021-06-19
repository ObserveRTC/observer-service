package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.functions.Function;
import org.observertc.webrtc.observer.dto.OutboundReport;
import org.observertc.webrtc.observer.samples.ObservedSample;

public class InboundAudioTrackReportsMaker implements Function<OutboundReport, ObservedSample> {

    @Override
    public ObservedSample apply(OutboundReport outboundReport) throws Throwable {
        return null;
    }
}
