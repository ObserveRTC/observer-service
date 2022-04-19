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
	private static final String SFU_STREAM_ID_FIELD_NAME = "sfuStreamId";
	private static final String SFU_SINK_ID_FIELD_NAME = "sfuSinkId";
	private static final String SSRC_FIELD_NAME = "ssrc";
	private static final String ADDED_FIELD_NAME = "added";
	private static final String DIRECTION_FIELD_NAME = "direction";
	private static final String MARKER_FIELD_NAME = "marker";

	public UUID callId;
	public String serviceId;
	public String roomId;

	public UUID clientId;
	public String mediaUnitId;
	public String userId;

	public UUID peerConnectionId;
	public UUID trackId;
	public UUID sfuStreamId;
	public UUID sfuSinkId;
	public Long ssrc;
	public Long added;
	public StreamDirection direction;
	public String marker;

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
		writer.writeString(SERVICE_ID_FIELD_NAME, this.serviceId);
		writer.writeString(ROOM_ID_FIELD_NAME, this.roomId);

		writer.writeByteArray(CLIENT_ID_FIELD_NAME, UUIDAdapter.toBytes(this.clientId));
		writer.writeString(MEDIA_UNIT_ID_FIELD_NAME, this.mediaUnitId);
		writer.writeString(USER_ID_FIELD_NAME, this.userId);

		writer.writeByteArray(PEER_CONNECTION_ID_FIELD_NAME, UUIDAdapter.toBytes(this.peerConnectionId));
		writer.writeByteArray(MEDIA_TRACK_ID_FIELD_NAME, UUIDAdapter.toBytes(this.trackId));
		SerDeUtils.writeNullableUUID(writer, SFU_STREAM_ID_FIELD_NAME, this.sfuStreamId);
		SerDeUtils.writeNullableUUID(writer, SFU_SINK_ID_FIELD_NAME, this.sfuSinkId);

		writer.writeLong(SSRC_FIELD_NAME, this.ssrc);
		writer.writeLong(ADDED_FIELD_NAME, this.added);
		var direction = this.direction.name();
		writer.writeString(DIRECTION_FIELD_NAME, direction);

		SerDeUtils.writeNullableString(writer, MARKER_FIELD_NAME, this.marker);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.callId = UUIDAdapter.toUUID(reader.readByteArray(CALL_ID_FIELD_NAME));
		this.serviceId = reader.readString(SERVICE_ID_FIELD_NAME);
		this.roomId = reader.readString(ROOM_ID_FIELD_NAME);

		this.clientId = UUIDAdapter.toUUID(reader.readByteArray(CLIENT_ID_FIELD_NAME));
		this.mediaUnitId = reader.readString(MEDIA_UNIT_ID_FIELD_NAME);
		this.userId = reader.readString(USER_ID_FIELD_NAME);

		this.peerConnectionId = UUIDAdapter.toUUID(reader.readByteArray(PEER_CONNECTION_ID_FIELD_NAME));
		this.trackId = UUIDAdapter.toUUID(reader.readByteArray(MEDIA_TRACK_ID_FIELD_NAME));
		this.sfuStreamId = SerDeUtils.readNullableUUID(reader, SFU_STREAM_ID_FIELD_NAME);
		this.sfuSinkId = SerDeUtils.readNullableUUID(reader, SFU_SINK_ID_FIELD_NAME);
		this.ssrc = reader.readLong(SSRC_FIELD_NAME);
		this.added = reader.readLong(ADDED_FIELD_NAME);
		var direction = reader.readString(DIRECTION_FIELD_NAME);
		this.direction = StreamDirection.valueOf(direction);

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
		MediaTrackDTO otherDTO = (MediaTrackDTO) other;
		if (!Objects.equals(this.callId, otherDTO.callId)) return false;
		if (!Objects.equals(this.serviceId, otherDTO.serviceId)) return false;
		if (!Objects.equals(this.roomId, otherDTO.roomId)) return false;

		if (!Objects.equals(this.clientId, otherDTO.clientId)) return false;
		if (!Objects.equals(this.mediaUnitId, otherDTO.mediaUnitId)) return false;
		if (!Objects.equals(this.userId, otherDTO.userId)) return false;

		if (!Objects.equals(this.peerConnectionId, otherDTO.peerConnectionId)) return false;
		if (!Objects.equals(this.trackId, otherDTO.trackId)) return false;
		if (!Objects.equals(this.sfuStreamId, otherDTO.sfuStreamId)) return false;
		if (!Objects.equals(this.sfuSinkId, otherDTO.sfuSinkId)) return false;
		if (!Objects.equals(this.ssrc, otherDTO.ssrc)) return false;
		if (!Objects.equals(this.added, otherDTO.added)) return false;
		if (!Objects.equals(this.direction, otherDTO.direction)) return false;
		if (!Objects.equals(this.marker, otherDTO.marker)) return false;
		return true;
	}

	public static class Builder {
		private final MediaTrackDTO result = new MediaTrackDTO();

		protected Builder() {

		}

		public Builder from(MediaTrackDTO source) {
			Objects.requireNonNull(source);
			return this
					.withCallId(source.callId)
					.withServiceId(source.serviceId)
					.withRoomId(source.roomId)
					.withClientId(source.clientId)
					.withMediaUnitId(source.mediaUnitId)
					.withUserId(source.userId)
					.withSSRC(source.ssrc)
					.withPeerConnectionId(source.peerConnectionId)
					.withTrackId(source.trackId)
					.withSfuStreamId(source.sfuStreamId)
					.withSfuSinkId(source.sfuSinkId)
					.withAddedTimestamp(source.added)
					.withDirection(source.direction)
					.withMarker(source.marker)
					;
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

		public Builder withSfuStreamId(UUID value) {
			this.result.sfuStreamId = value;
			return this;
		}

		public Builder withSfuSinkId(UUID value) {
			this.result.sfuSinkId = value;
			return this;
		}

		public Builder withAddedTimestamp(Long value) {
			this.result.added = value;
			return this;
		}

		public Builder withDirection(StreamDirection value) {
			this.result.direction = value;
			return this;
		}

		public Builder withMarker(String value) {
			this.result.marker = value;
			return this;
		}

		public MediaTrackDTO build() {
			Objects.requireNonNull(this.result.serviceId);
			Objects.requireNonNull(this.result.roomId);
			Objects.requireNonNull(this.result.callId);
			Objects.requireNonNull(this.result.clientId);
			Objects.requireNonNull(this.result.peerConnectionId);
			Objects.requireNonNull(this.result.trackId);
			Objects.requireNonNull(this.result.ssrc);
			Objects.requireNonNull(this.result.added);
			Objects.requireNonNull(this.result.direction);
			return this.result;
		}
	}
}
