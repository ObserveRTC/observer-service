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

public class TrackReportEntry implements BigQueryEntry {
	public static final String SERVICE_UUID_FIELD_NAME = "serviceUUID";
	public static final String SERVICE_NAME_FIELD_NAME = "serviceName";
	public static final String CALL_NAME_FIELD_NAME = "callName";
	public static final String CUSTOMER_PROVIDED_FIELD_NAME = "customerProvided";
	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String PEER_CONNECTION_UUID_FIELD_NAME = "peerConnectionUUID";
	public static final String BROWSERID_FIELD_NAME = "browserID";
	public static final String MEDIA_UNIT_ID_FIELD_NAME = "mediaUnitID";
	public static final String TRACK_ID_FIELD_NAME = "trackID";
	public static final String USER_ID_FIELD_NAME = "userID";

	public static final String AUDIO_LEVEL_FIELD_NAME = "audioLevel";
	public static final String CONCEALED_SAMPLES_FIELD_NAME = "concealedSamples";
	public static final String CONCEALMENT_EVENTS_FIELD_NAME = "concealmentEvents";
	public static final String DETACHED_FIELD_NAME = "detached";
	public static final String ENDED_FIELD_NAME = "ended";
	public static final String FRAMES_HEIGHT_FIELD_NAME = "frameHeight";
	public static final String FRAMES_DECODED_FIELD_NAME = "framesDecoded";
	public static final String FRAMES_DROPPED_FIELD_NAME = "framesDropped";
	public static final String FRAMES_RECEIVED_FIELD_NAME = "framesReceived";
	public static final String FRAMES_SENT_FIELD_NAME = "framesSent";
	public static final String FRAMES_WIDTH_FIELD_NAME = "frameWidth";
	public static final String HUGE_FRAMES_SENT_FIELD_NAME = "hugeFramesSent";
	public static final String INSERTED_SAMPLES_FOR_DECELERATION_FIELD_NAME = "insertedSamplesForDeceleration";
	public static final String JITTER_BUFFER_DELAY_FIELD_NAME = "jitterBufferDelay";
	public static final String JITTER_BUFFER_EMITTED_COUNT_FIELD_NAME = "jitterBufferEmittedCount";
	public static final String MEDIA_TYPE_FIELD_NAME = "mediaType";
	public static final String REMOTE_SOURCE_FIELD_NAME = "remoteSource";
	public static final String REMOVED_SAMPLES_FOR_ACCELERATION_FIELD_NAME = "removedSamplesForAcceleration";
	public static final String SILENT_CONCEALED_SAMPLES_FIELD_NAME = "silentConcealedSamples";
	public static final String TOTAL_AUDIO_ENERGY_FIELD_NAME = "totalAudioEnergy";
	public static final String TOTAL_SAMPLES_DURATION_FIELD_NAME = "totalSamplesDuration";
	public static final String TOTAL_SAMPLES_RECEIVED_FIELD_NAME = "totalSamplesReceived";
	public static final String MEDIA_SOURCE_ID_FIELD_NAME = "mediaSourceID";


	private static Logger logger = LoggerFactory.getLogger(TrackReportEntry.class);

	private final Map<String, Object> values;

	public TrackReportEntry() {
		this.values = new HashMap<>();
	}

	public TrackReportEntry withServiceUUID(String value) {
		this.values.put(SERVICE_UUID_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withServiceName(String value) {
		this.values.put(SERVICE_NAME_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withCallName(String value) {
		this.values.put(CALL_NAME_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withUserId(String value) {
		this.values.put(USER_ID_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withCustomProvided(String value) {
		this.values.put(CUSTOMER_PROVIDED_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withPeerConnectionUUID(String value) {
		this.values.put(PEER_CONNECTION_UUID_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withBrowserId(String value) {
		this.values.put(BROWSERID_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withTimestamp(Long value) {
		this.values.put(TIMESTAMP_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withMediaUnitId(String value) {
		this.values.put(MEDIA_UNIT_ID_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withMediaType(MediaType mediaType) {
		if (mediaType == null) {
			return this;
		}
		return this.withMediaType(mediaType.name());
	}

	public TrackReportEntry withMediaType(String value) {
		this.values.put(MEDIA_TYPE_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withTrackID(String value) {
		this.values.put(TRACK_ID_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withAudioLevel(Double value) {
		this.values.put(AUDIO_LEVEL_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withConcealmentEvents(Integer value) {
		this.values.put(CONCEALMENT_EVENTS_FIELD_NAME, value);
		return this;
	}


	public TrackReportEntry withDetached(Boolean value) {
		this.values.put(DETACHED_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withEnded(Boolean value) {
		this.values.put(ENDED_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withFramesHeight(Integer value) {
		this.values.put(FRAMES_HEIGHT_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withConcealedSamples(Integer value) {
		this.values.put(CONCEALED_SAMPLES_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withFramesDecoded(Integer value) {
		this.values.put(FRAMES_DECODED_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withFramesDropped(Integer value) {
		this.values.put(FRAMES_DROPPED_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withFramesReceived(Integer value) {
		this.values.put(FRAMES_RECEIVED_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withFramesSent(Integer value) {
		this.values.put(FRAMES_SENT_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withFramesWidth(Integer value) {
		this.values.put(FRAMES_WIDTH_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withHugeFramesSent(Integer value) {
		this.values.put(HUGE_FRAMES_SENT_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withInsertedSamplesForDeceleration(Integer value) {
		this.values.put(INSERTED_SAMPLES_FOR_DECELERATION_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withJitterBufferDelay(Double value) {
		this.values.put(JITTER_BUFFER_DELAY_FIELD_NAME, value);
		return this;
	}


	public TrackReportEntry withJitterBufferEmittedCount(Integer value) {
		this.values.put(JITTER_BUFFER_EMITTED_COUNT_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withRemoteSource(Boolean value) {
		this.values.put(REMOTE_SOURCE_FIELD_NAME, value);
		return this;
	}


	public TrackReportEntry withRemovedSamplesForAcceleration(Integer value) {
		this.values.put(REMOVED_SAMPLES_FOR_ACCELERATION_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withSilentConcealedSamples(Integer value) {
		this.values.put(SILENT_CONCEALED_SAMPLES_FIELD_NAME, value);
		return this;
	}


	public TrackReportEntry withTotalAudioEnergy(Double value) {
		this.values.put(TOTAL_AUDIO_ENERGY_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withTotalSamplesDuration(Double value) {
		this.values.put(TOTAL_SAMPLES_DURATION_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withTotalSamplesReceived(Integer value) {
		this.values.put(TOTAL_SAMPLES_RECEIVED_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withMediaSourceID(String value) {
		this.values.put(MEDIA_SOURCE_ID_FIELD_NAME, value);
		return this;
	}

	public TrackReportEntry withConcealmentSamples(Integer value) {
		this.values.put(CONCEALED_SAMPLES_FIELD_NAME, value);
		return this;
	}

	@Override
	public Map<String, Object> toMap() {
		return this.values;
	}


}
