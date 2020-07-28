package org.observertc.webrtc.common.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonTypeName("INBOUND_STREAM_REPORT")
public class InboundStreamReport extends MediaStreamReport {
	public static InboundStreamReport of(
			UUID observerUUID,
			UUID peerConnectionUUID,
			Long SSRC,
			LocalDateTime firstSample,
			LocalDateTime lastSample,
			MediaStreamRecordReport bytesReceivedRecord,
			MediaStreamRecordReport packetsReceivedRecord,
			MediaStreamRecordReport packetsLostRecord
	) {
		InboundStreamReport result = new InboundStreamReport();
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
	public InboundStreamReport() {
		super(ReportType.INBOUND_STREAM_REPORT);
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

	@JsonUnwrapped(prefix = "bytesReceivedRecord_")
	public MediaStreamRecordReport bytesReceivedRecord = new MediaStreamRecordReport();

	@JsonUnwrapped(prefix = "packetsReceivedRecord_")
	public MediaStreamRecordReport packetsReceivedRecord = new MediaStreamRecordReport();

	@JsonUnwrapped(prefix = "packetsLostRecord_")
	public MediaStreamRecordReport packetsLostRecord = new MediaStreamRecordReport();
}
