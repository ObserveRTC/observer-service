package org.observertc.webrtc.service.reportsink.bigquery;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.observertc.webrtc.common.reports.DetachedPeerConnectionReport;
import org.observertc.webrtc.common.reports.FinishedCallReport;
import org.observertc.webrtc.common.reports.InboundStreamSampleReport;
import org.observertc.webrtc.common.reports.InitiatedCallReport;
import org.observertc.webrtc.common.reports.JoinedPeerConnectionReport;
import org.observertc.webrtc.common.reports.OutboundStreamSampleReport;
import org.observertc.webrtc.common.reports.RemoteInboundStreamSampleReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.common.reports.ReportType;
import org.observertc.webrtc.common.reportsink.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigQueryReportService implements ReportService {

	private static final Logger logger = LoggerFactory.getLogger(BigQueryReportService.class);

	private BigQueryService bigQueryService;
	private static final String INITIATED_CALLS_TABLE_NAME = "InitiatedCalls";
	private static final String FINISHED_CALLS_TABLE_NAME = "FinishedCalls";
	private static final String JOINED_PEER_CONNECTIONS_TABLE_NAME = "JoinedPeerConnections";
	private static final String DETACHED_PEER_CONNECTIONS_TABLE_NAME = "DetachedPeerConnections";
	private static final String INBOUND_STREAM_SAMPLES_TABLE_NAME = "InboundStreamSamples";
	private static final String OUTBOUND_STREAM_SAMPLES_TABLE_NAME = "OutboundStreamSamples";
	private static final String REMOTE_INBOUND_STREAM_SAMPLES_TABLE_NAME = "RemoteInboundStreamSamples";

	private final BigQueryTable<InitiatedCallEntry> initiatedCalls;
	private final BigQueryTable<FinishedCallEntry> finishedCalls;
	private final BigQueryTable<JoinedPeerConnectionEntry> joinedPeerConnections;
	private final BigQueryTable<DetachedPeerConnectionEntry> detachedPeerConnections;
	private final BigQueryTable<InboundStreamSampleEntry> inboundStreamSamples;
	private final BigQueryTable<OutboundStreamSampleEntry> outboundStreamSamples;
	private final BigQueryTable<RemoteInboundStreamSampleEntry> remoteInboundStreamSamples;
	private final Map<String, ReportType> typeMapper;

	public BigQueryReportService(BigQueryService bigQueryService) {

		this.bigQueryService = bigQueryService;
		this.initiatedCalls = new BigQueryTable(bigQueryService, INITIATED_CALLS_TABLE_NAME);
		this.finishedCalls = new BigQueryTable(bigQueryService, FINISHED_CALLS_TABLE_NAME);
		this.joinedPeerConnections = new BigQueryTable(bigQueryService, JOINED_PEER_CONNECTIONS_TABLE_NAME);
		this.detachedPeerConnections = new BigQueryTable(bigQueryService, DETACHED_PEER_CONNECTIONS_TABLE_NAME);
		this.inboundStreamSamples = new BigQueryTable(bigQueryService, INBOUND_STREAM_SAMPLES_TABLE_NAME);
		this.outboundStreamSamples = new BigQueryTable(bigQueryService, OUTBOUND_STREAM_SAMPLES_TABLE_NAME);
		this.remoteInboundStreamSamples = new BigQueryTable(bigQueryService, REMOTE_INBOUND_STREAM_SAMPLES_TABLE_NAME);

		this.typeMapper = new HashMap<>();
		this.typeMapper.put(JoinedPeerConnectionReport.class.getName(), ReportType.JOINED_PEER_CONNECTION);
		this.typeMapper.put(DetachedPeerConnectionReport.class.getName(), ReportType.DETACHED_PEER_CONNECTION);
		this.typeMapper.put(InitiatedCallReport.class.getName(), ReportType.INITIATED_CALL);
		this.typeMapper.put(FinishedCallReport.class.getName(), ReportType.FINISHED_CALL);
		this.typeMapper.put(RemoteInboundStreamSampleReport.class.getName(), ReportType.REMOTE_INBOUND_STREAM_SAMPLE);
		this.typeMapper.put(InboundStreamSampleReport.class.getName(), ReportType.INBOUND_STREAM_SAMPLE);
		this.typeMapper.put(OutboundStreamSampleReport.class.getName(), ReportType.OUTBOUND_STREAM_SAMPLE);
	}

	@Override
	public void init(ProcessorContext context) {

	}

	@Override
	public void process(UUID key, Report report) {
		try {
			this.doProcess(key, report);
		} catch (Exception ex) {
			logger.error("Error during saving", ex);
		}
	}

	private void doProcess(UUID key, Report report) {
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
				FinishedCallEntry finishedCallEntry = FinishedCallEntry.from((FinishedCallReport) report);
				this.finishedCalls.insert(finishedCallEntry);
				break;
			case JOINED_PEER_CONNECTION:
				JoinedPeerConnectionEntry joinedPeerConnection = JoinedPeerConnectionEntry.from((JoinedPeerConnectionReport) report);
				this.joinedPeerConnections.insert(joinedPeerConnection);
				break;
			case INITIATED_CALL:
				InitiatedCallEntry initiatedCallEntry = InitiatedCallEntry.from((InitiatedCallReport) report);
				this.initiatedCalls.insert(initiatedCallEntry);
				break;
			case DETACHED_PEER_CONNECTION:
				DetachedPeerConnectionEntry detachedPeerConnectionEntry = DetachedPeerConnectionEntry.from((DetachedPeerConnectionReport) report);
				this.detachedPeerConnections.insert(detachedPeerConnectionEntry);
				break;
			case INBOUND_STREAM_SAMPLE:
				InboundStreamSampleEntry inboundStreamSampleEntry = InboundStreamSampleEntry.from((InboundStreamSampleReport) report);
				this.inboundStreamSamples.insert(inboundStreamSampleEntry);
				break;
			case OUTBOUND_STREAM_SAMPLE:
				OutboundStreamSampleEntry outboundStreamSampleEntry = OutboundStreamSampleEntry.from((OutboundStreamSampleReport) report);
				this.outboundStreamSamples.insert(outboundStreamSampleEntry);
				break;
			case REMOTE_INBOUND_STREAM_SAMPLE:
				RemoteInboundStreamSampleEntry remoteInboundStreamSampleEntry =
						RemoteInboundStreamSampleEntry.from((RemoteInboundStreamSampleReport) report);
				this.remoteInboundStreamSamples.insert(remoteInboundStreamSampleEntry);
				break;
		}
	}

	@Override
	public void close() {

	}
}
