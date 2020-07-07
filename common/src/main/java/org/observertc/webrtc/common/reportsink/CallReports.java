package org.observertc.webrtc.common.reportsink;

import org.observertc.webrtc.common.reports.DetachedPeerConnection;
import org.observertc.webrtc.common.reports.FinishedCall;
import org.observertc.webrtc.common.reports.InitiatedCall;
import org.observertc.webrtc.common.reports.JoinedPeerConnection;

public interface CallReports {
	
	ReportResponse joinedPeerConnection(JoinedPeerConnection value);

	ReportResponse detachedPeerConnection(DetachedPeerConnection value);

	ReportResponse initiatedCall(InitiatedCall initiatedCall);

	ReportResponse finishedCall(FinishedCall finishedCall);
}
