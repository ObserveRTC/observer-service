package org.observertc.webrtc.common.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonTypeName("INBOUND_STREAM_SAMPLE")
public class InboundStreamSampleReport extends Report {
	public static InboundStreamSampleReport of(
			UUID observerUUID,
			UUID peerConnectionUUID,
			Long SSRC,
			LocalDateTime firstSample,
			LocalDateTime lastSample,
			MediaStreamSampleRecordReport bytesReceivedRecord,
			MediaStreamSampleRecordReport packetsReceivedRecord,
			MediaStreamSampleRecordReport packetsLostRecord
	) {
		InboundStreamSampleReport result = new InboundStreamSampleReport();
		result.observerUUID = observerUUID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.SSRC = SSRC;
		result.firstSample = firstSample;
		result.lastSample = lastSample;
		result.bytesReceivedRecord = bytesReceivedRecord;
		result.packetsLostRecord = packetsLostRecord;
		result.packetsReceivedRecord = packetsReceivedRecord;
		return result;
	}

	@JsonCreator
	public InboundStreamSampleReport() {
		super(ReportType.INBOUND_STREAM_SAMPLE);
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

	@JsonUnwrapped(prefix = "bytesReceivedRecord_")
	public MediaStreamSampleRecordReport bytesReceivedRecord = new MediaStreamSampleRecordReport();

	@JsonUnwrapped(prefix = "packetsReceivedRecord_")
	public MediaStreamSampleRecordReport packetsReceivedRecord = new MediaStreamSampleRecordReport();

	@JsonUnwrapped(prefix = "packetsLostRecord_")
	public MediaStreamSampleRecordReport packetsLostRecord = new MediaStreamSampleRecordReport();
}
