package org.observertc.webrtc.service.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.util.UUID;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.samples.MediaStreamKey;
import org.observertc.webrtc.service.samples.ObserveRTCMediaStreamStatsSample;
import org.observertc.webrtc.service.samples.RemoteInboundStreamMeasurement;

@Prototype
public class RemoteInboundStreamSampleMapper implements KeyValueMapper
		<UUID, ObserveRTCMediaStreamStatsSample, KeyValue<UUID, RemoteInboundStreamMeasurement>> {

	public RemoteInboundStreamSampleMapper() {

	}

	@Override
	public KeyValue<UUID, RemoteInboundStreamMeasurement> apply(UUID peerConnectionUUID, ObserveRTCMediaStreamStatsSample sample) {
		RTCStats rtcStats = sample.rtcStats;
		RemoteInboundStreamMeasurement value = new RemoteInboundStreamMeasurement();
		value.SSRC = sample.rtcStats.getSsrc().longValue();
		value.peerConnectionUUID = peerConnectionUUID;
		value.RTTInMs = this.extractRTTInMs(rtcStats);
		value.sampled = sample.sampled;
		MediaStreamKey key = MediaStreamKey.of(sample.observerUUID, sample.rtcStats.getSsrc().longValue());
		return new KeyValue<>(key.observerUUID, value);
	}

	private Integer extractRTTInMs(RTCStats sample) {
		Double RTTInS = sample.getRoundTripTime();
		if (RTTInS == null) {
			return null;
		}
		RTTInS *= 1000d;
		return RTTInS.intValue();
	}


}
