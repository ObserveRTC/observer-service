package org.observertc.webrtc.common.reports;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractReportProcessor<T> implements Function<Report, T>, ReportProcessor<T> {
	private static Logger logger = LoggerFactory.getLogger(AbstractReportProcessor.class);

	private static final Map<String, ReportType> typeMapper;

	static {
		Map<String, ReportType> typeMap = new HashMap<>();
		typeMap.put(InitiatedCallReport.class.getName(), ReportType.INITIATED_CALL);
		typeMap.put(FinishedCallReport.class.getName(), ReportType.FINISHED_CALL);
		typeMap.put(JoinedPeerConnectionReport.class.getName(), ReportType.JOINED_PEER_CONNECTION);
		typeMap.put(DetachedPeerConnectionReport.class.getName(), ReportType.DETACHED_PEER_CONNECTION);
		typeMap.put(OutboundStreamReport.class.getName(), ReportType.OUTBOUND_STREAM_REPORT);
		typeMap.put(InboundStreamReport.class.getName(), ReportType.INBOUND_STREAM_REPORT);
		typeMap.put(RemoteInboundStreamReport.class.getName(), ReportType.REMOTE_INBOUND_STREAM_REPORT);
		typeMap.put(RemoteInboundRTPReport.class.getName(), ReportType.REMOTE_INBOUND_RTP_REPORT);
		typeMap.put(InboundRTPReport.class.getName(), ReportType.INBOUND_RTP_REPORT);
		typeMap.put(OutboundRTPReport.class.getName(), ReportType.OUTBOUND_RTP_REPORT);
		typeMapper = Collections.unmodifiableMap(typeMap);
	}

	@Override
	public T apply(Report report) {
		ReportType type = report.type;
		if (type == null) {
			type = this.typeMapper.get(report.getClass().getName());
			if (type != null) {
				logger.info("A report type field is null, but based on the class name it is {}", type.name());
				report.type = type;
			} else {
				logger.warn("A report type field is null, and cannot getinfo based on the classname", report.getClass().getName());
				return this.unprocessable(report);
			}
		}

		return this.process(report);
	}

	public T process(RemoteInboundRTPReport report) {
		return null;
	}

	@Override
	public T process(InboundRTPReport report) {
		return null;
	}

	@Override
	public T process(OutboundRTPReport report) {
		return null;
	}

	public T process(JoinedPeerConnectionReport report) {
		return null;
	}

	public T process(DetachedPeerConnectionReport report) {
		return null;
	}

	public T process(InitiatedCallReport report) {
		return null;
	}

	public T process(FinishedCallReport report) {
		return null;
	}

	public T process(OutboundStreamReport report) {
		return null;
	}

	public T process(InboundStreamReport report) {
		return null;
	}

	public T process(RemoteInboundStreamReport report) {
		return null;
	}

}
