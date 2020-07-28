package org.observertc.webrtc.common.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonTypeName("REMOTE_INBOUND_STREAM_REPORT")
public class RemoteInboundStreamReport extends MediaStreamReport {
	public static RemoteInboundStreamReport of(
			UUID observerUUID,
			UUID peerConnectionUUID,
			Long SSRC,
			LocalDateTime firstSample,
			LocalDateTime lastSample,
			MediaStreamRecordReport RTTInMsRecord
	) {
		RemoteInboundStreamReport result = new RemoteInboundStreamReport();
		result.observerUUID = observerUUID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.SSRC = SSRC;
		result.firstSample = firstSample;
		result.lastSample = lastSample;
		result.RTTInMsRecord = RTTInMsRecord;
		return result;
	}

	@JsonCreator
	public RemoteInboundStreamReport() {
		super(ReportType.REMOTE_INBOUND_STREAM_REPORT);
	}

//	public UUID observerUUID;
//
//	public UUID peerConnectionUUID;
//
//	public Long SSRC;
//
//	public Long count = 0L;
//
//	@JsonSerialize(using = LocalDateTimeSerializer.class)
//	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
//	public LocalDateTime firstSample;
//
//	@JsonSerialize(using = LocalDateTimeSerializer.class)
//	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
//	public LocalDateTime lastSample;

	@JsonUnwrapped(prefix = "RTTInMsRecord_")
	public MediaStreamRecordReport RTTInMsRecord = new MediaStreamRecordReport();


}
