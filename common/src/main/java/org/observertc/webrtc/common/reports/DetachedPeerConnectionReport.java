package org.observertc.webrtc.common.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.UUID;

public class DetachedPeerConnectionReport extends Report {
	public static DetachedPeerConnectionReport of(UUID observerUUID, UUID callUUID, UUID peerConnectionUUID, LocalDateTime detached) {
		DetachedPeerConnectionReport result = new DetachedPeerConnectionReport();
		result.callUUID = callUUID;
		result.observerUUID = observerUUID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.detached = detached;
		return result;
	}

	@JsonCreator
	public DetachedPeerConnectionReport() {
		super(ReportType.DETACHED_PEER_CONNECTION);
	}


	public UUID observerUUID;
	public UUID callUUID;
	public UUID peerConnectionUUID;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime detached;
}
