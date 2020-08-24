package org.observertc.webrtc.observer.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CandidatePair {
	private double availableOutgoingBitrate;
	private double bytesReceived;
	private double bytesSent;
	private double consentRequestsSent;
	private double currentRoundTripTime;
	private String id;
	private String localCandidateID;
	private boolean nominated;
	private double priority;
	private String remoteCandidateID;
	private double requestsReceived;
	private double requestsSent;
	private double responsesReceived;
	private double responsesSent;
	private State state;
	private double timestamp;
	private double totalRoundTripTime;
	private String transportID;
	private CandidatePairType type;
	private boolean writable;

	@JsonProperty("availableOutgoingBitrate")
	public double getAvailableOutgoingBitrate() { return availableOutgoingBitrate; }
	@JsonProperty("availableOutgoingBitrate")
	public void setAvailableOutgoingBitrate(double value) { this.availableOutgoingBitrate = value; }

	@JsonProperty("bytesReceived")
	public double getBytesReceived() { return bytesReceived; }
	@JsonProperty("bytesReceived")
	public void setBytesReceived(double value) { this.bytesReceived = value; }

	@JsonProperty("bytesSent")
	public double getBytesSent() { return bytesSent; }
	@JsonProperty("bytesSent")
	public void setBytesSent(double value) { this.bytesSent = value; }

	@JsonProperty("consentRequestsSent")
	public double getConsentRequestsSent() { return consentRequestsSent; }
	@JsonProperty("consentRequestsSent")
	public void setConsentRequestsSent(double value) { this.consentRequestsSent = value; }

	@JsonProperty("currentRoundTripTime")
	public double getCurrentRoundTripTime() { return currentRoundTripTime; }
	@JsonProperty("currentRoundTripTime")
	public void setCurrentRoundTripTime(double value) { this.currentRoundTripTime = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("localCandidateId")
	public String getLocalCandidateID() { return localCandidateID; }
	@JsonProperty("localCandidateId")
	public void setLocalCandidateID(String value) { this.localCandidateID = value; }

	@JsonProperty("nominated")
	public boolean getNominated() { return nominated; }
	@JsonProperty("nominated")
	public void setNominated(boolean value) { this.nominated = value; }

	@JsonProperty("priority")
	public double getPriority() { return priority; }
	@JsonProperty("priority")
	public void setPriority(double value) { this.priority = value; }

	@JsonProperty("remoteCandidateId")
	public String getRemoteCandidateID() { return remoteCandidateID; }
	@JsonProperty("remoteCandidateId")
	public void setRemoteCandidateID(String value) { this.remoteCandidateID = value; }

	@JsonProperty("requestsReceived")
	public double getRequestsReceived() { return requestsReceived; }
	@JsonProperty("requestsReceived")
	public void setRequestsReceived(double value) { this.requestsReceived = value; }

	@JsonProperty("requestsSent")
	public double getRequestsSent() { return requestsSent; }
	@JsonProperty("requestsSent")
	public void setRequestsSent(double value) { this.requestsSent = value; }

	@JsonProperty("responsesReceived")
	public double getResponsesReceived() { return responsesReceived; }
	@JsonProperty("responsesReceived")
	public void setResponsesReceived(double value) { this.responsesReceived = value; }

	@JsonProperty("responsesSent")
	public double getResponsesSent() { return responsesSent; }
	@JsonProperty("responsesSent")
	public void setResponsesSent(double value) { this.responsesSent = value; }

	@JsonProperty("state")
	public State getState() { return state; }
	@JsonProperty("state")
	public void setState(State value) { this.state = value; }

	@JsonProperty("timestamp")
	public double getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(double value) { this.timestamp = value; }

	@JsonProperty("totalRoundTripTime")
	public double getTotalRoundTripTime() { return totalRoundTripTime; }
	@JsonProperty("totalRoundTripTime")
	public void setTotalRoundTripTime(double value) { this.totalRoundTripTime = value; }

	@JsonProperty("transportId")
	public String getTransportID() { return transportID; }
	@JsonProperty("transportId")
	public void setTransportID(String value) { this.transportID = value; }

	@JsonProperty("type")
	public CandidatePairType getType() { return type; }
	@JsonProperty("type")
	public void setType(CandidatePairType value) { this.type = value; }

	@JsonProperty("writable")
	public boolean getWritable() { return writable; }
	@JsonProperty("writable")
	public void setWritable(boolean value) { this.writable = value; }
}
