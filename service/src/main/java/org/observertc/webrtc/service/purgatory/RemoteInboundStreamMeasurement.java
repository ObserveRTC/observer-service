package org.observertc.webrtc.service.purgatory;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class RemoteInboundStreamMeasurement extends MediaStreamMeasurement {

	public Integer RTTInMs = null;

}
