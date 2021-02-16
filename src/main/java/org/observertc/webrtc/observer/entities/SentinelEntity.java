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
@Deprecated
public class SentinelEntity implements VersionedPortable {
	private static final Logger logger = LoggerFactory.getLogger(ServiceEntity.class);

	public static final int CLASS_ID = 7000;
	public static final int CLASS_VERSION = 1;
	private static final String NAME_FIELD_NAME = "name";
	private static final String ADDRESSES_FIELD_NAME = "addresses";
	private static final String CALLFILTERS_FIELD_NAME = "callFilters";

	public static SentinelEntity of(
			String name) {
		SentinelEntity result = new SentinelEntity();
		result.name = name;
		return result;
	}

	public String name;
	public Set<String> addresses = new HashSet<>();
	public Set<String> callFilters = new HashSet<>();

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
		try {
			writer.writeUTF(NAME_FIELD_NAME, this.name);
			this.writeUTFArray(writer, ADDRESSES_FIELD_NAME, this.addresses);
			this.writeUTFArray(writer, CALLFILTERS_FIELD_NAME, this.callFilters);

		} catch (Throwable t) {
			logger.warn("Serialization error for entity {}, class version {} serialization part for version 1", this.toString(), this.getClassVersion());
			return;
		}
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		try {
			this.name = reader.readUTF(NAME_FIELD_NAME);
			this.readUTFArray(reader, ADDRESSES_FIELD_NAME, this.addresses);
			this.readUTFArray(reader, CALLFILTERS_FIELD_NAME, this.callFilters);
		} catch (Throwable t) {
			logger.warn("Deserialization error for version 1", t);
			return;
		}

	}

	@Override
	public String toString() {
		return ObjectToString.toString(this);
	}

	@Override
	public int getClassVersion() {
		return CLASS_VERSION;
	}


	private void writeUTFArray(PortableWriter writer, String fieldName, Set<String> field) throws IOException {
		String[] values;
		if (0 < field.size()) {
			values = new String[field.size()];
			Iterator<String> it = field.iterator();
			for (int i = 0; it.hasNext(); ) {
				String address = it.next();
				if (Objects.isNull(address)) {
					continue;
				}
				values[i++] = address;
			}
		} else {
			values = new String[]{""};
		}
		writer.writeUTFArray(fieldName, values);
	}

	private void readUTFArray(PortableReader reader, String fieldName, Set<String> field) throws IOException {
		String[] values = reader.readUTFArray(fieldName);
		if (values.length != 1 || values[0] != "") {
			Arrays.stream(values).forEach(field::add);
		}
	}
}
