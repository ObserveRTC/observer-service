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

public class DetachedPeerConnectionEntry implements BigQueryEntry {

	public static final String SERVICE_UUID_FIELD_NAME = "serviceUUID";
	public static final String SERVICE_NAME_FIELD_NAME = "serviceName";
	public static final String CALL_UUID_FIELD_NAME = "callUUID";
	public static final String CALL_NAME_FIELD_NAME = "callName";
	public static final String CUSTOMER_PROVIDED_FIELD_NAME = "customerProvided";
	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	public static final String BROWSERID_FIELD_NAME = "browserID";
	public static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitID";
	public static final String USER_ID_FIELD_NAME = "userID";
	public static final String TIMEZONE_FIELD_NAME = "timeZone";

	private final Map<String, Object> values;

	public DetachedPeerConnectionEntry() {
		this.values = new HashMap<>();
	}

	public DetachedPeerConnectionEntry withServiceUUID(String value) {
		this.values.put(SERVICE_UUID_FIELD_NAME, value);
		return this;
	}

	public DetachedPeerConnectionEntry withCallUUID(String value) {
		this.values.put(CALL_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public DetachedPeerConnectionEntry withServiceName(String value) {
		this.values.put(SERVICE_NAME_FIELD_NAME, value);
		return this;
	}

	public DetachedPeerConnectionEntry withCallName(String value) {
		this.values.put(CALL_NAME_FIELD_NAME, value);
		return this;
	}

	public DetachedPeerConnectionEntry withCustomProvided(String value) {
		this.values.put(CUSTOMER_PROVIDED_FIELD_NAME, value);
		return this;
	}

	public DetachedPeerConnectionEntry withUserId(String value) {
		this.values.put(USER_ID_FIELD_NAME, value);
		return this;
	}

	public DetachedPeerConnectionEntry withPeerConnectionUUID(String value) {
		this.values.put(PEER_CONNECTION_UUID_FIELD_NAME, value);
		return this;
	}

	public DetachedPeerConnectionEntry withBrowserId(String value) {
		this.values.put(BROWSERID_FIELD_NAME, value);
		return this;
	}

	public DetachedPeerConnectionEntry withTimestamp(Long value) {
		this.values.put(TIMESTAMP_FIELD_NAME, value);
		return this;
	}

	public DetachedPeerConnectionEntry withMediaUnitId(String value) {
		this.values.put(MEDIA_UNIT_ID_FIELD_NAME, value);
		return this;
	}

	public DetachedPeerConnectionEntry withTimeZone(String value) {
		this.values.put(TIMEZONE_FIELD_NAME, value);
		return this;
	}

	public Map<String, Object> toMap() {
		return this.values;
	}
}
