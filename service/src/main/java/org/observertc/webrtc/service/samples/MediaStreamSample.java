package org.observertc.webrtc.service.samples;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.UUID;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;

public class MediaStreamSample {

	public static MediaStreamSample of(UUID observerUUID,
									   UUID peerConnectionUUID,
									   String browserID,
									   String timeZoneID,
									   RTCStats rtcStats,
									   LocalDateTime sampled) {
		MediaStreamSample result = new MediaStreamSample();
		result.observerUUID = observerUUID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.browserID = browserID;
		result.timeZoneID = timeZoneID;
		result.rtcStats = rtcStats;
		result.sampled = sampled;
		return result;
	}

	public UUID observerUUID;
	public UUID peerConnectionUUID;
	public RTCStats rtcStats;
	public String browserID;
	public String timeZoneID;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime sampled;


	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append(String.format("observerUUID: %s, sampled: %s, RTCStats: %s", this.observerUUID, this.sampled, this.rtcStats));
		return result.toString();
	}
}
