package org.observertc.webrtc.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RTCStats {
	private Double audioLevel;
	private Double framesPerSecond;
	private Double height;
	private String id;
	private Kind kind;
	private Timestamp timestamp;
	private Double totalAudioEnergy;
	private Double totalSamplesDuration;
	private String trackIdentifier;
	private RTCStatsType type;
	private Double width;
	private Double availableOutgoingBitrate;
	private Double bytesReceived;
	private Double bytesSent;
	private Double consentRequestsSent;
	private Double currentRoundTripTime;
	private String localCandidateID;
	private Boolean nominated;
	private Double priority;
	private String remoteCandidateID;
	private Double requestsReceived;
	private Double requestsSent;
	private Double responsesReceived;
	private Double responsesSent;
	private State state;
	private Double totalRoundTripTime;
	private String transportID;
	private Boolean writable;
	private Double concealedSamples;
	private Double concealmentEvents;
	private Boolean detached;
	private Boolean ended;
	private Double frameHeight;
	private Double framesDecoded;
	private Double framesDropped;
	private Double framesReceived;
	private Double framesSent;
	private Double frameWidth;
	private Double hugeFramesSent;
	private Double insertedSamplesForDeceleration;
	private Double jitterBufferDelay;
	private Double jitterBufferEmittedCount;
	private String mediaSourceID;
	private Boolean remoteSource;
	private Double removedSamplesForAcceleration;
	private Double silentConcealedSamples;
	private Double totalSamplesReceived;
	private String codecID;
	private String encoderImplementation;
	private Double firCount;
	private Double framesEncoded;
	private Double headerBytesSent;
	private Boolean isRemote;
	private Double keyFramesEncoded;
	private Kind mediaType;
	private Double nackCount;
	private Double packetsSent;
	private Double pliCount;
	private Double qpSum;
	private QualityLimitationReason qualityLimitationReason;
	private Double qualityLimitationResolutionChanges;
	private String remoteID;
	private Double retransmittedBytesSent;
	private Double retransmittedPacketsSent;
	private Double ssrc;
	private Double totalEncodedBytesTarget;
	private Double totalEncodeTime;
	private Double totalPacketSendDelay;
	private String trackID;
	private String decoderImplementation;
	private Double estimatedPlayoutTimestamp;
	private Double fecPacketsDiscarded;
	private Double fecPacketsReceived;
	private Double headerBytesReceived;
	private Double jitter;
	private Double keyFramesDecoded;
	private Double lastPacketReceivedTimestamp;
	private Double packetsLost;
	private Double packetsReceived;
	private Double totalDecodeTime;
	private Double totalInterFrameDelay;
	private Double totalSquaredInterFrameDelay;
	private String localID;
	private Double roundTripTime;

	@JsonProperty("audioLevel")
	public Double getAudioLevel() { return audioLevel; }
	@JsonProperty("audioLevel")
	public void setAudioLevel(Double value) { this.audioLevel = value; }

	@JsonProperty("framesPerSecond")
	public Double getFramesPerSecond() { return framesPerSecond; }
	@JsonProperty("framesPerSecond")
	public void setFramesPerSecond(Double value) { this.framesPerSecond = value; }

	@JsonProperty("height")
	public Double getHeight() { return height; }
	@JsonProperty("height")
	public void setHeight(Double value) { this.height = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("kind")
	public Kind getKind() { return kind; }
	@JsonProperty("kind")
	public void setKind(Kind value) { this.kind = value; }

	@JsonProperty("timestamp")
	public Timestamp getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(Timestamp value) { this.timestamp = value; }

	@JsonProperty("totalAudioEnergy")
	public Double getTotalAudioEnergy() { return totalAudioEnergy; }
	@JsonProperty("totalAudioEnergy")
	public void setTotalAudioEnergy(Double value) { this.totalAudioEnergy = value; }

	@JsonProperty("totalSamplesDuration")
	public Double getTotalSamplesDuration() { return totalSamplesDuration; }
	@JsonProperty("totalSamplesDuration")
	public void setTotalSamplesDuration(Double value) { this.totalSamplesDuration = value; }

	@JsonProperty("trackIdentifier")
	public String getTrackIdentifier() { return trackIdentifier; }
	@JsonProperty("trackIdentifier")
	public void setTrackIdentifier(String value) { this.trackIdentifier = value; }

	@JsonProperty("type")
	public RTCStatsType getType() { return type; }
	@JsonProperty("type")
	public void setType(RTCStatsType value) { this.type = value; }

	@JsonProperty("width")
	public Double getWidth() { return width; }
	@JsonProperty("width")
	public void setWidth(Double value) { this.width = value; }

	@JsonProperty("availableOutgoingBitrate")
	public Double getAvailableOutgoingBitrate() { return availableOutgoingBitrate; }
	@JsonProperty("availableOutgoingBitrate")
	public void setAvailableOutgoingBitrate(Double value) { this.availableOutgoingBitrate = value; }

	@JsonProperty("bytesReceived")
	public Double getBytesReceived() { return bytesReceived; }
	@JsonProperty("bytesReceived")
	public void setBytesReceived(Double value) { this.bytesReceived = value; }

	@JsonProperty("bytesSent")
	public Double getBytesSent() { return bytesSent; }
	@JsonProperty("bytesSent")
	public void setBytesSent(Double value) { this.bytesSent = value; }

	@JsonProperty("consentRequestsSent")
	public Double getConsentRequestsSent() { return consentRequestsSent; }
	@JsonProperty("consentRequestsSent")
	public void setConsentRequestsSent(Double value) { this.consentRequestsSent = value; }

	@JsonProperty("currentRoundTripTime")
	public Double getCurrentRoundTripTime() { return currentRoundTripTime; }
	@JsonProperty("currentRoundTripTime")
	public void setCurrentRoundTripTime(Double value) { this.currentRoundTripTime = value; }

	@JsonProperty("localCandidateId")
	public String getLocalCandidateID() { return localCandidateID; }
	@JsonProperty("localCandidateId")
	public void setLocalCandidateID(String value) { this.localCandidateID = value; }

	@JsonProperty("nominated")
	public Boolean getNominated() { return nominated; }
	@JsonProperty("nominated")
	public void setNominated(Boolean value) { this.nominated = value; }

	@JsonProperty("priority")
	public Double getPriority() { return priority; }
	@JsonProperty("priority")
	public void setPriority(Double value) { this.priority = value; }

	@JsonProperty("remoteCandidateId")
	public String getRemoteCandidateID() { return remoteCandidateID; }
	@JsonProperty("remoteCandidateId")
	public void setRemoteCandidateID(String value) { this.remoteCandidateID = value; }

	@JsonProperty("requestsReceived")
	public Double getRequestsReceived() { return requestsReceived; }
	@JsonProperty("requestsReceived")
	public void setRequestsReceived(Double value) { this.requestsReceived = value; }

	@JsonProperty("requestsSent")
	public Double getRequestsSent() { return requestsSent; }
	@JsonProperty("requestsSent")
	public void setRequestsSent(Double value) { this.requestsSent = value; }

	@JsonProperty("responsesReceived")
	public Double getResponsesReceived() { return responsesReceived; }
	@JsonProperty("responsesReceived")
	public void setResponsesReceived(Double value) { this.responsesReceived = value; }

	@JsonProperty("responsesSent")
	public Double getResponsesSent() { return responsesSent; }
	@JsonProperty("responsesSent")
	public void setResponsesSent(Double value) { this.responsesSent = value; }

	@JsonProperty("state")
	public State getState() { return state; }
	@JsonProperty("state")
	public void setState(State value) { this.state = value; }

	@JsonProperty("totalRoundTripTime")
	public Double getTotalRoundTripTime() { return totalRoundTripTime; }
	@JsonProperty("totalRoundTripTime")
	public void setTotalRoundTripTime(Double value) { this.totalRoundTripTime = value; }

	@JsonProperty("transportId")
	public String getTransportID() { return transportID; }
	@JsonProperty("transportId")
	public void setTransportID(String value) { this.transportID = value; }

	@JsonProperty("writable")
	public Boolean getWritable() { return writable; }
	@JsonProperty("writable")
	public void setWritable(Boolean value) { this.writable = value; }

	@JsonProperty("concealedSamples")
	public Double getConcealedSamples() { return concealedSamples; }
	@JsonProperty("concealedSamples")
	public void setConcealedSamples(Double value) { this.concealedSamples = value; }

	@JsonProperty("concealmentEvents")
	public Double getConcealmentEvents() { return concealmentEvents; }
	@JsonProperty("concealmentEvents")
	public void setConcealmentEvents(Double value) { this.concealmentEvents = value; }

	@JsonProperty("detached")
	public Boolean getDetached() { return detached; }
	@JsonProperty("detached")
	public void setDetached(Boolean value) { this.detached = value; }

	@JsonProperty("ended")
	public Boolean getEnded() { return ended; }
	@JsonProperty("ended")
	public void setEnded(Boolean value) { this.ended = value; }

	@JsonProperty("frameHeight")
	public Double getFrameHeight() { return frameHeight; }
	@JsonProperty("frameHeight")
	public void setFrameHeight(Double value) { this.frameHeight = value; }

	@JsonProperty("framesDecoded")
	public Double getFramesDecoded() { return framesDecoded; }
	@JsonProperty("framesDecoded")
	public void setFramesDecoded(Double value) { this.framesDecoded = value; }

	@JsonProperty("framesDropped")
	public Double getFramesDropped() { return framesDropped; }
	@JsonProperty("framesDropped")
	public void setFramesDropped(Double value) { this.framesDropped = value; }

	@JsonProperty("framesReceived")
	public Double getFramesReceived() { return framesReceived; }
	@JsonProperty("framesReceived")
	public void setFramesReceived(Double value) { this.framesReceived = value; }

	@JsonProperty("framesSent")
	public Double getFramesSent() { return framesSent; }
	@JsonProperty("framesSent")
	public void setFramesSent(Double value) { this.framesSent = value; }

	@JsonProperty("frameWidth")
	public Double getFrameWidth() { return frameWidth; }
	@JsonProperty("frameWidth")
	public void setFrameWidth(Double value) { this.frameWidth = value; }

	@JsonProperty("hugeFramesSent")
	public Double getHugeFramesSent() { return hugeFramesSent; }
	@JsonProperty("hugeFramesSent")
	public void setHugeFramesSent(Double value) { this.hugeFramesSent = value; }

	@JsonProperty("insertedSamplesForDeceleration")
	public Double getInsertedSamplesForDeceleration() { return insertedSamplesForDeceleration; }
	@JsonProperty("insertedSamplesForDeceleration")
	public void setInsertedSamplesForDeceleration(Double value) { this.insertedSamplesForDeceleration = value; }

	@JsonProperty("jitterBufferDelay")
	public Double getJitterBufferDelay() { return jitterBufferDelay; }
	@JsonProperty("jitterBufferDelay")
	public void setJitterBufferDelay(Double value) { this.jitterBufferDelay = value; }

	@JsonProperty("jitterBufferEmittedCount")
	public Double getJitterBufferEmittedCount() { return jitterBufferEmittedCount; }
	@JsonProperty("jitterBufferEmittedCount")
	public void setJitterBufferEmittedCount(Double value) { this.jitterBufferEmittedCount = value; }

	@JsonProperty("mediaSourceId")
	public String getMediaSourceID() { return mediaSourceID; }
	@JsonProperty("mediaSourceId")
	public void setMediaSourceID(String value) { this.mediaSourceID = value; }

	@JsonProperty("remoteSource")
	public Boolean getRemoteSource() { return remoteSource; }
	@JsonProperty("remoteSource")
	public void setRemoteSource(Boolean value) { this.remoteSource = value; }

	@JsonProperty("removedSamplesForAcceleration")
	public Double getRemovedSamplesForAcceleration() { return removedSamplesForAcceleration; }
	@JsonProperty("removedSamplesForAcceleration")
	public void setRemovedSamplesForAcceleration(Double value) { this.removedSamplesForAcceleration = value; }

	@JsonProperty("silentConcealedSamples")
	public Double getSilentConcealedSamples() { return silentConcealedSamples; }
	@JsonProperty("silentConcealedSamples")
	public void setSilentConcealedSamples(Double value) { this.silentConcealedSamples = value; }

	@JsonProperty("totalSamplesReceived")
	public Double getTotalSamplesReceived() { return totalSamplesReceived; }
	@JsonProperty("totalSamplesReceived")
	public void setTotalSamplesReceived(Double value) { this.totalSamplesReceived = value; }

	@JsonProperty("codecId")
	public String getCodecID() { return codecID; }
	@JsonProperty("codecId")
	public void setCodecID(String value) { this.codecID = value; }

	@JsonProperty("encoderImplementation")
	public String getEncoderImplementation() { return encoderImplementation; }
	@JsonProperty("encoderImplementation")
	public void setEncoderImplementation(String value) { this.encoderImplementation = value; }

	@JsonProperty("firCount")
	public Double getFirCount() { return firCount; }
	@JsonProperty("firCount")
	public void setFirCount(Double value) { this.firCount = value; }

	@JsonProperty("framesEncoded")
	public Double getFramesEncoded() { return framesEncoded; }
	@JsonProperty("framesEncoded")
	public void setFramesEncoded(Double value) { this.framesEncoded = value; }

	@JsonProperty("headerBytesSent")
	public Double getHeaderBytesSent() { return headerBytesSent; }
	@JsonProperty("headerBytesSent")
	public void setHeaderBytesSent(Double value) { this.headerBytesSent = value; }

	@JsonProperty("isRemote")
	public Boolean getIsRemote() { return isRemote; }
	@JsonProperty("isRemote")
	public void setIsRemote(Boolean value) { this.isRemote = value; }

	@JsonProperty("keyFramesEncoded")
	public Double getKeyFramesEncoded() { return keyFramesEncoded; }
	@JsonProperty("keyFramesEncoded")
	public void setKeyFramesEncoded(Double value) { this.keyFramesEncoded = value; }

	@JsonProperty("mediaType")
	public Kind getMediaType() { return mediaType; }
	@JsonProperty("mediaType")
	public void setMediaType(Kind value) { this.mediaType = value; }

	@JsonProperty("nackCount")
	public Double getNACKCount() { return nackCount; }
	@JsonProperty("nackCount")
	public void setNACKCount(Double value) { this.nackCount = value; }

	@JsonProperty("packetsSent")
	public Double getPacketsSent() { return packetsSent; }
	@JsonProperty("packetsSent")
	public void setPacketsSent(Double value) { this.packetsSent = value; }

	@JsonProperty("pliCount")
	public Double getPliCount() { return pliCount; }
	@JsonProperty("pliCount")
	public void setPliCount(Double value) { this.pliCount = value; }

	@JsonProperty("qpSum")
	public Double getQpSum() { return qpSum; }
	@JsonProperty("qpSum")
	public void setQpSum(Double value) { this.qpSum = value; }

	@JsonProperty("qualityLimitationReason")
	public QualityLimitationReason getQualityLimitationReason() { return qualityLimitationReason; }
	@JsonProperty("qualityLimitationReason")
	public void setQualityLimitationReason(QualityLimitationReason value) { this.qualityLimitationReason = value; }

	@JsonProperty("qualityLimitationResolutionChanges")
	public Double getQualityLimitationResolutionChanges() { return qualityLimitationResolutionChanges; }
	@JsonProperty("qualityLimitationResolutionChanges")
	public void setQualityLimitationResolutionChanges(Double value) { this.qualityLimitationResolutionChanges = value; }

	@JsonProperty("remoteId")
	public String getRemoteID() { return remoteID; }
	@JsonProperty("remoteId")
	public void setRemoteID(String value) { this.remoteID = value; }

	@JsonProperty("retransmittedBytesSent")
	public Double getRetransmittedBytesSent() { return retransmittedBytesSent; }
	@JsonProperty("retransmittedBytesSent")
	public void setRetransmittedBytesSent(Double value) { this.retransmittedBytesSent = value; }

	@JsonProperty("retransmittedPacketsSent")
	public Double getRetransmittedPacketsSent() { return retransmittedPacketsSent; }
	@JsonProperty("retransmittedPacketsSent")
	public void setRetransmittedPacketsSent(Double value) { this.retransmittedPacketsSent = value; }

	@JsonProperty("ssrc")
	public Double getSsrc() { return ssrc; }
	@JsonProperty("ssrc")
	public void setSsrc(Double value) { this.ssrc = value; }

	@JsonProperty("totalEncodedBytesTarget")
	public Double getTotalEncodedBytesTarget() { return totalEncodedBytesTarget; }
	@JsonProperty("totalEncodedBytesTarget")
	public void setTotalEncodedBytesTarget(Double value) { this.totalEncodedBytesTarget = value; }

	@JsonProperty("totalEncodeTime")
	public Double getTotalEncodeTime() { return totalEncodeTime; }
	@JsonProperty("totalEncodeTime")
	public void setTotalEncodeTime(Double value) { this.totalEncodeTime = value; }

	@JsonProperty("totalPacketSendDelay")
	public Double getTotalPacketSendDelay() { return totalPacketSendDelay; }
	@JsonProperty("totalPacketSendDelay")
	public void setTotalPacketSendDelay(Double value) { this.totalPacketSendDelay = value; }

	@JsonProperty("trackId")
	public String getTrackID() { return trackID; }
	@JsonProperty("trackId")
	public void setTrackID(String value) { this.trackID = value; }

	@JsonProperty("decoderImplementation")
	public String getDecoderImplementation() { return decoderImplementation; }
	@JsonProperty("decoderImplementation")
	public void setDecoderImplementation(String value) { this.decoderImplementation = value; }

	@JsonProperty("estimatedPlayoutTimestamp")
	public Double getEstimatedPlayoutTimestamp() { return estimatedPlayoutTimestamp; }
	@JsonProperty("estimatedPlayoutTimestamp")
	public void setEstimatedPlayoutTimestamp(Double value) { this.estimatedPlayoutTimestamp = value; }

	@JsonProperty("fecPacketsDiscarded")
	public Double getFECPacketsDiscarded() { return fecPacketsDiscarded; }
	@JsonProperty("fecPacketsDiscarded")
	public void setFECPacketsDiscarded(Double value) { this.fecPacketsDiscarded = value; }

	@JsonProperty("fecPacketsReceived")
	public Double getFECPacketsReceived() { return fecPacketsReceived; }
	@JsonProperty("fecPacketsReceived")
	public void setFECPacketsReceived(Double value) { this.fecPacketsReceived = value; }

	@JsonProperty("headerBytesReceived")
	public Double getHeaderBytesReceived() { return headerBytesReceived; }
	@JsonProperty("headerBytesReceived")
	public void setHeaderBytesReceived(Double value) { this.headerBytesReceived = value; }

	@JsonProperty("jitter")
	public Double getJitter() { return jitter; }
	@JsonProperty("jitter")
	public void setJitter(Double value) { this.jitter = value; }

	@JsonProperty("keyFramesDecoded")
	public Double getKeyFramesDecoded() { return keyFramesDecoded; }
	@JsonProperty("keyFramesDecoded")
	public void setKeyFramesDecoded(Double value) { this.keyFramesDecoded = value; }

	@JsonProperty("lastPacketReceivedTimestamp")
	public Double getLastPacketReceivedTimestamp() { return lastPacketReceivedTimestamp; }
	@JsonProperty("lastPacketReceivedTimestamp")
	public void setLastPacketReceivedTimestamp(Double value) { this.lastPacketReceivedTimestamp = value; }

	@JsonProperty("packetsLost")
	public Double getPacketsLost() { return packetsLost; }
	@JsonProperty("packetsLost")
	public void setPacketsLost(Double value) { this.packetsLost = value; }

	@JsonProperty("packetsReceived")
	public Double getPacketsReceived() { return packetsReceived; }
	@JsonProperty("packetsReceived")
	public void setPacketsReceived(Double value) { this.packetsReceived = value; }

	@JsonProperty("totalDecodeTime")
	public Double getTotalDecodeTime() { return totalDecodeTime; }
	@JsonProperty("totalDecodeTime")
	public void setTotalDecodeTime(Double value) { this.totalDecodeTime = value; }

	@JsonProperty("totalInterFrameDelay")
	public Double getTotalInterFrameDelay() { return totalInterFrameDelay; }
	@JsonProperty("totalInterFrameDelay")
	public void setTotalInterFrameDelay(Double value) { this.totalInterFrameDelay = value; }

	@JsonProperty("totalSquaredInterFrameDelay")
	public Double getTotalSquaredInterFrameDelay() { return totalSquaredInterFrameDelay; }
	@JsonProperty("totalSquaredInterFrameDelay")
	public void setTotalSquaredInterFrameDelay(Double value) { this.totalSquaredInterFrameDelay = value; }

	@JsonProperty("localId")
	public String getLocalID() { return localID; }
	@JsonProperty("localId")
	public void setLocalID(String value) { this.localID = value; }

	@JsonProperty("roundTripTime")
	public Double getRoundTripTime() { return roundTripTime; }
	@JsonProperty("roundTripTime")
	public void setRoundTripTime(Double value) { this.roundTripTime = value; }
}
