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

package org.observertc.webrtc.observer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

// To avoid exposing hazelcast serialization specific fields
@JsonIgnoreProperties(value = { "classId", "factoryId", "classId" })
public class PeerConnectionDTO implements VersionedPortable {
	private static final Logger logger = LoggerFactory.getLogger(PeerConnectionDTO.class);
	public static final int CLASS_VERSION = 7;

	private static final String CLIENT_ID_FIELD_NAME = "clientId";
	private static final String PEER_CONNECTION_ID_FIELD_NAME = "peerConnectionId";
	private static final String CREATED_FIELD_NAME = "created";

	public static Builder builder() {
		return new Builder();
	}

	public static PeerConnectionDTO of(
			UUID clientId,
			UUID peerConnectionId,
			Long added
	) {
		Objects.requireNonNull(clientId);
		Objects.requireNonNull(peerConnectionId);
		Objects.requireNonNull(added);

		PeerConnectionDTO result = new PeerConnectionDTO();
		result.clientId = clientId;
		result.peerConnectionId = peerConnectionId;
		result.created = added;
		return result;
	}

	public UUID clientId;
	public UUID peerConnectionId;
	public Long created;


//	@Deprecated
//	public Set<Long> SSRCs = new HashSet<>();

	@Override
	public int getFactoryId() {
		return PortableDTOFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return PortableDTOFactory.PEER_CONNECTION_DTO_CLASS_ID;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeByteArray(CLIENT_ID_FIELD_NAME, UUIDAdapter.toBytes(this.clientId));
		writer.writeByteArray(PEER_CONNECTION_ID_FIELD_NAME, UUIDAdapter.toBytes(this.peerConnectionId));
		writer.writeLong(CREATED_FIELD_NAME, this.created);

//		SerDeUtils.writeLongArray(writer, SSRC_FIELD_NAME, this.SSRCs, -1);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.clientId = UUIDAdapter.toUUID(reader.readByteArray(CLIENT_ID_FIELD_NAME));
		this.peerConnectionId = UUIDAdapter.toUUID(reader.readByteArray(PEER_CONNECTION_ID_FIELD_NAME));
		this.created = reader.readLong(CREATED_FIELD_NAME);
	}

	@Override
	public String toString() {
		return ObjectToString.toString(this);
	}

	@Override
	public int getClassVersion() {
		return CLASS_VERSION;
	}

	@Override
	public boolean equals(Object other) {
		if (Objects.isNull(other) || !this.getClass().getName().equals(other.getClass().getName())) {
			return false;
		}
		PeerConnectionDTO otherDTO = (PeerConnectionDTO) other;
		if (!Objects.equals(this.clientId, otherDTO.clientId)) return false;
		if (!Objects.equals(this.peerConnectionId, otherDTO.peerConnectionId)) return false;
		if (!Objects.equals(this.created, otherDTO.created)) return false;
		return true;
	}

	public static class Builder {
		private final PeerConnectionDTO result = new PeerConnectionDTO();

		public PeerConnectionDTO.Builder withPeerConnectionId(UUID value) {
			this.result.peerConnectionId = value;
			return this;
		}

		public PeerConnectionDTO.Builder withClientId(UUID value) {
			this.result.clientId = value;
			return this;
		}

		public PeerConnectionDTO.Builder withCreatedTimestamp(Long value) {
			this.result.created = value;
			return this;
		}

		public PeerConnectionDTO build() {
			Objects.requireNonNull(this.result.peerConnectionId);
			Objects.requireNonNull(this.result.clientId);
			Objects.requireNonNull(this.result.created);
			return this.result;
		}
	}
}
