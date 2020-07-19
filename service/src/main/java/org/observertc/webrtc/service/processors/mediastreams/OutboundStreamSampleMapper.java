package org.observertc.webrtc.service.processors.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.util.UUID;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.samples.ObserveRTCMediaStreamStatsSample;
import org.observertc.webrtc.service.samples.OutboundStreamMeasurement;

@Prototype
public class OutboundStreamSampleMapper implements KeyValueMapper
		<UUID, ObserveRTCMediaStreamStatsSample, KeyValue<MediaStreamKey, OutboundStreamMeasurement>> {

	public OutboundStreamSampleMapper() {

	}

	@Override
	public KeyValue<MediaStreamKey, OutboundStreamMeasurement> apply(UUID peerConnectionUUID, ObserveRTCMediaStreamStatsSample sample) {
		RTCStats rtcStats = sample.rtcStats;
		OutboundStreamMeasurement value = new OutboundStreamMeasurement();
		value.SSRC = rtcStats.getSsrc().longValue();
		value.peerConnectionUUID = peerConnectionUUID;
		value.bytesSent = this.extractBytesSent(rtcStats);
		value.packetsSent = this.extractPacketsSent(rtcStats);
		value.sampled = sample.sampled;
		MediaStreamKey mediaStreamKey = MediaStreamKey.of(sample.observerUUID, value.SSRC);
		return new KeyValue<>(mediaStreamKey, value);
	}

	private Integer extractBytesSent(RTCStats sample) {
		Double bytesReceived = sample.getBytesSent();
		if (bytesReceived == null) {
			return null;
		}
		return bytesReceived.intValue();
	}

	private Integer extractPacketsSent(RTCStats sample) {
		Double packetsLost = sample.getPacketsSent();
		if (packetsLost == null) {
			return null;
		}
		return packetsLost.intValue();
	}


}
