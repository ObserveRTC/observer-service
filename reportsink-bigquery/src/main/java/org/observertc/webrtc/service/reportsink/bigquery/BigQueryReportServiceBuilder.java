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

		public String projectName;

		public String datasetName;

		public String initiatedCallsTable = "InitiatedCalls";

		public String finishedCallsTable = "FinishedCalls";

		public String joinedPeerConnectionsTable = "JoinedPeerConnections";

		public String detachedPeerConnectionsTable = "DetachedPeerConnections";

		public String inboundStreamSamplesTable = "InboundStreamSamples";

		public String remoteInboundStreamSamplesTable = "RemoteInboundStreamSamples";

		public String outboundStreamSamplesTable = "OutboundStreamSamples";

		public boolean schemaCheckerEnabled = true;

		public boolean createDatasetIfNotExists = true;

	}
}
