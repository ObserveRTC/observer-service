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
@JsonIgnoreProperties(value = { "classId", "factoryId" })
public class ConfigDTO implements VersionedPortable {
	public static final UUID DEFAULT_UUID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
	public static final byte[] DEFAULT_UUID_BYTES = UUIDAdapter.toBytes(DEFAULT_UUID);

	public static final int CLASS_VERSION = 1;
	private static final String PAYLOAD_FIELD_NAME = "payload";

	public static ConfigDTO of(
			byte[] payload) {
		ConfigDTO result = new ConfigDTO();
		result.payload = payload;
		return result;
	}

	public byte[] payload;

	@Override
	public int getFactoryId() {
		return PortableDTOFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return PortableDTOFactory.CONFIG_DTO_CLASS_ID;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeByteArray(PAYLOAD_FIELD_NAME, payload);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.payload = reader.readByteArray(PAYLOAD_FIELD_NAME);
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
		ConfigDTO otherDTO = (ConfigDTO) other;
		if (this.payload.length != otherDTO.payload.length) return false;
		for (int i = 0; i < this.payload.length; ++i) {
			if (this.payload[i] != otherDTO.payload[i]) return false;
		}
		return true;
	}
}
