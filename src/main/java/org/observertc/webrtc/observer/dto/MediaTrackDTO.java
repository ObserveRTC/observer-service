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
public class MediaTrackDTO implements VersionedPortable {
	private static final Logger logger = LoggerFactory.getLogger(MediaTrackDTO.class);
	public static final int CLASS_VERSION = 1;

	private static final String CALL_ID_FIELD_NAME = "callId";
	private static final String SERVICE_ID_FIELD_NAME = "serviceId";
	private static final String ROOM_ID_FIELD_NAME = "roomId";

	private static final String CLIENT_ID_FIELD_NAME = "clientId";
	private static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitId";
	private static final String USER_ID_FIELD_NAME = "userId";

	private static final String PEER_CONNECTION_ID_FIELD_NAME = "peerConnectionId";
	private static final String MEDIA_TRACK_ID_FIELD_NAME = "trackId";
	private static final String SSRC_FIELD_NAME = "ssrc";
	private static final String ADDED_FIELD_NAME = "added";
	private static final String DIRECTION_FIELD_NAME = "direction";

	public UUID callId;
	public String serviceId;
	public String roomId;

	public UUID clientId;
	public String mediaUnitId;
	public String userId;

	public UUID peerConnectionId;
	public UUID trackId;
	public Long ssrc;
	public Long added;
	public StreamDirection direction;

	public static Builder builder() {
		return new Builder();
	}


	@Override
	public int getFactoryId() {
		return PortableDTOFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return PortableDTOFactory.MEDIA_TRACK_DTO_CLASS_ID;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeByteArray(CALL_ID_FIELD_NAME, UUIDAdapter.toBytes(this.callId));
		writer.writeUTF(SERVICE_ID_FIELD_NAME, this.serviceId);
		writer.writeUTF(ROOM_ID_FIELD_NAME, this.roomId);

		writer.writeByteArray(CLIENT_ID_FIELD_NAME, UUIDAdapter.toBytes(this.clientId));
		writer.writeUTF(MEDIA_UNIT_ID_FIELD_NAME, this.mediaUnitId);
		writer.writeUTF(USER_ID_FIELD_NAME, this.userId);

		writer.writeByteArray(PEER_CONNECTION_ID_FIELD_NAME, UUIDAdapter.toBytes(this.peerConnectionId));
		writer.writeByteArray(MEDIA_TRACK_ID_FIELD_NAME, UUIDAdapter.toBytes(this.trackId));
		writer.writeLong(SSRC_FIELD_NAME, this.ssrc);
		writer.writeLong(ADDED_FIELD_NAME, this.added);
		writer.writeUTF(DIRECTION_FIELD_NAME, this.direction.name());

	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.callId = UUIDAdapter.toUUID(reader.readByteArray(CALL_ID_FIELD_NAME));
		this.serviceId = reader.readUTF(SERVICE_ID_FIELD_NAME);
		this.roomId = reader.readUTF(ROOM_ID_FIELD_NAME);

		this.clientId = UUIDAdapter.toUUID(reader.readByteArray(CLIENT_ID_FIELD_NAME));
		this.mediaUnitId = reader.readUTF(MEDIA_UNIT_ID_FIELD_NAME);
		this.userId = reader.readUTF(USER_ID_FIELD_NAME);

		this.peerConnectionId = UUIDAdapter.toUUID(reader.readByteArray(PEER_CONNECTION_ID_FIELD_NAME));
		this.trackId = UUIDAdapter.toUUID(reader.readByteArray(MEDIA_TRACK_ID_FIELD_NAME));
		this.ssrc = reader.readLong(SSRC_FIELD_NAME);
		this.added = reader.readLong(ADDED_FIELD_NAME);
		var direction = reader.readUTF(DIRECTION_FIELD_NAME);
		this.direction = StreamDirection.valueOf(direction);
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
		MediaTrackDTO otherDTO = (MediaTrackDTO) other;
		if (!Objects.equals(this.callId, otherDTO.callId)) return false;
		if (!Objects.equals(this.serviceId, otherDTO.serviceId)) return false;
		if (!Objects.equals(this.roomId, otherDTO.roomId)) return false;

		if (!Objects.equals(this.clientId, otherDTO.clientId)) return false;
		if (!Objects.equals(this.mediaUnitId, otherDTO.mediaUnitId)) return false;
		if (!Objects.equals(this.userId, otherDTO.userId)) return false;

		if (!Objects.equals(this.peerConnectionId, otherDTO.peerConnectionId)) return false;
		if (!Objects.equals(this.trackId, otherDTO.trackId)) return false;
		if (!Objects.equals(this.ssrc, otherDTO.ssrc)) return false;
		if (!Objects.equals(this.added, otherDTO.added)) return false;
		if (!Objects.equals(this.direction, otherDTO.direction)) return false;
		return true;
	}

	public static class Builder {
		private final MediaTrackDTO result = new MediaTrackDTO();

		private Builder() {

		}

		public Builder withCallId(UUID value) {
			Objects.requireNonNull(value);
			this.result.callId = value;
			return this;
		}

		public Builder withServiceId(String value) {
			Objects.requireNonNull(value);
			this.result.serviceId = value;
			return this;
		}

		public Builder withRoomId(String value) {
			Objects.requireNonNull(value);
			this.result.roomId = value;
			return this;
		}

		public Builder withClientId(UUID value) {
			Objects.requireNonNull(value);
			this.result.clientId = value;
			return this;
		}

		public Builder withMediaUnitId(String value) {
			Objects.requireNonNull(value);
			this.result.mediaUnitId = value;
			return this;
		}

		public Builder withUserId(String value) {
			this.result.userId = value;
			return this;
		}


		public Builder withSSRC(Long value) {
			Objects.requireNonNull(value);
			this.result.ssrc = value;
			return this;
		}

		public Builder withPeerConnectionId(UUID value) {
			Objects.requireNonNull(value);
			this.result.peerConnectionId = value;
			return this;
		}

		public Builder withTrackId(UUID value) {
			Objects.requireNonNull(value);
			this.result.trackId = value;
			return this;
		}

		public Builder withAddedTimestamp(Long value) {
			this.result.added = value;
			return this;
		}

		public MediaTrackDTO build() {
			Objects.requireNonNull(this.result.callId);
			Objects.requireNonNull(this.result.clientId);
			Objects.requireNonNull(this.result.peerConnectionId);
			Objects.requireNonNull(this.result.trackId);
			Objects.requireNonNull(this.result.ssrc);
			Objects.requireNonNull(this.result.added);
			Objects.requireNonNull(this.result.direction);
			return this.result;
		}

		public Builder withDirection(StreamDirection value) {
			this.result.direction = value;
			return this;
		}
	}
}
