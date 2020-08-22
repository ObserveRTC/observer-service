package org.observertc.webrtc.common.reports;

import java.util.UUID;
import java.util.function.Function;

public class ReportObserverUUIDExtractor implements Function<Report, UUID>, ReportProcessor<UUID> {
	@Override
	public UUID processMediaSourceReport(MediaSourceReport mediaSourceReport) {
		return mediaSourceReport.observerUUID;
	}

	@Override
	public UUID processTrackReport(TrackReport trackReport) {
		return trackReport.observerUUID;
	}

	@Override
	public UUID processJoinedPeerConnectionReport(JoinedPeerConnectionReport report) {
		return report.observerUUID;
	}

	@Override
	public UUID processDetachedPeerConnectionReport(DetachedPeerConnectionReport report) {
		return report.observerUUID;
	}

	@Override
	public UUID processInitiatedCallReport(InitiatedCallReport report) {
		return report.observerUUID;
	}

	@Override
	public UUID processFinishedCallReport(FinishedCallReport report) {
		return report.observerUUID;
	}

	@Override
	public UUID processRemoteInboundRTPReport(RemoteInboundRTPReport report) {
		return report.observerUUID;
	}

	@Override
	public UUID processInboundRTPReport(InboundRTPReport report) {
		return report.observerUUID;
	}

	@Override
	public UUID processOutboundRTPReport(OutboundRTPReport report) {
		return report.observerUUID;
	}

	@Override
	public UUID processICECandidatePairReport(ICECandidatePairReport report) {
		return report.observerUUID;
	}

	@Override
	public UUID processICELocalCandidateReport(ICELocalCandidateReport report) {
		return report.observerUUID;
	}

	@Override
	public UUID processICERemoteCandidateReport(ICERemoteCandidateReport report) {
		return report.observerUUID;
	}

	@Override
	public UUID apply(Report report) {
		return this.process(report);
	}
}
