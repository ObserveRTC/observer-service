package org.observertc.webrtc.service.evaluators.valueadapters;

import org.apache.commons.lang3.NotImplementedException;
import org.observertc.webrtc.common.reports.ProtocolType;
import org.observertc.webrtc.service.dto.webextrapp.Protocol;

public class ProtocolTypeConverter {

	public static ProtocolType fromDTOProtocolType(Protocol protocol) {
		if (protocol == null) {
			return null;
		}
		switch (protocol) {
			case TCP:
				return ProtocolType.TCP;
			case UDP:
				return ProtocolType.UDP;
			default:
				throw new NotImplementedException("ProtocolType for " + protocol.name() + " is not implemented");
		}
	}

}
