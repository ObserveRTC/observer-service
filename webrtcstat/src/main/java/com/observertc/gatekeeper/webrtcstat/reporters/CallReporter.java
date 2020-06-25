package com.observertc.gatekeeper.webrtcstat.reporters;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CallReporter {

	void joinedPeerConnection(UUID observerUUID, UUID callUUUID, UUID peerConnectionUUID, LocalDateTime joined);

	void detachedPeerConnection(UUID observerUUID, UUID callUUUID, UUID peerConnectionUUID, LocalDateTime detached);

	void initiatedCall(UUID observerUUID, UUID callUUID, LocalDateTime initiated);

	void finishedCall(UUID observerUUID, UUID callUUID, LocalDateTime fnished);
}
