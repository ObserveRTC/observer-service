package com.observertc.gatekeeper.webrtcstat.bigquery;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FinishedCall {
	public UUID observerUUID;
	public UUID callUUID;
	public LocalDateTime finished;

	public Map<String, Object> toMap() {
		Map<String, Object> result = new HashMap();
		result.put("ObserverUUID", this.observerUUID.toString());
		result.put("callUUID", this.callUUID.toString());
		ZoneId zoneId = ZoneId.systemDefault();
		long epoch = this.finished.atZone(zoneId).toEpochSecond();
		result.put("finished", epoch);
		return result;
	}

}
