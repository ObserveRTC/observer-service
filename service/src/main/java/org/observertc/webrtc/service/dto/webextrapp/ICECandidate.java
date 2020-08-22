package org.observertc.webrtc.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ICECandidate {
	private Candidate candidate;

	@JsonProperty("candidate")
	public Candidate getCandidate() { return candidate; }
	@JsonProperty("candidate")
	public void setCandidate(Candidate value) { this.candidate = value; }
}
