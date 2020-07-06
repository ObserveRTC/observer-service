package org.observertc.webrtc.observer.service.reportsink;

import org.observertc.webrtc.observer.service.reports.DetachedPeerConnection;
import org.observertc.webrtc.observer.service.reports.FinishedCall;
import org.observertc.webrtc.observer.service.reports.InitiatedCall;
import org.observertc.webrtc.observer.service.reports.JoinedPeerConnection;

public interface CallReports {
	
	ReportResponse joinedPeerConnection(JoinedPeerConnection value);

	ReportResponse detachedPeerConnection(DetachedPeerConnection value);

	ReportResponse initiatedCall(InitiatedCall initiatedCall);

	ReportResponse finishedCall(FinishedCall finishedCall);
}
