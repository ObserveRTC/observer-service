package org.observertc.webrtc.service.purgatory;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class InboundStreamMeasurement extends MediaStreamMeasurement {

	public Integer bytesReceived = null;
	public Integer packetsReceived = null;
	public Integer packetsLost = null;

}
