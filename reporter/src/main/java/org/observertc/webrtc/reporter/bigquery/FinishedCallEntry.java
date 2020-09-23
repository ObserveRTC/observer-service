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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.observertc.webrtc.common.reports.FinishedCallReport;
import org.observertc.webrtc.reporter.TimeConverter;

public class FinishedCallEntry implements BigQueryEntry {

	public static FinishedCallEntry from(FinishedCallReport finishedCallReport) {
		return new FinishedCallEntry()
				.withObserverUUID(finishedCallReport.observerUUID)
				.withCallUUID(finishedCallReport.callUUID)
				.withFinishedTimestamp(finishedCallReport.finished);
	}

	public static final String OBSERVER_UUID_FIELD_NAME = "observerUUID";
	public static final String CALL_UUID_FIELD_NAME = "callUUID";
	public static final String FINISHED_TIMESTAMP_FIELD_NAME = "finished";

	private final Map<String, Object> values;

	public FinishedCallEntry() {
		this.values = new HashMap<>();
	}

	public FinishedCallEntry withObserverUUID(UUID value) {
		this.values.put(OBSERVER_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public FinishedCallEntry withCallUUID(UUID value) {
		this.values.put(CALL_UUID_FIELD_NAME, value.toString());
		return this;
	}

	public FinishedCallEntry withFinishedTimestamp(LocalDateTime value) {
		Long epoch = TimeConverter.GMTLocalDateTimeToEpoch(value);
		this.values.put(FINISHED_TIMESTAMP_FIELD_NAME, epoch);
		return this;
	}

	public UUID getObserverUUID() {
		String value = (String) this.values.get(OBSERVER_UUID_FIELD_NAME);
		if (value == null) {
			return null;
		}
		return UUID.fromString(value);
	}

	public UUID getPeerConnectionUUID() {
		String value = (String) this.values.get(CALL_UUID_FIELD_NAME);
		if (value == null) {
			return null;
		}
		return UUID.fromString(value);
	}

	public LocalDateTime getFinishedTimestamp() {
		Long value = (Long) this.values.get(FINISHED_TIMESTAMP_FIELD_NAME);
		return TimeConverter.epochToGMTLocalDateTime(value);

	}

	public Map<String, Object> toMap() {
		return this.values;
	}
}
