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
public class SfuTransportDTO implements VersionedPortable {
	public static final int CLASS_VERSION = 1;

	public static Builder builder() {
		return new Builder();
	}

	private static final String SERVICE_ID_FIELD_NAME = "serviceId";
	private static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitId";
	private static final String SFU_ID_FIELD_NAME = "sfuId";
	private static final String TRANSPORT_ID_FIELD_NAME = "transportId";
	private static final String INTERNAL_TRANSPORT_FIELD_NAME = "internal";
	private static final String OPENED_FIELD_NAME = "opened";
	private static final String MARKER_FIELD_NAME = "marker";

	public String serviceId;
	public String mediaUnitId;
	public UUID sfuId;
	public UUID transportId;
	public boolean internal = false;
	public Long opened;
	public String marker;

	SfuTransportDTO() {

	}

	@Override
	public int getFactoryId() {
		return PortableDTOFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return PortableDTOFactory.SFU_TRANSPORT_DTO_CLASS_ID;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeString(SERVICE_ID_FIELD_NAME, this.serviceId);
		writer.writeString(MEDIA_UNIT_ID_FIELD_NAME, this.mediaUnitId);
		writer.writeByteArray(SFU_ID_FIELD_NAME, UUIDAdapter.toBytes(this.sfuId));
		writer.writeByteArray(TRANSPORT_ID_FIELD_NAME, UUIDAdapter.toBytes(this.transportId));
		writer.writeBoolean(INTERNAL_TRANSPORT_FIELD_NAME, this.internal);
		writer.writeLong(OPENED_FIELD_NAME, this.opened);

		SerDeUtils.writeNullableString(writer, MARKER_FIELD_NAME, this.marker);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.serviceId = reader.readString(SERVICE_ID_FIELD_NAME);
		this.mediaUnitId = reader.readString(MEDIA_UNIT_ID_FIELD_NAME);
		this.sfuId = UUIDAdapter.toUUID(reader.readByteArray(SFU_ID_FIELD_NAME));
		this.transportId = UUIDAdapter.toUUID(reader.readByteArray(TRANSPORT_ID_FIELD_NAME));
		this.internal = reader.readBoolean(INTERNAL_TRANSPORT_FIELD_NAME);
		this.opened = reader.readLong(OPENED_FIELD_NAME);

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
		if (Objects.isNull(other) || !this.getClass().getName().equals(other.getClass().getName())) {
			return false;
		}
		SfuTransportDTO otherDTO = (SfuTransportDTO) other;
		if (!Objects.equals(this.sfuId, otherDTO.sfuId) ||
			!Objects.equals(this.serviceId, otherDTO.serviceId) ||
			!Objects.equals(this.mediaUnitId, otherDTO.mediaUnitId) ||
			!Objects.equals(this.internal, otherDTO.internal) ||
			!Objects.equals(this.transportId, otherDTO.transportId) ||
			!Objects.equals(this.opened, otherDTO.opened) ||
			!Objects.equals(this.marker, otherDTO.marker)
		) {
			return false;
		}
		return true;
	}

	public static class Builder {
		private final SfuTransportDTO result = new SfuTransportDTO();

		public Builder from(SfuTransportDTO source) {
			Objects.requireNonNull(source);
			return this.withSfuId(source.sfuId)
					.withInternal(source.internal)
					.withTransportId(source.transportId)
					.withServiceId(source.serviceId)
					.withMediaUnitId(source.mediaUnitId)
					.withOpenedTimestamp(source.opened)
					.withMarker(source.marker)
					;
		}

		public Builder withSfuId(UUID value) {
			this.result.sfuId = value;
			return this;
		}

		public Builder withTransportId(UUID value) {
			this.result.transportId = value;
			return this;
		}

		public Builder withServiceId(String value) {
			this.result.serviceId = value;
			return this;
		}

		public Builder withMediaUnitId(String value) {
			this.result.mediaUnitId = value;
			return this;
		}

		public Builder withInternal(Boolean value) {
			this.result.internal = value == null ? false : value.booleanValue();
			return this;
		}

		public Builder withOpenedTimestamp(Long value) {
			this.result.opened = value;
			return this;
		}

		public Builder withMarker(String value) {
			this.result.marker = value;
			return this;
		}

		public SfuTransportDTO build() {
			Objects.requireNonNull(this.result.sfuId);
			Objects.requireNonNull(this.result.transportId);
			Objects.requireNonNull(this.result.opened);
			return this.result;
		}
    }
}
