package org.observertc.webrtc.common.reports;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.UUID;

public class InitiatedCallReport extends Report {
	public static InitiatedCallReport of(UUID observerUUID, UUID callUUID, LocalDateTime initiated) {
		InitiatedCallReport result = new InitiatedCallReport();
		result.callUUID = callUUID;
		result.observerUUID = observerUUID;
		result.initiated = initiated;
		return result;
	}

	public UUID observerUUID;
	public UUID callUUID;
	
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime initiated;
}
