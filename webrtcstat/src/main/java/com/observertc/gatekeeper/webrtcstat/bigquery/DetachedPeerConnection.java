package com.observertc.gatekeeper.webrtcstat.bigquery;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DetachedPeerConnection {
	public UUID observerUUID;
	public UUID callUUID;
	public UUID peerConnectionUUID;
	public LocalDateTime detached;

	public Map<String, Object> toMap() {
		Map<String, Object> result = new HashMap();
		result.put("ObserverUUID", this.observerUUID.toString());
		result.put("callUUID", this.callUUID.toString());
		result.put("callUUID", this.peerConnectionUUID.toString());
		ZoneId zoneId = ZoneId.systemDefault();
		long epoch = this.detached.atZone(zoneId).toEpochSecond();
		result.put("detached", epoch);
		return result;
	}

}
