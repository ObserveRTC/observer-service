package com.observertc.gatekeeper.webrtcstat.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MediaSource {
	private double audioLevel;
	private double framesPerSecond;
	private double height;
	private String id;
	private Kind kind;
	private double timestamp;
	private double totalAudioEnergy;
	private double totalSamplesDuration;
	private String trackIdentifier;
	private MediaSourceType type;
	private double width;

	@JsonProperty("audioLevel")
	public double getAudioLevel() { return audioLevel; }
	@JsonProperty("audioLevel")
	public void setAudioLevel(double value) { this.audioLevel = value; }

	@JsonProperty("framesPerSecond")
	public double getFramesPerSecond() { return framesPerSecond; }
	@JsonProperty("framesPerSecond")
	public void setFramesPerSecond(double value) { this.framesPerSecond = value; }

	@JsonProperty("height")
	public double getHeight() { return height; }
	@JsonProperty("height")
	public void setHeight(double value) { this.height = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("kind")
	public Kind getKind() { return kind; }
	@JsonProperty("kind")
	public void setKind(Kind value) { this.kind = value; }

	@JsonProperty("timestamp")
	public double getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(double value) { this.timestamp = value; }

	@JsonProperty("totalAudioEnergy")
	public double getTotalAudioEnergy() { return totalAudioEnergy; }
	@JsonProperty("totalAudioEnergy")
	public void setTotalAudioEnergy(double value) { this.totalAudioEnergy = value; }

	@JsonProperty("totalSamplesDuration")
	public double getTotalSamplesDuration() { return totalSamplesDuration; }
	@JsonProperty("totalSamplesDuration")
	public void setTotalSamplesDuration(double value) { this.totalSamplesDuration = value; }

	@JsonProperty("trackIdentifier")
	public String getTrackIdentifier() { return trackIdentifier; }
	@JsonProperty("trackIdentifier")
	public void setTrackIdentifier(String value) { this.trackIdentifier = value; }

	@JsonProperty("type")
	public MediaSourceType getType() { return type; }
	@JsonProperty("type")
	public void setType(MediaSourceType value) { this.type = value; }

	@JsonProperty("width")
	public double getWidth() { return width; }
	@JsonProperty("width")
	public void setWidth(double value) { this.width = value; }
}
