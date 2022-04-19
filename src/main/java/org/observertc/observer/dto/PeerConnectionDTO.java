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

package org.observertc.observer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.common.UUIDAdapter;
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

	private static final String CALL_ID_FIELD_NAME = "callId";
	private static final String SERVICE_ID_FIELD_NAME = "serviceId";
	private static final String ROOM_ID_FIELD_NAME = "roomId";

	private static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitId";
	private static final String USER_ID_FIELD_NAME = "userId";

	private static final String CLIENT_ID_FIELD_NAME = "clientId";
	private static final String PEER_CONNECTION_ID_FIELD_NAME = "peerConnectionId";
	private static final String CREATED_FIELD_NAME = "created";
	private static final String MARKER_FIELD_NAME = "marker";

	public static Builder builder() {
		return new Builder();
	}

	public UUID callId;
	public String serviceId;
	public String roomId;

	public String mediaUnitId;
	public String userId;

	public UUID clientId;
	public UUID peerConnectionId;
	public Long created;
	public String marker;

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
		writer.writeByteArray(CALL_ID_FIELD_NAME, UUIDAdapter.toBytes(this.callId));
		writer.writeString(SERVICE_ID_FIELD_NAME, this.serviceId);
		writer.writeString(ROOM_ID_FIELD_NAME, this.roomId);

		writer.writeString(MEDIA_UNIT_ID_FIELD_NAME, this.mediaUnitId);
		writer.writeString(USER_ID_FIELD_NAME, this.userId);

		writer.writeByteArray(CLIENT_ID_FIELD_NAME, UUIDAdapter.toBytes(this.clientId));
		writer.writeByteArray(PEER_CONNECTION_ID_FIELD_NAME, UUIDAdapter.toBytes(this.peerConnectionId));
		writer.writeLong(CREATED_FIELD_NAME, this.created);

		SerDeUtils.writeNullableString(writer, MARKER_FIELD_NAME, this.marker);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.callId = UUIDAdapter.toUUID(reader.readByteArray(CALL_ID_FIELD_NAME));
		this.serviceId = reader.readString(SERVICE_ID_FIELD_NAME);
		this.roomId = reader.readString(ROOM_ID_FIELD_NAME);

		this.mediaUnitId = reader.readString(MEDIA_UNIT_ID_FIELD_NAME);
		this.userId = reader.readString(USER_ID_FIELD_NAME);

		this.clientId = UUIDAdapter.toUUID(reader.readByteArray(CLIENT_ID_FIELD_NAME));
		this.peerConnectionId = UUIDAdapter.toUUID(reader.readByteArray(PEER_CONNECTION_ID_FIELD_NAME));
		this.created = reader.readLong(CREATED_FIELD_NAME);

		this.marker = SerDeUtils.readNullableString(reader, MARKER_FIELD_NAME);
	}

	@Override
	public String toString() {
		return JsonUtils.objectToString(this);
	}

	@Override
	public int getClassVersion() {
		return CLASS_VERSION;
	}

	@Override
	public boolean equals(Object other) {
		if (Objects.isNull(other) || !this.getClass().isAssignableFrom(other.getClass())) {
			return false;
		}
		PeerConnectionDTO otherDTO = (PeerConnectionDTO) other;
		if (!Objects.equals(this.callId, otherDTO.callId)) return false;
		if (!Objects.equals(this.serviceId, otherDTO.serviceId)) return false;
		if (!Objects.equals(this.roomId, otherDTO.roomId)) return false;

		if (!Objects.equals(this.mediaUnitId, otherDTO.mediaUnitId)) return false;
		if (!Objects.equals(this.userId, otherDTO.userId)) return false;

		if (!Objects.equals(this.clientId, otherDTO.clientId)) return false;
		if (!Objects.equals(this.peerConnectionId, otherDTO.peerConnectionId)) return false;
		if (!Objects.equals(this.created, otherDTO.created)) return false;
		if (!Objects.equals(this.marker, otherDTO.marker)) return false;
		return true;
	}

	public static class Builder {
		private final PeerConnectionDTO result = new PeerConnectionDTO();

		public PeerConnectionDTO.Builder from(PeerConnectionDTO source) {
			Objects.requireNonNull(source);
			return this.withCallId(source.callId)
					.withServiceId(source.serviceId)
					.withRoomId(source.roomId)
					.withMediaUnitId(source.mediaUnitId)
					.withUserId(source.userId)
					.withPeerConnectionId(source.peerConnectionId)
					.withClientId(source.clientId)
					.withCreatedTimestamp(source.created)
					.withMarker(source.marker)
					;
		}

		public PeerConnectionDTO.Builder withCallId(UUID value) {
			this.result.callId = value;
			return this;
		}

		public PeerConnectionDTO.Builder withServiceId(String value) {
			this.result.serviceId = value;
			return this;
		}

		public PeerConnectionDTO.Builder withRoomId(String value) {
			this.result.roomId = value;
			return this;
		}

		public PeerConnectionDTO.Builder withMediaUnitId(String value) {
			this.result.mediaUnitId = value;
			return this;
		}

		public PeerConnectionDTO.Builder withUserId(String value) {
			this.result.userId = value;
			return this;
		}

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

		public PeerConnectionDTO.Builder withMarker(String value) {
			this.result.marker = value;
			return this;
		}

		public PeerConnectionDTO build() {
			Objects.requireNonNull(this.result.serviceId);
			Objects.requireNonNull(this.result.roomId);
			Objects.requireNonNull(this.result.callId);
			Objects.requireNonNull(this.result.clientId);
			Objects.requireNonNull(this.result.peerConnectionId);
			Objects.requireNonNull(this.result.created);
			return this.result;
		}
	}
}
