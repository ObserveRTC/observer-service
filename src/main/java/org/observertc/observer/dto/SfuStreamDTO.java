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
//package org.observertc.observer.dto;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.hazelcast.nio.serialization.PortableReader;
//import com.hazelcast.nio.serialization.PortableWriter;
//import com.hazelcast.nio.serialization.VersionedPortable;
//import org.observertc.observer.common.JsonUtils;
//
//import java.io.IOException;
//import java.util.Objects;
//import java.util.UUID;
//
//// To avoid exposing hazelcast serialization specific fields
//@JsonIgnoreProperties(value = { "classId", "factoryId", "classId" })
//public class SfuStreamDTO implements VersionedPortable {
//	public static final int CLASS_VERSION = 1;
//
//	public static Builder builder() {
//		return new Builder();
//	}
//
//	private static final String SFU_ID_FIELD_NAME = "sfuId";
//	private static final String SFU_TRANSPORT_ID_FIELD_NAME = "sfuTransportId";
//	private static final String SFU_STREAM_ID_FIELD_NAME = "sfuStreamId";
//	private static final String PEER_CONNECTION_ID_FIELD_NAME = "peerConnectionId";
//	private static final String TRACK_ID_FIELD_NAME = "trackId";
//	private static final String CLIENT_ID_FIELD_NAME = "clientId";
//	private static final String CALL_ID_FIELD_NAME = "callId";
//
//	public UUID sfuId;
//	public UUID sfuTransportId;
//	public UUID sfuStreamId;
//	public UUID trackId;
//	public UUID clientId;
//	public UUID callId;
//	public UUID peerConnectionId;
//
//	SfuStreamDTO() {
//
//	}
//
//	@Override
//	public int getFactoryId() {
//		return PortableDTOFactory.FACTORY_ID;
//	}
//
//	@Override
//	public int getClassId() {
//		return PortableDTOFactory.SFU_STREAM_DTO_CLASS_ID;
//	}
//
//	@Override
//	public void writePortable(PortableWriter writer) throws IOException {
//		SerDeUtils.writeNullableUUID(writer, SFU_ID_FIELD_NAME, this.sfuId);
//		SerDeUtils.writeNullableUUID(writer, SFU_TRANSPORT_ID_FIELD_NAME, this.sfuTransportId);
//		SerDeUtils.writeNullableUUID(writer, SFU_STREAM_ID_FIELD_NAME, this.sfuStreamId);
//		SerDeUtils.writeNullableUUID(writer, TRACK_ID_FIELD_NAME, this.trackId);
//		SerDeUtils.writeNullableUUID(writer, CLIENT_ID_FIELD_NAME, this.clientId);
//		SerDeUtils.writeNullableUUID(writer, CALL_ID_FIELD_NAME, this.callId);
//		SerDeUtils.writeNullableUUID(writer, PEER_CONNECTION_ID_FIELD_NAME, this.peerConnectionId);
//	}
//
//	@Override
//	public void readPortable(PortableReader reader) throws IOException {
//		this.sfuId = SerDeUtils.readNullableUUID(reader, SFU_ID_FIELD_NAME);
//		this.sfuTransportId = SerDeUtils.readNullableUUID(reader, SFU_TRANSPORT_ID_FIELD_NAME);
//		this.sfuStreamId = SerDeUtils.readNullableUUID(reader, SFU_STREAM_ID_FIELD_NAME);
//		this.trackId = SerDeUtils.readNullableUUID(reader, TRACK_ID_FIELD_NAME);
//		this.clientId = SerDeUtils.readNullableUUID(reader, CLIENT_ID_FIELD_NAME);
//		this.callId = SerDeUtils.readNullableUUID(reader, CALL_ID_FIELD_NAME);
//		this.peerConnectionId = SerDeUtils.readNullableUUID(reader, PEER_CONNECTION_ID_FIELD_NAME);
//	}
//
//	@Override
//	public String toString() {
//		return JsonUtils.objectToString(this);
//	}
//
//
//	@Override
//	public int getClassVersion() {
//		return CLASS_VERSION;
//	}
//
//	@Override
//	public boolean equals(Object other) {
//		if (Objects.isNull(other) || !this.getClass().isAssignableFrom(other.getClass())) {
//			return false;
//		}
//		SfuStreamDTO otherDTO = (SfuStreamDTO) other;
//		if (!Objects.equals(this.sfuId, otherDTO.sfuId) ||
//			!Objects.equals(this.sfuTransportId, otherDTO.sfuTransportId) ||
//			!Objects.equals(this.sfuStreamId, otherDTO.sfuStreamId) ||
//			!Objects.equals(this.trackId, otherDTO.trackId) ||
//			!Objects.equals(this.clientId, otherDTO.clientId) ||
//			!Objects.equals(this.callId, otherDTO.callId) ||
//			!Objects.equals(this.peerConnectionId, otherDTO.peerConnectionId)
//		) {
//			return false;
//		}
//		return true;
//	}
//
//	public static class Builder {
//		private final SfuStreamDTO result = new SfuStreamDTO();
//
//		public Builder from(SfuStreamDTO source) {
//			Objects.requireNonNull(source);
//			return this
//					.withSfuId(source.sfuId)
//					.withSfuTransportId(source.sfuTransportId)
//					.withStreamId(source.sfuStreamId)
//					.withTrackId(source.trackId)
//					.withClientId(source.clientId)
//					.withCallId(source.callId)
//					.withPeerConnectionId(source.peerConnectionId)
//					;
//
//		}
//
//		public Builder withSfuId(UUID value) {
//			this.result.sfuId = value;
//			return this;
//		}
//
//		public Builder withSfuTransportId(UUID value) {
//			this.result.sfuTransportId = value;
//			return this;
//		}
//
//		public Builder withStreamId(UUID value) {
//			this.result.sfuStreamId = value;
//			return this;
//		}
//
//		public Builder withTrackId(UUID value) {
//			this.result.trackId = value;
//			return this;
//		}
//
//		public Builder withClientId(UUID value) {
//			this.result.clientId = value;
//			return this;
//		}
//
//		public Builder withCallId(UUID value) {
//			this.result.callId = value;
//			return this;
//		}
//
//		public Builder withPeerConnectionId(UUID value) {
//			this.result.peerConnectionId = value;
//			return this;
//		}
//
//		public SfuStreamDTO build() {
//			return this.result;
//		}
//    }
//}
