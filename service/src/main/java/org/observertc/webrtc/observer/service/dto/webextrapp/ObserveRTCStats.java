package org.observertc.webrtc.observer.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ObserveRTCStats {
	private RTCStats rtcStats;

	@JsonProperty("rtcStats")
	public RTCStats getRTCStats() { return rtcStats; }
	@JsonProperty("rtcStats")
	public void setRTCStats(RTCStats value) { this.rtcStats = value; }
}
