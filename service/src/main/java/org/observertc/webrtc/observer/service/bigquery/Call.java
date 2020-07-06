package org.observertc.webrtc.observer.service.bigquery;

import io.micronaut.core.annotation.Introspected;
import java.time.Instant;
import java.util.UUID;

@Introspected
public class Call {
	public Instant timestamp;
	public UUID observerUUID;
	public UUID callUUID;
	public String event;
}
