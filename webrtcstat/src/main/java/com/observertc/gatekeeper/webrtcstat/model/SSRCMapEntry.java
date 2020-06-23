package com.observertc.gatekeeper.webrtcstat.model;

import io.micronaut.core.annotation.Introspected;
import java.util.UUID;

@Introspected
public class SSRCMapEntry {
	public Long SSRC;
	public UUID peerConnectionUUID;
	public UUID observerUUID;

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.SSRC == null) ? 0 : this.SSRC.hashCode())
				+ ((this.peerConnectionUUID == null) ? 0 : this.peerConnectionUUID.hashCode())
				+ ((this.observerUUID == null) ? 0 : this.observerUUID.hashCode());
		return result;
	}
}
