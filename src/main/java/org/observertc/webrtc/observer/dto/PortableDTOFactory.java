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

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

public class PortableDTOFactory implements PortableFactory {

	public static final int FACTORY_ID = 200000;
	public static final int CALL_DTO_CLASS_ID = 102000;
	public static final int CLIENT_DTO_CLASS_ID = 103000;
	public static final int PEER_CONNECTION_DTO_CLASS_ID = 104000;
	public static final int MEDIA_TRACK_DTO_CLASS_ID = 105000;
	public static final int WEAKLOCKS_DTO_CLASS_ID = 106000;
	public static final int CONFIG_DTO_CLASS_ID = 107000;


	@Override
	public Portable create(int classId) {
		switch (classId) {
			case CALL_DTO_CLASS_ID:
				return new CallDTO();
			case CLIENT_DTO_CLASS_ID:
				return new ClientDTO();
			case PEER_CONNECTION_DTO_CLASS_ID:
				return new PeerConnectionDTO();
			case MEDIA_TRACK_DTO_CLASS_ID:
				return new MediaTrackDTO();
			case WEAKLOCKS_DTO_CLASS_ID:
				return new WeakLockDTO();
			case CONFIG_DTO_CLASS_ID:
				return new ConfigDTO();
		}
		throw new IllegalArgumentException("Unsupported type " + classId);
	}


}
