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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

// To avoid exposing hazelcast serialization specific fields
public class CallEntity {

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
		return ObjectToString.toString(this);
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

    public static class Builder {

		private final CallEntity result = new CallEntity();

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
