package org.observertc.webrtc.common.reports;

import java.time.LocalDateTime;
import java.util.UUID;

public interface FinishedCall {
	UUID getObserverUUID();

	UUID getCallUUID();

	LocalDateTime getTimestamp();
}
