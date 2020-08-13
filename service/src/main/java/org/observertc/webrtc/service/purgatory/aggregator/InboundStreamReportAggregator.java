package org.observertc.webrtc.service.purgatory.aggregator;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.common.reports.InboundStreamReport;
import org.observertc.webrtc.service.purgatory.InboundStreamMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class InboundStreamReportAggregator extends MediaStreamReportAggregator<InboundStreamMeasurement, InboundStreamReport> {
	private static final Logger logger = LoggerFactory.getLogger(InboundStreamReportAggregator.class);

	public InboundStreamReportAggregator() {

	}

	@Override
	protected InboundStreamReport doApply(InboundStreamMeasurement measurement, InboundStreamMeasurement lastMeasurement, InboundStreamReport report) {
		if (lastMeasurement == null) {
			// update all metrics does not requires a derivative calculations
			return report;
		}
		this.updateDerivativeValue(report.bytesReceivedRecord, measurement.bytesReceived, lastMeasurement.bytesReceived);
		this.updateDerivativeValue(report.packetsLostRecord, measurement.packetsLost, lastMeasurement.packetsLost);
		this.updateDerivativeValue(report.packetsReceivedRecord, measurement.packetsReceived, lastMeasurement.packetsReceived);
		return report;
	}
}
