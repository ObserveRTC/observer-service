package org.observertc.webrtc.service.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.micronaut.core.annotation.Introspected;
import java.time.LocalDateTime;
import java.util.UUID;

@Introspected
public class InboundStreamMeasurementDTO {
	public UUID observerUUID;
	public UUID peerConnectionUUID;
	public Long SSRC;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime firstSample = null;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime lastSample = null;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime reported = null;
	public Integer samples_count = 0;

	public Integer bytesReceived_count = 0;
	public Long bytesReceived_sum = 0L;
	public Integer bytesReceived_min = null;
	public Integer bytesReceived_max = null;
	public Integer bytesReceived_last = null;

	public Integer packetsReceived_count = 0;
	public Integer packetsReceived_sum = 0;
	public Integer packetsReceived_min = null;
	public Integer packetsReceived_max = null;
	public Integer packetsReceived_last = null;

	public Integer packetsLost_count = 0;
	public Integer packetsLost_sum = 0;
	public Integer packetsLost_min = null;
	public Integer packetsLost_max = null;
	public Integer packetsLost_last = null;

	public Integer decoded_frames_count = 0;
	public Integer qpSum_count = 0;
	public Integer qpSum_sum = 0;
	public Integer qpSum_min = null;
	public Integer qpSum_max = null;
	public Integer qpSum_last = null;
}
