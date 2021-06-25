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
public class CallDTO implements VersionedPortable {
	public static final int CLASS_VERSION = 2;

	private static final String SERVICE_ID_FIELD_NAME = "serviceId";
	private static final String ROOM_ID_FIELD_NAME = "roomId";
	private static final String CALL_ID_FIELD_NAME = "callId";
	private static final String STARTED_FIELD_NAME = "started"; // ended

	public static Builder builder() {
		return new Builder();
	}

	public String serviceId;
	public String roomId;
	public UUID callId;
	public Long started;

	@Override
	public int getFactoryId() {
		return PortableDTOFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return PortableDTOFactory.CALL_DTO_CLASS_ID;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeUTF(SERVICE_ID_FIELD_NAME, this.serviceId);
		writer.writeUTF(ROOM_ID_FIELD_NAME, this.roomId);
		writer.writeByteArray(CALL_ID_FIELD_NAME, UUIDAdapter.toBytes(this.callId));
		writer.writeLong(STARTED_FIELD_NAME, this.started);

	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.serviceId = reader.readUTF(SERVICE_ID_FIELD_NAME);
		this.roomId = reader.readUTF(ROOM_ID_FIELD_NAME);
		this.callId = UUIDAdapter.toUUID(reader.readByteArray(CALL_ID_FIELD_NAME));
		this.started = reader.readLong(STARTED_FIELD_NAME);
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
		CallDTO otherDTO = (CallDTO) other;
		if (!Objects.equals(this.serviceId, otherDTO.serviceId) ||
			!Objects.equals(this.roomId, otherDTO.roomId) ||
			!Objects.equals(this.callId, otherDTO.callId) ||
			!Objects.equals(this.started, otherDTO.started)
		) {
			return false;
		}
		return true;
	}

	public static class Builder {
		private final CallDTO result = new CallDTO();

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

		public Builder withCallId(UUID value) {
			Objects.requireNonNull(value);
			this.result.callId = value;
			return this;
		}

		public Builder withStartedTimestamp(Long value) {
			Objects.requireNonNull(value);
			this.result.started = value;
			return this;
		}


		public Builder copyFrom(Builder callDTOBuilder) {
			return this
					.withServiceId(callDTOBuilder.result.serviceId)
					.withRoomId(callDTOBuilder.result.roomId)
					.withCallId(callDTOBuilder.result.callId)
					.withStartedTimestamp(callDTOBuilder.result.started)
			;

		}

		public CallDTO build() {
			Objects.requireNonNull(this.result.serviceId);
			Objects.requireNonNull(this.result.roomId);
			Objects.requireNonNull(this.result.callId);
			return this.result;
		}
	}
}
