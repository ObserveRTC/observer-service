package org.observertc.webrtc.service.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.streams.kstream.Aggregator;
import org.javatuples.Pair;
import org.observertc.webrtc.common.reports.InboundStreamSampleReport;
import org.observertc.webrtc.service.samples.InboundStreamMeasurement;
import org.observertc.webrtc.service.samples.MediaStreamKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class InboundStreamReportAggregator implements Aggregator<UUID, InboundStreamMeasurement, InboundStreamSampleReport> {
	private static final Logger logger = LoggerFactory.getLogger(InboundStreamReportAggregator.class);
	private final Map<Pair<UUID, Long>, InboundStreamMeasurement> lastMeasurements;
	private final MediaStreamSampleRecordReportUpdater recordReportUpdater;

	public InboundStreamReportAggregator() {
		lastMeasurements = new HashMap<>();
		this.recordReportUpdater = new MediaStreamSampleRecordReportUpdater();
	}

	@Override
	public InboundStreamSampleReport apply(UUID observerUUID, InboundStreamMeasurement measurement,
										   InboundStreamSampleReport report) {
		try {
			return this.doApply(observerUUID, measurement, report);
		} catch (Exception ex) {
			logger.error("Exception occured during aggregation", ex);
			return report;
		}
	}

	public InboundStreamSampleReport doApply(UUID observerUUID, InboundStreamMeasurement measurement,
											 InboundStreamSampleReport report) {
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
		InboundStreamMeasurement lastMeasurement = this.lastMeasurements.get(mediaStreamKey);
		this.lastMeasurements.put(mediaStreamKey, measurement);

		if (lastMeasurement == null) {
			// update all metrics does not requires a derivative calculations
			return report;
		}
		if (lastMeasurement.packetsReceived != null && measurement.packetsReceived != null) {
			Integer dPacketsReceived = measurement.packetsReceived - lastMeasurement.packetsReceived;
			this.recordReportUpdater.update(report.packetsReceivedRecord, dPacketsReceived);
		}

		if (lastMeasurement.packetsLost != null && measurement.packetsLost != null) {
			Integer dPacketsLost = measurement.packetsLost - lastMeasurement.packetsLost;
			this.recordReportUpdater.update(report.packetsLostRecord, dPacketsLost);
		}

		if (lastMeasurement.bytesReceived != null && measurement.bytesReceived != null) {
			Integer dBytesReceived = measurement.bytesReceived - lastMeasurement.bytesReceived;
			this.recordReportUpdater.update(report.bytesReceivedRecord, dBytesReceived);
		}
		return report;
	}

}
