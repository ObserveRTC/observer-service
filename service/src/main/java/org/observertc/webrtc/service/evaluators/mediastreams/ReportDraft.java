package org.observertc.webrtc.service.evaluators.mediastreams;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.observertc.webrtc.common.reports.Report;

public class ReportDraft {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public Report report;
	public LocalDateTime drafted;
	public LocalDateTime processed;


	@JsonCreator
	public ReportDraft() {

	}

	public ReportDraft(Report report, LocalDateTime drafted) {
		this.report = report;
		this.drafted = drafted;
	}


	@Override
	public String toString() {
		String result;
		try {
			result = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return "ReportDraft is unprocessable: " + e.getMessage();
		}
		return result;
	}
}
