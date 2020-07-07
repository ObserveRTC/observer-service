package org.observertc.webrtc.common.reports;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DetachedPeerConnection {
	UUID getObserverUUID();

	UUID getPeerConnectionUUID();

	LocalDateTime getTimestamp();

	UUID getCallUUID();
}