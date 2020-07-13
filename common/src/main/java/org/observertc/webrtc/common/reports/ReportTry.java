package org.observertc.webrtc.common.reports;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.CLASS,
		property = "@class",
		visible = true)
@JsonSubTypes({
		@JsonSubTypes.Type(value = InitiatedCallReport.class, name = "REPORT_SUBCLASS")
})
public abstract class ReportTry {
	public ReportType type;

	/**
	 * //	 * Holds information for polymorphic deserialization
	 * //
	 */

	protected ReportTry(ReportType type) {
		this.type = type;
	}

}
