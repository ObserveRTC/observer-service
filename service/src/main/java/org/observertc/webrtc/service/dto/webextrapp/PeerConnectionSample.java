package org.observertc.webrtc.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PeerConnectionSample {
	private ObserveRTCCIceStats iceStats;
	private String browserId;
	private Double timeZoneOffsetInMinute;
	private String peerConnectionID;
	private RTCStats[] receiverStats;
	private RTCStats[] senderStats;

	@JsonProperty("browserId")
	public void setBrowserId(String value) {
		this.browserId = value;
	}

	@JsonProperty("browserId")
	public String getBrowserId() {
		return browserId;
	}

	@JsonProperty("timeZoneOffsetInMinute")
	public void setTimeZoneOffsetInMinute(Double value) {
		this.timeZoneOffsetInMinute = value;
	}

	@JsonProperty("timeZoneOffsetInMinute")
	public Double getTimeZoneOffsetInMinute() {
		return timeZoneOffsetInMinute;
	}

	@JsonProperty("iceStats")
	public ObserveRTCCIceStats getIceStats() {
		return iceStats;
	}

	@JsonProperty("iceStats")
	public void setIceStats(ObserveRTCCIceStats value) {
		this.iceStats = value;
	}


	@JsonProperty("peerConnectionId")
	public String getPeerConnectionID() {
		return peerConnectionID;
	}

	@JsonProperty("peerConnectionId")
	public void setPeerConnectionID(String value) {
		this.peerConnectionID = value;
	}

	@JsonProperty("receiverStats")
	public RTCStats[] getReceiverStats() {
		return receiverStats;
	}

	@JsonProperty("receiverStats")
	public void setReceiverStats(RTCStats[] value) {
		this.receiverStats = value;
	}

	@JsonProperty("senderStats")
	public RTCStats[] getSenderStats() {
		return senderStats;
	}

	@JsonProperty("senderStats")
	public void setSenderStats(RTCStats[] value) {
		this.senderStats = value;
	}
}
