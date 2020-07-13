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

@JsonTypeName("REMOTE_INBOUND_STREAM_SAMPLE")
public class RemoteInboundStreamSampleReport extends Report {
	public static RemoteInboundStreamSampleReport of(
			UUID observerUUID,
			UUID peerConnectionUUID,
			Long SSRC,
			LocalDateTime firstSample,
			LocalDateTime lastSample,
			MediaStreamSampleRecordReport RTTInMsRecord
	) {
		RemoteInboundStreamSampleReport result = new RemoteInboundStreamSampleReport();
		result.observerUUID = observerUUID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.SSRC = SSRC;
		result.firstSample = firstSample;
		result.lastSample = lastSample;
		result.RTTInMsRecord = RTTInMsRecord;
		return result;
	}

	@JsonCreator
	public RemoteInboundStreamSampleReport() {
		super(ReportType.REMOTE_INBOUND_STREAM_SAMPLE);
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

	@JsonUnwrapped(prefix = "RTTInMsRecord_")
	public MediaStreamSampleRecordReport RTTInMsRecord = new MediaStreamSampleRecordReport();


}
