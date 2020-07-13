package org.observertc.webrtc.service.reportsink.bigquery;

import java.util.UUID;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.observertc.webrtc.common.reports.Report;
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
	private static final String MEDIA_STREAM_SAMPLES_TABLE_NAME = "StreamSamples";

	private final BigQueryTable<InitiatedCallEntry> initiatedCalls;
	private final BigQueryTable<FinishedCallEntry> finishedCalls;
	private final BigQueryTable<JoinedPeerConnectionEntry> joinedPeerConnections;
	private final BigQueryTable<DetachedPeerConnectionEntry> detachedPeerConnections;
	private final BigQueryTable<MediaStreamSampleEntry> mediaStreamSamples;
//	private final Map<String, ReportType> typeMapper;

	public BigQueryReportService(BigQueryService bigQueryService) {

		this.bigQueryService = bigQueryService;
		this.initiatedCalls = new BigQueryTable(bigQueryService, INITIATED_CALLS_TABLE_NAME);
		this.finishedCalls = new BigQueryTable(bigQueryService, FINISHED_CALLS_TABLE_NAME);
		this.joinedPeerConnections = new BigQueryTable(bigQueryService, JOINED_PEER_CONNECTIONS_TABLE_NAME);
		this.detachedPeerConnections = new BigQueryTable(bigQueryService, DETACHED_PEER_CONNECTIONS_TABLE_NAME);
		this.mediaStreamSamples = new BigQueryTable(bigQueryService, MEDIA_STREAM_SAMPLES_TABLE_NAME);
//		this.typeMapper = new HashMap<>();
//		this.typeMapper.put(JoinedPeerConnectionReport.class.getName(), ReportType.JOINED_PEER_CONNECTION);
//		this.typeMapper.put(DetachedPeerConnectionReport.class.getName(), ReportType.DETACHED_PEER_CONNECTION);
//		this.typeMapper.put(InitiatedCallReport.class.getName(), ReportType.INITIATED_CALL);
//		this.typeMapper.put(FinishedCallReport.class.getName(), ReportType.FINISHED_CALL);
//		this.typeMapper.put(MediaStreamSampleReport.class.getName(), ReportType.MEDIA_STREAM_SAMPLE);
	}

	@Override
	public void init(ProcessorContext context) {

	}

	@Override
	public void process(UUID key, Report report) {
//		ReportType type = report.getType();
//		if (type == null) {
//			type = this.typeMapper.get(report.getClass().getName());
//			if (type != null) {
//				logger.info("A report type field is null, but based on the class name it is {}", type.name());
//			} else {
//				logger.warn("A report type field is null, and cannot getinfo based on the classname", report.getClass().getName());
//			}
//		}
//		switch (type) {
//			case FINISHED_CALL:
//				FinishedCallEntry finishedCallEntry = FinishedCallEntry.from((FinishedCallReport) report);
//				this.finishedCalls.insert(finishedCallEntry);
//				break;
//			case JOINED_PEER_CONNECTION:
//				JoinedPeerConnectionEntry joinedPeerConnection = JoinedPeerConnectionEntry.from((JoinedPeerConnectionReport) report);
//				this.joinedPeerConnections.insert(joinedPeerConnection);
//				break;
//			case INITIATED_CALL:
//				InitiatedCallEntry initiatedCallEntry = InitiatedCallEntry.from((InitiatedCallReport) report);
//				this.initiatedCalls.insert(initiatedCallEntry);
//				break;
//			case DETACHED_PEER_CONNECTION:
//				DetachedPeerConnectionEntry detachedPeerConnectionEntry = DetachedPeerConnectionEntry.from((DetachedPeerConnectionReport) report);
//				this.detachedPeerConnections.insert(detachedPeerConnectionEntry);
//				break;
//			case MEDIA_STREAM_SAMPLE:
//				MediaStreamSampleEntry mediaStreamSampleEntry = MediaStreamSampleEntry.from((MediaStreamSampleReport) report);
//				this.mediaStreamSamples.insert(mediaStreamSampleEntry);
//				break;
//		}
	}

	@Override
	public void close() {

	}
}
