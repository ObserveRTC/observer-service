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
public class MediaTrackDTO implements VersionedPortable {
	private static final Logger logger = LoggerFactory.getLogger(MediaTrackDTO.class);
	public static final int CLASS_VERSION = 1;

	private static final String PEER_CONNECTION_ID_FIELD_NAME = "peerConnectionId";
	private static final String SSRC_FIELD_NAME = "ssrc";
	private static final String ATTACHED_FIELD_NAME = "attached"; // antonym: detached


	public static MediaTrackDTO of(
			UUID peerConnectionId,
			Long ssrc,
			Long attached
	) {
		Objects.requireNonNull(peerConnectionId);
		Objects.requireNonNull(ssrc);
		Objects.requireNonNull(attached);

		MediaTrackDTO result = new MediaTrackDTO();
		result.peerConnectionId = peerConnectionId;
		result.ssrc = ssrc;
		result.attached = attached;
		return result;
	}

	public UUID peerConnectionId;
	public Long ssrc;
	public Long attached;


//	@Deprecated
//	public Set<Long> SSRCs = new HashSet<>();

	@Override
	public int getFactoryId() {
		return PortableDTOFactory.FACTORY_ID;
	}

	@Override
	public int getClassId() {
		return PortableDTOFactory.MEDIA_TRACK_DTO_CLASS_ID;
	}

	@Override
	public void writePortable(PortableWriter writer) throws IOException {
		writer.writeByteArray(PEER_CONNECTION_ID_FIELD_NAME, UUIDAdapter.toBytes(this.peerConnectionId));
		writer.writeLong(SSRC_FIELD_NAME, this.ssrc);
		writer.writeLong(ATTACHED_FIELD_NAME, this.attached);

//		SerDeUtils.writeLongArray(writer, SSRC_FIELD_NAME, this.SSRCs, -1);
	}

	@Override
	public void readPortable(PortableReader reader) throws IOException {
		this.peerConnectionId = UUIDAdapter.toUUID(reader.readByteArray(PEER_CONNECTION_ID_FIELD_NAME));
		this.ssrc = reader.readLong(SSRC_FIELD_NAME);
		this.attached = reader.readLong(ATTACHED_FIELD_NAME);
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
		MediaTrackDTO otherDTO = (MediaTrackDTO) other;
		if (!Objects.equals(this.peerConnectionId, otherDTO.peerConnectionId)) return false;
		if (!Objects.equals(this.ssrc, otherDTO.ssrc)) return false;
		if (!Objects.equals(this.attached, otherDTO.attached)) return false;
		return true;
	}
}
