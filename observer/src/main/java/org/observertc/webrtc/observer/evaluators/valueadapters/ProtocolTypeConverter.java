package org.observertc.webrtc.observer.evaluators.valueadapters;

import java.util.NoSuchElementException;
import org.observertc.webrtc.common.reports.ProtocolType;
import org.observertc.webrtc.observer.dto.webextrapp.Protocol;

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
				throw new NoSuchElementException("ProtocolType for " + protocol.name() + " is not implemented");
		}
	}

}
