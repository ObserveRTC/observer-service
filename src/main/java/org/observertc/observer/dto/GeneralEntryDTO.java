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

import java.io.IOException;
import java.util.Objects;

// To avoid exposing hazelcast serialization specific fields
@JsonIgnoreProperties(value = { "classId", "factoryId" })

public class GeneralEntryDTO implements VersionedPortable {

	public static final int CLASS_VERSION = 1;
	public static Builder builder() {
		return new Builder();
	}

	private static final String KEY_FIELD_NAME = "key";
	private static final String VALUE_FIELD_NAME = "value";
	private static final String TIMESTAMP_FIELD_NAME = "timestamp";

	public String key;
	public String value;
	public Long timestamp;

	@Override
	public int getFactoryId() {
		return PortableDTOFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return PortableDTOFactory.GENERAL_ENTRY_DTO_CLASS_ID;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeString(KEY_FIELD_NAME, this.key);
		writer.writeString(VALUE_FIELD_NAME, this.value);
		writer.writeLong(TIMESTAMP_FIELD_NAME, this.timestamp);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.key = reader.readString(KEY_FIELD_NAME);
		this.value = reader.readString(VALUE_FIELD_NAME);
		this.timestamp = reader.readLong(TIMESTAMP_FIELD_NAME);
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
		GeneralEntryDTO otherDTO = (GeneralEntryDTO) other;
		if (!this.key.equals(otherDTO.key)) return false;
		if (!this.value.equals(otherDTO.value)) return false;
		if (!this.timestamp.equals(otherDTO.timestamp)) return false;
		return true;
	}

	public static class Builder {
		private final GeneralEntryDTO result = new GeneralEntryDTO();

		private Builder() {

		}

		public GeneralEntryDTO.Builder from(GeneralEntryDTO source) {
			Objects.requireNonNull(source);
			return this
					.withKey(source.key)
					.withValue(source.value)
					.withTimestamp(source.timestamp)
					;
		}

		public GeneralEntryDTO.Builder withKey(String value) {
			Objects.requireNonNull(value);
			this.result.key = value;
			return this;
		}

		public GeneralEntryDTO.Builder withValue(String value) {
			Objects.requireNonNull(value);
			this.result.value = value;
			return this;
		}

		public GeneralEntryDTO.Builder withTimestamp(Long value) {
			Objects.requireNonNull(value);
			this.result.timestamp = value;
			return this;
		}

		public GeneralEntryDTO build() {
			Objects.requireNonNull(this.result.key);
			Objects.requireNonNull(this.result.value);
			Objects.requireNonNull(this.result.timestamp);
			return this.result;
		}
	}
}
