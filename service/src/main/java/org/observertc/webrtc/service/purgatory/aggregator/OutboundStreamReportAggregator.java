package org.observertc.webrtc.service.purgatory.aggregator;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.common.reports.OutboundStreamReport;
import org.observertc.webrtc.service.purgatory.OutboundStreamMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class OutboundStreamReportAggregator extends MediaStreamReportAggregator<OutboundStreamMeasurement, OutboundStreamReport> {
	private static final Logger logger = LoggerFactory.getLogger(OutboundStreamReportAggregator.class);

	public OutboundStreamReportAggregator() {

	}

	@Override
	protected OutboundStreamReport doApply(OutboundStreamMeasurement measurement, OutboundStreamMeasurement lastMeasurement, OutboundStreamReport report) {
		if (lastMeasurement == null) {
			// update all metrics does not requires a derivative calculations
			return report;
		}
		this.updateDerivativeValue(report.packetsSentRecord, measurement.packetsSent, lastMeasurement.packetsSent);
		this.updateDerivativeValue(report.bytesSentRecord, measurement.bytesSent, lastMeasurement.bytesSent);
		return report;
	}

}
