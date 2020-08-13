package org.observertc.webrtc.service.evaluators.purgatory.aggregator;

import io.micronaut.context.annotation.Prototype;
import java.util.UUID;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.samples.MediaStreamSample;
import org.observertc.webrtc.service.samples.RemoteInboundStreamMeasurement;

@Prototype
public class RemoteInboundStreamSampleMapper implements KeyValueMapper
		<UUID, MediaStreamSample, KeyValue<MediaStreamKey, RemoteInboundStreamMeasurement>> {

	public RemoteInboundStreamSampleMapper() {

	}

	@Override
	public KeyValue<MediaStreamKey, RemoteInboundStreamMeasurement> apply(UUID peerConnectionUUID, MediaStreamSample sample) {
		RTCStats rtcStats = sample.rtcStats;
		RemoteInboundStreamMeasurement value = new RemoteInboundStreamMeasurement();
		value.SSRC = sample.rtcStats.getSsrc().longValue();
		value.peerConnectionUUID = peerConnectionUUID;
		value.RTTInMs = this.extractRTTInMs(rtcStats);
		value.sampled = sample.sampled;
		MediaStreamKey mediaStreamKey = MediaStreamKey.of(sample.observerUUID, value.SSRC);
		return new KeyValue<>(mediaStreamKey, value);
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
