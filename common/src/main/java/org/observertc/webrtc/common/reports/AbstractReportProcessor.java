package org.observertc.webrtc.common.reports;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractReportProcessor implements Consumer<Report>, ReportProcessor {
	private static Logger logger = LoggerFactory.getLogger(AbstractReportProcessor.class);

	private static final Map<String, ReportType> typeMapper;

	static {
		Map<String, ReportType> typeMap = new HashMap<>();
		typeMap.put(InitiatedCallReport.class.getName(), ReportType.INITIATED_CALL);
		typeMap.put(FinishedCallReport.class.getName(), ReportType.FINISHED_CALL);
		typeMap.put(JoinedPeerConnectionReport.class.getName(), ReportType.JOINED_PEER_CONNECTION);
		typeMap.put(DetachedPeerConnectionReport.class.getName(), ReportType.DETACHED_PEER_CONNECTION);
		typeMap.put(OutboundStreamSampleReport.class.getName(), ReportType.OUTBOUND_STREAM_SAMPLE);
		typeMap.put(InboundStreamSampleReport.class.getName(), ReportType.INBOUND_STREAM_SAMPLE);
		typeMap.put(RemoteInboundStreamSampleReport.class.getName(), ReportType.REMOTE_INBOUND_STREAM_SAMPLE);
		typeMapper = Collections.unmodifiableMap(typeMap);
	}

	@Override
	public void accept(Report report) {
		ReportType type = report.type;
		if (type == null) {
			type = this.typeMapper.get(report.getClass().getName());
			if (type != null) {
				logger.info("A report type field is null, but based on the class name it is {}", type.name());
			} else {
				logger.warn("A report type field is null, and cannot getinfo based on the classname", report.getClass().getName());
				return;
			}
		}
		switch (type) {
			case FINISHED_CALL:
				FinishedCallReport finishedCallReport = (FinishedCallReport) report;
				this.process(finishedCallReport);
				break;
			case JOINED_PEER_CONNECTION:
				JoinedPeerConnectionReport joinedPeerConnectionReport = (JoinedPeerConnectionReport) report;
				this.process(joinedPeerConnectionReport);
				break;
			case INITIATED_CALL:
				InitiatedCallReport initiatedCallReport = (InitiatedCallReport) report;
				this.process(initiatedCallReport);
				break;
			case DETACHED_PEER_CONNECTION:
				DetachedPeerConnectionReport detachedPeerConnectionReport = (DetachedPeerConnectionReport) report;
				this.process(detachedPeerConnectionReport);
				break;
			case INBOUND_STREAM_SAMPLE:
				InboundStreamSampleReport inboundStreamSampleReport = (InboundStreamSampleReport) report;
				this.process(inboundStreamSampleReport);
				break;
			case OUTBOUND_STREAM_SAMPLE:
				OutboundStreamSampleReport outboundStreamSampleReport = (OutboundStreamSampleReport) report;
				this.process(outboundStreamSampleReport);
				break;
			case REMOTE_INBOUND_STREAM_SAMPLE:
				RemoteInboundStreamSampleReport remoteInboundStreamSampleReport = (RemoteInboundStreamSampleReport) report;
				this.process(remoteInboundStreamSampleReport);
				break;
		}
	}

	public void process(JoinedPeerConnectionReport report) {

	}

	public void process(DetachedPeerConnectionReport report) {

	}

	public void process(InitiatedCallReport report) {

	}

	public void process(FinishedCallReport report) {

	}

	public void process(OutboundStreamSampleReport report) {

	}

	public void process(InboundStreamSampleReport report) {

	}

	public void process(RemoteInboundStreamSampleReport report) {

	}
}
