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

import java.util.*;
import java.util.stream.Collectors;

// To avoid exposing hazelcast serialization specific fields
public class CallEntity {

	public static Builder builder() {
		return new Builder();
	}

	public final CallDTO call;
	public final Set<Long> SSRCs;
	public final Map<UUID, PeerConnectionEntity> peerConnections;

	private CallEntity(CallDTO call, Set<Long> ssrCs, Map<UUID, PeerConnectionEntity> peerConnections) {
		this.call = call;
		SSRCs = ssrCs;
		this.peerConnections = peerConnections;
	}

	@Override
	public boolean equals(Object other) {
		if (Objects.isNull(other) || other instanceof CallEntity == false) {
			return false;
		}
		CallEntity otherEntity = (CallEntity) other;
		if (!this.call.equals(otherEntity.call)) return false;
		if (!this.SSRCs.stream().allMatch(otherEntity.SSRCs::contains)) return false;
		if (!otherEntity.SSRCs.stream().allMatch(this.SSRCs::contains)) return false;
		if (!this.peerConnections.values().stream().allMatch(pcE -> pcE.equals(otherEntity.peerConnections.get(pcE.pcUUID)))) return false;
		if (!otherEntity.peerConnections.values().stream().allMatch(pcE -> pcE.equals(this.peerConnections.get(pcE.pcUUID)))) return false;
		return true;
	}

	@Override
	public int hashCode() {
		return this.call.callUUID.hashCode();
	}

	@Override
	public String toString() {
		return ObjectToString.toString(this);
	}

	public static class Builder {
		public CallDTO callDTO;
		public Map<UUID, PeerConnectionEntity> peerConnections = new HashMap<>();

		public CallEntity build() {
			Objects.requireNonNull(this.callDTO);
			Objects.requireNonNull(this.peerConnections);
			Set<Long> SSRCs = this.peerConnections.values().stream().flatMap(pc -> pc.SSRCs.stream()).collect(Collectors.toSet());
			return new CallEntity(this.callDTO,
					Collections.unmodifiableSet(SSRCs),
					Collections.unmodifiableMap(this.peerConnections)
			);
		}

		public Builder withCallDTO(CallDTO callDTO) {
			this.callDTO = callDTO;
			return this;
		}

		public Builder withPeerConnections(Map<UUID, PeerConnectionEntity> peerConnectionEntities) {
			this.peerConnections.putAll(peerConnectionEntities);
			return this;
		}
    }
}
