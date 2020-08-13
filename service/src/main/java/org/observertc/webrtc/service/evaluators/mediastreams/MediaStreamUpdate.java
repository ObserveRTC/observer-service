package org.observertc.webrtc.service.evaluators.mediastreams;//package com.observertc.gatekeeper.webrtcstat.processors.samples;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MediaStreamUpdate {

	public static MediaStreamUpdate of(
			UUID observerUUID,
			UUID peerConnectionUUID,
			LocalDateTime created,
			String browserID,
			String timeZoneID
	) {
		MediaStreamUpdate result = new MediaStreamUpdate();
		result.observerUUID = observerUUID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.created = result.updated = created;
		result.browserID = browserID;
		result.timeZoneID = timeZoneID;
		return result;
	}

	public Set<Long> SSRCs = new HashSet<>();
	public UUID peerConnectionUUID;
	public UUID observerUUID;
	public LocalDateTime created;
	public LocalDateTime updated;
	public String browserID;
	public String timeZoneID;

	public MediaStreamUpdate add(Long SSRC, LocalDateTime timestamp) {
		this.SSRCs.add(SSRC);
		if (this.updated == null) {
			this.updated = timestamp;
		} else if (0 < this.updated.compareTo(timestamp)) {
			this.updated = timestamp;
		}
		return this;
	}
}
