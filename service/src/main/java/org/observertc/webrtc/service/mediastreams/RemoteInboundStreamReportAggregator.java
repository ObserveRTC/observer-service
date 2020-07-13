package org.observertc.webrtc.service.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.streams.kstream.Aggregator;
import org.javatuples.Pair;
import org.observertc.webrtc.common.reports.RemoteInboundStreamSampleReport;
import org.observertc.webrtc.service.samples.InboundStreamMeasurement;
import org.observertc.webrtc.service.samples.RemoteInboundStreamMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class RemoteInboundStreamReportAggregator implements Aggregator<UUID, RemoteInboundStreamMeasurement,
		RemoteInboundStreamSampleReport> {
	private static final Logger logger = LoggerFactory.getLogger(RemoteInboundStreamReportAggregator.class);
	private final Map<Pair<UUID, Long>, InboundStreamMeasurement> lastMeasurements;
	private final MediaStreamSampleRecordReportUpdater recordReportUpdater;

	public RemoteInboundStreamReportAggregator() {
		lastMeasurements = new HashMap<>();
		this.recordReportUpdater = new MediaStreamSampleRecordReportUpdater();
	}

	@Override
	public RemoteInboundStreamSampleReport apply(UUID observerUUID, RemoteInboundStreamMeasurement measurement,
												 RemoteInboundStreamSampleReport report) {
		try {
			return this.doApply(observerUUID, measurement, report);
		} catch (Exception ex) {
			logger.error("Exception occured during aggregation", ex);
			return report;
		}
	}

	public RemoteInboundStreamSampleReport doApply(UUID observerUUID, RemoteInboundStreamMeasurement measurement,
												   RemoteInboundStreamSampleReport report) {
		if (report.firstSample == null) {
			report.firstSample = measurement.sampled;
		}
		report.lastSample = measurement.sampled;
		if (report.peerConnectionUUID == null) {
			report.peerConnectionUUID = measurement.peerConnectionUUID;
		}
		if (report.observerUUID == null) {
			report.observerUUID = observerUUID;
		}
		if (report.SSRC == null) {
			report.SSRC = measurement.SSRC;
		}
		++report.count;
		if (measurement.RTTInMs != null) {
			this.recordReportUpdater.update(report.RTTInMsRecord, measurement.RTTInMs);
		}

		return report;
	}

}
