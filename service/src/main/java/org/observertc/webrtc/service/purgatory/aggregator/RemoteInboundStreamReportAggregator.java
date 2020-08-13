package org.observertc.webrtc.service.purgatory.aggregator;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.common.reports.RemoteInboundStreamReport;
import org.observertc.webrtc.service.purgatory.RemoteInboundStreamMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class RemoteInboundStreamReportAggregator extends MediaStreamReportAggregator<RemoteInboundStreamMeasurement,
		RemoteInboundStreamReport> {
	private static final Logger logger = LoggerFactory.getLogger(RemoteInboundStreamReportAggregator.class);

	public RemoteInboundStreamReportAggregator() {

	}

	@Override
	protected RemoteInboundStreamReport doApply(RemoteInboundStreamMeasurement measurement, RemoteInboundStreamMeasurement lastMeasurement, RemoteInboundStreamReport report) {
		this.updateValue(report.RTTInMsRecord, measurement.RTTInMs);
		return report;
	}


}
