package org.observertc.webrtc.common.reports;

import java.time.LocalDateTime;
import java.util.UUID;

public interface InitiatedCall {
	UUID getObserverUUID();

	UUID getCallUUID();

	LocalDateTime getTimestamp();
}
