package org.observertc.webrtc.service.purgatory;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class OutboundStreamMeasurement extends MediaStreamMeasurement {

	public Integer bytesSent;
	public Integer packetsSent;

}
