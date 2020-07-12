package org.observertc.webrtc.service.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.observertc.webrtc.service.dto.InboundStreamMeasurementDTO;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.samples.InboundStreamMeasurement;
import org.observertc.webrtc.service.samples.MediaStreamKey;
import org.observertc.webrtc.service.samples.ObserveRTCMediaStreamStatsSample;

@Prototype
public class InboundStreamAggregator implements Aggregator<MediaStreamKey, ObserveRTCMediaStreamStatsSample, InboundStreamMeasurementDTO> {

	private final HashMap<MediaStreamKey, List<InboundStreamMeasurement>> measurements;

	public InboundStreamAggregator() {
		this.measurements = new HashMap<>();
	}

	public void init(ProcessorContext context, MediaStreamEvaluatorConfiguration configuration) {

	}

	public void add(MediaStreamKey key, InboundStreamMeasurement measurement) {
		List<InboundStreamMeasurement> measurements = this.measurements.getOrDefault(key, new LinkedList<>());
		measurements.add(measurement);
		this.measurements.put(key, measurements);
	}


	@Override
	public InboundStreamMeasurementDTO apply(MediaStreamKey key, ObserveRTCMediaStreamStatsSample value, InboundStreamMeasurementDTO aggregate) {
		InboundStreamMeasurement measurement = this.makeMeasurement(value);
		if (aggregate.firstSample == null) {
			// TODO: query from a global storage for the last value
			aggregate.firstSample = measurement.sampled;
		}
		aggregate.lastSample = measurement.sampled;
		++aggregate.samples_count;
		this.updatePacketsReceived(aggregate, measurement.packetsReceived);
		this.updatePacketsLost(aggregate, measurement.packetsLost);
		this.updateBytesReceived(aggregate, measurement.bytesReceived);
		this.updateQpSum(aggregate, measurement.qpSum);
		return aggregate;
	}

	private InboundStreamMeasurement makeMeasurement(ObserveRTCMediaStreamStatsSample sample) {
		RTCStats rtcStats = sample.rtcStats;
		InboundStreamMeasurement result = new InboundStreamMeasurement();
		result.bytesReceived = this.extractBytesReceived(rtcStats);
		result.packetsReceived = this.extractPacketsReceived(rtcStats);
		result.packetsLost = this.extractPacketsLost(rtcStats);
		result.sampled = sample.sampled;
		return result;
	}


	private void updateQpSum(InboundStreamMeasurementDTO result, Integer qpSum) {
		if (qpSum == null) {
			return;
		}
		++result.qpSum_count;
		if (result.qpSum_last == null) {
			result.qpSum_last = qpSum;
			return;
		}
		Integer dQpSum = qpSum - result.qpSum_last;
		result.qpSum_sum += dQpSum;
		if (result.qpSum_min == null || dQpSum < result.qpSum_min) {
			result.qpSum_min = dQpSum;
		}
		if (result.qpSum_max == null || result.qpSum_max < dQpSum) {
			result.qpSum_max = dQpSum;
		}
	}


	private void updatePacketsLost(InboundStreamMeasurementDTO result, Integer packetsLost) {
		if (packetsLost == null) {
			return;
		}
		++result.packetsLost_count;
		if (result.packetsLost_last == null) {
			result.bytesReceived_last = packetsLost;
			return;
		}
		Integer dPacketsLost = packetsLost - result.packetsLost_last;
		result.packetsLost_sum += dPacketsLost;
		if (result.packetsLost_min == null || dPacketsLost < result.packetsLost_min) {
			result.packetsLost_min = dPacketsLost;
		}
		if (result.packetsLost_max == null || result.packetsLost_max < dPacketsLost) {
			result.packetsLost_max = dPacketsLost;
		}
	}

	private void updatePacketsReceived(InboundStreamMeasurementDTO result, Integer packetsReceived) {
		if (packetsReceived == null) {
			return;
		}
		++result.packetsReceived_count;
		if (result.packetsReceived_last == null) {
			result.packetsReceived_last = packetsReceived;
			return;
		}
		Integer dPacketsReceived = packetsReceived - result.packetsReceived_last;
		result.packetsReceived_sum += dPacketsReceived;
		if (result.packetsReceived_min == null || dPacketsReceived < result.packetsReceived_min) {
			result.packetsReceived_min = dPacketsReceived;
		}
		if (result.packetsReceived_max == null || result.packetsReceived_max < dPacketsReceived) {
			result.packetsReceived_max = dPacketsReceived;
		}
	}

	private void updateBytesReceived(InboundStreamMeasurementDTO result, Integer bytesReceived) {
		if (bytesReceived == null) {
			return;
		}
		++result.bytesReceived_count;
		if (result.bytesReceived_last == null) {
			result.bytesReceived_last = bytesReceived;
			return;
		}
		Integer dBytesReceived = bytesReceived - result.bytesReceived_last;
		result.bytesReceived_sum += dBytesReceived;
		if (result.bytesReceived_min == null || dBytesReceived < result.bytesReceived_min) {
			result.bytesReceived_min = dBytesReceived;
		}
		if (result.bytesReceived_max == null || result.bytesReceived_max < dBytesReceived) {
			result.bytesReceived_max = dBytesReceived;
		}
	}


	private Integer extractBytesReceived(RTCStats sample) {
		Double bytesReceived = sample.getBytesReceived();
		if (bytesReceived == null) {
			return null;
		}
		return bytesReceived.intValue();
	}

	private Integer extractPacketsReceived(RTCStats sample) {
		Double packetsReceived = sample.getPacketsReceived();
		if (packetsReceived == null) {
			return null;
		}
		return packetsReceived.intValue();
	}

	private Integer extractPacketsLost(RTCStats sample) {
		Double packetsLost = sample.getPacketsLost();
		if (packetsLost == null) {
			return null;
		}
		return packetsLost.intValue();
	}

}
