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
import org.observertc.webrtc.schemas.reports.ICEState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ICECandidatePairEntry implements BigQueryEntry {

	private static Logger logger = LoggerFactory.getLogger(ICECandidatePairEntry.class);
	public static final String SERVICE_UUID_FIELD_NAME = "serviceUUID";
	public static final String SERVICE_NAME_FIELD_NAME = "serviceName";
	public static final String CALL_NAME_FIELD_NAME = "callName";
	public static final String CUSTOMER_PROVIDED_FIELD_NAME = "customerProvided";
	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	public static final String BROWSERID_FIELD_NAME = "browserID";
	public static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitID";
	public static final String USER_ID_FIELD_NAME = "userID";

	public static final String CANDIDATE_PAIR_ID_FIELD_NAME = "candidatePairID";
	public static final String LOCAL_CANDIDATE_ID_FIELD_NAME = "localCandidateID";
	public static final String REMOTE_CANDIDATE_ID_FIELD_NAME = "remoteCandidateID";
	public static final String WRITABLE_FIELD_NAME = "writable";
	public static final String TOTAL_ROUND_TRIP_TIME_FIELD_NAME = "totalRoundTripTime";
	public static final String ICE_STATE_FIELD_NAME = "state";
	public static final String NOMINATED_FIELD_NAME = "nominated";
	public static final String AVAILABLE_OUTGOING_BITRATE_FIELD_NAME = "availableOutgoingBitrate";
	public static final String BYTES_RECEIVED_FIELD_NAME = "bytesReceived";
	public static final String BYTES_SENT_FIELD_NAME = "bytesSent";
	public static final String CONSENT_REQUESTS_SENT_FIELD_NAME = "consentRequests";
	public static final String CURRENT_ROUND_TRIP_TIME_FIELD_NAME = "currentRoundTripTime";
	public static final String PRIORITY_FIELD_NAME = "priority";
	public static final String REQUESTS_RECEIVED_FIELD_NAME = "requestsReceived";
	public static final String REQUESTS_SENT_FIELD_NAME = "requestsSent";
	public static final String RESPONSES_RECEIVED_FIELD_NAME = "responsesReceived";
	public static final String RESPONSES_SENT_FIELD_NAME = "responsesSent";

	private final Map<String, Object> values;

	public ICECandidatePairEntry() {
		this.values = new HashMap<>();
	}

	public ICECandidatePairEntry withServiceUUID(String value) {
		this.values.put(SERVICE_UUID_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withLocalCandidateId(String value) {
		this.values.put(LOCAL_CANDIDATE_ID_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withRemoteCandidateId(String value) {
		this.values.put(REMOTE_CANDIDATE_ID_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withServiceName(String value) {
		this.values.put(SERVICE_NAME_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withCallName(String value) {
		this.values.put(CALL_NAME_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withUserId(String value) {
		this.values.put(USER_ID_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withCustomProvided(String value) {
		this.values.put(CUSTOMER_PROVIDED_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withPeerConnectionUUID(String value) {
		this.values.put(PEER_CONNECTION_UUID_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withBrowserId(String value) {
		this.values.put(BROWSERID_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withTimestamp(Long value) {
		this.values.put(TIMESTAMP_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withMediaUnitId(String value) {
		this.values.put(MEDIA_UNIT_ID_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withWritable(Boolean value) {
		this.values.put(WRITABLE_FIELD_NAME, value);
		return this;
	}


	public ICECandidatePairEntry withTotalRoundTripTime(Double value) {
		this.values.put(TOTAL_ROUND_TRIP_TIME_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withICEState(ICEState iceState) {
		if (iceState == null) {
			return this;
		}
		return this.withICEState(iceState.name());
	}

	public ICECandidatePairEntry withICEState(String value) {
		this.values.put(ICE_STATE_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withResponseSent(Integer value) {
		this.values.put(RESPONSES_SENT_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withResponseReceived(Integer value) {
		this.values.put(RESPONSES_RECEIVED_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withRequestsSent(Integer value) {
		this.values.put(REQUESTS_SENT_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withRequestsReceived(Integer value) {
		this.values.put(REQUESTS_RECEIVED_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withPriority(Long value) {
		this.values.put(PRIORITY_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withCurrentRoundTripTime(Double value) {
		this.values.put(CURRENT_ROUND_TRIP_TIME_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withConsentRequestsSent(Integer value) {
		this.values.put(CONSENT_REQUESTS_SENT_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withBytesSent(Long value) {
		this.values.put(BYTES_SENT_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withBytesReceived(Long value) {
		this.values.put(BYTES_RECEIVED_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withAvailableOutgoingBitrate(Integer value) {
		this.values.put(AVAILABLE_OUTGOING_BITRATE_FIELD_NAME, value);
		return this;
	}

	public ICECandidatePairEntry withNominated(Boolean value) {
		this.values.put(NOMINATED_FIELD_NAME, value);
		return this;
	}

	public Map<String, Object> toMap() {
		return this.values;
	}

	public ICECandidatePairEntry withCandidatePairId(String candidatePairId) {
		this.values.put(CANDIDATE_PAIR_ID_FIELD_NAME, candidatePairId);
		return this;
	}
}
