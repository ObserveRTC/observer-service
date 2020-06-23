package com.observertc.gatekeeper.webrtcstat.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RemoteCandidate {
	private CandidateTypeEnum candidateType;
	private boolean deleted;
	private String id;
	private String ip;
	private boolean isRemote;
	private double port;
	private double priority;
	private Protocol protocol;
	private double timestamp;
	private String transportID;
	private RemoteCandidateType type;

	@JsonProperty("candidateType")
	public CandidateTypeEnum getCandidateType() { return candidateType; }
	@JsonProperty("candidateType")
	public void setCandidateType(CandidateTypeEnum value) { this.candidateType = value; }

	@JsonProperty("deleted")
	public boolean getDeleted() { return deleted; }
	@JsonProperty("deleted")
	public void setDeleted(boolean value) { this.deleted = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("ip")
	public String getIP() { return ip; }
	@JsonProperty("ip")
	public void setIP(String value) { this.ip = value; }

	@JsonProperty("isRemote")
	public boolean getIsRemote() { return isRemote; }
	@JsonProperty("isRemote")
	public void setIsRemote(boolean value) { this.isRemote = value; }

	@JsonProperty("port")
	public double getPort() { return port; }
	@JsonProperty("port")
	public void setPort(double value) { this.port = value; }

	@JsonProperty("priority")
	public double getPriority() { return priority; }
	@JsonProperty("priority")
	public void setPriority(double value) { this.priority = value; }

	@JsonProperty("protocol")
	public Protocol getProtocol() { return protocol; }
	@JsonProperty("protocol")
	public void setProtocol(Protocol value) { this.protocol = value; }

	@JsonProperty("timestamp")
	public double getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(double value) { this.timestamp = value; }

	@JsonProperty("transportId")
	public String getTransportID() { return transportID; }
	@JsonProperty("transportId")
	public void setTransportID(String value) { this.transportID = value; }

	@JsonProperty("type")
	public RemoteCandidateType getType() { return type; }
	@JsonProperty("type")
	public void setType(RemoteCandidateType value) { this.type = value; }
}
