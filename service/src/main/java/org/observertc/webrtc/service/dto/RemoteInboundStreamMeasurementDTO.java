package org.observertc.webrtc.service.dto;

import io.micronaut.core.annotation.Introspected;
import java.time.LocalDateTime;
import java.util.UUID;

@Introspected
public class RemoteInboundStreamMeasurementDTO {
	public UUID observerUUID;
	public UUID peerConnectionUUID;
	public Long SSRC;
	public LocalDateTime firstSample = null;
	public LocalDateTime lastSample = null;
	public LocalDateTime reported = null;
	public Integer samples_count = 0;

	public Integer RTTInMs_count = 0;
	public Integer RTTInMs_sum = 0;
	public Integer RTTInMs_min = null;
	public Integer RTTInMs_max = null;
	public Integer RTTInMs_last = null;

}
