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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

// To avoid exposing hazelcast serialization specific fields
@JsonIgnoreProperties(value = { "classId", "factoryId", "classId" })
public class PeerConnectionDTO implements VersionedPortable {
	private static final Logger logger = LoggerFactory.getLogger(PeerConnectionDTO.class);
	public static final int CLASS_VERSION = 6;

	private static final String SERVICE_UUID_FIELD_NAME = "serviceUUID";
	private static final String SERVICE_NAME_FIELD_NAME = "serviceName";
	private static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitId";
	private static final String CALL_UUID_FIELD_NAME = "callUUID";
	private static final String CALL_NAME_FIELD_NAME = "callName";
	private static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";

	private static final String PROVIDED_USER_NAME_FIELD_NAME = "providedUserName";
	private static final String BROWSERID_FIELD_NAME = "browserId";
	private static final String TIMEZONE_FIELD_NAME = "timeZone";
	private static final String JOINED_FIELD_NAME = "joined";
	private static final String MARKER_FIELD_NAME = "marker";


	public static PeerConnectionDTO of(
			UUID serviceUUID,
			String serviceName,
			String mediaUnitId,
			UUID callUUID,
			String callName,
			UUID peerConnectionUUID,
			String providedUserName,
			String browserId,
			String timeZone,
			Long joined,
			String marker
	) {
		Objects.requireNonNull(peerConnectionUUID);
		Objects.requireNonNull(callUUID);
		Objects.requireNonNull(serviceUUID);

		PeerConnectionDTO result = new PeerConnectionDTO();
		result.serviceUUID = serviceUUID;
		result.serviceName = serviceName;
		result.mediaUnitId = mediaUnitId;
		result.callUUID = callUUID;
		result.callName = callName;
		result.peerConnectionUUID = peerConnectionUUID;
		result.providedUserName = providedUserName;
		result.browserId = browserId;
		result.timeZone = timeZone;
		result.joined = joined;
		result.marker = marker;
		return result;
	}

	public UUID serviceUUID;
	public String serviceName;
	public String mediaUnitId;
	public UUID callUUID;
	public String callName;
	public UUID peerConnectionUUID;
	public String providedUserName;
	public String browserId;
	public String timeZone;
	public Long joined;
	public String marker;

//	@Deprecated
//	public Set<Long> SSRCs = new HashSet<>();

	@Override
	public int getFactoryId() {
		return PortableDTOFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return PortableDTOFactory.PEER_CONNECTION_DTO_CLASS_ID;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeByteArray(PEER_CONNECTION_UUID_FIELD_NAME, UUIDAdapter.toBytes(this.peerConnectionUUID));
		writer.writeByteArray(CALL_UUID_FIELD_NAME, UUIDAdapter.toBytes(this.callUUID));
		writer.writeByteArray(SERVICE_UUID_FIELD_NAME, UUIDAdapter.toBytes(this.serviceUUID));
		writer.writeUTF(SERVICE_NAME_FIELD_NAME, this.serviceName);
		writer.writeUTF(MEDIA_UNIT_ID_FIELD_NAME, this.mediaUnitId);
		writer.writeUTF(CALL_NAME_FIELD_NAME, this.callName);
		writer.writeUTF(PROVIDED_USER_NAME_FIELD_NAME, this.providedUserName);
		writer.writeUTF(BROWSERID_FIELD_NAME, this.browserId);
		writer.writeUTF(TIMEZONE_FIELD_NAME, this.timeZone);
		writer.writeLong(JOINED_FIELD_NAME, this.joined);
		writer.writeUTF(MARKER_FIELD_NAME,this.marker);

//		SerDeUtils.writeLongArray(writer, SSRC_FIELD_NAME, this.SSRCs, -1);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.peerConnectionUUID = UUIDAdapter.toUUID(reader.readByteArray(PEER_CONNECTION_UUID_FIELD_NAME));
		this.callUUID = UUIDAdapter.toUUID(reader.readByteArray(CALL_UUID_FIELD_NAME));
		this.serviceUUID = UUIDAdapter.toUUID(reader.readByteArray(SERVICE_UUID_FIELD_NAME));
		this.serviceName = reader.readUTF(SERVICE_NAME_FIELD_NAME);
		this.mediaUnitId = reader.readUTF(MEDIA_UNIT_ID_FIELD_NAME);
		this.callName = reader.readUTF(CALL_NAME_FIELD_NAME);
		this.providedUserName = reader.readUTF(PROVIDED_USER_NAME_FIELD_NAME);
		this.browserId = reader.readUTF(BROWSERID_FIELD_NAME);
		this.timeZone = reader.readUTF(TIMEZONE_FIELD_NAME);
		this.joined = reader.readLong(JOINED_FIELD_NAME);
		this.marker = reader.readUTF(MARKER_FIELD_NAME);

//		SerDeUtils.readLongArray(reader, SSRC_FIELD_NAME, this.SSRCs, -1);
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
		PeerConnectionDTO otherDTO = (PeerConnectionDTO) other;
		if (!Objects.equals(this.callName, otherDTO.callName)) return false;
		if (!Objects.equals(this.callUUID, otherDTO.callUUID)) return false;
		if (!Objects.equals(this.serviceUUID, otherDTO.serviceUUID)) return false;
		if (!Objects.equals(this.marker, otherDTO.marker)) return false;
		if (!Objects.equals(this.joined, otherDTO.joined)) return false;
		if (!Objects.equals(this.serviceName, otherDTO.serviceName)) return false;
		if (!Objects.equals(this.mediaUnitId, otherDTO.mediaUnitId)) return false;
		if (!Objects.equals(this.providedUserName, otherDTO.providedUserName)) return false;
		return true;
	}
}
