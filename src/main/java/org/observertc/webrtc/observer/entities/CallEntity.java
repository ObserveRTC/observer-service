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

package org.observertc.webrtc.observer.entities;

import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.dto.CallDTO;
import org.observertc.webrtc.observer.dto.PeerConnectionDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// To avoid exposing hazelcast serialization specific fields
public class CallEntity {
	public CallDTO call;
	public Set<Long> SSRCs = new HashSet<>();
	public List<PeerConnectionDTO> peerConnections = new ArrayList<>();

	@Override
	public String toString() {
		return ObjectToString.toString(this);
	}

}
