package org.observertc.webrtc.service.mediastreams;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.micronaut.core.annotation.Introspected;
import java.time.LocalDateTime;
import java.util.UUID;

@Introspected
public class MediaStreamKey {

	public UUID observerUUID;
	public Long SSRC;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime sampled;

	public static MediaStreamKey of(UUID observerUUID, Long ssrc) {
		MediaStreamKey result = new MediaStreamKey();
		result.observerUUID = observerUUID;
		result.SSRC = ssrc;
		return result;
	}

	@Override
	public int hashCode() {
		int result = 1;
		final int prime = 31;
		if (this.observerUUID != null) {
			result += prime * this.observerUUID.hashCode();
		}
		if (this.SSRC != null) {
			result += prime * this.SSRC.hashCode();
		}
		return result;
	}

	@Override
	public boolean equals(Object peer) {
		return this.hashCode() == peer.hashCode();
	}
}
