package com.observertc.gatekeeper.webrtcstat.samples;

import java.time.LocalDateTime;

public class OutboundStreamMeasurement {

	public Integer RTTInMs;
	public Integer bytesSent;
	public Integer packetsSent;

	public LocalDateTime sampled;

	public OutboundStreamMeasurement() {

	}
}
