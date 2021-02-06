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

package org.observertc.webrtc.observer.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.nio.serialization.VersionedPortable;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

// To avoid exposing hazelcast serialization specific fields
@JsonIgnoreProperties(value = { "classId", "factoryId", "classId" })
public class ServiceEntity implements VersionedPortable {
	private static final Logger logger = LoggerFactory.getLogger(ServiceEntity.class);

	public static final int CLASS_ID = 6000;
	public static final int CLASS_VERSION = 1;
	private static final String SERVICE_UUIDS_FIELD_NAME = "serviceUUIDs";
	private static final String SERVICE_NAME_FIELD_NAME = "serviceName";

	public static ServiceEntity of(
			String serviceName
	) {
		ServiceEntity result = new ServiceEntity();
		result.serviceName = serviceName;
		return result;
	}

	public Set<UUID> serviceUUIDs = new HashSet<>();
	public String serviceName;

	@Override
	public int getFactoryId() {
		return EntityFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return CLASS_ID;
	}

	@Override
	public int getClassVersion() {
		return CLASS_VERSION;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		try {
			writer.writeUTF(SERVICE_NAME_FIELD_NAME, this.serviceName);

			String[] UUIDStrs;
			if (0 < this.serviceUUIDs.size()) {
				UUIDStrs = new String[this.serviceUUIDs.size()];
				Iterator<UUID> it = this.serviceUUIDs.iterator();
				for (int i = 0; it.hasNext(); ) {
					UUID uuid = it.next();
					if (Objects.isNull(uuid)) {
						continue;
					}
					UUIDStrs[i++] = uuid.toString();
				}
			} else {
				UUIDStrs = new String[]{""};
			}
			writer.writeUTFArray(SERVICE_UUIDS_FIELD_NAME, UUIDStrs);
		} catch (Throwable t) {
			logger.warn("Serialization error for entity {}, class version {} serialization part for version 1", this.toString(), this.getClassVersion());
			return;
		}

	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		try {
			this.serviceName = reader.readUTF(SERVICE_NAME_FIELD_NAME);

			String[] UUIDStrs = reader.readUTFArray(SERVICE_UUIDS_FIELD_NAME);
			if (UUIDStrs.length != 1 || UUIDStrs[0] != "") {
				Arrays.stream(UUIDStrs).map(UUID::fromString).forEach(this.serviceUUIDs::add);
			}
		} catch (Throwable t) {
			logger.warn("Deserialization error for version 1", t);
			return;
		}

	}

	@Override
	public String toString() {
		return ObjectToString.toString(this);
	}


}
