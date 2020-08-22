package org.observertc.webrtc.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Track {
	private double audioLevel;
	private double concealedSamples;
	private double concealmentEvents;
	private boolean detached;
	private boolean ended;
	private double frameHeight;
	private double framesDecoded;
	private double framesDropped;
	private double framesReceived;
	private double framesSent;
	private double frameWidth;
	private double hugeFramesSent;
	private String id;
	private double insertedSamplesForDeceleration;
	private double jitterBufferDelay;
	private double jitterBufferEmittedCount;
	private Kind kind;
	private String mediaSourceID;
	private boolean remoteSource;
	private double removedSamplesForAcceleration;
	private double silentConcealedSamples;
	private String timestamp;
	private double totalAudioEnergy;
	private double totalSamplesDuration;
	private double totalSamplesReceived;
	private String trackIdentifier;
	private TrackType type;

	@JsonProperty("audioLevel")
	public double getAudioLevel() { return audioLevel; }
	@JsonProperty("audioLevel")
	public void setAudioLevel(double value) { this.audioLevel = value; }

	@JsonProperty("concealedSamples")
	public double getConcealedSamples() { return concealedSamples; }
	@JsonProperty("concealedSamples")
	public void setConcealedSamples(double value) { this.concealedSamples = value; }

	@JsonProperty("concealmentEvents")
	public double getConcealmentEvents() { return concealmentEvents; }
	@JsonProperty("concealmentEvents")
	public void setConcealmentEvents(double value) { this.concealmentEvents = value; }

	@JsonProperty("detached")
	public boolean getDetached() { return detached; }
	@JsonProperty("detached")
	public void setDetached(boolean value) { this.detached = value; }

	@JsonProperty("ended")
	public boolean getEnded() { return ended; }
	@JsonProperty("ended")
	public void setEnded(boolean value) { this.ended = value; }

	@JsonProperty("frameHeight")
	public double getFrameHeight() { return frameHeight; }
	@JsonProperty("frameHeight")
	public void setFrameHeight(double value) { this.frameHeight = value; }

	@JsonProperty("framesDecoded")
	public double getFramesDecoded() { return framesDecoded; }
	@JsonProperty("framesDecoded")
	public void setFramesDecoded(double value) { this.framesDecoded = value; }

	@JsonProperty("framesDropped")
	public double getFramesDropped() { return framesDropped; }
	@JsonProperty("framesDropped")
	public void setFramesDropped(double value) { this.framesDropped = value; }

	@JsonProperty("framesReceived")
	public double getFramesReceived() { return framesReceived; }
	@JsonProperty("framesReceived")
	public void setFramesReceived(double value) { this.framesReceived = value; }

	@JsonProperty("framesSent")
	public double getFramesSent() { return framesSent; }
	@JsonProperty("framesSent")
	public void setFramesSent(double value) { this.framesSent = value; }

	@JsonProperty("frameWidth")
	public double getFrameWidth() { return frameWidth; }
	@JsonProperty("frameWidth")
	public void setFrameWidth(double value) { this.frameWidth = value; }

	@JsonProperty("hugeFramesSent")
	public double getHugeFramesSent() { return hugeFramesSent; }
	@JsonProperty("hugeFramesSent")
	public void setHugeFramesSent(double value) { this.hugeFramesSent = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("insertedSamplesForDeceleration")
	public double getInsertedSamplesForDeceleration() { return insertedSamplesForDeceleration; }
	@JsonProperty("insertedSamplesForDeceleration")
	public void setInsertedSamplesForDeceleration(double value) { this.insertedSamplesForDeceleration = value; }

	@JsonProperty("jitterBufferDelay")
	public double getJitterBufferDelay() { return jitterBufferDelay; }
	@JsonProperty("jitterBufferDelay")
	public void setJitterBufferDelay(double value) { this.jitterBufferDelay = value; }

	@JsonProperty("jitterBufferEmittedCount")
	public double getJitterBufferEmittedCount() { return jitterBufferEmittedCount; }
	@JsonProperty("jitterBufferEmittedCount")
	public void setJitterBufferEmittedCount(double value) { this.jitterBufferEmittedCount = value; }

	@JsonProperty("kind")
	public Kind getKind() { return kind; }
	@JsonProperty("kind")
	public void setKind(Kind value) { this.kind = value; }

	@JsonProperty("mediaSourceId")
	public String getMediaSourceID() { return mediaSourceID; }
	@JsonProperty("mediaSourceId")
	public void setMediaSourceID(String value) { this.mediaSourceID = value; }

	@JsonProperty("remoteSource")
	public boolean getRemoteSource() { return remoteSource; }
	@JsonProperty("remoteSource")
	public void setRemoteSource(boolean value) { this.remoteSource = value; }

	@JsonProperty("removedSamplesForAcceleration")
	public double getRemovedSamplesForAcceleration() { return removedSamplesForAcceleration; }
	@JsonProperty("removedSamplesForAcceleration")
	public void setRemovedSamplesForAcceleration(double value) { this.removedSamplesForAcceleration = value; }

	@JsonProperty("silentConcealedSamples")
	public double getSilentConcealedSamples() { return silentConcealedSamples; }
	@JsonProperty("silentConcealedSamples")
	public void setSilentConcealedSamples(double value) { this.silentConcealedSamples = value; }

	@JsonProperty("timestamp")
	public String getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(String value) { this.timestamp = value; }

	@JsonProperty("totalAudioEnergy")
	public double getTotalAudioEnergy() { return totalAudioEnergy; }
	@JsonProperty("totalAudioEnergy")
	public void setTotalAudioEnergy(double value) { this.totalAudioEnergy = value; }

	@JsonProperty("totalSamplesDuration")
	public double getTotalSamplesDuration() { return totalSamplesDuration; }
	@JsonProperty("totalSamplesDuration")
	public void setTotalSamplesDuration(double value) { this.totalSamplesDuration = value; }

	@JsonProperty("totalSamplesReceived")
	public double getTotalSamplesReceived() { return totalSamplesReceived; }
	@JsonProperty("totalSamplesReceived")
	public void setTotalSamplesReceived(double value) { this.totalSamplesReceived = value; }

	@JsonProperty("trackIdentifier")
	public String getTrackIdentifier() { return trackIdentifier; }
	@JsonProperty("trackIdentifier")
	public void setTrackIdentifier(String value) { this.trackIdentifier = value; }

	@JsonProperty("type")
	public TrackType getType() { return type; }
	@JsonProperty("type")
	public void setType(TrackType value) { this.type = value; }
}
