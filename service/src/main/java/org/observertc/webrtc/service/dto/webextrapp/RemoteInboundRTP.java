package org.observertc.webrtc.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteInboundRTP {
	private String codecID;
	private String id;
	private double jitter;
	private Kind kind;
	private String localID;
	private double packetsLost;
	private double roundTripTime;
	private double ssrc;
	private double timestamp;
	private String transportID;
	private RemoteInboundRTPType type;

	@JsonProperty("codecId")
	public String getCodecID() { return codecID; }
	@JsonProperty("codecId")
	public void setCodecID(String value) { this.codecID = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("jitter")
	public double getJitter() { return jitter; }
	@JsonProperty("jitter")
	public void setJitter(double value) { this.jitter = value; }

	@JsonProperty("kind")
	public Kind getKind() { return kind; }
	@JsonProperty("kind")
	public void setKind(Kind value) { this.kind = value; }

	@JsonProperty("localId")
	public String getLocalID() { return localID; }
	@JsonProperty("localId")
	public void setLocalID(String value) { this.localID = value; }

	@JsonProperty("packetsLost")
	public double getPacketsLost() { return packetsLost; }
	@JsonProperty("packetsLost")
	public void setPacketsLost(double value) { this.packetsLost = value; }

	@JsonProperty("roundTripTime")
	public double getRoundTripTime() { return roundTripTime; }
	@JsonProperty("roundTripTime")
	public void setRoundTripTime(double value) { this.roundTripTime = value; }

	@JsonProperty("ssrc")
	public double getSsrc() { return ssrc; }
	@JsonProperty("ssrc")
	public void setSsrc(double value) { this.ssrc = value; }

	@JsonProperty("timestamp")
	public double getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(double value) { this.timestamp = value; }

	@JsonProperty("transportId")
	public String getTransportID() { return transportID; }
	@JsonProperty("transportId")
	public void setTransportID(String value) { this.transportID = value; }

	@JsonProperty("type")
	public RemoteInboundRTPType getType() { return type; }
	@JsonProperty("type")
	public void setType(RemoteInboundRTPType value) { this.type = value; }
}
