package org.observertc.webrtc.common.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("REPORT_SUBCLASS")
public class ReportTrySubClass extends ReportTry {
	public int something;

	@JsonCreator
	public ReportTrySubClass() {
		super(ReportType.JOINED_PEER_CONNECTION);
	}
}
