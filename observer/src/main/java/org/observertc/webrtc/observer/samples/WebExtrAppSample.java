package org.observertc.webrtc.observer.samples;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.UUID;
import org.observertc.webrtc.observer.dto.webextrapp.PeerConnectionSample;

/**
 * Use {@link ObservedPCS}
 */
@Deprecated
public class WebExtrAppSample {

	public static WebExtrAppSample of(UUID observerUUID,
									  UUID peerConnectionUUID,
									  PeerConnectionSample peerConnectionSample,
									  String clientTimeZoneID,
									  LocalDateTime timestamp) {
		WebExtrAppSample result = new WebExtrAppSample();
		result.observerUUID = observerUUID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.peerConnectionSample = peerConnectionSample;
		result.sampleTimeZoneID = clientTimeZoneID;
		result.timestamp = timestamp;
		return result;
	}

	public UUID observerUUID;
	public UUID peerConnectionUUID;
	public PeerConnectionSample peerConnectionSample;
	public String sampleTimeZoneID;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime timestamp;


}
