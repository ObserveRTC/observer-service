package com.observertc.gatekeeper.webrtcstat.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ICECandidate {
	private Candidate candidate;

	@JsonProperty("candidate")
	public Candidate getCandidate() { return candidate; }
	@JsonProperty("candidate")
	public void setCandidate(Candidate value) { this.candidate = value; }
}
