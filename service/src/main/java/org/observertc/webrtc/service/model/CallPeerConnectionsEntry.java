package org.observertc.webrtc.service.model;

import io.micronaut.core.annotation.Introspected;
import java.time.LocalDateTime;
import java.util.UUID;

@Introspected
public class CallPeerConnectionsEntry {
	public static CallPeerConnectionsEntry of(UUID peerConnectionUUID, UUID callUUID, LocalDateTime updated) {
		CallPeerConnectionsEntry result = new CallPeerConnectionsEntry();
		result.callUUID = callUUID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.updated = updated;
		return result;
	}

	public UUID peerConnectionUUID;
	public UUID callUUID;
	public LocalDateTime updated;
}
