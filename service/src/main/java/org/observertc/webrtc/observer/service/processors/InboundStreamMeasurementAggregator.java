package org.observertc.webrtc.observer.service.processors;

import org.observertc.webrtc.observer.service.samples.InboundStreamMeasurement;
import org.observertc.webrtc.observer.service.samples.MediaStreamKey;
import org.observertc.webrtc.observer.service.samples.MediaStreamSample;
import org.observertc.webrtc.observer.service.samples.SampleDescription;
import org.apache.kafka.streams.kstream.Aggregator;

public class InboundStreamMeasurementAggregator implements Aggregator<MediaStreamKey, InboundStreamMeasurement, MediaStreamSample> {

	@Override
	public MediaStreamSample apply(MediaStreamKey key, InboundStreamMeasurement measurement, MediaStreamSample result) {
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

	private void updateRTT(Integer RTTInMs, SampleDescription sampleDescription) {
		if (RTTInMs == null) {
			sampleDescription.empty += 1;
			return;
		}
		this.updateSampleDescription(RTTInMs, sampleDescription);
	}

	private void updatePacketsLost(Integer packetsLost, SampleDescription sampleDescription) {
		if (packetsLost == null) {
			sampleDescription.empty += 1;
			return;
		}
		if (sampleDescription.last == null) {
			sampleDescription.last = packetsLost;
			return;
		}
		Integer dPacketsLost = packetsLost - sampleDescription.last;
		this.updateSampleDescription(dPacketsLost, sampleDescription);

	}

	private void updatePacketsReceived(Integer packetsReceived, SampleDescription sampleDescription) {
		if (packetsReceived == null) {
			sampleDescription.empty += 1;
			return;
		}
		if (sampleDescription.last == null) {
			sampleDescription.last = packetsReceived;
			return;
		}
		Integer dPacketsReceived = packetsReceived - sampleDescription.last;
		this.updateSampleDescription(dPacketsReceived, sampleDescription);

	}

	private void updateBytesReceived(Integer bytesReceived, SampleDescription sampleDescription) {
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
