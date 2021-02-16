package org.observertc.webrtc.observer.evaluators.witholders;

import org.observertc.webrtc.schemas.reports.RemoteInboundRTP;

public class ReportHolder {
    private ValueHolder<Double> jitterHolder = new ValueHolder<>((reduced, actual) -> actual < reduced - .05 || reduced + .05 < actual, (reduced, actual) -> (reduced + actual) / 2.);
    private ValueHolder<Double> RTTHolder = new ValueHolder<>((reduced, actual) -> actual < reduced - .05 || reduced + .05 < actual, (reduced, actual) -> (reduced + actual) / 2.);

        void t(RemoteInboundRTP report) {
            boolean send = false;
            send |= this.jitterHolder.apply(report.getJitter().doubleValue());
            send |= this.RTTHolder.apply(report.getRoundTripTime());
        }
}
