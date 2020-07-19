package org.observertc.webrtc.common.reports;

import java.util.function.Consumer;

public interface ReportProcessor extends Consumer<Report> {

	void process(JoinedPeerConnectionReport report);

	void process(DetachedPeerConnectionReport report);

	void process(InitiatedCallReport report);

	void process(FinishedCallReport report);

	void process(OutboundStreamSampleReport report);

	void process(InboundStreamSampleReport report);

	void process(RemoteInboundStreamSampleReport report);
}
