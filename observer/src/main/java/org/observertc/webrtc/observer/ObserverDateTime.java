package org.observertc.webrtc.observer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.inject.Singleton;

@Singleton
public class ObserverDateTime {

	private static final ZoneId GMT = ZoneId.of("GMT");

	public ObserverDateTime() {

	}

	public LocalDateTime now() {
		return LocalDateTime.now(GMT);
	}


}