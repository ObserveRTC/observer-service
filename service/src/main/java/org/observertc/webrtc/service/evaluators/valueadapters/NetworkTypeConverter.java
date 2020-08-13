package org.observertc.webrtc.service.evaluators.valueadapters;

import org.apache.commons.lang3.NotImplementedException;
import org.observertc.webrtc.common.reports.NetworkType;

public class NetworkTypeConverter {

	public static NetworkType fromDTONetworkType(org.observertc.webrtc.service.dto.webextrapp.NetworkType networkType) {
		if (networkType == null) {
			return null;
		}
		switch (networkType) {
			case VPN:
				return NetworkType.VPN;
			case WIFI:
				return NetworkType.WIFI;
			case WIMAX:
				return NetworkType.WIMAX;
			case CELLULAR:
				return NetworkType.CELLULAR;
			case ETHERNET:
				return NetworkType.ETHERNET;
			case BLUETOOTH:
				return NetworkType.BLUETOOTH;
			case UNKNOWN:
				return NetworkType.UNKNOWN;
			default:
				throw new NotImplementedException("NetworkType for " + networkType.name() + " is not implemented");
		}
	}

}
