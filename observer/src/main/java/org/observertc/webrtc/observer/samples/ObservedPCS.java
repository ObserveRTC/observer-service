/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.observertc.webrtc.observer.samples;

import java.util.UUID;
import org.observertc.webrtc.common.ObjectToString;
import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;

public class ObservedPCS {


	public static ObservedPCS of(UUID serviceUUID,
								 String mediaUnit,
								 UUID peerConnectionUUID,
								 PeerConnectionSample peerConnectionSample,
								 String timeZoneID,
								 String serviceName,
								 String marker,
								 Long timestamp) {
		ObservedPCS result = new ObservedPCS();
		result.serviceUUID = serviceUUID;
		result.mediaUnitId = mediaUnit;
		result.peerConnectionUUID = peerConnectionUUID;
		result.peerConnectionSample = peerConnectionSample;
		result.timeZoneID = timeZoneID;
		result.timestamp = timestamp;
		result.serviceName = serviceName;
		result.marker = marker;
		return result;
	}

	public UUID serviceUUID;
	public String mediaUnitId;
	public UUID peerConnectionUUID;
	public PeerConnectionSample peerConnectionSample;
	public String timeZoneID;
	public Long timestamp;
	public String serviceName;
	public String marker;

	public String toString() {
		return ObjectToString.toString(this);
	}
}
