package org.observertc.webrtc.service.evaluators.purgatory.aggregator;

import io.micronaut.context.annotation.Prototype;
import java.util.UUID;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.samples.InboundStreamMeasurement;
import org.observertc.webrtc.service.samples.MediaStreamSample;

@Prototype
public class InboundStreamSampleMapper implements KeyValueMapper
		<UUID, MediaStreamSample, KeyValue<MediaStreamKey, InboundStreamMeasurement>> {

	public InboundStreamSampleMapper() {

	}

	@Override
	public KeyValue<MediaStreamKey, InboundStreamMeasurement> apply(UUID peerConnectionUUID, MediaStreamSample sample) {
		RTCStats rtcStats = sample.rtcStats;
		InboundStreamMeasurement value = new InboundStreamMeasurement();
		value.SSRC = sample.rtcStats.getSsrc().longValue();
		value.peerConnectionUUID = peerConnectionUUID;
		value.bytesReceived = this.extractBytesReceived(rtcStats);
		value.packetsReceived = this.extractPacketsReceived(rtcStats);
		value.packetsLost = this.extractPacketsLost(rtcStats);
		value.sampled = sample.sampled;
		MediaStreamKey mediaStreamKey = MediaStreamKey.of(sample.observerUUID, value.SSRC);
		return new KeyValue<>(mediaStreamKey, value);
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
