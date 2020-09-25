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
import org.observertc.webrtc.schemas.reports.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaSourceEntry implements BigQueryEntry {

	private static Logger logger = LoggerFactory.getLogger(MediaSourceEntry.class);

	public static final String SERVICE_UUID_FIELD_NAME = "serviceUUID";
	public static final String SERVICE_NAME_FIELD_NAME = "serviceName";
	public static final String CALL_NAME_FIELD_NAME = "callName";
	public static final String CUSTOMER_PROVIDED_FIELD_NAME = "customerProvided";
	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	public static final String BROWSERID_FIELD_NAME = "browserID";
	public static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitID";
	public static final String USER_ID_FIELD_NAME = "userID";

	public static final String MEDIA_SOURCE_ID_FIELD_NAME = "mediaSourceID";
	public static final String FRAMES_PER_SECOND_FIELD_NAME = "framesPerSecond";
	public static final String HEIGHT_FIELD_NAME = "height";
	public static final String WIDTH_FIELD_NAME = "width";
	public static final String AUDIO_LEVEL_FIELD_NAME = "audioLevel";
	public static final String MEDIA_TYPE_FIELD_NAME = "mediaType";
	public static final String TOTAL_AUDIO_ENERGY_FIELD_NAME = "totalAudioEnergy";
	public static final String TOTAL_SAMPLES_DURATION_FIELD_NAME = "totalSamplesDuration";

	private final Map<String, Object> values;

	public MediaSourceEntry() {
		this.values = new HashMap<>();
	}

	public MediaSourceEntry withServiceUUID(String value) {
		this.values.put(SERVICE_UUID_FIELD_NAME, value);
		return this;
	}

	public MediaSourceEntry withServiceName(String value) {
		this.values.put(SERVICE_NAME_FIELD_NAME, value);
		return this;
	}

	public MediaSourceEntry withCallName(String value) {
		this.values.put(CALL_NAME_FIELD_NAME, value);
		return this;
	}

	public MediaSourceEntry withUserId(String value) {
		this.values.put(USER_ID_FIELD_NAME, value);
		return this;
	}

	public MediaSourceEntry withCustomProvided(String value) {
		this.values.put(CUSTOMER_PROVIDED_FIELD_NAME, value);
		return this;
	}

	public MediaSourceEntry withPeerConnectionUUID(String value) {
		this.values.put(PEER_CONNECTION_UUID_FIELD_NAME, value);
		return this;
	}

	public MediaSourceEntry withBrowserId(String value) {
		this.values.put(BROWSERID_FIELD_NAME, value);
		return this;
	}

	public MediaSourceEntry withTimestamp(Long value) {
		this.values.put(TIMESTAMP_FIELD_NAME, value);
		return this;
	}

	public MediaSourceEntry withMediaUnitId(String value) {
		this.values.put(MEDIA_UNIT_ID_FIELD_NAME, value);
		return this;
	}

	public MediaSourceEntry withMediaType(MediaType mediaType) {
		if (mediaType == null) {
			return this;
		}
		return this.withMediaType(mediaType.name());
	}

	public MediaSourceEntry withMediaType(String value) {
		this.values.put(MEDIA_TYPE_FIELD_NAME, value);
		return this;
	}

	public MediaSourceEntry withMediaSourceID(String value) {
		this.values.put(MEDIA_SOURCE_ID_FIELD_NAME, value);
		return this;
	}

	public MediaSourceEntry withFramesPerSecond(Double value) {
		this.values.put(FRAMES_PER_SECOND_FIELD_NAME, value);
		return this;
	}

	public MediaSourceEntry withAudioLevel(Float value) {
		this.values.put(AUDIO_LEVEL_FIELD_NAME, value);
		return this;
	}

	public MediaSourceEntry withTotalAudioEnergy(Float value) {
		this.values.put(TOTAL_AUDIO_ENERGY_FIELD_NAME, value);
		return this;
	}

	public MediaSourceEntry withTotalSamplesDuration(Double value) {
		this.values.put(TOTAL_SAMPLES_DURATION_FIELD_NAME, value);
		return this;
	}


	public MediaSourceEntry withHeight(Double value) {
		this.values.put(HEIGHT_FIELD_NAME, value);
		return this;
	}

	public MediaSourceEntry withWidth(Double value) {
		this.values.put(WIDTH_FIELD_NAME, value);
		return this;
	}


	public Map<String, Object> toMap() {
		return this.values;
	}

}
