package com.observertc.gatekeeper.webrtcstat.processors;

import com.observertc.gatekeeper.webrtcstat.samples.MediaStreamKey;
import com.observertc.gatekeeper.webrtcstat.samples.MediaStreamSample;
import com.observertc.gatekeeper.webrtcstat.samples.OutboundStreamMeasurement;
import com.observertc.gatekeeper.webrtcstat.samples.SampleDescription;
import org.apache.kafka.streams.kstream.Aggregator;

public class OutboundStreamMeasurementAggregator implements Aggregator<MediaStreamKey, OutboundStreamMeasurement, MediaStreamSample> {

	@Override
	public MediaStreamSample apply(MediaStreamKey key, OutboundStreamMeasurement measurement, MediaStreamSample result) {
		this.updateBytesSent(measurement.bytesSent, result.bytesSent);
		this.updatePacketsSent(measurement.packetsSent, result.packetsSent);
		this.updateRTT(measurement.RTTInMs, result.RTTInMs);
		if (result.first == null) {
			result.first = measurement.sampled;
		}
		result.last = measurement.sampled;
		return result;
	}

	private void updateRTT(Integer RTTInMs, SampleDescription sampleDescription) {
		if (RTTInMs == null) {
			sampleDescription.empty += 1;
			return;
		}
		this.updateSampleDescription(RTTInMs, sampleDescription);
	}

	private void updatePacketsSent(Integer packetsSent, SampleDescription sampleDescription) {
		if (packetsSent == null) {
			sampleDescription.empty += 1;
			return;
		}
		if (sampleDescription.last == null) {
			sampleDescription.last = packetsSent;
			return;
		}
		Integer dPacketsSent = packetsSent - sampleDescription.last;
		this.updateSampleDescription(dPacketsSent, sampleDescription);
	}

	private void updateBytesSent(Integer bytesSent, SampleDescription sampleDescription) {
		if (bytesSent == null) {
			sampleDescription.empty += 1;
			return;
		}
		if (sampleDescription.last == null) {
			sampleDescription.last = bytesSent;
			return;
		}
		Integer dPacketsSent = bytesSent - sampleDescription.last;
		this.updateSampleDescription(dPacketsSent, sampleDescription);
	}

	private void updateSampleDescription(Integer value, SampleDescription sampleDescription) {
		sampleDescription.presented += 1;
		if (sampleDescription.min == null || value < sampleDescription.min) {
			sampleDescription.min = value;
		}

		if (sampleDescription.max == null || sampleDescription.max < value) {
			sampleDescription.max = value;
		}
		sampleDescription.sum += value;
		sampleDescription.last = value;
	}
}
