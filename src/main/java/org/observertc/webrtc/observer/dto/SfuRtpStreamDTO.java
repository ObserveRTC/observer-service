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

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

// To avoid exposing hazelcast serialization specific fields
@JsonIgnoreProperties(value = { "classId", "factoryId", "classId" })
public class SfuRtpStreamDTO implements VersionedPortable {
	public static final int CLASS_VERSION = 1;

	public static Builder builder() {
		return new Builder();
	}
	public static Builder builderFrom(SfuRtpStreamDTO source) {
		return new Builder()
				.withMediaUnitId(source.mediaUnitId)
				.withSfuId(source.sfuId)
				.withTransportId(source.transportId)
				.withStreamId(source.streamId)
				.withAddedTimestamp(source.added)
				.withDirection(source.direction)
				.withTrackId(source.trackId)
				.withClientId(source.clientId)
				.withCallId(source.callId)
				.withPipedStreamId(source.pipedStreamId)
				;
	}
	private static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitId";
	private static final String SFU_ID_FIELD_NAME = "sfuId";
	private static final String TRANSPORT_ID_FIELD_NAME = "transportId";
	private static final String STREAM_ID_FIELD_NAME = "streamId";
	private static final String ADDED_FIELD_NAME = "added";
	private static final String DIRECTION_ID_FIELD_NAME = "direction";

	private static final String TRACK_ID_FIELD_NAME = "trackId";
	private static final String CLIENT_ID_FIELD_NAME = "clientId";
	private static final String CALL_ID_FIELD_NAME = "callId";
	private static final String PIPED_STREAM_ID_FIELD_NAME = "pipedStreamId";

	public String mediaUnitId;
	public UUID sfuId;
	public UUID transportId;
	public UUID streamId;
	public Long added;
	public StreamDirection direction;

	public UUID trackId;
	public UUID clientId;
	public UUID callId;
	public UUID pipedStreamId;

	SfuRtpStreamDTO() {

	}

	@Override
	public int getFactoryId() {
		return PortableDTOFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return PortableDTOFactory.SFU_RTP_STREAM_DTO_CLASS_ID;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeUTF(MEDIA_UNIT_ID_FIELD_NAME, this.mediaUnitId);
		writer.writeByteArray(SFU_ID_FIELD_NAME, UUIDAdapter.toBytes(this.sfuId));
		writer.writeByteArray(TRANSPORT_ID_FIELD_NAME, UUIDAdapter.toBytes(this.transportId));
		writer.writeByteArray(STREAM_ID_FIELD_NAME, UUIDAdapter.toBytes(this.streamId));
		SerDeUtils.writeNullableUUID(writer, TRACK_ID_FIELD_NAME, this.trackId);
		SerDeUtils.writeNullableUUID(writer, CLIENT_ID_FIELD_NAME, this.clientId);
		SerDeUtils.writeNullableUUID(writer, CALL_ID_FIELD_NAME, this.callId);
		SerDeUtils.writeNullableUUID(writer, PIPED_STREAM_ID_FIELD_NAME, this.pipedStreamId);
		writer.writeLong(ADDED_FIELD_NAME, this.added);
		writer.writeUTF(DIRECTION_ID_FIELD_NAME, this.direction.name());
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.mediaUnitId = reader.readUTF(MEDIA_UNIT_ID_FIELD_NAME);
		this.sfuId = UUIDAdapter.toUUID(reader.readByteArray(SFU_ID_FIELD_NAME));
		this.transportId = UUIDAdapter.toUUID(reader.readByteArray(TRANSPORT_ID_FIELD_NAME));
		this.streamId = UUIDAdapter.toUUID(reader.readByteArray(STREAM_ID_FIELD_NAME));
		this.trackId = SerDeUtils.readNullableUUID(reader, TRACK_ID_FIELD_NAME);
		this.clientId = SerDeUtils.readNullableUUID(reader, CLIENT_ID_FIELD_NAME);
		this.callId = SerDeUtils.readNullableUUID(reader, CALL_ID_FIELD_NAME);
		this.pipedStreamId = SerDeUtils.readNullableUUID(reader, PIPED_STREAM_ID_FIELD_NAME);
		this.added = reader.readLong(ADDED_FIELD_NAME);
		this.direction = StreamDirection.valueOf(reader.readUTF(DIRECTION_ID_FIELD_NAME));
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
		SfuRtpStreamDTO otherDTO = (SfuRtpStreamDTO) other;
		if (!Objects.equals(this.sfuId, otherDTO.sfuId) ||
			!Objects.equals(this.mediaUnitId, otherDTO.mediaUnitId) ||
			!Objects.equals(this.transportId, otherDTO.transportId) ||
			!Objects.equals(this.streamId, otherDTO.streamId) ||
			!Objects.equals(this.trackId, otherDTO.trackId) ||
			!Objects.equals(this.clientId, otherDTO.clientId) ||
			!Objects.equals(this.callId, otherDTO.callId) ||
			!Objects.equals(this.pipedStreamId, otherDTO.pipedStreamId) ||
			!Objects.equals(this.added, otherDTO.added) ||
			!Objects.equals(this.direction, otherDTO.direction)
		) {
			return false;
		}
		return true;
	}

	public static class Builder {
		private final SfuRtpStreamDTO result = new SfuRtpStreamDTO();

		public Builder withMediaUnitId(String value) {
			this.result.mediaUnitId = value;
			return this;
		}

		public Builder withSfuId(UUID value) {
			this.result.sfuId = value;
			return this;
		}

		public Builder withTransportId(UUID value) {
			this.result.transportId = value;
			return this;
		}

		public Builder withStreamId(UUID value) {
			this.result.streamId = value;
			return this;
		}

		public Builder withDirection(StreamDirection value) {
			this.result.direction = value;
			return this;
		}

		public Builder withTrackId(UUID value) {
			this.result.trackId = value;
			return this;
		}

		public Builder withClientId(UUID value) {
			this.result.clientId = value;
			return this;
		}

		public Builder withCallId(UUID value) {
			this.result.callId = value;
			return this;
		}

		public Builder withPipedStreamId(UUID value) {
			this.result.pipedStreamId = value;
			return this;
		}

		public Builder withAddedTimestamp(Long value) {
			this.result.added = value;
			return this;
		}

		public SfuRtpStreamDTO build() {
			Objects.requireNonNull(this.result.sfuId);
			Objects.requireNonNull(this.result.transportId);
			Objects.requireNonNull(this.result.streamId);
			Objects.requireNonNull(this.result.added);
			Objects.requireNonNull(this.result.direction);
			return this.result;
		}
    }
}
