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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractReportDraftProcessor implements Consumer<ReportDraft> {
	private static Logger logger = LoggerFactory.getLogger(AbstractReportDraftProcessor.class);

	private static final Map<String, ReportDraftType> typeMapper;

	static {
		Map<String, ReportDraftType> typeMap = new HashMap<>();
		typeMap.put(InitiatedCallReportDraft.class.getName(), ReportDraftType.INITIATED_CALL);
		typeMap.put(FinishedCallReportDraft.class.getName(), ReportDraftType.FINISHED_CALL);
		typeMapper = Collections.unmodifiableMap(typeMap);
	}

	public void accept(ReportDraft report) {
		ReportDraftType type = report.type;
		if (type == null) {
			type = this.typeMapper.get(report.getClass().getName());
			if (type != null) {
				logger.info("A report type field is null, but based on the class name it is {}", type.name());
				report.type = type;
			} else {
				logger.warn("A report type field is null, and cannot getinfo based on the classname", report.getClass().getName());
				this.unprocessable(report);
				return;
			}
		}

		try {
			switch (report.type) {
				case INITIATED_CALL:
					this.processInitiatedCallReport((InitiatedCallReportDraft) report);
					break;
				case FINISHED_CALL:
					this.processFinishedCallReport((FinishedCallReportDraft) report);
					break;
				default:
					this.unprocessable(report);
					break;
			}
		} catch (Exception ex) {
			logger.error("Cannot process report draft", ex);
		}
	}

	public abstract void processInitiatedCallReport(InitiatedCallReportDraft report);

	public abstract void processFinishedCallReport(FinishedCallReportDraft report);

	public void unprocessable(ReportDraft reportDraft) {
		logger.error("Cannot process reportDraft: {}", reportDraft.toString());
	}
}
