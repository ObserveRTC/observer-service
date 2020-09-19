package org.observertc.webrtc.observer.evaluators.mediastreams;//package com.observertc.gatekeeper.webrtcstat.processors.samples;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MediaStreamUpdate {

	public static MediaStreamUpdate of(
			UUID serviceUUID,
			UUID peerConnectionUUID,
			Long created,
			String browserID,
			String provdedCallID,
			String timeZoneID,
			String providedUserID,
			String mediaUnitID,
			String serviceName
	) {
		MediaStreamUpdate result = new MediaStreamUpdate();
		result.serviceUUID = serviceUUID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.created = result.updated = created;
		result.browserID = browserID;
		result.timeZoneID = timeZoneID;
		result.providedCallID = provdedCallID;
		result.providedUserID = providedUserID;
		result.mediaUnitID = mediaUnitID;
		result.serviceName = serviceName;
		return result;
	}

	public Set<Long> SSRCs = new HashSet<>();
	public UUID peerConnectionUUID;
	public UUID serviceUUID;
	public Long created;
	public Long updated;
	public String browserID;
	public String timeZoneID;
	public String providedCallID;
	public String providedUserID;
	public String mediaUnitID;
	public String serviceName;

}
