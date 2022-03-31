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

package org.observertc.observer.entities;

import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.dto.CallDTO;
import org.observertc.observer.dto.ClientDTO;
import org.observertc.observer.dto.MediaTrackDTO;
import org.observertc.observer.dto.PeerConnectionDTO;

import java.util.*;

// To avoid exposing hazelcast serialization specific fields
public class CallEntity implements Iterable<ClientEntity> {
	public static CallEntity from(CallDTO callDTO,
								  Map<UUID, ClientDTO> clientDTOs,
								  Map<UUID, PeerConnectionDTO> peerConnectionDTOs,
								  Map<UUID, MediaTrackDTO> mediaTrackDTOMap) {
		var result = new CallEntity();
		result.callDTO = callDTO;
		for (var clientDTO : clientDTOs.values()) {
			if (clientDTO.callId != callDTO.callId) continue;
			var clientEntity = ClientEntity.from(clientDTO, peerConnectionDTOs, mediaTrackDTOMap);
			result.clients.put(clientEntity.getClientId(), clientEntity);
		}
		return result;
	}
	public static Builder builder() {
		return new Builder();
	}

	private CallDTO callDTO;
	private Map<UUID, ClientEntity> clients = new HashMap<>();

	CallEntity() {

	}

	public UUID getCallId() {
		return this.callDTO.callId;
	}

	public ClientEntity getClientEntity(UUID clientId) {
		return this.clients.get(clientId);
	}

	@Override
	public int hashCode() {
		return this.callDTO.callId.hashCode();
	}

	@Override
	public String toString() {
		return JsonUtils.objectToString(this);
	}

	@Override
	public boolean equals(Object other) {
		if (Objects.isNull(other) || other instanceof CallEntity == false) {
			return false;
		}
		CallEntity otherEntity = (CallEntity) other;
		if (!this.callDTO.equals(otherEntity.callDTO)) return false;
		if (!this.clients.values().stream().allMatch(clientEntity -> clientEntity.equals(otherEntity.getClientEntity(clientEntity.getClientId())))) return false;
		if (!otherEntity.clients.values().stream().allMatch(clientEntity -> clientEntity.equals(this.clients.get(clientEntity.getClientId())))) return false;
		return true;
	}

	public CallDTO getCallDTO() {
		return this.callDTO;
	}

	@Override
	public Iterator<ClientEntity> iterator() {
		return this.clients.values().iterator();
	}

	public static class Builder {

		private final CallEntity result = new CallEntity();

		public Builder from(CallEntity source) {
			return this.withCallDTO(source.getCallDTO())
					.withClientEntities(source.clients)
					;
		}

		public CallEntity build() {
			Objects.requireNonNull(this.result.callDTO);
			return this.result;
		}

		public Builder withCallDTO(CallDTO callDTO) {
			this.result.callDTO = callDTO;
			return this;
		}

		public Builder withClientEntities(Map<UUID, ClientEntity> clientEntities) {
			this.result.clients.putAll(clientEntities);
			return this;
		}

		public Builder withClientEntity(ClientEntity clientEntity) {
			this.result.clients.put(clientEntity.getClientId(), clientEntity);
			return this;
		}


	}
}
