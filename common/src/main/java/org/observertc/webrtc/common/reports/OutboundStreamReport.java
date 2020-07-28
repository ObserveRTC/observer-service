package org.observertc.webrtc.common.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonTypeName("OUTBOUND_STREAM_REPORT")
public class OutboundStreamReport extends MediaStreamReport {
	public static OutboundStreamReport of(
			UUID observerUUID,
			UUID peerConnectionUUID,
			Long SSRC,
			LocalDateTime firstSample,
			LocalDateTime lastSample,
			MediaStreamRecordReport bytesSentRecord,
			MediaStreamRecordReport packetsSentRecord
	) {
		OutboundStreamReport result = new OutboundStreamReport();
		result.observerUUID = observerUUID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.SSRC = SSRC;
		result.firstSample = firstSample;
		result.lastSample = lastSample;
		result.bytesSentRecord = bytesSentRecord;
		result.packetsSentRecord = packetsSentRecord;
		return result;
	}

	@JsonCreator
	public OutboundStreamReport() {
		super(ReportType.OUTBOUND_STREAM_REPORT);
	}

//	public UUID observerUUID;
//
//	public UUID peerConnectionUUID;
//
//	public Long SSRC;

//	public Long count = 0L;

//	@JsonSerialize(using = LocalDateTimeSerializer.class)
//	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
//	public LocalDateTime firstSample;
//
//	@JsonSerialize(using = LocalDateTimeSerializer.class)
//	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
//	public LocalDateTime lastSample;

	@JsonUnwrapped(prefix = "bytesSentRecord_")
	public MediaStreamRecordReport bytesSentRecord = new MediaStreamRecordReport();

	@JsonUnwrapped(prefix = "packetsSentRecord_")
	public MediaStreamRecordReport packetsSentRecord = new MediaStreamRecordReport();

}
