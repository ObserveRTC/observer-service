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
public class SfuRtpStreamPodDTO implements VersionedPortable {
	public static final int CLASS_VERSION = 1;

	public static Builder builder() {
		return new Builder();
	}
	public static Builder builderFrom(SfuRtpStreamPodDTO source) {
		return new Builder()
				.withMediaUnitId(source.mediaUnitId)
				.withSfuId(source.sfuId)
				.withSfuTransportId(source.sfuTransportId)
				.withSfuStreamId(source.sfuStreamId)
				.withSfuPodId(source.sfuPodId)
				.withSfuPodRole(source.sfuPodRole)
				.withAddedTimestamp(source.added)
				.withTrackId(source.trackId)
				.withClientId(source.clientId)
				.withCallId(source.callId)
				;
	}
	private static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitId";
	private static final String SFU_ID_FIELD_NAME = "sfuId";
	private static final String SFU_NAME_FIELD_NAME = "sfuName";
	private static final String SFU_TRANSPORT_ID_FIELD_NAME = "transportId";
	private static final String SFU_STREAM_ID_FIELD_NAME = "sfuStreamId";
	private static final String SFU_POD_ID_FIELD_NAME = "sfuPodId";
	private static final String SFU_POD_ROLE_FIELD_NAME = "sfuPodRole";
	private static final String ADDED_FIELD_NAME = "added";

	private static final String TRACK_ID_FIELD_NAME = "trackId";
	private static final String CLIENT_ID_FIELD_NAME = "clientId";
	private static final String CALL_ID_FIELD_NAME = "callId";

	public String mediaUnitId;
	public UUID sfuId;
	public String sfuName;
	public UUID sfuTransportId;
	public UUID sfuStreamId;
	public UUID sfuPodId;
	public SfuPodRole sfuPodRole;
	public Long added;

	public UUID trackId;
	public UUID clientId;
	public UUID callId;

	SfuRtpStreamPodDTO() {

	}

	@Override
	public int getFactoryId() {
		return PortableDTOFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return PortableDTOFactory.SFU_RTP_STREAM_POD_DTO_CLASS_ID;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeUTF(MEDIA_UNIT_ID_FIELD_NAME, this.mediaUnitId);
		writer.writeByteArray(SFU_ID_FIELD_NAME, UUIDAdapter.toBytes(this.sfuId));
		writer.writeUTF(SFU_NAME_FIELD_NAME, this.sfuName);
		writer.writeByteArray(SFU_TRANSPORT_ID_FIELD_NAME, UUIDAdapter.toBytes(this.sfuTransportId));
		writer.writeByteArray(SFU_STREAM_ID_FIELD_NAME, UUIDAdapter.toBytes(this.sfuStreamId));
		writer.writeByteArray(SFU_POD_ID_FIELD_NAME, UUIDAdapter.toBytes(this.sfuPodId));
		writer.writeUTF(SFU_POD_ROLE_FIELD_NAME, this.sfuPodRole.name());

		SerDeUtils.writeNullableUUID(writer, TRACK_ID_FIELD_NAME, this.trackId);
		SerDeUtils.writeNullableUUID(writer, CLIENT_ID_FIELD_NAME, this.clientId);
		SerDeUtils.writeNullableUUID(writer, CALL_ID_FIELD_NAME, this.callId);
		writer.writeLong(ADDED_FIELD_NAME, this.added);

	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.mediaUnitId = reader.readUTF(MEDIA_UNIT_ID_FIELD_NAME);
		this.sfuId = UUIDAdapter.toUUID(reader.readByteArray(SFU_ID_FIELD_NAME));
		this.sfuName = reader.readUTF(SFU_NAME_FIELD_NAME);
		this.sfuTransportId = UUIDAdapter.toUUID(reader.readByteArray(SFU_TRANSPORT_ID_FIELD_NAME));
		this.sfuStreamId = UUIDAdapter.toUUID(reader.readByteArray(SFU_STREAM_ID_FIELD_NAME));
		this.sfuPodId =  UUIDAdapter.toUUID(reader.readByteArray(SFU_POD_ID_FIELD_NAME));
		this.sfuPodRole =  SfuPodRole.valueOf(reader.readUTF(SFU_POD_ROLE_FIELD_NAME));

		this.trackId = SerDeUtils.readNullableUUID(reader, TRACK_ID_FIELD_NAME);
		this.clientId = SerDeUtils.readNullableUUID(reader, CLIENT_ID_FIELD_NAME);
		this.callId = SerDeUtils.readNullableUUID(reader, CALL_ID_FIELD_NAME);
		this.added = reader.readLong(ADDED_FIELD_NAME);
	}

	@Override
	public String toString() {
		return ObjectToString.toString(this);
	}

	public String getSourceId() {
		if (!SfuPodRole.SOURCE.equals(this.sfuPodRole)) {
			return null;
		}
		return this.sfuPodId.toString();
	}

	public String getSinkId() {
		if (!SfuPodRole.SINK.equals(this.sfuPodRole)) {
			return null;
		}
		return this.sfuPodId.toString();
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
		SfuRtpStreamPodDTO otherDTO = (SfuRtpStreamPodDTO) other;
		if (!Objects.equals(this.sfuId, otherDTO.sfuId) ||
			!Objects.equals(this.sfuName, otherDTO.sfuName) ||
			!Objects.equals(this.mediaUnitId, otherDTO.mediaUnitId) ||
			!Objects.equals(this.sfuTransportId, otherDTO.sfuTransportId) ||
			!Objects.equals(this.sfuStreamId, otherDTO.sfuStreamId) ||
			!Objects.equals(this.sfuPodId, otherDTO.sfuPodId) ||
			!Objects.equals(this.sfuPodRole, otherDTO.sfuPodRole) ||
			!Objects.equals(this.trackId, otherDTO.trackId) ||
			!Objects.equals(this.clientId, otherDTO.clientId) ||
			!Objects.equals(this.callId, otherDTO.callId) ||
			!Objects.equals(this.added, otherDTO.added)
		) {
			return false;
		}
		return true;
	}

	public static class Builder {
		private final SfuRtpStreamPodDTO result = new SfuRtpStreamPodDTO();

		public Builder withMediaUnitId(String value) {
			this.result.mediaUnitId = value;
			return this;
		}

		public Builder withSfuId(UUID value) {
			this.result.sfuId = value;
			return this;
		}

		public Builder withSfuName(String value) {
			this.result.sfuName = value;
			return this;
		}

		public Builder withSfuTransportId(UUID value) {
			this.result.sfuTransportId = value;
			return this;
		}

		public Builder withSfuStreamId(UUID value) {
			this.result.sfuStreamId = value;
			return this;
		}

		public Builder withSfuPodId(UUID value) {
			this.result.sfuPodId = value;
			return this;
		}

		public Builder withSfuPodRole(SfuPodRole value) {
			this.result.sfuPodRole = value;
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

		public Builder withAddedTimestamp(Long value) {
			this.result.added = value;
			return this;
		}

		public SfuRtpStreamPodDTO build() {
			Objects.requireNonNull(this.result.sfuId);
			Objects.requireNonNull(this.result.sfuTransportId);
			Objects.requireNonNull(this.result.sfuStreamId);
			Objects.requireNonNull(this.result.sfuPodId);
			Objects.requireNonNull(this.result.added);
			return this.result;
		}
    }
}
