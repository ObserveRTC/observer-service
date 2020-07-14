package org.observertc.webrtc.service.mediastreams;

import io.micronaut.context.annotation.Prototype;
import org.observertc.webrtc.common.reports.RemoteInboundStreamSampleReport;
import org.observertc.webrtc.service.samples.RemoteInboundStreamMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class RemoteInboundStreamReportAggregator extends MediaStreamReportAggregator<RemoteInboundStreamMeasurement,
		RemoteInboundStreamSampleReport> {
	private static final Logger logger = LoggerFactory.getLogger(RemoteInboundStreamReportAggregator.class);

	public RemoteInboundStreamReportAggregator() {

	}

	@Override
	protected RemoteInboundStreamSampleReport doApply(RemoteInboundStreamMeasurement measurement, RemoteInboundStreamMeasurement lastMeasurement, RemoteInboundStreamSampleReport report) {
		this.updateValue(report.RTTInMsRecord, measurement.RTTInMs);
		return report;
	}


}
