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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "@class",
		defaultImpl = ReportDraft.class)
@JsonSubTypes({
		@JsonSubTypes.Type(value = InitiatedCallReportDraft.class, name = "INITIATED_CALL"),
		@JsonSubTypes.Type(value = FinishedCallReportDraft.class, name = "FINISHED_CALL"),
})
public abstract class ReportDraft {

	public ReportDraftType type;

	public Long created;

	public Long processed;

	protected ReportDraft(ReportDraftType type) {
		this.type = type;
	}

}
