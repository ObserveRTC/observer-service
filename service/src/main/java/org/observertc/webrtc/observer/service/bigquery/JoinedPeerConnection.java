package org.observertc.webrtc.observer.service.bigquery;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JoinedPeerConnection {
	public UUID observerUUID;
	public UUID callUUID;
	public UUID peerConnectionUUID;
	public LocalDateTime joined;

	public Map<String, Object> toMap() {
		Map<String, Object> result = new HashMap();
		result.put("ObserverUUID", this.observerUUID.toString());
		result.put("callUUID", this.callUUID.toString());
		result.put("callUUID", this.peerConnectionUUID.toString());
		ZoneId zoneId = ZoneId.systemDefault();
		long epoch = this.joined.atZone(zoneId).toEpochSecond();
		result.put("joined", epoch);
		return result;
	}

}
