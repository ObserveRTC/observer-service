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
public class ClientDTO implements VersionedPortable {
	public static final int CLASS_VERSION = 1;

	private static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitId";
	private static final String CALL_ID_FIELD_NAME = "callId";
	private static final String USER_ID_FIELD_NAME = "userId";
	private static final String CLIENT_ID_FIELD_NAME = "clientId";
	private static final String CONNECTED_FIELD_NAME = "connected"; // antonym: disconnected
	private static final String TIMEZONE_FIELD_NAME = "timeZone";

	public static ClientDTO of(
			String mediaUnitId,
			UUID callId,
			String userId,
			UUID clientId,
			Long connected,
			String timeZoneId) {
		ClientDTO result = new ClientDTO();
		result.mediaUnitId = mediaUnitId;
		result.callId = callId;
		result.userId = userId;
		result.clientId = clientId;
		result.connected = connected;
		result.timeZoneId = timeZoneId;
		return result;
	}

	public String mediaUnitId;
	public UUID callId;
	public String userId;
	public UUID clientId;
	public Long connected;
	public String timeZoneId;

	@Override
	public int getFactoryId() {
		return PortableDTOFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return PortableDTOFactory.CLIENT_DTO_CLASS_ID;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeUTF(MEDIA_UNIT_ID_FIELD_NAME, this.mediaUnitId);
		writer.writeByteArray(CALL_ID_FIELD_NAME, UUIDAdapter.toBytes(this.callId));
		writer.writeUTF(USER_ID_FIELD_NAME, this.userId);
		writer.writeByteArray(CLIENT_ID_FIELD_NAME, UUIDAdapter.toBytes(this.clientId));
		writer.writeLong(CONNECTED_FIELD_NAME, this.connected);
		writer.writeUTF(TIMEZONE_FIELD_NAME, this.timeZoneId);

	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.userId = reader.readUTF(MEDIA_UNIT_ID_FIELD_NAME);
		this.callId = UUIDAdapter.toUUID(reader.readByteArray(CALL_ID_FIELD_NAME));
		this.userId = reader.readUTF(USER_ID_FIELD_NAME);
		this.clientId = UUIDAdapter.toUUID(reader.readByteArray(CLIENT_ID_FIELD_NAME));
		this.connected = reader.readLong(CONNECTED_FIELD_NAME);
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
		ClientDTO otherDTO = (ClientDTO) other;
		if (!Objects.equals(this.callId, otherDTO.callId) ||
			!Objects.equals(this.userId, otherDTO.userId) ||
			!Objects.equals(this.clientId, otherDTO.clientId) ||
			!Objects.equals(this.connected, otherDTO.connected) ||
			!Objects.equals(this.timeZoneId, otherDTO.timeZoneId)
		) {
			return false;
		}
		return true;
	}
}
