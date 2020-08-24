package org.observertc.webrtc.reporter;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
import java.util.Iterator;
import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerConfig;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KafkaListener(
		offsetReset = OffsetReset.EARLIEST,
		groupId = "observertc-webrtc-reporter-BigQueryReportService",
		sessionTimeout = "120000ms",
		pollTimeout = "60000ms",
		threads = 6,
		batch = true,
		properties = {
				@Property(name = ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, value = "30000"),
				@Property(name = ConsumerConfig.FETCH_MIN_BYTES_CONFIG, value = "10485760"),
				@Property(name = ConsumerConfig.MAX_POLL_RECORDS_CONFIG, value = "10000")
		}
)
@Prototype
public class BigQueryReportService {

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
	private final AbstractReportProcessor processor;

	public BigQueryReportService(ReporterConfig config) {
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
	}

	@Topic(value = "${reporter.kafkaReportsTopic}")
	public void receive(List<Report> reports) {
		Iterator<Report> it = reports.iterator();
		for (; it.hasNext(); ) {
			Report report = it.next();
			try {
				this.processor.apply(report);
			} catch (Exception ex) {
				logger.error("Error during saving", ex);
				continue;
			}
		}
		try {
			this.flush();
		} catch (Exception ex) {

		}
	}

	private void flush() {
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

}
