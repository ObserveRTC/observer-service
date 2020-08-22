package org.observertc.webrtc.service.reportsink.bigquery;

import java.time.Duration;
import java.time.ZoneId;
import java.util.UUID;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.Punctuator;
import org.observertc.webrtc.common.reports.AbstractReportProcessor;
import org.observertc.webrtc.common.reports.DetachedPeerConnectionReport;
import org.observertc.webrtc.common.reports.FinishedCallReport;
import org.observertc.webrtc.common.reports.ICECandidatePairReport;
import org.observertc.webrtc.common.reports.ICELocalCandidateReport;
import org.observertc.webrtc.common.reports.ICERemoteCandidateReport;
import org.observertc.webrtc.common.reports.InboundRTPReport;
import org.observertc.webrtc.common.reports.InitiatedCallReport;
import org.observertc.webrtc.common.reports.JoinedPeerConnectionReport;
import org.observertc.webrtc.common.reports.MediaSourceReport;
import org.observertc.webrtc.common.reports.OutboundRTPReport;
import org.observertc.webrtc.common.reports.RemoteInboundRTPReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.common.reports.TrackReport;
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
	private final BigQueryTable<RemoteInboundRTPReportEntry> remoteInboundRTPSamples;
	private final BigQueryTable<InboundRTPReportEntry> inboundRTPSamples;
	private final BigQueryTable<OutboundRTPReportEntry> outboundRTPSamples;
	private final BigQueryTable<ICECandidatePairEntry> iceCandidatePairs;
	private final BigQueryTable<ICELocalCandidateEntry> iceLocalCandidates;
	private final BigQueryTable<ICERemoteCandidateEntry> iceRemoteCandidates;
	private final BigQueryTable<MediaSourceEntry> mediaSources;
	private final BigQueryTable<TrackReportEntry> trackReports;
	private final BigQueryReportServiceBuilder.Config config;
	private final AbstractReportProcessor processor;
	private volatile int buffered = 0;
	private long lastFlushed;

	public BigQueryReportService(BigQueryReportServiceBuilder.Config config) {
		this.config = config;
		// TODO: hacky solution!
		serviceZoneId = ZoneId.of(config.timeZoneId);
		this.bigQueryService = new BigQueryService(config.projectName, config.datasetName);
		this.initiatedCalls = new BigQueryTable(bigQueryService, config.initiatedCallsTable);
		this.finishedCalls = new BigQueryTable(bigQueryService, config.finishedCallsTable);
		this.joinedPeerConnections = new BigQueryTable(bigQueryService, config.joinedPeerConnectionsTable);
		this.detachedPeerConnections = new BigQueryTable(bigQueryService, config.detachedPeerConnectionsTable);
		this.remoteInboundRTPSamples = new BigQueryTable<>(bigQueryService, config.remoteInboundRTPSamplesTable);
		this.inboundRTPSamples = new BigQueryTable<>(bigQueryService, config.inboundRTPSamplesTable);
		this.outboundRTPSamples = new BigQueryTable<>(bigQueryService, config.outboundRTPSamplesTable);
		this.iceCandidatePairs = new BigQueryTable<>(bigQueryService, config.iceCandidatePairsTable);
		this.iceLocalCandidates = new BigQueryTable<>(bigQueryService, config.iceLocalCandidatesTable);
		this.iceRemoteCandidates = new BigQueryTable<>(bigQueryService, config.iceRemoteCandidatesTable);
		this.trackReports = new BigQueryTable<>(bigQueryService, config.trackReportsTable);
		this.mediaSources = new BigQueryTable<>(bigQueryService, config.mediaSourcesTable);
		this.processor = this.makeReportProcessor();
		this.lastFlushed = System.currentTimeMillis();
	}

	@Override
	public void init(ProcessorContext context) {
		BigQueryServiceSchemaCheckerJob schemaChecker = new BigQueryServiceSchemaCheckerJob(this.config);
		schemaChecker.perform();
		context.schedule(Duration.ofSeconds(30), PunctuationType.WALL_CLOCK_TIME, new Punctuator() {
			@Override
			public void punctuate(long timestamp) {
				long now = System.currentTimeMillis();
				if (60000 < now - lastFlushed) {
					if (0 < buffered) {
						flush();
					}
				}
			}
		});
	}

	@Override
	public void process(UUID key, Report report) {
		try {
			this.processor.apply(report);
		} catch (Exception ex) {
			logger.error("Error during saving", ex);
		}
		long now = System.currentTimeMillis();
		++this.buffered;
		if (10000 < this.buffered || 30000 < now - this.lastFlushed) {
			try {
				this.flush();
			} catch (Exception ex) {
				logger.error("During flush, exception happened.", ex);
			}
		}
	}

	private void flush() {
		logger.info("{} rows will be inserted to BQ now", this.buffered);
		this.joinedPeerConnections.flush();
		this.detachedPeerConnections.flush();
		this.initiatedCalls.flush();
		this.finishedCalls.flush();
		this.iceRemoteCandidates.flush();
		this.iceLocalCandidates.flush();
		this.iceCandidatePairs.flush();
		this.inboundRTPSamples.flush();
		this.outboundRTPSamples.flush();
		this.remoteInboundRTPSamples.flush();
		this.trackReports.flush();
		this.mediaSources.flush();
		this.lastFlushed = System.currentTimeMillis();
		this.buffered = 0;
	}

	private AbstractReportProcessor<Void> makeReportProcessor() {
		return new AbstractReportProcessor<Void>() {
			@Override
			public Void processJoinedPeerConnectionReport(JoinedPeerConnectionReport report) {
				JoinedPeerConnectionEntry joinedPeerConnection = JoinedPeerConnectionEntry.from((JoinedPeerConnectionReport) report);
//				joinedPeerConnections.insert(joinedPeerConnection);
				joinedPeerConnections.add(joinedPeerConnection);
				return null;
			}

			@Override
			public Void processDetachedPeerConnectionReport(DetachedPeerConnectionReport report) {
				DetachedPeerConnectionEntry detachedPeerConnectionEntry = DetachedPeerConnectionEntry.from((DetachedPeerConnectionReport) report);
//				detachedPeerConnections.insert(detachedPeerConnectionEntry);
				detachedPeerConnections.add(detachedPeerConnectionEntry);
				return null;
			}

			@Override
			public Void processInitiatedCallReport(InitiatedCallReport report) {
				InitiatedCallEntry initiatedCallEntry = InitiatedCallEntry.from((InitiatedCallReport) report);
//				initiatedCalls.insert(initiatedCallEntry);
				initiatedCalls.add(initiatedCallEntry);
				return null;
			}

			@Override
			public Void processFinishedCallReport(FinishedCallReport report) {
				FinishedCallEntry finishedCallEntry = FinishedCallEntry.from((FinishedCallReport) report);
//				finishedCalls.insert(finishedCallEntry);
				finishedCalls.add(finishedCallEntry);
				return null;
			}

			@Override
			public Void processRemoteInboundRTPReport(RemoteInboundRTPReport report) {
				RemoteInboundRTPReportEntry remoteInboundRTPReportEntry = RemoteInboundRTPReportEntry.from(report);
//				remoteInboundRTPSamples.insert(remoteInboundRTPReportEntry);
				remoteInboundRTPSamples.add(remoteInboundRTPReportEntry);
				return null;
			}

			@Override
			public Void processInboundRTPReport(InboundRTPReport report) {
				InboundRTPReportEntry inboundRTPReportEntry = InboundRTPReportEntry.from(report);
//				inboundRTPSamples.insert(inboundRTPReportEntry);
				inboundRTPSamples.add(inboundRTPReportEntry);
				return null;
			}

			@Override
			public Void processOutboundRTPReport(OutboundRTPReport report) {
				OutboundRTPReportEntry outboundRTPReportEntry = OutboundRTPReportEntry.from(report);
//				outboundRTPSamples.insert(outboundRTPReportEntry);
				outboundRTPSamples.add(outboundRTPReportEntry);
				return null;
			}

			@Override
			public Void processICECandidatePairReport(ICECandidatePairReport report) {
				ICECandidatePairEntry iceCandidatePairEntry = ICECandidatePairEntry.from(report);
//				iceCandidatePairs.insert(iceCandidatePairEntry);
				iceCandidatePairs.add(iceCandidatePairEntry);
				return null;
			}

			@Override
			public Void processICELocalCandidateReport(ICELocalCandidateReport report) {
				ICELocalCandidateEntry iceCandidatePairEntry = ICELocalCandidateEntry.from(report);
//				iceLocalCandidates.insert(iceCandidatePairEntry);
				iceLocalCandidates.add(iceCandidatePairEntry);
				return null;
			}

			@Override
			public Void processICERemoteCandidateReport(ICERemoteCandidateReport report) {
				ICERemoteCandidateEntry iceRemoteCandidateEntry = ICERemoteCandidateEntry.from(report);
//				iceRemoteCandidates.insert(iceRemoteCandidateEntry);
				iceRemoteCandidates.add(iceRemoteCandidateEntry);
				return null;
			}

			@Override
			public Void processTrackReport(TrackReport report) {
				TrackReportEntry reportEntry = TrackReportEntry.from(report);
//				trackReports.insert(reportEntry);
				trackReports.add(reportEntry);
				return null;
			}

			@Override
			public Void processMediaSourceReport(MediaSourceReport report) {
				MediaSourceEntry reportEntry = MediaSourceEntry.from(report);
//				mediaSources.insert(reportEntry);
				mediaSources.add(reportEntry);
				return null;
			}
		};
	}

	@Override
	public void close() {
		this.flush();
	}
}
