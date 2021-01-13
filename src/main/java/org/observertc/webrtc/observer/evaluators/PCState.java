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

package org.observertc.webrtc.observer.evaluators;//package com.observertc.gatekeeper.webrtcstat.processors.samples;

import org.observertc.webrtc.observer.common.ObjectToString;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PCState {

	public static PCState of(
			UUID serviceUUID,
			UUID peerConnectionUUID,
			Long created,
			String browserID,
			String callName,
			String timeZoneID,
			String userId,
			String mediaUnitID,
			String serviceName,
			String marker
	) {
		PCState result = new PCState();
		result.serviceUUID = serviceUUID;
		result.serviceName = serviceName;
		result.mediaUnitID = mediaUnitID;
		result.peerConnectionUUID = peerConnectionUUID;
		result.created = result.updated = created;
		result.browserId = browserID;
		result.timeZoneId = timeZoneID;
		result.callName = callName;
		result.userId = userId;
		result.marker = marker;
		return result;
	}

	public Set<Long> SSRCs = new HashSet<>();
	public UUID serviceUUID;
	public String serviceName;
	public String mediaUnitID;
	public UUID peerConnectionUUID;
	public Long created;
	public Long updated;
	public String browserId;
	public String timeZoneId;
	public String callName;
	public String userId;
	public String marker;

	/**
	 * The timestamp when the pcState is touched last time
	 * automatically initialized when the object has been created
	 */
	public Instant touched = Instant.now();


	@Override
	public String toString() {
		return ObjectToString.toString(this);
	}
}
