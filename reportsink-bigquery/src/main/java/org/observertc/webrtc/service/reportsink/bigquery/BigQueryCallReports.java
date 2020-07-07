package org.observertc.webrtc.service.reportsink.bigquery;

import javax.inject.Singleton;
import org.observertc.webrtc.common.reports.DetachedPeerConnection;
import org.observertc.webrtc.common.reports.FinishedCall;
import org.observertc.webrtc.common.reports.InitiatedCall;
import org.observertc.webrtc.common.reports.JoinedPeerConnection;
import org.observertc.webrtc.common.reportsink.CallReports;
import org.observertc.webrtc.common.reportsink.ReportResponse;

@Singleton
public class BigQueryCallReports implements CallReports {
	private static final String INITIATED_CALLS_TABLE_NAME = "InitiatedCalls";
	private static final String FINISHED_CALLS_TABLE_NAME = "FinishedCalls";
	private static final String JOINED_PEER_CONNECTIONS_TABLE_NAME = "JoinedPeerConnections";
	private static final String DETACHED_PEER_CONNECTIONS_TABLE_NAME = "DetachedPeerConnections";

	private final BigQueryTable<InitiatedCallEntry> initiatedCalls;
	private final BigQueryTable<FinishedCallEntry> finishedCalls;
	private final BigQueryTable<JoinedPeerConnectionEntry> joinedPeerConnections;
	private final BigQueryTable<DetachedPeerConnectionEntry> detachedPeerConnections;

	public BigQueryCallReports(BigQueryService bigQueryService) {
		this.initiatedCalls = new BigQueryTable(bigQueryService, INITIATED_CALLS_TABLE_NAME);
		this.finishedCalls = new BigQueryTable(bigQueryService, FINISHED_CALLS_TABLE_NAME);
		this.joinedPeerConnections = new BigQueryTable(bigQueryService, JOINED_PEER_CONNECTIONS_TABLE_NAME);
		this.detachedPeerConnections = new BigQueryTable(bigQueryService, DETACHED_PEER_CONNECTIONS_TABLE_NAME);
	}

	@Override
	public ReportResponse joinedPeerConnection(JoinedPeerConnection value) {
		JoinedPeerConnectionEntry joinedPeerConnectionEntry = JoinedPeerConnectionEntry.from(value);
		this.joinedPeerConnections.insert(joinedPeerConnectionEntry);
		return null;
	}

	@Override
	public ReportResponse detachedPeerConnection(DetachedPeerConnection value) {
		DetachedPeerConnectionEntry detachedPeerConnectionEntry = DetachedPeerConnectionEntry.from(value);
		this.detachedPeerConnections.insert(detachedPeerConnectionEntry);
		return null;
	}

	@Override
	public ReportResponse initiatedCall(InitiatedCall value) {
		InitiatedCallEntry initiatedCallEntry = InitiatedCallEntry.from(value);
		this.initiatedCalls.insert(initiatedCallEntry);
		return null;
	}

	@Override
	public ReportResponse finishedCall(FinishedCall value) {
		FinishedCallEntry finishedCallEntry = FinishedCallEntry.from(value);
		this.finishedCalls.insert(finishedCallEntry);
		return null;
	}
}
