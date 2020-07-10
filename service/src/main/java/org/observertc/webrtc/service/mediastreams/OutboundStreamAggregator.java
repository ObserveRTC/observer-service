package org.observertc.webrtc.service.mediastreams;

import org.apache.kafka.streams.kstream.Aggregator;
import org.observertc.webrtc.service.samples.MediaStreamAggregate;
import org.observertc.webrtc.service.samples.MediaStreamAggregateRecord;
import org.observertc.webrtc.service.samples.MediaStreamKey;
import org.observertc.webrtc.service.samples.OutboundStreamMeasurement;

public class OutboundStreamAggregator implements Aggregator<MediaStreamKey, OutboundStreamMeasurement, MediaStreamAggregate> {

	@Override
	public MediaStreamAggregate apply(MediaStreamKey key, OutboundStreamMeasurement measurement, MediaStreamAggregate result) {
		this.updateBytesSent(measurement.bytesSent, result.bytesSent);
		this.updatePacketsSent(measurement.packetsSent, result.packetsSent);
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

	private void updatePacketsSent(Integer packetsSent, MediaStreamAggregateRecord mediaStreamAggregateRecord) {
		if (packetsSent == null) {
			mediaStreamAggregateRecord.empty += 1;
			return;
		}
		if (mediaStreamAggregateRecord.last == null) {
			mediaStreamAggregateRecord.last = packetsSent;
			return;
		}
		Integer dPacketsSent = packetsSent - mediaStreamAggregateRecord.last;
		this.updateSampleDescription(dPacketsSent, mediaStreamAggregateRecord);
	}

	private void updateBytesSent(Integer bytesSent, MediaStreamAggregateRecord mediaStreamAggregateRecord) {
		if (bytesSent == null) {
			mediaStreamAggregateRecord.empty += 1;
			return;
		}
		if (mediaStreamAggregateRecord.last == null) {
			mediaStreamAggregateRecord.last = bytesSent;
			return;
		}
		Integer dPacketsSent = bytesSent - mediaStreamAggregateRecord.last;
		this.updateSampleDescription(dPacketsSent, mediaStreamAggregateRecord);
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
