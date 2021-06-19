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
import org.observertc.webrtc.observer.dto.ClientDTO;

import java.util.*;

// To avoid exposing hazelcast serialization specific fields
public class ClientEntity {

	public static Builder builder() {
		return new Builder();
	}

	private ClientDTO clientDTO;
	private Map<UUID, PeerConnectionEntity> peerConnections = new HashMap<>();

	ClientEntity() {

	}

	public UUID getClientId() {
		return this.clientDTO.clientId;
	}

	@Override
	public int hashCode() {
		return this.clientDTO.clientId.hashCode();
	}

	@Override
	public String toString() {
		return ObjectToString.toString(this);
	}

	@Override
	public boolean equals(Object other) {
		if (Objects.isNull(other) || other instanceof ClientEntity == false) {
			return false;
		}
		ClientEntity otherEntity = (ClientEntity) other;
		if (!this.clientDTO.equals(otherEntity.clientDTO)) return false;
		if (!this.peerConnections.values().stream().allMatch(pcE -> pcE.equals(otherEntity.peerConnections.get(pcE.getPeerConnectionId())))) return false;
		if (!otherEntity.peerConnections.values().stream().allMatch(pcE -> pcE.equals(this.peerConnections.get(pcE.getPeerConnectionId())))) return false;
		return true;
	}

	public ClientDTO getClientDTO() {
		return this.clientDTO;
	}

	public Map<UUID, PeerConnectionEntity> getPeerConnections() {
		return Collections.unmodifiableMap(this.peerConnections);
	}

	public UUID getCallId() {
		return this.clientDTO.callId;
	}

	public static class Builder {
		private final ClientEntity result = new ClientEntity();
		public ClientEntity build() {
			Objects.requireNonNull(this.result.clientDTO);
			return this.result;
		}

		public Builder withClientDTO(ClientDTO clientDTO) {
			this.result.clientDTO = clientDTO;
			return this;
		}

		public Builder withPeerConnectionEntities(Map<UUID, PeerConnectionEntity> peerConnectionEntities) {
			this.result.peerConnections.putAll(peerConnectionEntities);
			return this;
		}

		public Builder withPeerConnectionEntity(PeerConnectionEntity peerConnectionEntity) {
			Objects.requireNonNull(peerConnectionEntity);
			this.result.peerConnections.put(peerConnectionEntity.getPeerConnectionId(), peerConnectionEntity);
			return this;
		}
	}
}
