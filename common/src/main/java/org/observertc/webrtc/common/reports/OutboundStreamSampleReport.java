package org.observertc.webrtc.common.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonTypeName("OUTBOUND_STREAM_SAMPLE")
public class OutboundStreamSampleReport extends Report {
	public static OutboundStreamSampleReport of(
			UUID observerUUID,
			UUID peerConnectionUUID,
			Long SSRC,
			LocalDateTime firstSample,
			LocalDateTime lastSample,
			MediaStreamSampleRecordReport bytesSentRecord,
			MediaStreamSampleRecordReport packetsSentRecord
	) {
		OutboundStreamSampleReport result = new OutboundStreamSampleReport();
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
	public OutboundStreamSampleReport() {
		super(ReportType.OUTBOUND_STREAM_SAMPLE);
//		super(type);
	}

	public UUID observerUUID;

	public UUID peerConnectionUUID;

	public Long SSRC;

	public Long count = 0L;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime firstSample;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime lastSample;

	@JsonUnwrapped(prefix = "bytesSentRecord_")
	public MediaStreamSampleRecordReport bytesSentRecord = new MediaStreamSampleRecordReport();

	@JsonUnwrapped(prefix = "packetsSentRecord_")
	public MediaStreamSampleRecordReport packetsSentRecord = new MediaStreamSampleRecordReport();

}
