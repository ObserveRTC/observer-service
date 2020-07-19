package org.observertc.webrtc.service.reportsink.bigquery;

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
import org.observertc.webrtc.common.reports.AbstractReportProcessor;
import org.observertc.webrtc.common.reportsink.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BigQueryReportService implements ReportService {

	private static final Logger logger = LoggerFactory.getLogger(BigQueryReportService.class);

	private final BigQueryService bigQueryService;
	private final BigQueryTable<InitiatedCallEntry> initiatedCalls;
	private final BigQueryTable<FinishedCallEntry> finishedCalls;
	private final BigQueryTable<JoinedPeerConnectionEntry> joinedPeerConnections;
	private final BigQueryTable<DetachedPeerConnectionEntry> detachedPeerConnections;
	private final BigQueryTable<InboundStreamSampleEntry> inboundStreamSamples;
	private final BigQueryTable<OutboundStreamSampleEntry> outboundStreamSamples;
	private final BigQueryTable<RemoteInboundStreamSampleEntry> remoteInboundStreamSamples;
	private final BigQueryReportServiceBuilder.Config config;
	private final AbstractReportProcessor processor;


	public BigQueryReportService(BigQueryReportServiceBuilder.Config config) {
		this.config = config;
		this.bigQueryService = new BigQueryService(config.projectName, config.datasetName);
		this.initiatedCalls = new BigQueryTable(bigQueryService, config.initiatedCallsTable);
		this.finishedCalls = new BigQueryTable(bigQueryService, config.finishedCallsTable);
		this.joinedPeerConnections = new BigQueryTable(bigQueryService, config.joinedPeerConnectionsTable);
		this.detachedPeerConnections = new BigQueryTable(bigQueryService, config.detachedPeerConnectionsTable);
		this.inboundStreamSamples = new BigQueryTable(bigQueryService, config.inboundStreamSamplesTable);
		this.outboundStreamSamples = new BigQueryTable(bigQueryService, config.outboundStreamSamplesTable);
		this.remoteInboundStreamSamples = new BigQueryTable(bigQueryService, config.remoteInboundStreamSamplesTable);
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
			this.processor.accept(report);
		} catch (Exception ex) {
			logger.error("Error during saving", ex);
		}
	}

	private AbstractReportProcessor makeReportProcessor() {
		return new AbstractReportProcessor() {
			@Override
			public void process(JoinedPeerConnectionReport report) {
				JoinedPeerConnectionEntry joinedPeerConnection = JoinedPeerConnectionEntry.from((JoinedPeerConnectionReport) report);
				joinedPeerConnections.insert(joinedPeerConnection);
			}

			@Override
			public void process(DetachedPeerConnectionReport report) {
				DetachedPeerConnectionEntry detachedPeerConnectionEntry = DetachedPeerConnectionEntry.from((DetachedPeerConnectionReport) report);
				detachedPeerConnections.insert(detachedPeerConnectionEntry);
			}

			@Override
			public void process(InitiatedCallReport report) {
				InitiatedCallEntry initiatedCallEntry = InitiatedCallEntry.from((InitiatedCallReport) report);
				initiatedCalls.insert(initiatedCallEntry);
			}

			@Override
			public void process(FinishedCallReport report) {
				FinishedCallEntry finishedCallEntry = FinishedCallEntry.from((FinishedCallReport) report);
				finishedCalls.insert(finishedCallEntry);
			}

			@Override
			public void process(OutboundStreamSampleReport report) {
				OutboundStreamSampleEntry outboundStreamSampleEntry = OutboundStreamSampleEntry.from((OutboundStreamSampleReport) report);
				outboundStreamSamples.insert(outboundStreamSampleEntry);
			}

			@Override
			public void process(InboundStreamSampleReport report) {
				InboundStreamSampleEntry inboundStreamSampleEntry = InboundStreamSampleEntry.from((InboundStreamSampleReport) report);
				inboundStreamSamples.insert(inboundStreamSampleEntry);
			}

			@Override
			public void process(RemoteInboundStreamSampleReport report) {
				RemoteInboundStreamSampleEntry remoteInboundStreamSampleEntry =
						RemoteInboundStreamSampleEntry.from((RemoteInboundStreamSampleReport) report);
				remoteInboundStreamSamples.insert(remoteInboundStreamSampleEntry);
			}
		};
	}

	@Override
	public void close() {
		// TODO: add the service shutdown here
	}
}
