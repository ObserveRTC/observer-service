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
		return new Builder().from(source);
	}
	private static final String SERVICE_ID_FIELD_NAME = "serviceId";
	private static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitId";
	private static final String SFU_ID_FIELD_NAME = "sfuId";
	private static final String SFU_TRANSPORT_ID_FIELD_NAME = "transportId";
	private static final String SFU_STREAM_ID_FIELD_NAME = "streamId";
	private static final String SFU_SINK_ID_FIELD_NAME = "sinkId";
	private static final String SFU_RTP_PAD_ID_FIELD_NAME = "rtpPadId";
	private static final String SFU_RTP_STREAM_DIRECTION_FIELD_NAME = "sfuStreamDirection";
	private static final String SFU_RTP_INTERNAL_FIELD_NAME = "internal";
	private static final String SFU_RTP_SSRC_FIELD_NAME = "ssrc";
	private static final String ADDED_FIELD_NAME = "added";
	private static final String MARKER_FIELD_NAME = "marker";

	public String serviceId;
	public String mediaUnitId;
	public UUID sfuId;
	public UUID transportId;
	public UUID streamId;
	public UUID sinkId;
	public UUID rtpPadId;
	public Long ssrc;
	public StreamDirection streamDirection;
	public boolean internal;
	public Long added;
	public String marker;

	SfuRtpPadDTO() {

	}

	@Override
	public int getFactoryId() {
		return PortableDTOFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return PortableDTOFactory.SFU_RTP_PAD_DTO_CLASS_ID;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeString(SERVICE_ID_FIELD_NAME, this.serviceId);
		writer.writeString(MEDIA_UNIT_ID_FIELD_NAME, this.mediaUnitId);
		writer.writeByteArray(SFU_ID_FIELD_NAME, UUIDAdapter.toBytes(this.sfuId));
		writer.writeByteArray(SFU_TRANSPORT_ID_FIELD_NAME, UUIDAdapter.toBytes(this.transportId));
//		writer.writeByteArray(SFU_RTP_STREAM_ID_FIELD_NAME, UUIDAdapter.toBytes(this.rtpStreamId));
		SerDeUtils.writeNullableUUID(writer, SFU_STREAM_ID_FIELD_NAME, this.streamId);
		SerDeUtils.writeNullableUUID(writer, SFU_SINK_ID_FIELD_NAME, this.sinkId);
		writer.writeByteArray(SFU_RTP_PAD_ID_FIELD_NAME, UUIDAdapter.toBytes(this.rtpPadId));
		writer.writeBoolean(SFU_RTP_INTERNAL_FIELD_NAME, this.internal);
		writer.writeString(SFU_RTP_STREAM_DIRECTION_FIELD_NAME, this.streamDirection.name());

		writer.writeLong(SFU_RTP_SSRC_FIELD_NAME, this.ssrc);
		writer.writeLong(ADDED_FIELD_NAME, this.added);

		SerDeUtils.writeNullableString(writer, MARKER_FIELD_NAME, this.marker);

	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.serviceId = reader.readString(SERVICE_ID_FIELD_NAME);
		this.mediaUnitId = reader.readString(MEDIA_UNIT_ID_FIELD_NAME);
		this.sfuId = UUIDAdapter.toUUID(reader.readByteArray(SFU_ID_FIELD_NAME));
		this.transportId = UUIDAdapter.toUUID(reader.readByteArray(SFU_TRANSPORT_ID_FIELD_NAME));
//		this.rtpStreamId = UUIDAdapter.toUUID(reader.readByteArray(SFU_RTP_STREAM_ID_FIELD_NAME));
		this.streamId = SerDeUtils.readNullableUUID(reader, SFU_STREAM_ID_FIELD_NAME);
		this.sinkId = SerDeUtils.readNullableUUID(reader, SFU_SINK_ID_FIELD_NAME);
		this.rtpPadId =  UUIDAdapter.toUUID(reader.readByteArray(SFU_RTP_PAD_ID_FIELD_NAME));
		this.internal = reader.readBoolean(SFU_RTP_INTERNAL_FIELD_NAME);
		this.streamDirection =  StreamDirection.valueOf(reader.readString(SFU_RTP_STREAM_DIRECTION_FIELD_NAME));

		this.ssrc = reader.readLong(SFU_RTP_SSRC_FIELD_NAME);
		this.added = reader.readLong(ADDED_FIELD_NAME);

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
		SfuRtpPadDTO otherDTO = (SfuRtpPadDTO) other;
		if (!Objects.equals(this.sfuId, otherDTO.sfuId) ||
			!Objects.equals(this.serviceId, otherDTO.serviceId) ||
			!Objects.equals(this.mediaUnitId, otherDTO.mediaUnitId) ||
			!Objects.equals(this.transportId, otherDTO.transportId) ||
			!Objects.equals(this.sinkId, otherDTO.sinkId) ||
			!Objects.equals(this.streamId, otherDTO.streamId) ||
			!Objects.equals(this.rtpPadId, otherDTO.rtpPadId) ||
			!Objects.equals(this.streamDirection, otherDTO.streamDirection) ||
			!Objects.equals(this.internal, otherDTO.internal) ||
			!Objects.equals(this.ssrc, otherDTO.ssrc) ||
			!Objects.equals(this.added, otherDTO.added) ||
			!Objects.equals(this.marker, otherDTO.marker)
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
					.withSfuTransportId(source.transportId)
					.withSfuRtpPadId(source.rtpPadId)
					.withStreamId(source.streamId)
					.withSinkId(source.sinkId)
					.withStreamDirection(source.streamDirection)
					.withAddedTimestamp(source.added)
					.withSsrc(source.ssrc)
					.withInternal(source.internal)
					.withMarker(source.marker)
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
			this.result.transportId = value;
			return this;
		}

		public Builder withSfuRtpPadId(UUID value) {
			this.result.rtpPadId = value;
			return this;
		}

		public Builder withStreamId(UUID value) {
			this.result.streamId = value;
			return this;
		}

		public Builder withSinkId(UUID value) {
			this.result.sinkId = value;
			return this;
		}

		public Builder withStreamDirection(StreamDirection value) {
			this.result.streamDirection = value;
			return this;
		}

		public Builder withInternal(Boolean value) {
			this.result.internal = value == null ? false : value.booleanValue();
			return this;
		}

		public Builder withAddedTimestamp(Long value) {
			this.result.added = value;
			return this;
		}

		public Builder withSsrc(Long value) {
			this.result.ssrc = value;
			return this;
		}

		public Builder withMarker(String value) {
			this.result.marker = value;
			return this;
		}

		public SfuRtpPadDTO build() {
			Objects.requireNonNull(this.result.sfuId);
			Objects.requireNonNull(this.result.transportId);
			Objects.requireNonNull(this.result.rtpPadId);
			Objects.requireNonNull(this.result.added);
			Objects.requireNonNull(this.result.ssrc);
			if (Objects.isNull(this.result.sinkId) && Objects.isNull(this.result.streamId)) {
				throw new NullPointerException("SfuRtpPad must have a sink or streamId");
			}
			Objects.requireNonNull(this.result.added);
			return this.result;
		}
    }
}
