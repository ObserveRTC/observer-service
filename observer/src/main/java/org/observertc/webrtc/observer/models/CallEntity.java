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
import java.util.UUID;
import org.observertc.webrtc.common.ObjectToString;
import org.observertc.webrtc.common.UUIDAdapter;

public class CallEntity implements Portable {

	public static final int CLASS_ID = 3000;
	private static final String CALL_UUID_FIELD_NAME = "callUUID";
	private static final String SERVICE_UUID_FIELD_NAME = "serviceUUID";
	private static final String SERVICE_NAME_FIELD_NAME = "serviceName";
	private static final String INITIATED_FIELD_NAME = "initiated";
	private static final String FINISHED_FIELD_NAME = "finished";
	private static final String CALL_NAME_FIELD_NAME = "callName";
	private static final String MARKER_FIELD_NAME = "marker";

	public static CallEntity of(
			UUID callUUID,
			UUID serviceUUID,
			String serviceName,
			Long initiated,
			String callName,
			String marker) {
		CallEntity result = new CallEntity();
		result.callUUID = callUUID;
		result.initiated = initiated;
		result.serviceUUID = serviceUUID;
		result.serviceName = serviceName;
		result.callName = callName;
		result.marker = marker;
		return result;
	}

	public UUID serviceUUID;
	public String serviceName;
	public Long initiated;
	public Long finished;
	public UUID callUUID;
	public String callName;
	public String marker;

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

		writer.writeByteArray(CALL_UUID_FIELD_NAME, UUIDAdapter.toBytesOrDefault(this.callUUID, EntityFactory.DEFAULT_UUID_BYTES));
		writer.writeByteArray(SERVICE_UUID_FIELD_NAME, UUIDAdapter.toBytesOrDefault(this.serviceUUID, EntityFactory.DEFAULT_UUID_BYTES));
		writer.writeUTF(SERVICE_NAME_FIELD_NAME, this.serviceName);
		writer.writeUTF(CALL_NAME_FIELD_NAME, this.callName);
		writer.writeLong(INITIATED_FIELD_NAME, this.initiated);
		if (this.finished != null) {
			writer.writeLong(FINISHED_FIELD_NAME, this.finished);
		}
		writer.writeUTF(MARKER_FIELD_NAME, this.marker);

	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.callUUID = UUIDAdapter.toUUIDOrDefault(reader.readByteArray(CALL_UUID_FIELD_NAME), null);
		this.serviceUUID = UUIDAdapter.toUUIDOrDefault(reader.readByteArray(SERVICE_UUID_FIELD_NAME), null);
		this.serviceName = reader.readUTF(SERVICE_NAME_FIELD_NAME);
		this.callName = reader.readUTF(CALL_NAME_FIELD_NAME);
		this.initiated = reader.readLong(INITIATED_FIELD_NAME);
		if (reader.hasField(FINISHED_FIELD_NAME)) {
			this.initiated = reader.readLong(FINISHED_FIELD_NAME);
		}
		this.marker = reader.readUTF(MARKER_FIELD_NAME);
	}

	@Override
	public String toString() {
		return ObjectToString.toString(this);
	}
}
