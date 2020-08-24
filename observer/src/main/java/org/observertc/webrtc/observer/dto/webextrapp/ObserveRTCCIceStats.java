package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ObserveRTCCIceStats {
	private CandidatePair[] iceCandidatePair;
	private LocalCandidate[] localCandidates;
	private RemoteCandidate[] remoteCandidates;

	@JsonProperty("iceCandidatePair")
	public CandidatePair[] getIceCandidatePair() {
		return iceCandidatePair;
	}

	@JsonProperty("iceCandidatePair")
	public void setIceCandidatePair(CandidatePair[] value) {
		this.iceCandidatePair = value;
	}

	@JsonProperty("localCandidates")
	public LocalCandidate[] getLocalCandidates() {
		return localCandidates;
	}

	@JsonProperty("localCandidates")
	public void setLocalCandidates(LocalCandidate[] value) {
		this.localCandidates = value;
	}

	@JsonProperty("remoteCandidates")
	public RemoteCandidate[] getRemoteCandidates() {
		return remoteCandidates;
	}

	@JsonProperty("remoteCandidates")
	public void setRemoteCandidates(RemoteCandidate[] value) {
		this.remoteCandidates = value;
	}
}
