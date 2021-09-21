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
public class SfuDTO implements VersionedPortable {
	public static final int CLASS_VERSION = 1;

	public static Builder builder() {
		return new Builder();
	}
	private static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitId";
	private static final String SFU_ID_FIELD_NAME = "sfuId";
	private static final String SFU_NAME_FIELD_NAME = "sfuName";
	private static final String JOINED_FIELD_NAME = "joined";
	private static final String TIMEZONE_FIELD_NAME = "timeZone";

	public String mediaUnitId;
	public UUID sfuId;
	public String sfuName;
	public Long joined;
	public String timeZoneId;

	SfuDTO() {

	}

	@Override
	public int getFactoryId() {
		return PortableDTOFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return PortableDTOFactory.SFU_DTO_CLASS_ID;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeUTF(MEDIA_UNIT_ID_FIELD_NAME, this.mediaUnitId);
		writer.writeByteArray(SFU_ID_FIELD_NAME, UUIDAdapter.toBytes(this.sfuId));
		writer.writeUTF(SFU_NAME_FIELD_NAME, this.sfuName);
		writer.writeLong(JOINED_FIELD_NAME, this.joined);
		writer.writeUTF(TIMEZONE_FIELD_NAME, this.timeZoneId);

	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.mediaUnitId = reader.readUTF(MEDIA_UNIT_ID_FIELD_NAME);
		this.sfuId = UUIDAdapter.toUUID(reader.readByteArray(SFU_ID_FIELD_NAME));
		this.sfuName = reader.readUTF(SFU_NAME_FIELD_NAME);
		this.joined = reader.readLong(JOINED_FIELD_NAME);
		this.timeZoneId = reader.readUTF(TIMEZONE_FIELD_NAME);
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
		SfuDTO otherDTO = (SfuDTO) other;
		if (!Objects.equals(this.sfuId, otherDTO.sfuId) ||
			!Objects.equals(this.sfuName, otherDTO.sfuName) ||
			!Objects.equals(this.mediaUnitId, otherDTO.mediaUnitId) ||
			!Objects.equals(this.joined, otherDTO.joined) ||
			!Objects.equals(this.timeZoneId, otherDTO.timeZoneId)
		) {
			return false;
		}
		return true;
	}

	public static class Builder {
		private final SfuDTO result = new SfuDTO();

		public Builder from(SfuDTO source) {
			Objects.requireNonNull(source);
			return this.withSfuId(source.sfuId)
					.withMediaUnitId(source.mediaUnitId)
					.withConnectedTimestamp(source.joined)
					.withTimeZoneId(source.timeZoneId)
				;
		}

		public Builder withSfuId(UUID value) {
			this.result.sfuId = value;
			return this;
		}

		public Builder withSfuName(String value) {
			this.result.sfuName = value;
			return this;
		}

		public Builder withMediaUnitId(String value) {
			this.result.mediaUnitId = value;
			return this;
		}

		public Builder withConnectedTimestamp(Long value) {
			this.result.joined = value;
			return this;
		}

		public Builder withTimeZoneId(String value) {
			this.result.timeZoneId = value;
			return this;
		}

		public SfuDTO build() {
			Objects.requireNonNull(this.result.sfuId);
			Objects.requireNonNull(this.result.joined);
			return this.result;
		}
    }
}
