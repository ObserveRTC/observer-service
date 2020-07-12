package org.observertc.webrtc.service.samples;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.micronaut.core.annotation.Introspected;
import java.time.LocalDateTime;

@Introspected
public class InboundStreamMeasurement {

	@JsonUnwrapped
	public InboundStreamMeasurement last;

	public Integer RTTInMs;
	public Integer bytesReceived;
	public Integer packetsReceived;
	public Integer packetsLost;
	public Integer qpSum;

	public LocalDateTime sampled;

	public InboundStreamMeasurement() {

	}
}
