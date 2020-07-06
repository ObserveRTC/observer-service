package org.observertc.webrtc.observer.service.samples;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.observertc.webrtc.observer.service.dto.webextrapp.RTCStats;
import java.time.LocalDateTime;
import java.util.UUID;

public class ObserveRTCMediaStreamStatsSample {

	public UUID observerUUID;
	public RTCStats rtcStats;
	
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime sampled;
}
