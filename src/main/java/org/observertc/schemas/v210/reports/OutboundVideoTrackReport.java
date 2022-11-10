package org.observertc.schemas.v210.reports;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
* A Report created for Outbound Video Tracks. A combination of Video source, Codec metadata carrying outbound and remote inbound RTP stat measurements
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class OutboundVideoTrackReport {
	public static final String VERSION="2.1.8";
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
	* Total number of RTP header and padding bytes sent over the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("headerBytesSent")
	public Long headerBytesSent;
	/**
	* Total number of retransmitted packets sent over the corresponding synchronization source (ssrc).
	*/
	@JsonProperty("retransmittedPacketsSent")
	public Integer retransmittedPacketsSent;
	/**
	* Total number of retransmitted bytes sent over the corresponding synchronization source (ssrc).
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
	* Count the total number of Negative ACKnowledgement (NACK) packets received over the corresponding synchronization source (ssrc)
	*/
	@JsonProperty("nackCount")
	public Integer nackCount;
	/**
	* Indicate the name of the encoder implementation library
	*/
	@JsonProperty("encoderImplementation")
	public String encoderImplementation;
	/**
	* Indicates whether this RTP stream is configured to be sent or disabled
	*/
	@JsonProperty("active")
	public Boolean active;
	/**
	* The frame width in pixels of the frames targeted by the media encoder
	*/
	@JsonProperty("frameWidth")
	public Integer frameWidth;
	/**
	* The frame width the media encoder targeted
	*/
	@JsonProperty("frameHeight")
	public Integer frameHeight;
	/**
	* The encoded number of frames in the last second on the corresponded media source
	*/
	@JsonProperty("framesPerSecond")
	public Double framesPerSecond;
	/**
	* TThe total number of frames sent on the corresponded RTP stream
	*/
	@JsonProperty("framesSent")
	public Integer framesSent;
	/**
	* The total number of huge frames (avgFrameSize * 2.5) on the corresponded RTP stream
	*/
	@JsonProperty("hugeFramesSent")
	public Integer hugeFramesSent;
	/**
	* The total number of frames encoded by the media source
	*/
	@JsonProperty("framesEncoded")
	public Integer framesEncoded;
	/**
	* The total number of keyframes encoded on the corresponded RTP stream
	*/
	@JsonProperty("keyFramesEncoded")
	public Integer keyFramesEncoded;
	/**
	* The sum of the QP the media encoder provided on the corresponded RTP stream.
	*/
	@JsonProperty("qpSum")
	public Long qpSum;
	/**
	* The total time in seconds spent in encoding media frames for the corresponded RTP stream.
	*/
	@JsonProperty("totalEncodeTime")
	public Double totalEncodeTime;
	/**
	* Time elapsed in seconds when the RTC connection has not limited the quality
	*/
	@JsonProperty("qualityLimitationDurationNone")
	public Double qualityLimitationDurationNone;
	/**
	* Time elapsed in seconds the RTC connection had a limitation because of CPU
	*/
	@JsonProperty("qualityLimitationDurationCPU")
	public Double qualityLimitationDurationCPU;
	/**
	* Time elapsed in seconds the RTC connection had a limitation because of Bandwidth
	*/
	@JsonProperty("qualityLimitationDurationBandwidth")
	public Double qualityLimitationDurationBandwidth;
	/**
	* Time elapsed in seconds the RTC connection had a limitation because of Other factor
	*/
	@JsonProperty("qualityLimitationDurationOther")
	public Double qualityLimitationDurationOther;
	/**
	* Indicate a reason for the quality limitation of the corresponded synchronization source
	*/
	@JsonProperty("qualityLimitationReason")
	public String qualityLimitationReason;
	/**
	* The total number of resolution changes occured ont he corresponded RTP stream due to quality changes
	*/
	@JsonProperty("qualityLimitationResolutionChanges")
	public Integer qualityLimitationResolutionChanges;
	/**
	* The total number FIR packets sent from this endpoint to the source on the corresponded RTP stream
	*/
	@JsonProperty("firCount")
	public Integer firCount;
	/**
	* The total number of Picture Loss Indication sent on the corresponded RTP stream
	*/
	@JsonProperty("pliCount")
	public Integer pliCount;
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
	* The total number of calculated RR measurements received on this source
	*/
	@JsonProperty("roundTripTimeMeasurements")
	public Integer roundTripTimeMeasurements;
	/**
	* The total number of frames reported to be lost by the remote endpoit on the corresponded RTP stream
	*/
	@JsonProperty("framesDropped")
	public Integer framesDropped;
	/**
	* True if the corresponded media source is remote, false otherwise (or null depending on browser and version)
	*/
	@JsonProperty("relayedSource")
	public Boolean relayedSource;
	/**
	* The width, in pixels, of the last frame originating from the media source
	*/
	@JsonProperty("width")
	public Integer width;
	/**
	* The height, in pixels, of the last frame originating from the media source
	*/
	@JsonProperty("height")
	public Integer height;
	/**
	* The total number of frames originated from the media source
	*/
	@JsonProperty("frames")
	public Integer frames;


	public static class Builder {

		private OutboundVideoTrackReport result = new OutboundVideoTrackReport();

		public Builder setServiceId(String value) {
			this.result.serviceId = value;
			return this;
		}
		public Builder setMediaUnitId(String value) {
			this.result.mediaUnitId = value;
			return this;
		}
		public Builder setMarker(String value) {
			this.result.marker = value;
			return this;
		}
		public Builder setTimestamp(Long value) {
			this.result.timestamp = value;
			return this;
		}
		public Builder setCallId(String value) {
			this.result.callId = value;
			return this;
		}
		public Builder setRoomId(String value) {
			this.result.roomId = value;
			return this;
		}
		public Builder setClientId(String value) {
			this.result.clientId = value;
			return this;
		}
		public Builder setUserId(String value) {
			this.result.userId = value;
			return this;
		}
		public Builder setPeerConnectionId(String value) {
			this.result.peerConnectionId = value;
			return this;
		}
		public Builder setLabel(String value) {
			this.result.label = value;
			return this;
		}
		public Builder setTrackId(String value) {
			this.result.trackId = value;
			return this;
		}
		public Builder setSfuStreamId(String value) {
			this.result.sfuStreamId = value;
			return this;
		}
		public Builder setSampleSeq(Integer value) {
			this.result.sampleSeq = value;
			return this;
		}
		public Builder setSsrc(Long value) {
			this.result.ssrc = value;
			return this;
		}
		public Builder setPacketsSent(Integer value) {
			this.result.packetsSent = value;
			return this;
		}
		public Builder setBytesSent(Long value) {
			this.result.bytesSent = value;
			return this;
		}
		public Builder setRid(String value) {
			this.result.rid = value;
			return this;
		}
		public Builder setHeaderBytesSent(Long value) {
			this.result.headerBytesSent = value;
			return this;
		}
		public Builder setRetransmittedPacketsSent(Integer value) {
			this.result.retransmittedPacketsSent = value;
			return this;
		}
		public Builder setRetransmittedBytesSent(Long value) {
			this.result.retransmittedBytesSent = value;
			return this;
		}
		public Builder setTargetBitrate(Integer value) {
			this.result.targetBitrate = value;
			return this;
		}
		public Builder setTotalEncodedBytesTarget(Long value) {
			this.result.totalEncodedBytesTarget = value;
			return this;
		}
		public Builder setTotalPacketSendDelay(Double value) {
			this.result.totalPacketSendDelay = value;
			return this;
		}
		public Builder setAverageRtcpInterval(Double value) {
			this.result.averageRtcpInterval = value;
			return this;
		}
		public Builder setNackCount(Integer value) {
			this.result.nackCount = value;
			return this;
		}
		public Builder setEncoderImplementation(String value) {
			this.result.encoderImplementation = value;
			return this;
		}
		public Builder setActive(Boolean value) {
			this.result.active = value;
			return this;
		}
		public Builder setFrameWidth(Integer value) {
			this.result.frameWidth = value;
			return this;
		}
		public Builder setFrameHeight(Integer value) {
			this.result.frameHeight = value;
			return this;
		}
		public Builder setFramesPerSecond(Double value) {
			this.result.framesPerSecond = value;
			return this;
		}
		public Builder setFramesSent(Integer value) {
			this.result.framesSent = value;
			return this;
		}
		public Builder setHugeFramesSent(Integer value) {
			this.result.hugeFramesSent = value;
			return this;
		}
		public Builder setFramesEncoded(Integer value) {
			this.result.framesEncoded = value;
			return this;
		}
		public Builder setKeyFramesEncoded(Integer value) {
			this.result.keyFramesEncoded = value;
			return this;
		}
		public Builder setQpSum(Long value) {
			this.result.qpSum = value;
			return this;
		}
		public Builder setTotalEncodeTime(Double value) {
			this.result.totalEncodeTime = value;
			return this;
		}
		public Builder setQualityLimitationDurationNone(Double value) {
			this.result.qualityLimitationDurationNone = value;
			return this;
		}
		public Builder setQualityLimitationDurationCPU(Double value) {
			this.result.qualityLimitationDurationCPU = value;
			return this;
		}
		public Builder setQualityLimitationDurationBandwidth(Double value) {
			this.result.qualityLimitationDurationBandwidth = value;
			return this;
		}
		public Builder setQualityLimitationDurationOther(Double value) {
			this.result.qualityLimitationDurationOther = value;
			return this;
		}
		public Builder setQualityLimitationReason(String value) {
			this.result.qualityLimitationReason = value;
			return this;
		}
		public Builder setQualityLimitationResolutionChanges(Integer value) {
			this.result.qualityLimitationResolutionChanges = value;
			return this;
		}
		public Builder setFirCount(Integer value) {
			this.result.firCount = value;
			return this;
		}
		public Builder setPliCount(Integer value) {
			this.result.pliCount = value;
			return this;
		}
		public Builder setPacketsReceived(Integer value) {
			this.result.packetsReceived = value;
			return this;
		}
		public Builder setPacketsLost(Integer value) {
			this.result.packetsLost = value;
			return this;
		}
		public Builder setJitter(Double value) {
			this.result.jitter = value;
			return this;
		}
		public Builder setRoundTripTime(Double value) {
			this.result.roundTripTime = value;
			return this;
		}
		public Builder setTotalRoundTripTime(Double value) {
			this.result.totalRoundTripTime = value;
			return this;
		}
		public Builder setFractionLost(Double value) {
			this.result.fractionLost = value;
			return this;
		}
		public Builder setRoundTripTimeMeasurements(Integer value) {
			this.result.roundTripTimeMeasurements = value;
			return this;
		}
		public Builder setFramesDropped(Integer value) {
			this.result.framesDropped = value;
			return this;
		}
		public Builder setRelayedSource(Boolean value) {
			this.result.relayedSource = value;
			return this;
		}
		public Builder setWidth(Integer value) {
			this.result.width = value;
			return this;
		}
		public Builder setHeight(Integer value) {
			this.result.height = value;
			return this;
		}
		public Builder setFrames(Integer value) {
			this.result.frames = value;
			return this;
		}
		public OutboundVideoTrackReport build() {
			return this.result;
		}
	}
}