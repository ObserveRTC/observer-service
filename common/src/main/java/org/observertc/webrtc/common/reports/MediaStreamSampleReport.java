package org.observertc.webrtc.common.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.UUID;

public class MediaStreamSampleReport extends Report {
	public static MediaStreamSampleReport of(
			UUID observerUUID,
			UUID peerConnectionUUID,
			Long SSRC,
			LocalDateTime firstSample,
			LocalDateTime lastSample,
			MediaStreamSampleRecordReport RTTRecord,
			MediaStreamSampleRecordReport bytesReceivedRecord,
			MediaStreamSampleRecordReport bytesSentRecord,
			MediaStreamSampleRecordReport packetsLostRecord,
			MediaStreamSampleRecordReport packetsReceivedRecord,
			MediaStreamSampleRecordReport packetsSentRecord
	) {
		MediaStreamSampleReport result = new MediaStreamSampleReport();
		result.observerUUID = observerUUID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.SSRC = SSRC;
		result.firstSample = firstSample;
		result.lastSample = lastSample;
		result.RTTRecord = RTTRecord;
		result.bytesReceivedRecord = bytesReceivedRecord;
		result.bytesSentRecord = bytesSentRecord;
		result.packetsLostRecord = packetsLostRecord;
		result.packetsReceivedRecord = packetsReceivedRecord;
		result.packetsSentRecord = packetsSentRecord;
		return result;
	}

	@JsonCreator
	public MediaStreamSampleReport() {
		super(ReportType.MEDIA_STREAM_SAMPLE);
	}

	public UUID peerConnectionUUID;

	public UUID observerUUID;

	public Long SSRC;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime firstSample;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime lastSample;

	@JsonUnwrapped
	public MediaStreamSampleRecordReport RTTRecord;

	@JsonUnwrapped
	public MediaStreamSampleRecordReport bytesReceivedRecord;

	@JsonUnwrapped
	public MediaStreamSampleRecordReport bytesSentRecord;

	@JsonUnwrapped
	public MediaStreamSampleRecordReport packetsSentRecord;

	@JsonUnwrapped
	public MediaStreamSampleRecordReport packetsReceivedRecord;

	@JsonUnwrapped
	public MediaStreamSampleRecordReport packetsLostRecord;
}
