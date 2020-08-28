package org.observertc.webrtc.reporter;

import java.util.function.Function;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.common.reports.ReportProcessor;

public interface Reporter extends ReportProcessor<Void>, Function<Report, Void> {

	void flush();
}
