package org.observertc.webrtc.common.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.UUID;

public class MediaSourceReport extends Report {

	public static MediaSourceReport of(UUID observerUUID,
									   UUID peerConnectionUUID,
									   LocalDateTime timestamp,
									   String mediaSourceID,
									   Double audioLevel,
									   Double framesPerSecond,
									   Integer height,
									   Integer width,
									   MediaType mediaType,
									   Double totalAudioEnergy,
									   Double totalSamplesDuration
	) {
		MediaSourceReport result = new MediaSourceReport();
		result.observerUUID = observerUUID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.timestamp = timestamp;
		result.mediaSourceID = mediaSourceID;
		result.audioLevel = audioLevel;
		result.framesPerSecond = framesPerSecond;
		result.height = height;
		result.width = width;
		result.mediaType = mediaType;
		result.totalAudioEnergy = totalAudioEnergy;
		result.totalSamplesDuration = totalSamplesDuration;
		return result;
	}

	public UUID observerUUID;

	public UUID peerConnectionUUID;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime timestamp;

	public String mediaSourceID;

	public Double audioLevel;

	public Double framesPerSecond;

	public Integer height;

	public Integer width;

	public MediaType mediaType;

	public Double totalAudioEnergy;

	public Double totalSamplesDuration;

	@JsonCreator
	public MediaSourceReport() {
		super(ReportType.MEDIA_SOURCE_REPORT);
	}
}
