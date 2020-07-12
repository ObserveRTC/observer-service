package org.observertc.webrtc.service.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.util.HashMap;
import java.util.List;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.observertc.webrtc.service.dto.OutboundStreamMeasurementDTO;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.samples.MediaStreamKey;
import org.observertc.webrtc.service.samples.ObserveRTCMediaStreamStatsSample;
import org.observertc.webrtc.service.samples.OutboundStreamMeasurement;

@Prototype
public class OutboundStreamAggregator implements Aggregator<MediaStreamKey, ObserveRTCMediaStreamStatsSample,
		OutboundStreamMeasurementDTO> {

	private final HashMap<MediaStreamKey, List<OutboundStreamMeasurementDTO>> measurements;

	public OutboundStreamAggregator() {
		this.measurements = new HashMap<>();
	}

	public void init(ProcessorContext context, MediaStreamEvaluatorConfiguration configuration) {

	}

	@Override
	public OutboundStreamMeasurementDTO apply(MediaStreamKey key, ObserveRTCMediaStreamStatsSample value, OutboundStreamMeasurementDTO aggregate) {
		OutboundStreamMeasurement measurement = this.makeMeasurement(value);
		if (aggregate.firstSample == null) {
			// TODO: query from a global storage for the last value
			aggregate.firstSample = measurement.sampled;
		}
		aggregate.lastSample = measurement.sampled;
		++aggregate.samples_count;
		this.updatePacketsSent(aggregate, measurement.packetsSent);
		this.updateBytesSent(aggregate, measurement.bytesSent);
		return aggregate;
	}

	private OutboundStreamMeasurement makeMeasurement(ObserveRTCMediaStreamStatsSample sample) {
		RTCStats rtcStats = sample.rtcStats;
		OutboundStreamMeasurement outboundStreamMeasurement = new OutboundStreamMeasurement();
		outboundStreamMeasurement.bytesSent = this.extractBytesSent(rtcStats);
		outboundStreamMeasurement.packetsSent = this.extractPacketsSent(rtcStats);
		outboundStreamMeasurement.sampled = sample.sampled;
		return outboundStreamMeasurement;
	}


	private void updatePacketsSent(OutboundStreamMeasurementDTO result, Integer packetsSent) {
		if (packetsSent == null) {
			return;
		}
		++result.packetsSent_count;
		if (result.packetsSent_last == null) {
			result.packetsSent_last = packetsSent;
			return;
		}
		Integer dPacketsSent = packetsSent - result.packetsSent_last;
		result.packetsSent_sum += dPacketsSent;
		if (result.packetsSent_min == null || dPacketsSent < result.packetsSent_min) {
			result.packetsSent_min = dPacketsSent;
		}
		if (result.packetsSent_max == null || result.packetsSent_max < dPacketsSent) {
			result.packetsSent_max = dPacketsSent;
		}
	}

	private void updateBytesSent(OutboundStreamMeasurementDTO result, Integer bytesSent) {
		if (bytesSent == null) {
			return;
		}
		++result.bytesSent_count;
		if (result.bytesSent_last == null) {
			result.bytesSent_last = bytesSent;
			return;
		}
		Integer dBytesSent = bytesSent - result.bytesSent_last;
		result.bytesSent_sum += dBytesSent;
		if (result.bytesSent_min == null || dBytesSent < result.bytesSent_min) {
			result.bytesSent_min = dBytesSent;
		}
		if (result.bytesSent_max == null || result.bytesSent_max < dBytesSent) {
			result.bytesSent_max = dBytesSent;
		}
	}

	private Integer extractBytesSent(RTCStats sample) {
		Double bytesSent = sample.getBytesSent();
		if (bytesSent == null) {
			return null;
		}
		return bytesSent.intValue();
	}

	private Integer extractPacketsSent(RTCStats sample) {
		Double packetsSent = sample.getPacketsSent();
		if (packetsSent == null) {
			return null;
		}
		return packetsSent.intValue();
	}

}
