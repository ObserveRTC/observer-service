package org.observertc.webrtc.service.reportsink.bigquery;

import java.time.ZoneId;
import org.observertc.webrtc.common.builders.AbstractBuilder;
import org.observertc.webrtc.common.reportsink.ReportService;
import org.observertc.webrtc.common.reportsink.ReportServiceAbstractBuilder;

public class BigQueryReportServiceBuilder extends ReportServiceAbstractBuilder {

	public ReportService build() {
		Config config = this.convertAndValidate(Config.class);
		ZoneId incomingTsZoneId = ZoneId.of(config.incomingTimestampsZoneId);
		BigQueryServiceTimeConverter.construct(incomingTsZoneId);
		BigQueryReportService result = new BigQueryReportService(config);
		return result;
	}

	public static class Config extends AbstractBuilder.Config {

		public String incomingTimestampsZoneId;

		public String projectName;

		public String datasetName;

		public String initiatedCallsTable = "InitiatedCalls";

		public String finishedCallsTable = "FinishedCalls";

		public String joinedPeerConnectionsTable = "JoinedPeerConnections";

		public String detachedPeerConnectionsTable = "DetachedPeerConnections";

		public String remoteInboundRTPSamplesTable = "RemoteInboundRTPSamples";

		public String outboundRTPSamplesTable = "OutboundRTPSamples";

		public boolean createDatasetIfNotExists = true;

		public boolean createTableIfNotExists = true;

		public String inboundRTPSamplesTable = "InboundRTPSamples";

		public String iceCandidatePairsTable = "ICECandidatePairs";

		public String iceLocalCandidatesTable = "ICELocalCandidates";

		public String iceRemoteCandidatesTable = "ICERemoteCandidates";

		public String mediaSourcesTable = "MediaSources";

		public String trackReportsTable = "TrackReports";
	}
}
