package org.observertc.webrtc.service.dto.webextrapp;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InboundRTP {
	private double bytesReceived;
	private String codecID;
	private String decoderImplementation;
	private double estimatedPlayoutTimestamp;
	private double fecPacketsDiscarded;
	private double fecPacketsReceived;
	private double firCount;
	private double framesDecoded;
	private double headerBytesReceived;
	private String id;
	private boolean isRemote;
	private double jitter;
	private double keyFramesDecoded;
	private Kind kind;
	private double lastPacketReceivedTimestamp;
	private Kind mediaType;
	private double nackCount;
	private double packetsLost;
	private double packetsReceived;
	private double pliCount;
	private double qpSum;
	private double ssrc;
	private double timestamp;
	private double totalDecodeTime;
	private double totalInterFrameDelay;
	private double totalSquaredInterFrameDelay;
	private String trackID;
	private String transportID;
	private InboundRTPType type;

	@JsonProperty("bytesReceived")
	public double getBytesReceived() { return bytesReceived; }
	@JsonProperty("bytesReceived")
	public void setBytesReceived(double value) { this.bytesReceived = value; }

	@JsonProperty("codecId")
	public String getCodecID() { return codecID; }
	@JsonProperty("codecId")
	public void setCodecID(String value) { this.codecID = value; }

	@JsonProperty("decoderImplementation")
	public String getDecoderImplementation() { return decoderImplementation; }
	@JsonProperty("decoderImplementation")
	public void setDecoderImplementation(String value) { this.decoderImplementation = value; }

	@JsonProperty("estimatedPlayoutTimestamp")
	public double getEstimatedPlayoutTimestamp() { return estimatedPlayoutTimestamp; }
	@JsonProperty("estimatedPlayoutTimestamp")
	public void setEstimatedPlayoutTimestamp(double value) { this.estimatedPlayoutTimestamp = value; }

	@JsonProperty("fecPacketsDiscarded")
	public double getFECPacketsDiscarded() { return fecPacketsDiscarded; }
	@JsonProperty("fecPacketsDiscarded")
	public void setFECPacketsDiscarded(double value) { this.fecPacketsDiscarded = value; }

	@JsonProperty("fecPacketsReceived")
	public double getFECPacketsReceived() { return fecPacketsReceived; }
	@JsonProperty("fecPacketsReceived")
	public void setFECPacketsReceived(double value) { this.fecPacketsReceived = value; }

	@JsonProperty("firCount")
	public double getFirCount() { return firCount; }
	@JsonProperty("firCount")
	public void setFirCount(double value) { this.firCount = value; }

	@JsonProperty("framesDecoded")
	public double getFramesDecoded() { return framesDecoded; }
	@JsonProperty("framesDecoded")
	public void setFramesDecoded(double value) { this.framesDecoded = value; }

	@JsonProperty("headerBytesReceived")
	public double getHeaderBytesReceived() { return headerBytesReceived; }
	@JsonProperty("headerBytesReceived")
	public void setHeaderBytesReceived(double value) { this.headerBytesReceived = value; }

	@JsonProperty("id")
	public String getID() { return id; }
	@JsonProperty("id")
	public void setID(String value) { this.id = value; }

	@JsonProperty("isRemote")
	public boolean getIsRemote() { return isRemote; }
	@JsonProperty("isRemote")
	public void setIsRemote(boolean value) { this.isRemote = value; }

	@JsonProperty("jitter")
	public double getJitter() { return jitter; }
	@JsonProperty("jitter")
	public void setJitter(double value) { this.jitter = value; }

	@JsonProperty("keyFramesDecoded")
	public double getKeyFramesDecoded() { return keyFramesDecoded; }
	@JsonProperty("keyFramesDecoded")
	public void setKeyFramesDecoded(double value) { this.keyFramesDecoded = value; }

	@JsonProperty("kind")
	public Kind getKind() { return kind; }
	@JsonProperty("kind")
	public void setKind(Kind value) { this.kind = value; }

	@JsonProperty("lastPacketReceivedTimestamp")
	public double getLastPacketReceivedTimestamp() { return lastPacketReceivedTimestamp; }
	@JsonProperty("lastPacketReceivedTimestamp")
	public void setLastPacketReceivedTimestamp(double value) { this.lastPacketReceivedTimestamp = value; }

	@JsonProperty("mediaType")
	public Kind getMediaType() { return mediaType; }
	@JsonProperty("mediaType")
	public void setMediaType(Kind value) { this.mediaType = value; }

	@JsonProperty("nackCount")
	public double getNACKCount() { return nackCount; }
	@JsonProperty("nackCount")
	public void setNACKCount(double value) { this.nackCount = value; }

	@JsonProperty("packetsLost")
	public double getPacketsLost() { return packetsLost; }
	@JsonProperty("packetsLost")
	public void setPacketsLost(double value) { this.packetsLost = value; }

	@JsonProperty("packetsReceived")
	public double getPacketsReceived() { return packetsReceived; }
	@JsonProperty("packetsReceived")
	public void setPacketsReceived(double value) { this.packetsReceived = value; }

	@JsonProperty("pliCount")
	public double getPliCount() { return pliCount; }
	@JsonProperty("pliCount")
	public void setPliCount(double value) { this.pliCount = value; }

	@JsonProperty("qpSum")
	public double getQpSum() { return qpSum; }
	@JsonProperty("qpSum")
	public void setQpSum(double value) { this.qpSum = value; }

	@JsonProperty("ssrc")
	public double getSsrc() { return ssrc; }
	@JsonProperty("ssrc")
	public void setSsrc(double value) { this.ssrc = value; }

	@JsonProperty("timestamp")
	public double getTimestamp() { return timestamp; }
	@JsonProperty("timestamp")
	public void setTimestamp(double value) { this.timestamp = value; }

	@JsonProperty("totalDecodeTime")
	public double getTotalDecodeTime() { return totalDecodeTime; }
	@JsonProperty("totalDecodeTime")
	public void setTotalDecodeTime(double value) { this.totalDecodeTime = value; }

	@JsonProperty("totalInterFrameDelay")
	public double getTotalInterFrameDelay() { return totalInterFrameDelay; }
	@JsonProperty("totalInterFrameDelay")
	public void setTotalInterFrameDelay(double value) { this.totalInterFrameDelay = value; }

	@JsonProperty("totalSquaredInterFrameDelay")
	public double getTotalSquaredInterFrameDelay() { return totalSquaredInterFrameDelay; }
	@JsonProperty("totalSquaredInterFrameDelay")
	public void setTotalSquaredInterFrameDelay(double value) { this.totalSquaredInterFrameDelay = value; }

	@JsonProperty("trackId")
	public String getTrackID() { return trackID; }
	@JsonProperty("trackId")
	public void setTrackID(String value) { this.trackID = value; }

	@JsonProperty("transportId")
	public String getTransportID() { return transportID; }
	@JsonProperty("transportId")
	public void setTransportID(String value) { this.transportID = value; }

	@JsonProperty("type")
	public InboundRTPType getType() { return type; }
	@JsonProperty("type")
	public void setType(InboundRTPType value) { this.type = value; }
}
