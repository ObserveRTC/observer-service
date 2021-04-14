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
	public static final int PEER_CONNECTION_DTO_CLASS_ID = 1000;
	public static final int CALL_DTO_CLASS_ID = 2000;
	public static final int WEAKLOCKS_DTO_CLASS_ID = 4000;
	public static final int SENTINEL_DTO_CLASS_ID = 5000;
	public static final int COLLECTION_FILTER_DTO_CLASS_ID = 6000;
	public static final int CALL_FILTER_DTO_CLASS_ID = 7000;
	public static final int PEER_CONNECTION_FILTER_DTO_CLASS_ID = 8000;
	public static final int INBOUND_RTP_TRAFFIC_DTO_CLASS_ID = 9000;
	public static final int OUTBOUND_RTP_TRAFFIC_DTO_CLASS_ID = 10000;
	public static final int REMOTE_INBOUND_RTP_TRAFFIC_DTO_CLASS_ID = 11000;
	public static final int CONFIG_DTO_CLASS_ID = 12000;

	@Override
	public Portable create(int classId) {
		switch (classId) {
			case PEER_CONNECTION_DTO_CLASS_ID:
				return new PeerConnectionDTO();
			case CALL_DTO_CLASS_ID:
				return new CallDTO();
			case WEAKLOCKS_DTO_CLASS_ID:
				return new WeakLockDTO();
			case SENTINEL_DTO_CLASS_ID:
				return new SentinelDTO();
			case CALL_FILTER_DTO_CLASS_ID:
				return new CallFilterDTO();
			case COLLECTION_FILTER_DTO_CLASS_ID:
				return new CollectionFilterDTO();
			case PEER_CONNECTION_FILTER_DTO_CLASS_ID:
				return new PeerConnectionFilterDTO();
			case INBOUND_RTP_TRAFFIC_DTO_CLASS_ID:
				return new InboundRtpTrafficDTO();
			case OUTBOUND_RTP_TRAFFIC_DTO_CLASS_ID:
				return new OutboundRtpTrafficDTO();
			case REMOTE_INBOUND_RTP_TRAFFIC_DTO_CLASS_ID:
				return new RemoteInboundRtpTrafficDTO();
			case CONFIG_DTO_CLASS_ID:
				return new ConfigDTO();
		}
		throw new IllegalArgumentException("Unsupported type " + classId);
	}


}
