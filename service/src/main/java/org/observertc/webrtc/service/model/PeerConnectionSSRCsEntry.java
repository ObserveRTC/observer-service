package org.observertc.webrtc.service.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.micronaut.core.annotation.Introspected;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.validation.constraints.NotNull;

@Introspected
public class PeerConnectionSSRCsEntry {
	@NotNull
	public Long SSRC;
	@NotNull
	public UUID peerConnectionUUID;
	@NotNull
	public UUID observerUUID;

	public String browserID;

	public String timeZoneId;

	@NotNull
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime updated;

}
