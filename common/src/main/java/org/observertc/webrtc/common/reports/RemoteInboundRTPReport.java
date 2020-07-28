package org.observertc.webrtc.common.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.UUID;

public class RemoteInboundRTPReport extends Report {

	public static RemoteInboundRTPReport of(UUID observerUUID,
											UUID peerConnectionUUID,
											Long SSRC,
											LocalDateTime timestamp,
											String codec,
											Double jitter,
											Integer packetsLost,
											Double RTT,
											MediaType mediaType) {
		RemoteInboundRTPReport result = new RemoteInboundRTPReport();
		result.observerUUID = observerUUID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.SSRC = SSRC;
		result.timestamp = timestamp;
		result.codec = codec;
		result.jitter = jitter;
		result.packetsLost = packetsLost;
		result.RTT = RTT;
		result.mediaType = mediaType;
		return result;
	}

	public UUID observerUUID;

	public UUID peerConnectionUUID;

	public Long SSRC;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime timestamp;

	public String codec;

	public Double jitter;

	public Integer packetsLost;

	public Double RTT;

	public MediaType mediaType;

	@JsonCreator
	public RemoteInboundRTPReport() {
		super(ReportType.REMOTE_INBOUND_RTP_REPORT);
	}
}
