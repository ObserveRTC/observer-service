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
//public class InboundRTPReport extends Report {
//
//	public static InboundRTPReport of(
//			UUID observerUUID,
//			UUID peerConnectionUUID,
//			Long SSRC,
//			LocalDateTime timestamp,
//			Long bytesReceived,
//			String decoderImplementation,
//			Long estimatedPlayoutTimestamp,
//			Integer fecPacketsDiscarded,
//			Integer fecPacketsReceived,
//			Integer firCount,
//			Integer framesDecoded,
//			Long headerBytesReceived,
//			Double jitter,
//			Integer keyFramesDecoded,
//			Integer nackCount,
//			Long lastPacketReceivedTimestamp,
//			MediaType mediaType,
//			Integer packetsLost,
//			Integer packetsReceived,
//			Integer pliCount,
//			Double qpSum,
//			Double totalDecodeTime,
//			Double totalInterFrameDelay,
//			Double totalSquaredInterFrameDelay
//	) {
//
//		InboundRTPReport result = new InboundRTPReport();
//		result.observerUUID = observerUUID;
//		result.peerConnectionUUID = peerConnectionUUID;
//		result.SSRC = SSRC;
//		result.timestamp = timestamp;
//		result.bytesReceived = bytesReceived;
//		result.decoderImplementation = decoderImplementation;
//		result.estimatedPlayoutTimestamp = estimatedPlayoutTimestamp;
//		result.fecPacketsDiscarded = fecPacketsDiscarded;
//		result.fecPacketsReceived = fecPacketsReceived;
//		result.firCount = firCount;
//		result.framesDecoded = framesDecoded;
//		result.headerBytesReceived = headerBytesReceived;
//		result.jitter = jitter;
//		result.keyFramesDecoded = keyFramesDecoded;
//		result.nackCount = nackCount;
//		result.lastPacketReceivedTimestamp = lastPacketReceivedTimestamp;
//		result.mediaType = mediaType;
//		result.packetsLost = packetsLost;
//		result.packetsReceived = packetsReceived;
//		result.pliCount = pliCount;
//		result.qpSum = qpSum;
//		result.totalDecodeTime = totalDecodeTime;
//		result.totalInterFrameDelay = totalInterFrameDelay;
//		result.totalSquaredInterFrameDelay = totalSquaredInterFrameDelay;
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
//	public Long bytesReceived;
//	public String decoderImplementation;
//	public Long estimatedPlayoutTimestamp;
//	public Integer fecPacketsDiscarded;
//	public Integer fecPacketsReceived;
//	public Integer firCount;
//	public Integer framesDecoded;
//	public Long headerBytesReceived;
//	public Double jitter;
//	public Integer keyFramesDecoded;
//	public Integer nackCount;
//	public Long lastPacketReceivedTimestamp;
//	public MediaType mediaType;
//	public Integer packetsLost;
//	public Integer packetsReceived;
//	public Integer pliCount;
//	public Double qpSum;
//	public Double totalDecodeTime;
//	public Double totalInterFrameDelay;
//	public Double totalSquaredInterFrameDelay;
//
//	@JsonCreator
//	public InboundRTPReport() {
//		super(ReportType.INBOUND_RTP_REPORT);
//	}
//}
