package com.observertc.gatekeeper.webrtcstat.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OutboundRTP {
	private double bytesSent;
	private String codecID;
	private String encoderImplementation;
	private double firCount;
	private double framesEncoded;
	private double headerBytesSent;
	private String id;
	private boolean isRemote;
	private double keyFramesEncoded;
	private Kind kind;
	private String mediaSourceID;
	private Kind mediaType;
	private double nackCount;
	private double packetsSent;
	private double pliCount;
	private double qpSum;
	private QualityLimitationReason qualityLimitationReason;
	private double qualityLimitationResolutionChanges;
	private String remoteID;
	private double retransmittedBytesSent;
	private double retransmittedPacketsSent;
	private double ssrc;
	private double timestamp;
	private double totalEncodedBytesTarget;
	private double totalEncodeTime;
	private double totalPacketSendDelay;
	private String trackID;
	private String transportID;
	private OutboundRTPType type;

	@JsonProperty("bytesSent")
	public double getBytesSent() { return bytesSent; }
	@JsonProperty("bytesSent")
	public void setBytesSent(double value) { this.bytesSent = value; }

	@JsonProperty("codecId")
	public String getCodecID() { return codecID; }
	@JsonProperty("codecId")
	public void setCodecID(String value) { this.codecID = value; }

	@JsonProperty("encoderImplementation")
	public String getEncoderImplementation() { return encoderImplementation; }
	@JsonProperty("encoderImplementation")
	public void setEncoderImplementation(String value) { this.encoderImplementation = value; }

	@JsonProperty("firCount")
	public double getFirCount() { return firCount; }
	@JsonProperty("firCount")
	public void setFirCount(double value) { this.firCount = value; }

	@JsonProperty("framesEncoded")
	public double getFramesEncoded() { return framesEncoded; }
	@JsonProperty("framesEncoded")
	public void setFramesEncoded(double value) { this.framesEncoded = value; }

	@JsonProperty("headerBytesSent")
	public double getHeaderBytesSent() { return headerBytesSent; }
	@JsonProperty("headerBytesSent")
	public void setHeaderBytesSent(double value) { this.headerBytesSent = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("isRemote")
	public boolean getIsRemote() { return isRemote; }
	@JsonProperty("isRemote")
	public void setIsRemote(boolean value) { this.isRemote = value; }

	@JsonProperty("keyFramesEncoded")
	public double getKeyFramesEncoded() { return keyFramesEncoded; }
	@JsonProperty("keyFramesEncoded")
	public void setKeyFramesEncoded(double value) { this.keyFramesEncoded = value; }

	@JsonProperty("kind")
	public Kind getKind() { return kind; }
	@JsonProperty("kind")
	public void setKind(Kind value) { this.kind = value; }

	@JsonProperty("mediaSourceId")
	public String getMediaSourceID() { return mediaSourceID; }
	@JsonProperty("mediaSourceId")
	public void setMediaSourceID(String value) { this.mediaSourceID = value; }

	@JsonProperty("mediaType")
	public Kind getMediaType() { return mediaType; }
	@JsonProperty("mediaType")
	public void setMediaType(Kind value) { this.mediaType = value; }

	@JsonProperty("nackCount")
	public double getNACKCount() { return nackCount; }
	@JsonProperty("nackCount")
	public void setNACKCount(double value) { this.nackCount = value; }

	@JsonProperty("packetsSent")
	public double getPacketsSent() { return packetsSent; }
	@JsonProperty("packetsSent")
	public void setPacketsSent(double value) { this.packetsSent = value; }

	@JsonProperty("pliCount")
	public double getPliCount() { return pliCount; }
	@JsonProperty("pliCount")
	public void setPliCount(double value) { this.pliCount = value; }

	@JsonProperty("qpSum")
	public double getQpSum() { return qpSum; }
	@JsonProperty("qpSum")
	public void setQpSum(double value) { this.qpSum = value; }

	@JsonProperty("qualityLimitationReason")
	public QualityLimitationReason getQualityLimitationReason() { return qualityLimitationReason; }
	@JsonProperty("qualityLimitationReason")
	public void setQualityLimitationReason(QualityLimitationReason value) { this.qualityLimitationReason = value; }

	@JsonProperty("qualityLimitationResolutionChanges")
	public double getQualityLimitationResolutionChanges() { return qualityLimitationResolutionChanges; }
	@JsonProperty("qualityLimitationResolutionChanges")
	public void setQualityLimitationResolutionChanges(double value) { this.qualityLimitationResolutionChanges = value; }

	@JsonProperty("remoteId")
	public String getRemoteID() { return remoteID; }
	@JsonProperty("remoteId")
	public void setRemoteID(String value) { this.remoteID = value; }

	@JsonProperty("retransmittedBytesSent")
	public double getRetransmittedBytesSent() { return retransmittedBytesSent; }
	@JsonProperty("retransmittedBytesSent")
	public void setRetransmittedBytesSent(double value) { this.retransmittedBytesSent = value; }

	@JsonProperty("retransmittedPacketsSent")
	public double getRetransmittedPacketsSent() { return retransmittedPacketsSent; }
	@JsonProperty("retransmittedPacketsSent")
	public void setRetransmittedPacketsSent(double value) { this.retransmittedPacketsSent = value; }

	@JsonProperty("ssrc")
	public double getSsrc() { return ssrc; }
	@JsonProperty("ssrc")
	public void setSsrc(double value) { this.ssrc = value; }

	@JsonProperty("timestamp")
	public double getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(double value) { this.timestamp = value; }

	@JsonProperty("totalEncodedBytesTarget")
	public double getTotalEncodedBytesTarget() { return totalEncodedBytesTarget; }
	@JsonProperty("totalEncodedBytesTarget")
	public void setTotalEncodedBytesTarget(double value) { this.totalEncodedBytesTarget = value; }

	@JsonProperty("totalEncodeTime")
	public double getTotalEncodeTime() { return totalEncodeTime; }
	@JsonProperty("totalEncodeTime")
	public void setTotalEncodeTime(double value) { this.totalEncodeTime = value; }

	@JsonProperty("totalPacketSendDelay")
	public double getTotalPacketSendDelay() { return totalPacketSendDelay; }
	@JsonProperty("totalPacketSendDelay")
	public void setTotalPacketSendDelay(double value) { this.totalPacketSendDelay = value; }

	@JsonProperty("trackId")
	public String getTrackID() { return trackID; }
	@JsonProperty("trackId")
	public void setTrackID(String value) { this.trackID = value; }

	@JsonProperty("transportId")
	public String getTransportID() { return transportID; }
	@JsonProperty("transportId")
	public void setTransportID(String value) { this.transportID = value; }

	@JsonProperty("type")
	public OutboundRTPType getType() { return type; }
	@JsonProperty("type")
	public void setType(OutboundRTPType value) { this.type = value; }
}
