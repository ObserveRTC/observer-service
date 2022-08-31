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
//import org.observertc.observer.common.UUIDAdapter;
//
//import java.io.IOException;
//import java.util.Objects;
//import java.util.UUID;
//
//// To avoid exposing hazelcast serialization specific fields
//@JsonIgnoreProperties(value = { "classId", "factoryId", "classId" })
//public class SfuDTO implements VersionedPortable {
//	public static final int CLASS_VERSION = 1;
//
//	public static Builder builder() {
//		return new Builder();
//	}
//
//	private static final String SERVICE_ID_FIELD_NAME = "serviceId";
//	private static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitId";
//	private static final String SFU_ID_FIELD_NAME = "sfuId";
//	private static final String JOINED_FIELD_NAME = "joined";
//	private static final String TIMEZONE_FIELD_NAME = "timeZone";
//	private static final String MARKER_FIELD_NAME = "marker";
//
//	public String serviceId;
//	public String mediaUnitId;
//	public UUID sfuId;
//	public Long joined;
//	public String timeZoneId;
//	public String marker;
//
//	SfuDTO() {
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
//		return PortableDTOFactory.SFU_DTO_CLASS_ID;
//	}
//
//	@Override
//	public void writePortable(PortableWriter writer) throws IOException {
//		writer.writeString(SERVICE_ID_FIELD_NAME, this.serviceId);
//		writer.writeString(MEDIA_UNIT_ID_FIELD_NAME, this.mediaUnitId);
//		writer.writeByteArray(SFU_ID_FIELD_NAME, UUIDAdapter.toBytes(this.sfuId));
//		writer.writeLong(JOINED_FIELD_NAME, this.joined);
//		writer.writeString(TIMEZONE_FIELD_NAME, this.timeZoneId);
//
//		SerDeUtils.writeNullableString(writer, MARKER_FIELD_NAME, this.marker);
//
//	}
//
//	@Override
//	public void readPortable(PortableReader reader) throws IOException {
//		this.serviceId = reader.readString(SERVICE_ID_FIELD_NAME);
//		this.mediaUnitId = reader.readString(MEDIA_UNIT_ID_FIELD_NAME);
//		this.sfuId = UUIDAdapter.toUUID(reader.readByteArray(SFU_ID_FIELD_NAME));
//		this.joined = reader.readLong(JOINED_FIELD_NAME);
//		this.timeZoneId = reader.readString(TIMEZONE_FIELD_NAME);
//
//		this.marker = SerDeUtils.readNullableString(reader, MARKER_FIELD_NAME);
//	}
//
//	@Override
//	public String toString() {
//		return JsonUtils.objectToString(this);
//	}
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
//		SfuDTO otherDTO = (SfuDTO) other;
//		if (!Objects.equals(this.sfuId, otherDTO.sfuId) ||
//			!Objects.equals(this.serviceId, otherDTO.serviceId) ||
//			!Objects.equals(this.mediaUnitId, otherDTO.mediaUnitId) ||
//			!Objects.equals(this.joined, otherDTO.joined) ||
//			!Objects.equals(this.timeZoneId, otherDTO.timeZoneId) ||
//			!Objects.equals(this.marker, otherDTO.marker)
//		) {
//			return false;
//		}
//		return true;
//	}
//
//	public static class Builder {
//		private final SfuDTO result = new SfuDTO();
//
//		public Builder from(SfuDTO source) {
//			Objects.requireNonNull(source);
//			return this.withSfuId(source.sfuId)
//					.withServiceId(source.serviceId)
//					.withMediaUnitId(source.mediaUnitId)
//					.withConnectedTimestamp(source.joined)
//					.withTimeZoneId(source.timeZoneId)
//					.withMarker(source.marker)
//				;
//		}
//
//		public Builder withSfuId(UUID value) {
//			this.result.sfuId = value;
//			return this;
//		}
//
//		public Builder withServiceId(String value) {
//			this.result.serviceId = value;
//			return this;
//		}
//
//		public Builder withMediaUnitId(String value) {
//			this.result.mediaUnitId = value;
//			return this;
//		}
//
//		public Builder withConnectedTimestamp(Long value) {
//			this.result.joined = value;
//			return this;
//		}
//
//		public Builder withTimeZoneId(String value) {
//			this.result.timeZoneId = value;
//			return this;
//		}
//
//		public Builder withMarker(String value) {
//			this.result.marker = value;
//			return this;
//		}
//
//		public SfuDTO build() {
//			Objects.requireNonNull(this.result.sfuId);
//			Objects.requireNonNull(this.result.joined);
//			return this.result;
//		}
//    }
//}
