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

package org.observertc.webrtc.observer.models;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import org.observertc.webrtc.common.ObjectToString;
import org.observertc.webrtc.common.UUIDAdapter;

public class PeerConnectionEntity implements Portable {

	public static final int CLASS_ID = 1000;

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
	private static final String UPDATED_FIELD_NAME = "updated";
	private static final String DETACHED_FIELD_NAME = "detached";
	private static final String MARKER_FIELD_NAME = "marker";


	public static PeerConnectionEntity of(
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
			Long updated,
			Long detached,
			String marker
	) {
		PeerConnectionEntity result = new PeerConnectionEntity();
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
		result.updated = updated;
		result.detached = detached;
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
	public Long updated;
	public Long detached;
	public String marker;

	@Override
	public String toString() {
		return ObjectToString.toString(this);
	}

	@Override
	public int getFactoryId() {
		return EntityFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return CLASS_ID;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeByteArray(PEER_CONNECTION_UUID_FIELD_NAME, UUIDAdapter.toBytesOrDefault(this.peerConnectionUUID, EntityFactory.DEFAULT_UUID_BYTES));
		writer.writeByteArray(CALL_UUID_FIELD_NAME, UUIDAdapter.toBytesOrDefault(this.callUUID, EntityFactory.DEFAULT_UUID_BYTES));
		writer.writeByteArray(SERVICE_UUID_FIELD_NAME, UUIDAdapter.toBytesOrDefault(this.serviceUUID, EntityFactory.DEFAULT_UUID_BYTES));
		writer.writeUTF(SERVICE_NAME_FIELD_NAME, this.serviceName);
		writer.writeUTF(MEDIA_UNIT_ID_FIELD_NAME, this.mediaUnitId);
		writer.writeUTF(CALL_NAME_FIELD_NAME, this.callName);
		writer.writeUTF(PROVIDED_USER_NAME_FIELD_NAME, this.providedUserName);
		writer.writeUTF(BROWSERID_FIELD_NAME, this.browserId);
		writer.writeUTF(TIMEZONE_FIELD_NAME, this.timeZone);
		if (Objects.nonNull(this.joined)) {
			writer.writeLong(JOINED_FIELD_NAME, this.joined);
		}
		if (Objects.nonNull(this.updated)) {
			writer.writeLong(UPDATED_FIELD_NAME, this.updated);
		}
		if (Objects.nonNull(this.detached)) {
			writer.writeLong(DETACHED_FIELD_NAME, this.detached);
		}
		writer.writeUTF(MARKER_FIELD_NAME, this.marker);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.peerConnectionUUID = UUIDAdapter.toUUIDOrDefault(reader.readByteArray(PEER_CONNECTION_UUID_FIELD_NAME), null);
		this.callUUID = UUIDAdapter.toUUIDOrDefault(reader.readByteArray(CALL_UUID_FIELD_NAME), null);
		this.serviceUUID = UUIDAdapter.toUUIDOrDefault(reader.readByteArray(SERVICE_UUID_FIELD_NAME), null);
		this.serviceName = reader.readUTF(SERVICE_NAME_FIELD_NAME);
		this.mediaUnitId = reader.readUTF(MEDIA_UNIT_ID_FIELD_NAME);
		this.callName = reader.readUTF(CALL_NAME_FIELD_NAME);
		this.providedUserName = reader.readUTF(PROVIDED_USER_NAME_FIELD_NAME);
		this.browserId = reader.readUTF(BROWSERID_FIELD_NAME);
		this.timeZone = reader.readUTF(TIMEZONE_FIELD_NAME);
		this.joined = reader.readLong(JOINED_FIELD_NAME);
		if (reader.hasField(UPDATED_FIELD_NAME)) {
			this.updated = reader.readLong(UPDATED_FIELD_NAME);
		}
		if (reader.hasField(DETACHED_FIELD_NAME)) {
			this.detached = reader.readLong(DETACHED_FIELD_NAME);
		}
		this.marker = reader.readUTF(MARKER_FIELD_NAME);
	}
}
