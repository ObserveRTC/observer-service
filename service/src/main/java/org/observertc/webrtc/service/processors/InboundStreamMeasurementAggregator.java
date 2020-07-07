package org.observertc.webrtc.service.processors;

import org.apache.kafka.streams.kstream.Aggregator;
import org.observertc.webrtc.service.samples.InboundStreamMeasurement;
import org.observertc.webrtc.service.samples.MediaStreamAggregate;
import org.observertc.webrtc.service.samples.MediaStreamAggregateRecord;
import org.observertc.webrtc.service.samples.MediaStreamKey;

public class InboundStreamMeasurementAggregator implements Aggregator<MediaStreamKey, InboundStreamMeasurement, MediaStreamAggregate> {

	@Override
	public MediaStreamAggregate apply(MediaStreamKey key, InboundStreamMeasurement measurement, MediaStreamAggregate result) {
		this.updateBytesReceived(measurement.bytesReceived, result.bytesReceived);
		this.updatePacketsLost(measurement.packetsLost, result.packetsLost);
		this.updatePacketsReceived(measurement.packetsReceived, result.packetsReceived);
		this.updateRTT(measurement.RTTInMs, result.RTTInMs);
		if (result.first == null) {
			result.first = measurement.sampled;
		}
		result.last = measurement.sampled;
		return result;
	}

	private void updateRTT(Integer RTTInMs, MediaStreamAggregateRecord mediaStreamAggregateRecord) {
		if (RTTInMs == null) {
			mediaStreamAggregateRecord.empty += 1;
			return;
		}
		this.updateSampleDescription(RTTInMs, mediaStreamAggregateRecord);
	}

	private void updatePacketsLost(Integer packetsLost, MediaStreamAggregateRecord mediaStreamAggregateRecord) {
		if (packetsLost == null) {
			mediaStreamAggregateRecord.empty += 1;
			return;
		}
		if (mediaStreamAggregateRecord.last == null) {
			mediaStreamAggregateRecord.last = packetsLost;
			return;
		}
		Integer dPacketsLost = packetsLost - mediaStreamAggregateRecord.last;
		this.updateSampleDescription(dPacketsLost, mediaStreamAggregateRecord);

	}

	private void updatePacketsReceived(Integer packetsReceived, MediaStreamAggregateRecord mediaStreamAggregateRecord) {
		if (packetsReceived == null) {
			mediaStreamAggregateRecord.empty += 1;
			return;
		}
		if (mediaStreamAggregateRecord.last == null) {
			mediaStreamAggregateRecord.last = packetsReceived;
			return;
		}
		Integer dPacketsReceived = packetsReceived - mediaStreamAggregateRecord.last;
		this.updateSampleDescription(dPacketsReceived, mediaStreamAggregateRecord);

	}

	private void updateBytesReceived(Integer bytesReceived, MediaStreamAggregateRecord sampleDescription) {
		if (bytesReceived == null) {
			sampleDescription.empty += 1;
			return;
		}
		if (sampleDescription.last == null) {
			sampleDescription.last = bytesReceived;
			return;
		}
		Integer dPacketsReceived = bytesReceived - sampleDescription.last;
		this.updateSampleDescription(dPacketsReceived, sampleDescription);

	}

	private void updateSampleDescription(Integer value, MediaStreamAggregateRecord mediaStreamAggregateRecord) {
		mediaStreamAggregateRecord.presented += 1;
		if (mediaStreamAggregateRecord.min == null || value < mediaStreamAggregateRecord.min) {
			mediaStreamAggregateRecord.min = value;
		}

		if (mediaStreamAggregateRecord.max == null || mediaStreamAggregateRecord.max < value) {
			mediaStreamAggregateRecord.max = value;
		}
		mediaStreamAggregateRecord.sum += value;
		mediaStreamAggregateRecord.last = value;
	}
}
