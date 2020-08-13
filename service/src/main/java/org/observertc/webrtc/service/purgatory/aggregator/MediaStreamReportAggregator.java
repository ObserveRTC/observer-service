package org.observertc.webrtc.service.purgatory.aggregator;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.streams.kstream.Aggregator;
import org.observertc.webrtc.common.reports.MediaStreamRecordReport;
import org.observertc.webrtc.common.reports.MediaStreamReport;
import org.observertc.webrtc.service.purgatory.MediaStreamMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MediaStreamReportAggregator<TMeasurement extends MediaStreamMeasurement, TReport extends MediaStreamReport> implements Aggregator<MediaStreamKey,
		TMeasurement,
		TReport> {
	private static final Logger logger = LoggerFactory.getLogger(MediaStreamReportAggregator.class);
	private final Map<MediaStreamKey, TMeasurement> lastMeasurements;
	private final MediaStreamSampleRecordReportUpdater recordReportUpdater;

	public MediaStreamReportAggregator() {
		this.lastMeasurements = new HashMap<>();
		this.recordReportUpdater = new MediaStreamSampleRecordReportUpdater();
	}

	@Override
	public TReport apply(MediaStreamKey mediaStreamKey, TMeasurement measurement,
						 TReport report) {
		try {
			if (report.firstSample == null) {
				report.firstSample = measurement.sampled;
			}
			report.lastSample = measurement.sampled;
			if (report.peerConnectionUUID == null) {
				report.peerConnectionUUID = measurement.peerConnectionUUID;
			}
			if (report.observerUUID == null) {
				report.observerUUID = mediaStreamKey.observerUUID;
			}
			if (report.SSRC == null) {
				report.SSRC = measurement.SSRC;
			}
			++report.count;
			TMeasurement lastMeasurement = this.lastMeasurements.get(mediaStreamKey);

			this.doApply(measurement, lastMeasurement, report);

			this.lastMeasurements.put(mediaStreamKey, measurement);
			return report;
		} catch (Exception ex) {
			logger.error("Exception occured during aggregation", ex);
			return report;
		}
	}

	protected abstract TReport doApply(TMeasurement measurement, TMeasurement lastMeasurement, TReport report);

	protected void updateDerivativeValue(MediaStreamRecordReport record, Integer actualValue, Integer lastValue) {
		if (actualValue == null || lastValue == null) {
			return;
		}
		Integer dValue = actualValue;
		if (0 < lastValue) {
			dValue -= lastValue;
		}
		this.recordReportUpdater.update(record, dValue);
	}

	protected void updateValue(MediaStreamRecordReport record, Integer value) {
		if (value == null) {
			return;
		}
		this.recordReportUpdater.update(record, value);
	}
}
