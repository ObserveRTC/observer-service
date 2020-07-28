package org.observertc.webrtc.service.reportsink.bigquery;

import java.time.ZoneId;
import java.util.UUID;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.observertc.webrtc.common.reports.AbstractReportProcessor;
import org.observertc.webrtc.common.reports.DetachedPeerConnectionReport;
import org.observertc.webrtc.common.reports.FinishedCallReport;
import org.observertc.webrtc.common.reports.InboundRTPReport;
import org.observertc.webrtc.common.reports.InboundStreamReport;
import org.observertc.webrtc.common.reports.InitiatedCallReport;
import org.observertc.webrtc.common.reports.JoinedPeerConnectionReport;
import org.observertc.webrtc.common.reports.OutboundRTPReport;
import org.observertc.webrtc.common.reports.OutboundStreamReport;
import org.observertc.webrtc.common.reports.RemoteInboundRTPReport;
import org.observertc.webrtc.common.reports.RemoteInboundStreamReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.common.reportsink.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigQueryReportService implements ReportService {
	public static ZoneId serviceZoneId = ZoneId.of("EET");

	private static final Logger logger = LoggerFactory.getLogger(BigQueryReportService.class);

	private final BigQueryService bigQueryService;
	private final BigQueryTable<InitiatedCallEntry> initiatedCalls;
	private final BigQueryTable<FinishedCallEntry> finishedCalls;
	private final BigQueryTable<JoinedPeerConnectionEntry> joinedPeerConnections;
	private final BigQueryTable<DetachedPeerConnectionEntry> detachedPeerConnections;
	private final BigQueryTable<InboundStreamReportEntry> inboundStreamReports;
	private final BigQueryTable<OutboundStreamReportEntry> outboundStreamReports;
	private final BigQueryTable<RemoteInboundStreamReportEntry> remoteInboundStreamReports;
	private final BigQueryTable<RemoteInboundRTPReportEntry> remoteInboundRTPSamples;
	private final BigQueryTable<InboundRTPReportEntry> inboundRTPSamples;
	private final BigQueryTable<OutboundRTPReportEntry> outboundRTPSamples;
	private final BigQueryReportServiceBuilder.Config config;
	private final AbstractReportProcessor processor;


	public BigQueryReportService(BigQueryReportServiceBuilder.Config config) {
		this.config = config;
		// TODO: hacky solution!
		serviceZoneId = ZoneId.of(config.timeZoneId);
		this.bigQueryService = new BigQueryService(config.projectName, config.datasetName);
		this.initiatedCalls = new BigQueryTable(bigQueryService, config.initiatedCallsTable);
		this.finishedCalls = new BigQueryTable(bigQueryService, config.finishedCallsTable);
		this.joinedPeerConnections = new BigQueryTable(bigQueryService, config.joinedPeerConnectionsTable);
		this.detachedPeerConnections = new BigQueryTable(bigQueryService, config.detachedPeerConnectionsTable);
		this.inboundStreamReports = new BigQueryTable(bigQueryService, config.inboundStreamReportsTable);
		this.outboundStreamReports = new BigQueryTable(bigQueryService, config.outboundStreamReportsTable);
		this.remoteInboundStreamReports = new BigQueryTable(bigQueryService, config.remoteInboundStreamReportsTable);
		this.remoteInboundRTPSamples = new BigQueryTable<>(bigQueryService, config.remoteInboundRTPSamplesTable);
		this.inboundRTPSamples = new BigQueryTable<>(bigQueryService, config.inboundRTPSamplesTable);
		this.outboundRTPSamples = new BigQueryTable<>(bigQueryService, config.outboundRTPSamplesTable);
		this.processor = this.makeReportProcessor();
	}

	@Override
	public void init(ProcessorContext context) {
		BigQueryServiceSchemaCheckerJob schemaChecker = new BigQueryServiceSchemaCheckerJob(this.config);
		schemaChecker.perform();
	}

	@Override
	public void process(UUID key, Report report) {
		try {
			this.processor.apply(report);
		} catch (Exception ex) {
			logger.error("Error during saving", ex);
		}
	}

	private AbstractReportProcessor<Void> makeReportProcessor() {
		return new AbstractReportProcessor<Void>() {
			@Override
			public Void process(JoinedPeerConnectionReport report) {
				JoinedPeerConnectionEntry joinedPeerConnection = JoinedPeerConnectionEntry.from((JoinedPeerConnectionReport) report);
				joinedPeerConnections.insert(joinedPeerConnection);
				return null;
			}

			@Override
			public Void process(DetachedPeerConnectionReport report) {
				DetachedPeerConnectionEntry detachedPeerConnectionEntry = DetachedPeerConnectionEntry.from((DetachedPeerConnectionReport) report);
				detachedPeerConnections.insert(detachedPeerConnectionEntry);
				return null;
			}

			@Override
			public Void process(InitiatedCallReport report) {
				InitiatedCallEntry initiatedCallEntry = InitiatedCallEntry.from((InitiatedCallReport) report);
				initiatedCalls.insert(initiatedCallEntry);
				return null;
			}

			@Override
			public Void process(FinishedCallReport report) {
				FinishedCallEntry finishedCallEntry = FinishedCallEntry.from((FinishedCallReport) report);
				finishedCalls.insert(finishedCallEntry);
				return null;
			}

			@Override
			public Void process(OutboundStreamReport report) {
				OutboundStreamReportEntry outboundStreamSampleEntry = OutboundStreamReportEntry.from((OutboundStreamReport) report);
				outboundStreamReports.insert(outboundStreamSampleEntry);
				return null;
			}

			@Override
			public Void process(InboundStreamReport report) {
				InboundStreamReportEntry inboundStreamSampleEntry = InboundStreamReportEntry.from((InboundStreamReport) report);
				inboundStreamReports.insert(inboundStreamSampleEntry);
				return null;
			}

			@Override
			public Void process(RemoteInboundStreamReport report) {
				RemoteInboundStreamReportEntry remoteInboundStreamSampleEntry =
						RemoteInboundStreamReportEntry.from((RemoteInboundStreamReport) report);
				remoteInboundStreamReports.insert(remoteInboundStreamSampleEntry);
				return null;
			}

			@Override
			public Void process(RemoteInboundRTPReport report) {
				RemoteInboundRTPReportEntry remoteInboundRTPReportEntry = RemoteInboundRTPReportEntry.from(report);
				remoteInboundRTPSamples.insert(remoteInboundRTPReportEntry);
				return null;
			}

			@Override
			public Void process(InboundRTPReport report) {
				InboundRTPReportEntry inboundRTPReportEntry = InboundRTPReportEntry.from(report);
				inboundRTPSamples.insert(inboundRTPReportEntry);
				return null;
			}

			@Override
			public Void process(OutboundRTPReport report) {
				OutboundRTPReportEntry outboundRTPReportEntry = OutboundRTPReportEntry.from(report);
				outboundRTPSamples.insert(outboundRTPReportEntry);
				return null;
			}
		};
	}

	@Override
	public void close() {
		// TODO: add the service shutdown here
	}
}
