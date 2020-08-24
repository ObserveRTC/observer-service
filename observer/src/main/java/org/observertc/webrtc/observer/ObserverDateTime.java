package org.observertc.webrtc.observer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.inject.Singleton;

@Singleton
public class ObserverDateTime {

	private ZoneId zoneId;

	public ObserverDateTime(
			ObserverConfig config
	) {
		this.zoneId = ZoneId.of(config.timeZoneID);
	}

	public ZoneId getZoneId() {
		return this.zoneId;
	}

	public LocalDateTime now() {
		return LocalDateTime.now(this.zoneId);
	}
	
	
}