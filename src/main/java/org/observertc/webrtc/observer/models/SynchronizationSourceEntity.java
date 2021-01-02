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
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.common.UUIDAdapter;

public class SynchronizationSourceEntity implements Portable {

	public static final int CLASS_ID = 2000;

	private static final String SERVICE_UUID_FIELD_NAME = "serviceUUID";
	private static final String CALL_UUID_FIELD_NAME = "callUUID";
	private static final String SSRC_FIELD_NAME = "SSRC";


	public static SynchronizationSourceEntity of(UUID serviceUUID, Long SSRC, UUID callUUID) {
		SynchronizationSourceEntity result = new SynchronizationSourceEntity();
		result.serviceUUID = serviceUUID;
		result.SSRC = SSRC;
		result.callUUID = callUUID;
		return result;
	}

	public UUID serviceUUID;
	public UUID callUUID;
	public Long SSRC;

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
		writer.writeLong(SSRC_FIELD_NAME, this.SSRC);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.callUUID = UUIDAdapter.toUUIDOrDefault(reader.readByteArray(CALL_UUID_FIELD_NAME), null);
		this.serviceUUID = UUIDAdapter.toUUIDOrDefault(reader.readByteArray(SERVICE_UUID_FIELD_NAME), null);
		this.SSRC = reader.readLong(SSRC_FIELD_NAME);
	}

	@Override
	public String toString() {
		return ObjectToString.toString(this);
	}

}
