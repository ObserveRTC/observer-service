package org.observertc.webrtc.service.samples;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.UUID;
import org.observertc.webrtc.service.dto.webextrapp.ObserveRTCCIceStats;

public class ICEStatsSample {

	public static ICEStatsSample of(UUID observerUUID,
									UUID peerConnectionUUID,
									ObserveRTCCIceStats iceStats,
									LocalDateTime sampled) {
		ICEStatsSample result = new ICEStatsSample();
		result.observerUUID = observerUUID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.iceStats = iceStats;
		result.sampled = sampled;
		return result;
	}

	public UUID observerUUID;

	public UUID peerConnectionUUID;

	public ObserveRTCCIceStats iceStats;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime sampled;
}

