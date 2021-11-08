///*
// * Copyright  2020 Balazs Kreith
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.observertc.webrtc.observer.dto;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.hazelcast.nio.serialization.PortableReader;
//import com.hazelcast.nio.serialization.PortableWriter;
//import com.hazelcast.nio.serialization.VersionedPortable;
//import org.observertc.webrtc.observer.common.ObjectToString;
//import org.observertc.webrtc.observer.common.UUIDAdapter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.util.Objects;
//import java.util.UUID;
//
//// To avoid exposing hazelcast serialization specific fields
//@JsonIgnoreProperties(value = { "classId", "factoryId", "classId" })
//public class SsrcMapDTO implements VersionedPortable {
//	private static final Logger logger = LoggerFactory.getLogger(SsrcMapDTO.class);
//	public static final int CLASS_VERSION = 1;
//
//	private static final String SSRC_FIELD_NAME = "ssrc";
//	private static final String MEDIA_TRACK_ID_FIELD_NAME = "trackId";
//	private static final String PEER_CONNECTION_ID_FIELD_NAME = "peerConnectionId";
//	private static final String CLIENT_ID_FIELD_NAME = "clientId";
//	private static final String CALL_ID_FIELD_NAME = "callId";
//
//	public Long ssrc;
//	public UUID trackId;
//	public UUID peerConnectionId;
//	public UUID clientId;
//	public UUID callId;
//
//	public static Builder builder() {
//		return new Builder();
//	}
//
//
//	@Override
//	public int getFactoryId() {
//		return PortableDTOFactory.FACTORY_ID;
//	}
//
//	@Override
//	public int getClassId() {
//		return PortableDTOFactory.MEDIA_TRACK_DTO_CLASS_ID;
//	}
//
//	@Override
//	public void writePortable(PortableWriter writer) throws IOException {
//		writer.writeLong(SSRC_FIELD_NAME, this.ssrc);
//		writer.writeByteArray(MEDIA_TRACK_ID_FIELD_NAME, UUIDAdapter.toBytes(this.trackId));
//		writer.writeByteArray(PEER_CONNECTION_ID_FIELD_NAME, UUIDAdapter.toBytes(this.peerConnectionId));
//		writer.writeByteArray(PEER_CONNECTION_ID_FIELD_NAME, UUIDAdapter.toBytes(this.peerConnectionId));
//		writer.writeByteArray(PEER_CONNECTION_ID_FIELD_NAME, UUIDAdapter.toBytes(this.peerConnectionId));
//
//
//		writer.writeLong(ADDED_FIELD_NAME, this.added);
//		writer.writeUTF(DIRECTION_FIELD_NAME, this.direction.name());
//
//	}
//
//	@Override
//	public void readPortable(PortableReader reader) throws IOException {
//		this.peerConnectionId = UUIDAdapter.toUUID(reader.readByteArray(PEER_CONNECTION_ID_FIELD_NAME));
//		this.trackId = UUIDAdapter.toUUID(reader.readByteArray(MEDIA_TRACK_ID_FIELD_NAME));
//		this.ssrc = reader.readLong(SSRC_FIELD_NAME);
//		this.added = reader.readLong(ADDED_FIELD_NAME);
//		var direction = reader.readUTF(DIRECTION_FIELD_NAME);
//		this.direction = StreamDirection.valueOf(direction);
//	}
//
//	@Override
//	public String toString() {
//		return ObjectToString.toString(this);
//	}
//
//	@Override
//	public int getClassVersion() {
//		return CLASS_VERSION;
//	}
//
//	@Override
//	public boolean equals(Object other) {
//		if (Objects.isNull(other) || !this.getClass().getName().equals(other.getClass().getName())) {
//			return false;
//		}
//		SsrcMapDTO otherDTO = (SsrcMapDTO) other;
//		if (!Objects.equals(this.peerConnectionId, otherDTO.peerConnectionId)) return false;
//		if (!Objects.equals(this.trackId, otherDTO.trackId)) return false;
//		if (!Objects.equals(this.ssrc, otherDTO.ssrc)) return false;
//		if (!Objects.equals(this.added, otherDTO.added)) return false;
//		if (!Objects.equals(this.direction, otherDTO.direction)) return false;
//		return true;
//	}
//
//	public static class Builder {
//		private final SsrcMapDTO result = new SsrcMapDTO();
//
//		private Builder() {
//
//		}
//
//		public Builder withSSRC(Long value) {
//			Objects.requireNonNull(value);
//			this.result.ssrc = value;
//			return this;
//		}
//
//		public Builder withPeerConnectionId(UUID value) {
//			Objects.requireNonNull(value);
//			this.result.peerConnectionId = value;
//			return this;
//		}
//
//		public Builder withTrackId(UUID value) {
//			Objects.requireNonNull(value);
//			this.result.trackId = value;
//			return this;
//		}
//
//		public Builder withAddedTimestamp(Long value) {
//			this.result.added = value;
//			return this;
//		}
//
//		public SsrcMapDTO build() {
//			Objects.requireNonNull(this.result.peerConnectionId);
//			Objects.requireNonNull(this.result.ssrc);
//			Objects.requireNonNull(this.result.added);
//			Objects.requireNonNull(this.result.direction);
//			return this.result;
//		}
//
//		public Builder withDirection(StreamDirection value) {
//			this.result.direction = value;
//			return this;
//		}
//	}
//}
