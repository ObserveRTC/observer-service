package org.observertc.webrtc.common.reports;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractReportConsumer<T, R> implements BiFunction<T, Report, R>, ReportConsumer<T, R> {
	private static Logger logger = LoggerFactory.getLogger(AbstractReportConsumer.class);

	private static final Map<String, ReportType> typeMapper;

	static {
		Map<String, ReportType> typeMap = new HashMap<>();
		typeMap.put(InitiatedCallReport.class.getName(), ReportType.INITIATED_CALL);
		typeMap.put(FinishedCallReport.class.getName(), ReportType.FINISHED_CALL);
		typeMap.put(JoinedPeerConnectionReport.class.getName(), ReportType.JOINED_PEER_CONNECTION);
		typeMap.put(DetachedPeerConnectionReport.class.getName(), ReportType.DETACHED_PEER_CONNECTION);
		typeMap.put(RemoteInboundRTPReport.class.getName(), ReportType.REMOTE_INBOUND_RTP_REPORT);
		typeMap.put(InboundRTPReport.class.getName(), ReportType.INBOUND_RTP_REPORT);
		typeMap.put(OutboundRTPReport.class.getName(), ReportType.OUTBOUND_RTP_REPORT);
		typeMap.put(ICELocalCandidateReport.class.getName(), ReportType.ICE_LOCAL_CANDIDATE_REPORT);
		typeMap.put(ICERemoteCandidateReport.class.getName(), ReportType.ICE_REMOTE_CANDIDATE_REPORT);
		typeMap.put(TrackReport.class.getName(), ReportType.TRACK_REPORT);
		typeMap.put(MediaSourceReport.class.getName(), ReportType.MEDIA_SOURCE_REPORT);
		typeMapper = Collections.unmodifiableMap(typeMap);
	}

	@Override
	public R apply(T meta, Report report) {
		ReportType type = report.type;
		if (type == null) {
			type = this.typeMapper.get(report.getClass().getName());
			if (type != null) {
				logger.info("A report type field is null, but based on the class name it is {}", type.name());
				report.type = type;
			} else {
				logger.warn("A report type field is null, and cannot getinfo based on the classname", report.getClass().getName());
				return this.unprocessable(meta, report);
			}
		}

		return this.process(meta, report);
	}

	@Override
	public R processRemoteInboundRTPReport(T meta, RemoteInboundRTPReport report) {
		return null;
	}

	@Override
	public R processInboundRTPReport(T meta, InboundRTPReport report) {
		return null;
	}

	@Override
	public R processOutboundRTPReport(T meta, OutboundRTPReport report) {
		return null;
	}

	@Override
	public R processMediaSourceReport(T meta, MediaSourceReport mediaSourceReport) {
		return null;
	}

	@Override
	public R processTrackReport(T meta, TrackReport trackReport) {
		return null;
	}

	@Override
	public R processJoinedPeerConnectionReport(T meta, JoinedPeerConnectionReport report) {
		return null;
	}

	@Override
	public R processDetachedPeerConnectionReport(T meta, DetachedPeerConnectionReport report) {
		return null;
	}

	@Override
	public R processInitiatedCallReport(T meta, InitiatedCallReport report) {
		return null;
	}

	@Override
	public R processFinishedCallReport(T meta, FinishedCallReport report) {
		return null;
	}

	@Override
	public R processICECandidatePairReport(T meta, ICECandidatePairReport report) {
		return null;
	}

	@Override
	public R processICELocalCandidateReport(T meta, ICELocalCandidateReport report) {
		return null;
	}

	@Override
	public R processICERemoteCandidateReport(T meta, ICERemoteCandidateReport report) {
		return null;
	}


}
