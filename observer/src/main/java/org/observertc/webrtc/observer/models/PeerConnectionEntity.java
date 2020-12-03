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

package org.observertc.webrtc.observer.models;

import java.util.UUID;

public class PeerConnectionEntity {

	public static PeerConnectionEntity of(
			UUID serviceUUID,
			String serviceName,
			String mediaUnitId,
			UUID callUUID,
			String callName,
			UUID peerConnectionUUID,
			String providedUserName,
			String browserId,
			String timeZone,
			Long joined,
			Long updated,
			Long detached,
			String marker
	) {
		PeerConnectionEntity result = new PeerConnectionEntity();
		result.serviceUUID = serviceUUID;
		result.serviceName = serviceName;
		result.mediaUnitId = mediaUnitId;
		result.callUUID = callUUID;
		result.callName = callName;
		result.peerConnectionUUID = peerConnectionUUID;
		result.providedUserName = providedUserName;
		result.browserId = browserId;
		result.timeZone = timeZone;
		result.joined = joined;
		result.updated = updated;
		result.detached = detached;
		result.marker = marker;
		return result;
	}

	public UUID serviceUUID;
	public String serviceName;
	public String mediaUnitId;
	public UUID callUUID;
	public String callName;
	public UUID peerConnectionUUID;
	public String providedUserName;
	public String browserId;
	public String timeZone;
	public Long joined;
	public Long updated;
	public Long detached;
	public String marker;
}
