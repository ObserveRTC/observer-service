package org.observertc.webrtc.service.samples;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class OutboundStreamMeasurement extends MediaStreamMeasurement {

	public Integer bytesSent;
	public Integer packetsSent;

}
