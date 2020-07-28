package org.observertc.webrtc.service.reportsink.bigquery;

import org.observertc.webrtc.common.builders.AbstractBuilder;
import org.observertc.webrtc.common.reportsink.ReportService;
import org.observertc.webrtc.common.reportsink.ReportServiceAbstractBuilder;

public class BigQueryReportServiceBuilder extends ReportServiceAbstractBuilder {

	public ReportService build() {
		Config config = this.convertAndValidate(Config.class);
		BigQueryReportService result = new BigQueryReportService(config);
		return result;
	}

	public static class Config extends AbstractBuilder.Config {

		public String timeZoneId = "EET";

		public String projectName;

		public String datasetName;

		public String initiatedCallsTable = "InitiatedCalls";

		public String finishedCallsTable = "FinishedCalls";

		public String joinedPeerConnectionsTable = "JoinedPeerConnections";

		public String detachedPeerConnectionsTable = "DetachedPeerConnections";

		public String inboundStreamReportsTable = "InboundStreamReports";

		public String remoteInboundStreamReportsTable = "RemoteInboundStreamReports";

		public String outboundStreamReportsTable = "OutboundStreamReports";

		public String remoteInboundRTPSamplesTable = "RemoteInboundRTPSamples";

		public String outboundRTPSamplesTable = "OutboundRTPSamples";

		public boolean createDatasetIfNotExists = true;

		public String inboundRTPSamplesTable = "InboundRTPSamples";
	}
}
