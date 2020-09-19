package org.observertc.webrtc.observer.samples;

import java.util.UUID;
import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;

public class ObservedPCS {


	public static ObservedPCS of(UUID serviceUUID,
								 String mediaUnit,
								 UUID peerConnectionUUID,
								 PeerConnectionSample peerConnectionSample,
								 String timeZoneID,
								 String serviceName,
								 Long timestamp) {
		ObservedPCS result = new ObservedPCS();
		result.serviceUUID = serviceUUID;
		result.mediaUnit = mediaUnit;
		result.peerConnectionUUID = peerConnectionUUID;
		result.peerConnectionSample = peerConnectionSample;
		result.timeZoneID = timeZoneID;
		result.timestamp = timestamp;
		result.serviceName = serviceName;
		return result;
	}

	public UUID serviceUUID;
	public String mediaUnit;
	public UUID peerConnectionUUID;
	public PeerConnectionSample peerConnectionSample;
	public String timeZoneID;
	public Long timestamp;
	public String serviceName;


}
