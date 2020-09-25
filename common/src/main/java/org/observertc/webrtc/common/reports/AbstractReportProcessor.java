//package org.observertc.webrtc.common.reports;
//
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public abstract class AbstractReportProcessor<T> implements Function<Report, T>, ReportProcessor<T> {
//	private static Logger logger = LoggerFactory.getLogger(AbstractReportProcessor.class);
//
//	private static final Map<String, ReportType> typeMapper;
//
//	static {
//		Map<String, ReportType> typeMap = new HashMap<>();
//		typeMap.put(InitiatedCallReport.class.getName(), ReportType.INITIATED_CALL);
//		typeMap.put(FinishedCallReport.class.getName(), ReportType.FINISHED_CALL);
//		typeMap.put(JoinedPeerConnectionReport.class.getName(), ReportType.JOINED_PEER_CONNECTION);
//		typeMap.put(DetachedPeerConnectionReport.class.getName(), ReportType.DETACHED_PEER_CONNECTION);
//		typeMap.put(RemoteInboundRTPReport.class.getName(), ReportType.REMOTE_INBOUND_RTP_REPORT);
//		typeMap.put(InboundRTPReport.class.getName(), ReportType.INBOUND_RTP_REPORT);
//		typeMap.put(OutboundRTPReport.class.getName(), ReportType.OUTBOUND_RTP_REPORT);
//		typeMap.put(ICELocalCandidateReport.class.getName(), ReportType.ICE_LOCAL_CANDIDATE_REPORT);
//		typeMap.put(ICERemoteCandidateReport.class.getName(), ReportType.ICE_REMOTE_CANDIDATE_REPORT);
//		typeMap.put(ICECandidatePairReport.class.getName(), ReportType.ICE_CANDIDATE_PAIR_REPORT);
//		typeMap.put(TrackReport.class.getName(), ReportType.TRACK_REPORT);
//		typeMap.put(MediaSourceReport.class.getName(), ReportType.MEDIA_SOURCE_REPORT);
//		typeMapper = Collections.unmodifiableMap(typeMap);
//	}
//
//	@Override
//	public T apply(Report report) {
//		ReportType type = report.type;
//		if (type == null) {
//			type = this.typeMapper.get(report.getClass().getName());
//			if (type != null) {
//				logger.info("A report type field is null, but based on the class name it is {}", type.name());
//				report.type = type;
//			} else {
//				logger.warn("A report type field is null, and cannot getinfo based on the classname", report.getClass().getName());
//				return this.unprocessable(report);
//			}
//		}
//
//		return this.process(report);
//	}
//
//	@Override
//	public T processRemoteInboundRTPReport(RemoteInboundRTPReport report) {
//		return null;
//	}
//
//	@Override
//	public T processInboundRTPReport(InboundRTPReport report) {
//		return null;
//	}
//
//	@Override
//	public T processOutboundRTPReport(OutboundRTPReport report) {
//		return null;
//	}
//
//	@Override
//	public T processMediaSourceReport(MediaSourceReport mediaSourceReport) {
//		return null;
//	}
//
//	@Override
//	public T processTrackReport(TrackReport trackReport) {
//		return null;
//	}
//
//	@Override
//	public T processJoinedPeerConnectionReport(JoinedPeerConnectionReport report) {
//		return null;
//	}
//
//	@Override
//	public T processDetachedPeerConnectionReport(DetachedPeerConnectionReport report) {
//		return null;
//	}
//
//	@Override
//	public T processInitiatedCallReport(InitiatedCallReport report) {
//		return null;
//	}
//
//	@Override
//	public T processFinishedCallReport(FinishedCallReport report) {
//		return null;
//	}
//
//	@Override
//	public T processICECandidatePairReport(ICECandidatePairReport report) {
//		return null;
//	}
//
//	@Override
//	public T processICELocalCandidateReport(ICELocalCandidateReport report) {
//		return null;
//	}
//
//	@Override
//	public T processICERemoteCandidateReport(ICERemoteCandidateReport report) {
//		return null;
//	}
//
//
//}
