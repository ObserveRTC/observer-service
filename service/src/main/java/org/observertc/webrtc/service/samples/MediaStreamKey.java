package org.observertc.webrtc.service.samples;

import io.micronaut.core.annotation.Introspected;
import java.util.UUID;

@Introspected
public class MediaStreamKey {
	public static MediaStreamKey of(UUID observerUUID, UUID peerConnectionUUID, Long SSRC) {
		MediaStreamKey result = new MediaStreamKey();
		result.observerUUID = observerUUID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.SSRC = SSRC;
		return result;
	}

	public UUID observerUUID;
	public UUID peerConnectionUUID;
	public Long SSRC;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if (this.observerUUID != null) {
			result += prime * this.observerUUID.hashCode();
		}
		if (this.peerConnectionUUID != null) {
			result += prime * this.peerConnectionUUID.hashCode();
		}
		if (this.SSRC != null) {
			result += prime * this.SSRC.hashCode();
		}
		return result;
//		return this.observerUUID.hashCode() + this.peerConnectionUUID.hashCode() + this.SSRC.hashCode();
	}

	@Override
	public boolean equals(Object peer) {
		return peer.hashCode() == this.hashCode();
	}
}
