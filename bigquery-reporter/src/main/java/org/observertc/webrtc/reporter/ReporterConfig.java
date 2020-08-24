package org.observertc.webrtc.reporter;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("reporter")
public class ReporterConfig {

	public String observeRTCReportsTopic;

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
