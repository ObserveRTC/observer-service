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
import org.observertc.webrtc.observer.common.JsonUtils;
import org.observertc.webrtc.observer.common.UUIDAdapter;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

// To avoid exposing hazelcast serialization specific fields
@JsonIgnoreProperties(value = { "classId", "factoryId", "classId" })
public class SfuRtpPadDTO implements VersionedPortable {
	public static final int CLASS_VERSION = 1;

	public static Builder builder() {
		return new Builder();
	}
	public static Builder builderFrom(SfuRtpPadDTO source) {
		return new Builder()
				.withServiceId(source.serviceId)
				.withMediaUnitId(source.mediaUnitId)
				.withSfuId(source.sfuId)
				.withSfuTransportId(source.sfuTransportId)
				.withRtpStreamId(source.rtpStreamId)
				.withSfuRtpPadId(source.sfuPadId)
				.withStreamDirection(source.streamDirection)
				.withAddedTimestamp(source.added)
				.withTrackId(source.trackId)
				.withClientId(source.clientId)
				.withCallId(source.callId)
				;
	}
	private static final String SERVICE_ID_FIELD_NAME = "serviceId";
	private static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitId";
	private static final String SFU_ID_FIELD_NAME = "sfuId";
	private static final String SFU_TRANSPORT_ID_FIELD_NAME = "transportId";
	private static final String SFU_RTP_STREAM_ID_FIELD_NAME = "rtpStreamId";
	private static final String SFU_RTP_PAD_ID_FIELD_NAME = "sfuRtpPadId";
	private static final String SFU_RTP_STREAM_DIRECTION_FIELD_NAME = "sfuStreamDirection";
	private static final String SFU_RTP_INTERNAL_PAD_FIELD_NAME = "sfuRtpInternalPad";
	private static final String ADDED_FIELD_NAME = "added";

	private static final String TRACK_ID_FIELD_NAME = "trackId";
	private static final String CLIENT_ID_FIELD_NAME = "clientId";
	private static final String CALL_ID_FIELD_NAME = "callId";

	public String serviceId;
	public String mediaUnitId;
	public UUID sfuId;
	public UUID sfuTransportId;
	public UUID rtpStreamId;
	public UUID sfuPadId;
	public StreamDirection streamDirection;
	public boolean internalPad;
	public Long added;

	public UUID trackId;
	public UUID clientId;
	public UUID callId;

