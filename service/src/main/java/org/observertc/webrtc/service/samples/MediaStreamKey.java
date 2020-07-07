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
		int result = 31;
		if (this.observerUUID != null) {
			result += this.observerUUID.hashCode();
		}
		if (this.peerConnectionUUID != null) {
			result += this.peerConnectionUUID.hashCode();
		}
		if (this.SSRC != null) {
			result += this.SSRC.hashCode();
		}
		return result;
//		return this.observerUUID.hashCode() + this.peerConnectionUUID.hashCode() + this.SSRC.hashCode();
	}

	@Override
	public boolean equals(Object peer) {
		return peer.hashCode() == this.hashCode();
	}
}
