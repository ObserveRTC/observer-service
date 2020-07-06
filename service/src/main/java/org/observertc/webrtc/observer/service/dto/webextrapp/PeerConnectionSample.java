package org.observertc.webrtc.observer.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PeerConnectionSample {
	private ObserveRTCCIceStats iceStats;
	private String peerConnectionID;
	private RTCStats[] receiverStats;
	private RTCStats[] senderStats;

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
