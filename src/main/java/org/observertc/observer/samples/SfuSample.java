package org.observertc.observer.samples;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * A compound object holds a set of measurements belonging to a aspecific time
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public  class SfuSample {

	/**
	 * a Unique generated id for the sfu samples are originated from
	 */
	@JsonProperty("sfuId")
	public String sfuId;

	/**
	 * array of measurements related to inbound RTP streams
	 */
	@JsonProperty("inboundRtpPads")
	public SfuInboundRtpPad[] inboundRtpPads;

	/**
	 * array of measurements related to outbound RTP streams
	 */
	@JsonProperty("outboundRtpPads")
	public SfuOutboundRtpPad[] outboundRtpPads;

	/**
	 * array of measurements of SCTP streams
	 */
	@JsonProperty("sctpStreams")
	public SctpStream[] sctpStreams;

	/**
	 * array of measurements of SFU peer connection transports
	 */
	@JsonProperty("sfuTransports")
	public SfuTransport[] sfuTransports;

	/**
	 * The timestamp when the sample is created
	 */
	@JsonProperty("timestamp")
	public Long timestamp;

	/**
	 * The client app running offsets from GMT in hours
	 */
	@JsonProperty("timeZoneOffsetInHours")
	public Integer timeZoneOffsetInHours;

	/**
	 * A sample marker indicate an additional information from the app
	 */
	@JsonProperty("marker")
	public String marker;

	/**
	 * undefined
	 */

	public static class SfuInboundRtpPad {

		/**
		 * The id of the transport the stream belongs to
		 */
		@JsonProperty("transportId")
		public String transportId;

		/**
		 * unique identifier for the stream
		 */
		@JsonProperty("rtpStreamId")
		public String rtpStreamId;

		/**
		 * id of the source pod
		 */
		@JsonProperty("padId")
		public String padId;

		/**
		 * Indicates if this transport is not receive or send traffic outside of the SFU mesh.
		 */
		@JsonProperty("internal")
		public boolean internal;

		/**
		 * Indicate if this message measurements should be kept and oly used as keep alive message for the transports
		 */
		@JsonProperty("skipMeasurements")
		public boolean skipMeasurements;

		/**
		 * if the sink is internally piped between the SFUs, this id represents the remote SFU outbound pad id
		 */
		@JsonProperty("outboundPadId")
		public String outboundPadId;

		/**
		 * The SSRC identifier of the corresponded RTP stream
		 */
		@JsonProperty("ssrc")
		public Long ssrc;

		/**
		 * The type of the media the stream carries
		 */
		@JsonProperty("mediaType")
		public String mediaType;

		/**
		 * The type of the payload the RTP stream carries
		 */
		@JsonProperty("payloadType")
		public Integer payloadType;

		/**
		 * The MIME type of the media codec
		 */
		@JsonProperty("mimeType")
		public String mimeType;

		/**
		 * the clock rate of the media source generates samples or frames
		 */
		@JsonProperty("clockRate")
		public Long clockRate;

		/**
		 * The corresponded SDP line in SDP negotiation
		 */
		@JsonProperty("sdpFmtpLine")
		public String sdpFmtpLine;

		/**
		 * The rid parameter of the corresponded RTP stream
		 */
		@JsonProperty("rid")
		public String rid;

		/**
		 * If RTX is negotiated as a separate stream, this is the SSRC of the RTX stream that is associated with this stream's ssrc.
		 */
		@JsonProperty("rtxSsrc")
		public Long rtxSsrc;

		/**
		 * The bitrate the corresponded stream targets to.
		 */
		@JsonProperty("targetBitrate")
		public Long targetBitrate;

		/**
		 * The RTP header V flag indicate of the activity of the media source by the media codec if the RTP transport ships it through
		 */
		@JsonProperty("voiceActivityFlag")
		public boolean voiceActivityFlag;

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
		 * The total number of negative acknowledgement received on the corresponded RTP stream
		 */
		@JsonProperty("nackCount")
		public Integer nackCount;

		/**
		 * The total number of SLI indicator sent from the endpoint on the corresponded RTP stream
		 */
		@JsonProperty("sliCount")
		public Integer sliCount;

		/**
		 * The total number of packets lost on the corresponded RTP stream
		 */
		@JsonProperty("packetsLost")
		public Integer packetsLost;

		/**
		 * The total number of packets received on the corresponded RTP stream,
		 */
		@JsonProperty("packetsReceived")
		public Integer packetsReceived;

		/**
		 * The total number of discarded packets on the corresponded RTP stream.
		 */
		@JsonProperty("packetsDiscarded")
		public Integer packetsDiscarded;

		/**
		 * The total number of packets repaired by either retransmission or FEC on the corresponded RTP stream.
		 */
		@JsonProperty("packetsRepaired")
		public Integer packetsRepaired;

		/**
		 * The total number of packets failed to be decrypted on the corresponded RTP stream
		 */
		@JsonProperty("packetsFailedDecryption")
		public Integer packetsFailedDecryption;

		/**
		 * The total number of duplicated packets appeared on the corresponded RTP stream.
		 */
		@JsonProperty("packetsDuplicated")
		public Integer packetsDuplicated;

		/**
		 * The total number of FEC packets received on the corresponded RTP stream.
		 */
		@JsonProperty("fecPacketsReceived")
		public Integer fecPacketsReceived;

		/**
		 * The total number of FEC packets discarded on the corresponded RTP stream.
		 */
		@JsonProperty("fecPacketsDiscarded")
		public Integer fecPacketsDiscarded;

		/**
		 * The total amount of payload bytes received on the corresponded RTP stream
		 */
		@JsonProperty("bytesReceived")
		public Long bytesReceived;

		/**
		 * The total number of SR reports received by the remote endpoint on the corresponded RTP stream
		 */
		@JsonProperty("rtcpSrReceived")
		public Integer rtcpSrReceived;

		/**
		 * The total number of RR reports sent by the the local endpoint on the corresponded RTP stream
		 */
		@JsonProperty("rtcpRrSent")
		public Integer rtcpRrSent;

		/**
		 * If rtx packets are received on the same stream then this number indicates how may has been received
		 */
		@JsonProperty("rtxPacketsReceived")
		public Integer rtxPacketsReceived;

		/**
		 * If rtx packets are received on the same stream then this number indicates how may has been discarded
		 */
		@JsonProperty("rtxPacketsDiscarded")
		public Integer rtxPacketsDiscarded;

		/**
		 * The number of frames received on the corresponded RTP stream
		 */
		@JsonProperty("framesReceived")
		public Integer framesReceived;

		/**
		 * Indicate the number of frames the Sfu has been decoded
		 */
		@JsonProperty("framesDecoded")
		public Integer framesDecoded;

		/**
		 * Indicate the number of keyframes the Sfu has been decoded
		 */
		@JsonProperty("keyFramesDecoded")
		public Integer keyFramesDecoded;

		/**
		 * The calculated fractionLost of the stream
		 */
		@JsonProperty("fractionLost")
		public Double fractionLost;

		/**
		 * The calculated jitter for the stream
		 */
		@JsonProperty("jitter")
		public Double jitter;

		/**
		 * The calculated round trip time for the corresponded RTP stream
		 */
		@JsonProperty("roundTripTime")
		public Double roundTripTime;

		/**
		 * Arbitrary attachments holds relevant information about the stream
		 */
		@JsonProperty("attachments")
		public String attachments;

	}
	/**
	 * undefined
	 */

	public static class SfuOutboundRtpPad {

		/**
		 * The id of the transport the stream belongs to
		 */
		@JsonProperty("transportId")
		public String transportId;

		/**
		 * unique identifier of the stream
		 */
		@JsonProperty("rtpStreamId")
		public String rtpStreamId;

		/**
		 * id of the sink pod
		 */
		@JsonProperty("padId")
		public String padId;

		/**
		 * Indicates if this transport is not receive or send traffic outside of the SFU mesh.
		 */
		@JsonProperty("internal")
		public boolean internal;

		/**
		 * Indicate if this message measurements should be kept and oly used as keep alive message for the transports
		 */
		@JsonProperty("skipMeasurements")
		public boolean skipMeasurements;

		/**
		 * The SSRC identifier of the corresponded RTP stream
		 */
		@JsonProperty("ssrc")
		public Long ssrc;

		/**
		 * The type of the media the stream carries
		 */
		@JsonProperty("mediaType")
		public String mediaType;

		/**
		 * The type of the payload the RTP stream carries
		 */
		@JsonProperty("payloadType")
		public Integer payloadType;

		/**
		 * The MIME type of the media codec
		 */
		@JsonProperty("mimeType")
		public String mimeType;

		/**
		 * the clock rate of the media source generates samples or frames
		 */
		@JsonProperty("clockRate")
		public Long clockRate;

		/**
		 * The corresponded SDP line in SDP negotiation
		 */
		@JsonProperty("sdpFmtpLine")
		public String sdpFmtpLine;

		/**
		 * The rid parameter of the corresponded RTP stream
		 */
		@JsonProperty("rid")
		public String rid;

		/**
		 * If RTX is negotiated as a separate stream, this is the SSRC of the RTX stream that is associated with this stream's ssrc.
		 */
		@JsonProperty("rtxSsrc")
		public Long rtxSsrc;

		/**
		 * The bitrate the corresponded stream targets to.
		 */
		@JsonProperty("targetBitrate")
		public Long targetBitrate;

		/**
		 * The RTP header V flag indicate of the activity of the media source by the media codec if the RTP transport ships it through
		 */
		@JsonProperty("voiceActivityFlag")
		public boolean voiceActivityFlag;

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
		 * The total number of negative acknowledgement received on the corresponded RTP stream
		 */
		@JsonProperty("nackCount")
		public Integer nackCount;

		/**
		 * The total number of SLI indicator sent from the endpoint on the corresponded RTP stream
		 */
		@JsonProperty("sliCount")
		public Integer sliCount;

		/**
		 * The total number of packets sent on the corresponded RTP stream,
		 */
		@JsonProperty("packetsSent")
		public Integer packetsSent;

		/**
		 * The total number of packets lost on the corresponded RTP stream
		 */
		@JsonProperty("packetsLost")
		public Integer packetsLost;

		/**
		 * The total number of discarded packets on the corresponded RTP stream.
		 */
		@JsonProperty("packetsDiscarded")
		public Integer packetsDiscarded;

		/**
		 * The total number of packets retransmitted on the corresponded RTP stream.
		 */
		@JsonProperty("packetsRetransmitted")
		public Integer packetsRetransmitted;

		/**
		 * The total number of packets failed to be encrypted on the corresponded RTP stream
		 */
		@JsonProperty("packetsFailedEncryption")
		public Integer packetsFailedEncryption;

		/**
		 * The total number of duplicated packets appeared on the corresponded RTP stream.
		 */
		@JsonProperty("packetsDuplicated")
		public Integer packetsDuplicated;

		/**
		 * The total number of FEC packets sent on the corresponded RTP stream.
		 */
		@JsonProperty("fecPacketsSent")
		public Integer fecPacketsSent;

		/**
		 * The total number of FEC packets discarded on the corresponded RTP stream.
		 */
		@JsonProperty("fecPacketsDiscarded")
		public Integer fecPacketsDiscarded;

		/**
		 * The total amount of payload bytes sent on the corresponded RTP stream
		 */
		@JsonProperty("bytesSent")
		public Long bytesSent;

		/**
		 * The total number of SR reports sent to the remote endpoint on the corresponded RTP stream
		 */
		@JsonProperty("rtcpSrSent")
		public Integer rtcpSrSent;

		/**
		 * The total number of RR reports received by the the local endpoint on the corresponded RTP stream
		 */
		@JsonProperty("rtcpRrReceived")
		public Integer rtcpRrReceived;

		/**
		 * If rtx packets are sent on the same stream then this number indicates how may has been sent
		 */
		@JsonProperty("rtxPacketsSent")
		public Integer rtxPacketsSent;

		/**
		 * If rtx packets are received on the same stream then this number indicates how may has been discarded
		 */
		@JsonProperty("rtxPacketsDiscarded")
		public Integer rtxPacketsDiscarded;

		/**
		 * The number of frames sent on the corresponded RTP stream
		 */
		@JsonProperty("framesSent")
		public Integer framesSent;

		/**
		 * Indicate the number of frames the Sfu has been encoded
		 */
		@JsonProperty("framesEncoded")
		public Integer framesEncoded;

		/**
		 * Indicate the number of keyframes the Sfu has been encoded on the corresponded RTP stream
		 */
		@JsonProperty("keyFramesEncoded")
		public Integer keyFramesEncoded;

		/**
		 * Arbitrary attachments holds relevant information about the stream
		 */
		@JsonProperty("attachments")
		public String attachments;

	}
	/**
	 * undefined
	 */

	public static class SctpStream {

		/**
		 * The id of the transport the stream belongs to
		 */
		@JsonProperty("transportId")
		public String transportId;

		/**
		 * The id of the sctp stream
		 */
		@JsonProperty("streamId")
		public String streamId;

		/**
		 * The label of the sctp stream
		 */
		@JsonProperty("label")
		public String label;

		/**
		 * The protocol used to establish an sctp stream
		 */
		@JsonProperty("protocol")
		public String protocol;

		/**
		 * The latest smoothed round-trip time value, corresponding to spinfo_srtt defined in [RFC6458] but converted to seconds. If there has been no round-trip time measurements yet, this value is undefined.
		 */
		@JsonProperty("sctpSmoothedRoundTripTime")
		public Double sctpSmoothedRoundTripTime;

		/**
		 * The latest congestion window, corresponding to spinfo_cwnd defined in [RFC6458].
		 */
		@JsonProperty("sctpCongestionWindow")
		public Double sctpCongestionWindow;

		/**
		 * The latest receiver window, corresponding to sstat_rwnd defined in [RFC6458].
		 */
		@JsonProperty("sctpReceiverWindow")
		public Double sctpReceiverWindow;

		/**
		 * The latest maximum transmission unit, corresponding to spinfo_mtu defined in [RFC6458].
		 */
		@JsonProperty("sctpMtu")
		public Integer sctpMtu;

		/**
		 * The number of unacknowledged DATA chunks, corresponding to sstat_unackdata defined in [RFC6458].
		 */
		@JsonProperty("sctpUnackData")
		public Integer sctpUnackData;

		/**
		 * The number of message received on the corresponded SCTP stream
		 */
		@JsonProperty("messageReceived")
		public Integer messageReceived;

		/**
		 * The number of message sent on the corresponded SCTP stream
		 */
		@JsonProperty("messageSent")
		public Integer messageSent;

		/**
		 * The number of bytes received on the corresponded SCTP stream
		 */
		@JsonProperty("bytesReceived")
		public Long bytesReceived;

		/**
		 * The number of bytes sent on the corresponded SCTP stream
		 */
		@JsonProperty("bytesSent")
		public Long bytesSent;

	}
	/**
	 * undefined
	 */

	public static class SfuTransport {

		/**
		 * The identifier of the transport
		 */
		@JsonProperty("transportId")
		public String transportId;

		/**
		 * Indicates if this transport is not receive or send traffic outside of the SFU mesh.
		 */
		@JsonProperty("internal")
		public boolean internal;

		/**
		 * Indicate if this message measurements should be kept and oly used as keep alive message for the transports
		 */
		@JsonProperty("skipMeasurements")
		public boolean skipMeasurements;

		/**
		 * Set to the current value of the state attribute of the underlying RTCDtlsTransport.
		 */
		@JsonProperty("dtlsState")
		public String dtlsState;

		/**
		 * Set to the current value of the state attribute of the underlying RTCIceTransport.
		 */
		@JsonProperty("iceState")
		public String iceState;

		/**
		 * The state of the SCTP for this transport
		 */
		@JsonProperty("sctpState")
		public String sctpState;

		/**
		 * Set to the current value of the role attribute of the underlying ICE role.
		 */
		@JsonProperty("iceRole")
		public String iceRole;

		/**
		 * The local address of the ICE candidate selected for the transport (IPv4, IPv6, FQDN)
		 */
		@JsonProperty("localAddress")
		public String localAddress;

		/**
		 * The local port number
		 */
		@JsonProperty("localPort")
		public Integer localPort;

		/**
		 * The protocol used by the transport

		 Possible values: UDP, TCP
		 */
		@JsonProperty("protocol")
		public String protocol;

		/**
		 * The remote address of the ICE candidate selected for the transport (IPv4, IPv6, FQDN)
		 */
		@JsonProperty("remoteAddress")
		public String remoteAddress;

		/**
		 * The remote port number
		 */
		@JsonProperty("remotePort")
		public Integer remotePort;

		/**
		 * The total amount of RTP bytes received on this transport
		 */
		@JsonProperty("rtpBytesReceived")
		public Long rtpBytesReceived;

		/**
		 * The total amount of RTP bytes sent on this transport
		 */
		@JsonProperty("rtpBytesSent")
		public Long rtpBytesSent;

		/**
		 * The total amount of RTP packets received on this transport
		 */
		@JsonProperty("rtpPacketsReceived")
		public Integer rtpPacketsReceived;

		/**
		 * The total amount of RTP packets sent on this transport
		 */
		@JsonProperty("rtpPacketsSent")
		public Integer rtpPacketsSent;

		/**
		 * The total amount of RTP packets lost on this transport
		 */
		@JsonProperty("rtpPacketsLost")
		public Integer rtpPacketsLost;

		/**
		 * The total amount of RTX bytes received on this transport
		 */
		@JsonProperty("rtxBytesReceived")
		public Long rtxBytesReceived;

		/**
		 * The total amount of RTX bytes sent on this transport
		 */
		@JsonProperty("rtxBytesSent")
		public Long rtxBytesSent;

		/**
		 * The total amount of RTX packets received on this transport
		 */
		@JsonProperty("rtxPacketsReceived")
		public Integer rtxPacketsReceived;

		/**
		 * The total amount of RTX packets sent on this transport
		 */
		@JsonProperty("rtxPacketsSent")
		public Integer rtxPacketsSent;

		/**
		 * The total amount of RTX packets discarded on this transport
		 */
		@JsonProperty("rtxPacketsDiscarded")
		public Integer rtxPacketsDiscarded;

		/**
		 * The total amount of SCTP bytes received on this transport
		 */
		@JsonProperty("sctpBytesReceived")
		public Long sctpBytesReceived;

		/**
		 * The total amount of SCTP bytes sent on this transport
		 */
		@JsonProperty("sctpBytesSent")
		public Long sctpBytesSent;

		/**
		 * The total amount of SCTP packets received on this transport
		 */
		@JsonProperty("sctpPacketsReceived")
		public Integer sctpPacketsReceived;

		/**
		 * The total amount of SCTP packets sent on this transport
		 */
		@JsonProperty("sctpPacketsSent")
		public Integer sctpPacketsSent;

	}
}