package org.observertc.webrtc.service.samples;

import java.time.LocalDateTime;

public class InboundStreamMeasurement {

	public Integer RTTInMs;
	public Integer bytesReceived;
	public Integer packetsReceived;
	public Integer packetsLost;

	public LocalDateTime sampled;

	public InboundStreamMeasurement() {

	}
}
