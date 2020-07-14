package org.observertc.webrtc.service.dto;

import io.micronaut.core.annotation.Introspected;
import java.time.LocalDateTime;
import java.util.UUID;

@Introspected
public class OutboundStreamMeasurementDTO {
	public UUID observerUUID;
	public UUID peerConnectionUUID;
	public Long SSRC;
	public LocalDateTime firstSample = null;
	public LocalDateTime lastSample = null;
	public LocalDateTime reported = null;
	public Integer samples_count = 0;

	public Integer bytesSent_count = 0;
	public Long bytesSent_sum = 0L;
	public Integer bytesSent_min = null;
	public Integer bytesSent_max = null;
	public Integer bytesSent_last = null;

	public Integer packetsSent_count = 0;
	public Integer packetsSent_sum = 0;
	public Integer packetsSent_min = null;
	public Integer packetsSent_max = null;
	public Integer packetsSent_last = null;

	public Integer encoded_frames_count = 0;
	public Integer qpSum_count = 0;
	public Integer qpSum_sum = 0;
	public Integer qpSum_min = null;
	public Integer qpSum_max = null;
	public Integer qpSum_last = null;

}
