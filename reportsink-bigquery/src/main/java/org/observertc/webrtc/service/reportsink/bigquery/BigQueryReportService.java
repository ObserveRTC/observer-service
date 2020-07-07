package org.observertc.webrtc.service.reportsink.bigquery;

import org.observertc.webrtc.common.reportsink.CallReports;
import org.observertc.webrtc.common.reportsink.MediaStreamReports;
import org.observertc.webrtc.common.reportsink.ReportService;

public class BigQueryReportService implements ReportService {
	private BigQueryService bigQueryService;
	private final BigQueryCallReports callReports;
	private final BigQueryMediaStreamReports mediaStreamReports;

	public BigQueryReportService(BigQueryService bigQueryService) {

		this.bigQueryService = bigQueryService;
		this.callReports = new BigQueryCallReports(this.bigQueryService);
		this.mediaStreamReports = new BigQueryMediaStreamReports(this.bigQueryService);
	}

	@Override
	public CallReports getCallReports() {
		return this.callReports;
	}

	@Override
	public MediaStreamReports getMediaStreamReports() {
		return this.mediaStreamReports;
	}
}
