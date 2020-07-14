package org.observertc.webrtc.service;

import io.micronaut.context.annotation.Value;
import java.time.ZoneId;
import javax.inject.Singleton;

@Singleton
public class ApplicationTimeZoneId {
	
	private ZoneId zoneId;

	public ApplicationTimeZoneId(@Value("${micronaut.application.timeZoneId}") String timeZoneId) {
		this.zoneId = ZoneId.of(timeZoneId);
	}

	public ZoneId getZoneId() {
		return this.zoneId;
	}
}