	SfuRtpPadDTO() {

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
		writer.writeUTF(SERVICE_ID_FIELD_NAME, this.serviceId);
		writer.writeUTF(MEDIA_UNIT_ID_FIELD_NAME, this.mediaUnitId);
		writer.writeByteArray(SFU_ID_FIELD_NAME, UUIDAdapter.toBytes(this.sfuId));
		writer.writeByteArray(SFU_TRANSPORT_ID_FIELD_NAME, UUIDAdapter.toBytes(this.sfuTransportId));
		writer.writeByteArray(SFU_RTP_STREAM_ID_FIELD_NAME, UUIDAdapter.toBytes(this.rtpStreamId));
		writer.writeByteArray(SFU_RTP_PAD_ID_FIELD_NAME, UUIDAdapter.toBytes(this.sfuPadId));
		writer.writeBoolean(SFU_RTP_INTERNAL_PAD_FIELD_NAME, this.internalPad);
		writer.writeUTF(SFU_RTP_STREAM_DIRECTION_FIELD_NAME, this.streamDirection.name());

		SerDeUtils.writeNullableUUID(writer, TRACK_ID_FIELD_NAME, this.trackId);
		SerDeUtils.writeNullableUUID(writer, CLIENT_ID_FIELD_NAME, this.clientId);
		SerDeUtils.writeNullableUUID(writer, CALL_ID_FIELD_NAME, this.callId);
		writer.writeLong(ADDED_FIELD_NAME, this.added);

	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.serviceId = reader.readUTF(SERVICE_ID_FIELD_NAME);
		this.mediaUnitId = reader.readUTF(MEDIA_UNIT_ID_FIELD_NAME);
		this.sfuId = UUIDAdapter.toUUID(reader.readByteArray(SFU_ID_FIELD_NAME));
		this.sfuTransportId = UUIDAdapter.toUUID(reader.readByteArray(SFU_TRANSPORT_ID_FIELD_NAME));
		this.rtpStreamId = UUIDAdapter.toUUID(reader.readByteArray(SFU_RTP_STREAM_ID_FIELD_NAME));
		this.sfuPadId =  UUIDAdapter.toUUID(reader.readByteArray(SFU_RTP_PAD_ID_FIELD_NAME));
		this.internalPad = reader.readBoolean(SFU_RTP_INTERNAL_PAD_FIELD_NAME);
		this.streamDirection =  StreamDirection.valueOf(reader.readUTF(SFU_RTP_STREAM_DIRECTION_FIELD_NAME));

		this.trackId = SerDeUtils.readNullableUUID(reader, TRACK_ID_FIELD_NAME);
		this.clientId = SerDeUtils.readNullableUUID(reader, CLIENT_ID_FIELD_NAME);
		this.callId = SerDeUtils.readNullableUUID(reader, CALL_ID_FIELD_NAME);
		this.added = reader.readLong(ADDED_FIELD_NAME);
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
		if (Objects.isNull(other) || !this.getClass().getName().equals(other.getClass().getName())) {
			return false;
		}
		SfuRtpPadDTO otherDTO = (SfuRtpPadDTO) other;
		if (!Objects.equals(this.sfuId, otherDTO.sfuId) ||
			!Objects.equals(this.serviceId, otherDTO.serviceId) ||
			!Objects.equals(this.mediaUnitId, otherDTO.mediaUnitId) ||
			!Objects.equals(this.sfuTransportId, otherDTO.sfuTransportId) ||
			!Objects.equals(this.rtpStreamId, otherDTO.rtpStreamId) ||
			!Objects.equals(this.sfuPadId, otherDTO.sfuPadId) ||
			!Objects.equals(this.streamDirection, otherDTO.streamDirection) ||
			!Objects.equals(this.internalPad, otherDTO.internalPad) ||
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
		private final SfuRtpPadDTO result = new SfuRtpPadDTO();

		public Builder from(SfuRtpPadDTO source) {
			Objects.requireNonNull(source);
			return this
					.withServiceId(source.serviceId)
					.withMediaUnitId(source.mediaUnitId)
					.withSfuId(source.sfuId)
					.withSfuTransportId(source.sfuTransportId)
					.withSfuRtpPadId(source.sfuPadId)
					.withStreamDirection(source.streamDirection)
					.withTrackId(source.trackId)
					.withClientId(source.clientId)
					.withCallId(source.callId)
					.withAddedTimestamp(source.added)
					;

		}

		public Builder withServiceId(String value) {
			this.result.serviceId = value;
			return this;
		}

		public Builder withMediaUnitId(String value) {
			this.result.mediaUnitId = value;
			return this;
		}

		public Builder withSfuId(UUID value) {
			this.result.sfuId = value;
			return this;
		}

		public Builder withSfuTransportId(UUID value) {
			this.result.sfuTransportId = value;
			return this;
		}

		public Builder withRtpStreamId(UUID value) {
			this.result.rtpStreamId = value;
			return this;
		}

		public Builder withSfuRtpPadId(UUID value) {
			this.result.sfuPadId = value;
			return this;
		}

		public Builder withStreamDirection(StreamDirection value) {
			this.result.streamDirection = value;
			return this;
		}

		public Builder withInternalPad(boolean value) {
			this.result.internalPad = value;
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

		public SfuRtpPadDTO build() {
			Objects.requireNonNull(this.result.sfuId);
			Objects.requireNonNull(this.result.sfuTransportId);
			Objects.requireNonNull(this.result.rtpStreamId);
			Objects.requireNonNull(this.result.sfuPadId);
			Objects.requireNonNull(this.result.internalPad);
			Objects.requireNonNull(this.result.added);
			return this.result;
		}
    }
}
