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

package org.observertc.webrtc.observer.evaluators.reportdrafts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.Instant;
import java.util.UUID;

@JsonTypeName("FINISHED_CALL")
public class FinishedCallReportDraft extends ReportDraft {
	public static FinishedCallReportDraft of(UUID serviceUUID, UUID callUUID, Long finished) {
		Long created = Instant.now().toEpochMilli();
		FinishedCallReportDraft result = new FinishedCallReportDraft();
		result.created = created;
		result.callUUID = callUUID;
		result.serviceUUID = serviceUUID;
		result.finished = finished;
		return result;
	}

	@JsonCreator
	public FinishedCallReportDraft() {
		super(ReportDraftType.FINISHED_CALL);
	}

	public UUID serviceUUID;
	public UUID callUUID;
	public Long finished;
}
