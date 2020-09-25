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

package org.observertc.webrtc.reporter.bigquery;

import java.util.HashMap;
import java.util.Map;
import org.observertc.webrtc.schemas.reports.CandidateType;
import org.observertc.webrtc.schemas.reports.NetworkType;
import org.observertc.webrtc.schemas.reports.TransportProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ICELocalCandidateEntry implements BigQueryEntry {

	private static Logger logger = LoggerFactory.getLogger(ICELocalCandidateEntry.class);

	public static final String SERVICE_UUID_FIELD_NAME = "serviceUUID";
	public static final String SERVICE_NAME_FIELD_NAME = "serviceName";
	public static final String CALL_NAME_FIELD_NAME = "callName";
	public static final String CUSTOMER_PROVIDED_FIELD_NAME = "customerProvided";
	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	public static final String BROWSERID_FIELD_NAME = "browserID";
	public static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitID";
	public static final String USER_ID_FIELD_NAME = "userID";

	public static final String CANDIDATE_ID_FIELD_NAME = "CandidateID";
	public static final String CANDIDATE_TYPE_FIELD_NAME = "candidateType";
	public static final String DELETED_FIELD_NAME = "deleted";
	public static final String IP_LSH_FIELD_NAME = "ipLSH";
	public static final String NETWORK_TYPE_FIELD_NAME = "networkType";
	public static final String PORT_FIELD_NAME = "port";
	public static final String PRIORITY_FIELD_NAME = "priority";
	public static final String PROTOCOL_TYPE_FIELD_NAME = "protocolType";

	private final Map<String, Object> values;

	public ICELocalCandidateEntry() {
		this.values = new HashMap<>();
	}


	public ICELocalCandidateEntry withServiceUUID(String value) {
		this.values.put(SERVICE_UUID_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withServiceName(String value) {
		this.values.put(SERVICE_NAME_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withCallName(String value) {
		this.values.put(CALL_NAME_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withCustomProvided(String value) {
		this.values.put(CUSTOMER_PROVIDED_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withTimestamp(Long value) {
		this.values.put(TIMESTAMP_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withBrowserId(String value) {
		this.values.put(BROWSERID_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withUserId(String value) {
		this.values.put(USER_ID_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withPeerConnectionUUID(String value) {
		this.values.put(PEER_CONNECTION_UUID_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withMediaUnitId(String value) {
		this.values.put(MEDIA_UNIT_ID_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withCandidateID(String value) {
		this.values.put(CANDIDATE_ID_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withDeleted(Boolean value) {
		this.values.put(DELETED_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withIPLSH(String value) {
		this.values.put(IP_LSH_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withNetworkType(String value) {
		this.values.put(NETWORK_TYPE_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withCandidateType(String value) {
		this.values.put(CANDIDATE_TYPE_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withPort(Integer value) {
		this.values.put(PORT_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withPriority(Long value) {
		this.values.put(PRIORITY_FIELD_NAME, value);
		return this;
	}

	public ICELocalCandidateEntry withProtocol(String value) {
		this.values.put(PROTOCOL_TYPE_FIELD_NAME, value);
		return this;
	}

	public Map<String, Object> toMap() {
		return this.values;
	}

	public ICELocalCandidateEntry withCandidateType(CandidateType candidateType) {
		if (candidateType == null) {
			return this;
		}
		return this.withCandidateType(candidateType.name());
	}

	public ICELocalCandidateEntry withNetworkType(NetworkType networkType) {
		if (networkType == null) {
			return this;
		}
		return this.withNetworkType(networkType.name());
	}

	public ICELocalCandidateEntry withProtocol(TransportProtocol protocol) {
		if (protocol == null) {
			return this;
		}
		return this.withProtocol(protocol.name());
	}
}
