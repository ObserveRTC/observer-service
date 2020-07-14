package org.observertc.webrtc.service.mediastreams;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.common.reports.InboundStreamSampleReport;
import org.observertc.webrtc.service.samples.InboundStreamMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class InboundStreamReportAggregator extends MediaStreamReportAggregator<InboundStreamMeasurement, InboundStreamSampleReport> {
	private static final Logger logger = LoggerFactory.getLogger(InboundStreamReportAggregator.class);

	public InboundStreamReportAggregator() {

	}

	@Override
	protected InboundStreamSampleReport doApply(InboundStreamMeasurement measurement, InboundStreamMeasurement lastMeasurement, InboundStreamSampleReport report) {
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
