package org.observertc.webrtc.service;

import java.time.ZoneId;
import javax.inject.Singleton;

@Singleton
public class ObserverTimeZoneId {

	private ZoneId zoneId;

	public ObserverTimeZoneId(
			ObserverConfig config
	) {
		this.zoneId = ZoneId.of(config.timeZoneID);
	}

	public ZoneId getZoneId() {
		return this.zoneId;
	}
}