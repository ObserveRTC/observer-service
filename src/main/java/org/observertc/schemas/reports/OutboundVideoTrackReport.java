package org.observertc.schemas.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* A Report created for Outbound Video Tracks. A combination of Video source, Codec metadata carrying outbound and remote inbound RTP stat measurements
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class OutboundVideoTrackReport {
	public static final String VERSION="2.0.0-beta.44";
	public static Builder newBuilder() {
		return new Builder();
	}
	/**
	* The unique identifier of the service
	*/
	@JsonProperty("serviceId")
	public String serviceId;
	/**
	* The media unit id the report belongs to
	*/
	@JsonProperty("mediaUnitId")
	public String mediaUnitId;
	/**
	* The marker the originated sample is reported with
	*/
	@JsonProperty("marker")
	public String marker;
	/**
	* The timestamp when the corresponded data is generated for the report (UTC Epoch in ms)
	*/
	@JsonProperty("timestamp")
	public Long timestamp;
	/**
	* The generated unique identifier of the call
	*/
	@JsonProperty("callId")
	public String callId;
	/**
	* webrtc app provided room id
	*/
	@JsonProperty("roomId")
	public String roomId;
	/**
	* The generated unique identifier of the client
	*/
	@JsonProperty("clientId")
	public String clientId;
	/**
	* webrtc app provided user identifier
	*/
	@JsonProperty("userId")
	public String userId;
	/**
	* The unique identifier of the peer connection
	*/
	@JsonProperty("peerConnectionId")
	public String peerConnectionId;
	/**
	* The webrtc app provided label the peer connection is labeled with
	*/
	@JsonProperty("label")
	public String label;
	/**
	* The id of the track
	*/
	@JsonProperty("trackId")
	public String trackId;
	/**
	* The id of the Sfu stream corresponds to the outbound track
	*/
	@JsonProperty("sfuStreamId")
	public String sfuStreamId;
	/**
	* The sequence number of the sample the report is generated from
	*/
	@JsonProperty("sampleSeq")
	public Integer sampleSeq;
	/**
	* The RTP SSRC field
	*/
	@JsonProperty("ssrc")
	public Long ssrc;
	/**
	* The total number of packets sent on the corresponded synchronization source
	*/
	@JsonProperty("packetsSent")
	public Integer packetsSent;
	/**
	* The total number of bytes sent on the corresponded synchronization source
	*/
	@JsonProperty("bytesSent")
	public Long bytesSent;
	/**
	*  The rid encoding parameter of the corresponded synchronization source
	*/
	@JsonProperty("rid")
	public String rid;
	/**
	*  the timestamp the last packet was sent. (UTC epoch in ms)
	*/
	@JsonProperty("lastPacketSentTimestamp")
	public Long lastPacketSentTimestamp;
	/**
	* Total number of RTP header and padding bytes sent over the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("headerBytesSent")
	public Long headerBytesSent;
	/**
	* Total number of RTP packets discarded at sender side over the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("packetsDiscardedOnSend")
	public Integer packetsDiscardedOnSend;
	/**
	* Total number of RTP bytes discarded at sender side over the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("bytesDiscardedOnSend")
	public Long bytesDiscardedOnSend;
	/**
	* Total number of FEC packets sent over the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("fecPacketsSent")
	public Integer fecPacketsSent;
	/**
	* Total number of retransmitted packets sent over the corresponding synchronization source (ssrc).
	*/
	@JsonProperty("retransmittedPacketsSent")
	public Integer retransmittedPacketsSent;
	/**
	* Total number of retransmitted bytes sent over the corresponded synchronization source (ssrc).
	*/
	@JsonProperty("retransmittedBytesSent")
	public Long retransmittedBytesSent;
	/**
	* Reflects the current encoder target in bits per second.
	*/
	@JsonProperty("targetBitrate")
	public Integer targetBitrate;
	/**
	* The total number of bytes of RTP coherent frames encoded completly depending on the frame size the encoder targets
	*/
	@JsonProperty("totalEncodedBytesTarget")
	public Long totalEncodedBytesTarget;
	/**
	* Represents the height of the last encoded frame sent over the corresponded synchronization source
	*/
	@JsonProperty("frameWidth")
	public Integer frameWidth;
	/**
	* Represents the width of the last encoded frame sent over the corresponded synchronization source
	*/
	@JsonProperty("frameHeight")
	public Integer frameHeight;
	/**
	* Represents the bit depth per pixel of the last encoded frame sent over the corresponded synchronization source
	*/
	@JsonProperty("frameBitDepth")
	public Integer frameBitDepth;
	/**
	* The number of encoded frames over the last second sent over the corresponded synchronization source
	*/
	@JsonProperty("framesPerSecond")
	public Double framesPerSecond;
	/**
	* The number of frames sent over the corresponded synchronization source
	*/
	@JsonProperty("framesSent")
	public Integer framesSent;
	/**
	* The number of huge frames (2.5x greater than the average size of frame) sent over the corresponded synchronization source
	*/
	@JsonProperty("hugeFramesSent")
	public Integer hugeFramesSent;
	/**
	* The number of frames encoded over the corresponded synchronization source
	*/
	@JsonProperty("framesEncoded")
	public Integer framesEncoded;
	/**
	* The number of keyframes sent over the corresponded synchronization source
	*/
	@JsonProperty("keyFramesEncoded")
	public Integer keyFramesEncoded;
	/**
	* The number of frames discarded before sending over the corresponded synchronization source
	*/
	@JsonProperty("framesDiscardedOnSend")
	public Integer framesDiscardedOnSend;
	/**
	* The sum of QP values encoded by the encoder corresponded to the synchronization source
	*/
	@JsonProperty("qpSum")
	public Long qpSum;
	/**
	* The sum of encoding time spent by the encoder corresponded to the synchronization source
	*/
	@JsonProperty("totalEncodeTime")
	public Double totalEncodeTime;
	/**
	* The total number of delay packets buffered at the sender side in seconds over the corresponding synchronization source
	*/
	@JsonProperty("totalPacketSendDelay")
	public Double totalPacketSendDelay;
	/**
	* The average RTCP interval between two consecutive compound RTCP packets sent for the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("averageRtcpInterval")
	public Double averageRtcpInterval;
	/**
	* Time elapsed in seconds when the the corresponding synchronization source (ssrc) was in a limited state due to CPU
	*/
	@JsonProperty("qualityLimitationDurationCPU")
	public Double qualityLimitationDurationCPU;
	/**
	* Time elapsed in seconds when the the corresponding synchronization source (ssrc) was not in a limited state
	*/
	@JsonProperty("qualityLimitationDurationNone")
	public Double qualityLimitationDurationNone;
	/**
	* Time elapsed in seconds when the the corresponding synchronization source (ssrc) was in a limited state becasue of bandwidth
	*/
	@JsonProperty("qualityLimitationDurationBandwidth")
	public Double qualityLimitationDurationBandwidth;
	/**
	* Time elapsed in seconds when the the corresponding synchronization source (ssrc) was in a limited state becaue of other factor
	*/
	@JsonProperty("qualityLimitationDurationOther")
	public Double qualityLimitationDurationOther;
	/**
	* Indicate a reason for the corresponding synchronization source (ssrc) quality is limited
	*/
	@JsonProperty("qualityLimitationReason")
	public String qualityLimitationReason;
	/**
	* The number of quality limiatation changes happened for the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("qualityLimitationResolutionChanges")
	public Integer qualityLimitationResolutionChanges;
	/**
	* The total number of DSCP flagged RTP packets sent over the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("perDscpPacketsSent")
	public Integer perDscpPacketsSent;
	/**
	* Count the total number of Negative ACKnowledgement (NACK) packets received over the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("nackCount")
	public Integer nackCount;
	/**
	* The number of full inter requests happened over the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("firCount")
	public Integer firCount;
	/**
	* The number of picture loss indication happened received over the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("pliCount")
	public Integer pliCount;
	/**
	* The number of slice loss indication happened over the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("sliCount")
	public Integer sliCount;
	/**
	* Indicate the name of the encoder implementation library
	*/
	@JsonProperty("encoderImplementation")
	public String encoderImplementation;
	/**
	* The total number of packets received on the corresponded synchronization source
	*/
	@JsonProperty("packetsReceived")
	public Integer packetsReceived;
	/**
	* The total number of bytes received on the corresponded synchronization source
	*/
	@JsonProperty("packetsLost")
	public Integer packetsLost;
	/**
	* The corresponded synchronization source reported jitter
	*/
	@JsonProperty("jitter")
	public Double jitter;
	/**
	* The total number of packets missed the playout point and therefore discarded by the jitterbuffer
	*/
	@JsonProperty("packetsDiscarded")
	public Integer packetsDiscarded;
	/**
	* The total number of packets repaired by either FEC or due to retransmission on the corresponded synchronization source
	*/
	@JsonProperty("packetsRepaired")
	public Integer packetsRepaired;
	/**
	* The total number of packets lost in burst (RFC6958)
	*/
	@JsonProperty("burstPacketsLost")
	public Integer burstPacketsLost;
	/**
	* The total number of packets discarded in burst (RFC6958)
	*/
	@JsonProperty("burstPacketsDiscarded")
	public Integer burstPacketsDiscarded;
	/**
	* The total number of burst happened causes burstPacketsLost on the corresponding synchronization source
	*/
	@JsonProperty("burstLossCount")
	public Integer burstLossCount;
	/**
	* The total number of burst happened causes burstPacketsDiscarded on the corresponding synchronization source
	*/
	@JsonProperty("burstDiscardCount")
	public Integer burstDiscardCount;
	/**
	* The fraction of RTP packets lost during bursts proportionally to the total number of RTP packets expected in the bursts on the corresponding synchronization source
	*/
	@JsonProperty("burstLossRate")
	public Double burstLossRate;
	/**
	* The fraction of RTP packets discarded during bursts proportionally to the total number of RTP packets expected in the bursts on the corresponding synchronization source
	*/
	@JsonProperty("burstDiscardRate")
	public Double burstDiscardRate;
	/**
	* The fraction of RTP packets lost during gap proportionally to the total number of RTP packets expected in the bursts on the corresponding synchronization source
	*/
	@JsonProperty("gapLossRate")
	public Double gapLossRate;
	/**
	* The fraction of RTP packets discarded during gap proportionally to the total number of RTP packets expected in the bursts on the corresponding synchronization source
	*/
	@JsonProperty("gapDiscardRate")
	public Double gapDiscardRate;
	/**
	* The number of frames dropped over the corresponded synchronization source
	*/
	@JsonProperty("framesDropped")
	public Integer framesDropped;
	/**
	* The number of partial frames lost over the corresponded synchronization source
	*/
	@JsonProperty("partialFramesLost")
	public Integer partialFramesLost;
	/**
	* The number of full frames lost over the corresponded synchronization source
	*/
	@JsonProperty("fullFramesLost")
	public Integer fullFramesLost;
	/**
	* RTT measurement in seconds based on (most likely) SR, and RR belongs to the corresponded synchronization source
	*/
	@JsonProperty("roundTripTime")
	public Double roundTripTime;
	/**
	* The sum of RTT measurements belongs to the corresponded synchronization source
	*/
	@JsonProperty("totalRoundTripTime")
	public Double totalRoundTripTime;
	/**
	* The receiver reported fractional lost belongs to the corresponded synchronization source
	*/
	@JsonProperty("fractionLost")
	public Double fractionLost;
	/**
	* The total number of RR reports received, which is the base of the remote inbound calculation on this source
	*/
	@JsonProperty("reportsReceived")
	public Integer reportsReceived;
	/**
	* The total number of calculated RR measurements received on this source
	*/
	@JsonProperty("roundTripTimeMeasurements")
	public Integer roundTripTimeMeasurements;
	/**
	* True if the corresponded media source is remote, false otherwise (or null depending on browser and version)
	*/
	@JsonProperty("relayedSource")
	public Boolean relayedSource;
	/**
	* Indicate the encoded width of the frame received on the corresponded synchronization source (ssrc)
	*/
	@JsonProperty("encodedFrameWidth")
	public Integer encodedFrameWidth;
	/**
	* Indicate the encoded height of the frame received on the corresponded synchronization source (ssrc)
	*/
	@JsonProperty("encodedFrameHeight")
	public Integer encodedFrameHeight;
	/**
	* Indicate the encoded bit depth per pixel of the last decoded frame received on the corresponded synchronization source (ssrc)
	*/
	@JsonProperty("encodedFrameBitDepth")
	public Integer encodedFrameBitDepth;
	/**
	* Indicate the encoded number of decoded frames in the last second received on the corresponded synchronization source (ssrc)
	*/
	@JsonProperty("encodedFramesPerSecond")
	public Double encodedFramesPerSecond;
	/**
	* Flag represents if the sender ended the media stream track or not.
	*/
	@JsonProperty("ended")
	public Boolean ended;
	/**
	* The type of the payload the RTP packet SSRC belongs to
	*/
	@JsonProperty("payloadType")
	public Integer payloadType;
	/**
	* the MIME type of the codec (e.g.: video/vp8)
	*/
	@JsonProperty("mimeType")
	public String mimeType;
	/**
	* The negotiated clock rate the RTP timestamp is generated of
	*/
	@JsonProperty("clockRate")
	public Integer clockRate;
	/**
	* The number of channels for audio is used (in stereo it is 2, otherwise it is most likely null)
	*/
	@JsonProperty("channels")
	public Integer channels;
	/**
	* The a=fmtp line in the SDP corresponding to the codec
	*/
	@JsonProperty("sdpFmtpLine")
	public String sdpFmtpLine;


	public static class Builder {

		private OutboundVideoTrackReport result = new OutboundVideoTrackReport();

		public Builder setServiceId(String value) { this.result.serviceId = value; return this; }
		public Builder setMediaUnitId(String value) { this.result.mediaUnitId = value; return this; }
		public Builder setMarker(String value) { this.result.marker = value; return this; }
		public Builder setTimestamp(Long value) { this.result.timestamp = value; return this; }
		public Builder setCallId(String value) { this.result.callId = value; return this; }
		public Builder setRoomId(String value) { this.result.roomId = value; return this; }
		public Builder setClientId(String value) { this.result.clientId = value; return this; }
		public Builder setUserId(String value) { this.result.userId = value; return this; }
		public Builder setPeerConnectionId(String value) { this.result.peerConnectionId = value; return this; }
		public Builder setLabel(String value) { this.result.label = value; return this; }
		public Builder setTrackId(String value) { this.result.trackId = value; return this; }
		public Builder setSfuStreamId(String value) { this.result.sfuStreamId = value; return this; }
		public Builder setSampleSeq(Integer value) { this.result.sampleSeq = value; return this; }
		public Builder setSsrc(Long value) { this.result.ssrc = value; return this; }
		public Builder setPacketsSent(Integer value) { this.result.packetsSent = value; return this; }
		public Builder setBytesSent(Long value) { this.result.bytesSent = value; return this; }
		public Builder setRid(String value) { this.result.rid = value; return this; }
		public Builder setLastPacketSentTimestamp(Long value) { this.result.lastPacketSentTimestamp = value; return this; }
		public Builder setHeaderBytesSent(Long value) { this.result.headerBytesSent = value; return this; }
		public Builder setPacketsDiscardedOnSend(Integer value) { this.result.packetsDiscardedOnSend = value; return this; }
		public Builder setBytesDiscardedOnSend(Long value) { this.result.bytesDiscardedOnSend = value; return this; }
		public Builder setFecPacketsSent(Integer value) { this.result.fecPacketsSent = value; return this; }
		public Builder setRetransmittedPacketsSent(Integer value) { this.result.retransmittedPacketsSent = value; return this; }
		public Builder setRetransmittedBytesSent(Long value) { this.result.retransmittedBytesSent = value; return this; }
		public Builder setTargetBitrate(Integer value) { this.result.targetBitrate = value; return this; }
		public Builder setTotalEncodedBytesTarget(Long value) { this.result.totalEncodedBytesTarget = value; return this; }
		public Builder setFrameWidth(Integer value) { this.result.frameWidth = value; return this; }
		public Builder setFrameHeight(Integer value) { this.result.frameHeight = value; return this; }
		public Builder setFrameBitDepth(Integer value) { this.result.frameBitDepth = value; return this; }
		public Builder setFramesPerSecond(Double value) { this.result.framesPerSecond = value; return this; }
		public Builder setFramesSent(Integer value) { this.result.framesSent = value; return this; }
		public Builder setHugeFramesSent(Integer value) { this.result.hugeFramesSent = value; return this; }
		public Builder setFramesEncoded(Integer value) { this.result.framesEncoded = value; return this; }
		public Builder setKeyFramesEncoded(Integer value) { this.result.keyFramesEncoded = value; return this; }
		public Builder setFramesDiscardedOnSend(Integer value) { this.result.framesDiscardedOnSend = value; return this; }
		public Builder setQpSum(Long value) { this.result.qpSum = value; return this; }
		public Builder setTotalEncodeTime(Double value) { this.result.totalEncodeTime = value; return this; }
		public Builder setTotalPacketSendDelay(Double value) { this.result.totalPacketSendDelay = value; return this; }
		public Builder setAverageRtcpInterval(Double value) { this.result.averageRtcpInterval = value; return this; }
		public Builder setQualityLimitationDurationCPU(Double value) { this.result.qualityLimitationDurationCPU = value; return this; }
		public Builder setQualityLimitationDurationNone(Double value) { this.result.qualityLimitationDurationNone = value; return this; }
		public Builder setQualityLimitationDurationBandwidth(Double value) { this.result.qualityLimitationDurationBandwidth = value; return this; }
		public Builder setQualityLimitationDurationOther(Double value) { this.result.qualityLimitationDurationOther = value; return this; }
		public Builder setQualityLimitationReason(String value) { this.result.qualityLimitationReason = value; return this; }
		public Builder setQualityLimitationResolutionChanges(Integer value) { this.result.qualityLimitationResolutionChanges = value; return this; }
		public Builder setPerDscpPacketsSent(Integer value) { this.result.perDscpPacketsSent = value; return this; }
		public Builder setNackCount(Integer value) { this.result.nackCount = value; return this; }
		public Builder setFirCount(Integer value) { this.result.firCount = value; return this; }
		public Builder setPliCount(Integer value) { this.result.pliCount = value; return this; }
		public Builder setSliCount(Integer value) { this.result.sliCount = value; return this; }
		public Builder setEncoderImplementation(String value) { this.result.encoderImplementation = value; return this; }
		public Builder setPacketsReceived(Integer value) { this.result.packetsReceived = value; return this; }
		public Builder setPacketsLost(Integer value) { this.result.packetsLost = value; return this; }
		public Builder setJitter(Double value) { this.result.jitter = value; return this; }
		public Builder setPacketsDiscarded(Integer value) { this.result.packetsDiscarded = value; return this; }
		public Builder setPacketsRepaired(Integer value) { this.result.packetsRepaired = value; return this; }
		public Builder setBurstPacketsLost(Integer value) { this.result.burstPacketsLost = value; return this; }
		public Builder setBurstPacketsDiscarded(Integer value) { this.result.burstPacketsDiscarded = value; return this; }
		public Builder setBurstLossCount(Integer value) { this.result.burstLossCount = value; return this; }
		public Builder setBurstDiscardCount(Integer value) { this.result.burstDiscardCount = value; return this; }
		public Builder setBurstLossRate(Double value) { this.result.burstLossRate = value; return this; }
		public Builder setBurstDiscardRate(Double value) { this.result.burstDiscardRate = value; return this; }
		public Builder setGapLossRate(Double value) { this.result.gapLossRate = value; return this; }
		public Builder setGapDiscardRate(Double value) { this.result.gapDiscardRate = value; return this; }
		public Builder setFramesDropped(Integer value) { this.result.framesDropped = value; return this; }
		public Builder setPartialFramesLost(Integer value) { this.result.partialFramesLost = value; return this; }
		public Builder setFullFramesLost(Integer value) { this.result.fullFramesLost = value; return this; }
		public Builder setRoundTripTime(Double value) { this.result.roundTripTime = value; return this; }
		public Builder setTotalRoundTripTime(Double value) { this.result.totalRoundTripTime = value; return this; }
		public Builder setFractionLost(Double value) { this.result.fractionLost = value; return this; }
		public Builder setReportsReceived(Integer value) { this.result.reportsReceived = value; return this; }
		public Builder setRoundTripTimeMeasurements(Integer value) { this.result.roundTripTimeMeasurements = value; return this; }
		public Builder setRelayedSource(Boolean value) { this.result.relayedSource = value; return this; }
		public Builder setEncodedFrameWidth(Integer value) { this.result.encodedFrameWidth = value; return this; }
		public Builder setEncodedFrameHeight(Integer value) { this.result.encodedFrameHeight = value; return this; }
		public Builder setEncodedFrameBitDepth(Integer value) { this.result.encodedFrameBitDepth = value; return this; }
		public Builder setEncodedFramesPerSecond(Double value) { this.result.encodedFramesPerSecond = value; return this; }
		public Builder setEnded(Boolean value) { this.result.ended = value; return this; }
		public Builder setPayloadType(Integer value) { this.result.payloadType = value; return this; }
		public Builder setMimeType(String value) { this.result.mimeType = value; return this; }
		public Builder setClockRate(Integer value) { this.result.clockRate = value; return this; }
		public Builder setChannels(Integer value) { this.result.channels = value; return this; }
		public Builder setSdpFmtpLine(String value) { this.result.sdpFmtpLine = value; return this; }
		public OutboundVideoTrackReport build() {
			return this.result;
		}
	}
}