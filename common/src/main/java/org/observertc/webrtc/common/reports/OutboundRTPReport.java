//package org.observertc.webrtc.common.reports;
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
//import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
//import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//public class OutboundRTPReport extends Report {
//
//	public static OutboundRTPReport of(
//			UUID observerUUID,
//			UUID peerConnectionUUID,
//			Long SSRC,
//			LocalDateTime timestamp,
//			Long bytesSent,
//			String encoderImplementation,
//			Integer firCount,
//			Integer framesEncoded,
//			Long headerBytesSent,
//			Integer keyFramesEncoded,
//			Integer nackCount,
//			MediaType mediaType,
//			Integer packetsSent,
//			Integer pliCount,
//			Double qpSum,
//			String qualityLimitationReason,
//			Double qualityLimitationResolutionChanges,
//			Long retransmittedBytesSent,
//			Integer retransmittedPacketsSent,
//			Long totalEncodedBytesTarget,
//			Long totalEncodeTime,
//			Double totalPacketSendDelay
//	) {
//
//		OutboundRTPReport result = new OutboundRTPReport();
//		result.observerUUID = observerUUID;
//		result.peerConnectionUUID = peerConnectionUUID;
//		result.SSRC = SSRC;
//		result.timestamp = timestamp;
//		result.bytesSent = bytesSent;
//		result.encoderImplementation = encoderImplementation;
//		result.firCount = firCount;
//		result.framesEncoded = framesEncoded;
//		result.headerBytesSent = headerBytesSent;
//		result.keyFramesEncoded = keyFramesEncoded;
//		result.nackCount = nackCount;
//		result.mediaType = mediaType;
//		result.packetsSent = packetsSent;
//		result.pliCount = pliCount;
//		result.qpSum = qpSum;
//		result.qualityLimitationReason = qualityLimitationReason;
//		result.qualityLimitationResolutionChanges = qualityLimitationResolutionChanges;
//		result.retransmittedBytesSent = retransmittedBytesSent;
//		result.retransmittedPacketsSent = retransmittedPacketsSent;
//		result.totalEncodedBytesTarget = totalEncodedBytesTarget;
//		result.totalEncodeTime = totalEncodeTime;
//		result.totalPacketSendDelay = totalPacketSendDelay;
//		return result;
//	}
//
//	public UUID observerUUID;
//
//	public UUID peerConnectionUUID;
//
//	public Long SSRC;
//
//	@JsonSerialize(using = LocalDateTimeSerializer.class)
//	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
//	public LocalDateTime timestamp;
//
//	public Long bytesSent;
//	public String codecID;
//	public String encoderImplementation;
//	public Integer firCount;
//	public Integer framesEncoded;
//	public Long headerBytesSent;
//	public Integer keyFramesEncoded;
//	public MediaType mediaType;
//	public Integer nackCount;
//	public Integer packetsSent;
//	public Integer pliCount;
//	public Double qpSum;
//	public String qualityLimitationReason;
//	public Double qualityLimitationResolutionChanges;
//	public Long retransmittedBytesSent;
//	public Integer retransmittedPacketsSent;
//	public Long totalEncodedBytesTarget;
//	public Long totalEncodeTime;
//	public Double totalPacketSendDelay;
//
//	@JsonCreator
//	public OutboundRTPReport() {
//		super(ReportType.OUTBOUND_RTP_REPORT);
//	}
//}
