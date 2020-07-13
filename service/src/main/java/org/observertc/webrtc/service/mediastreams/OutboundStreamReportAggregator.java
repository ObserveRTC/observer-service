package org.observertc.webrtc.service.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.streams.kstream.Aggregator;
import org.javatuples.Pair;
import org.observertc.webrtc.common.reports.OutboundStreamSampleReport;
import org.observertc.webrtc.service.samples.OutboundStreamMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class OutboundStreamReportAggregator implements Aggregator<UUID, OutboundStreamMeasurement, OutboundStreamSampleReport> {
	private static final Logger logger = LoggerFactory.getLogger(OutboundStreamReportAggregator.class);
	private final Map<Pair<UUID, Long>, OutboundStreamMeasurement> lastMeasurements;
	private final MediaStreamSampleRecordReportUpdater recordReportUpdater;

	public OutboundStreamReportAggregator() {
		this.lastMeasurements = new HashMap<>();
		this.recordReportUpdater = new MediaStreamSampleRecordReportUpdater();
	}

	@Override
	public OutboundStreamSampleReport apply(UUID observerUUID, OutboundStreamMeasurement measurement,
											OutboundStreamSampleReport report) {
		try {
			return this.doApply(observerUUID, measurement, report);
		} catch (Exception ex) {
			logger.error("Exception occured during aggregation", ex);
			return report;
		}
	}

	public OutboundStreamSampleReport doApply(UUID observerUUID, OutboundStreamMeasurement measurement,
											  OutboundStreamSampleReport report) {
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
		Pair<UUID, Long> mediaStreamKey = Pair.with(report.peerConnectionUUID, report.SSRC);
		OutboundStreamMeasurement lastMeasurement = this.lastMeasurements.get(mediaStreamKey);
		this.lastMeasurements.put(mediaStreamKey, measurement);

		if (lastMeasurement == null) {
			// update all metrics does not requires a derivative calculations
			return report;
		}
		if (lastMeasurement.packetsSent != null && measurement.packetsSent != null) {
			Integer dPacketsSent = measurement.packetsSent - lastMeasurement.packetsSent;
			this.recordReportUpdater.update(report.packetsSentRecord, dPacketsSent);
		}

		if (lastMeasurement.bytesSent != null && measurement.bytesSent != null) {
			Integer dBytesSent = measurement.bytesSent - lastMeasurement.bytesSent;
			this.recordReportUpdater.update(report.bytesSentRecord, dBytesSent);
		}
		return report;
	}
}
