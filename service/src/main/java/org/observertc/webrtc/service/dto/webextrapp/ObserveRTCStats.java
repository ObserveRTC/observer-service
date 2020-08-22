package org.observertc.webrtc.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ObserveRTCStats {
	private RTCStats rtcStats;

	@JsonProperty("rtcStats")
	public RTCStats getRTCStats() { return rtcStats; }
	@JsonProperty("rtcStats")
	public void setRTCStats(RTCStats value) { this.rtcStats = value; }
}
