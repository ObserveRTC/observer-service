package org.observertc.webrtc.observer.evaluators.mediastreams;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.observertc.webrtc.common.reports.FinishedCallReport;
import org.observertc.webrtc.common.reports.InitiatedCallReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.common.reports.ReportObserverUUIDExtractor;
import org.observertc.webrtc.observer.EvaluatorsConfig;
import org.observertc.webrtc.observer.KafkaSinks;
import org.observertc.webrtc.observer.ObserverDateTime;
import org.observertc.webrtc.observer.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.observer.repositories.PeerConnectionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KafkaListener(
		offsetReset = OffsetReset.EARLIEST,
		groupId = "observertc-webrtc-observer-ReportDraftsEvaluator",
		pollTimeout = "30000ms",
		threads = 1,
		batch = true,
		properties = {
				@Property(name = ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, value = "15000"),
				@Property(name = ConsumerConfig.FETCH_MIN_BYTES_CONFIG, value = "10485760"),
				@Property(name = ConsumerConfig.MAX_POLL_RECORDS_CONFIG, value = "5000")
		}
)
@Prototype
public class ReportDraftsEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(ReportDraftsEvaluator.class);
	private final PeerConnectionsRepository peerConnectionsRepository;
	private final KafkaSinks kafkaSinks;
	private final ObserverDateTime observerDateTime;
	private final ReportObserverUUIDExtractor uuidExtractor;
	private final EvaluatorsConfig.ReportDraftsConfig config;

	public ReportDraftsEvaluator(PeerConnectionsRepository peerConnectionsRepository,
								 ObserverDateTime observerDateTime,
								 EvaluatorsConfig.ReportDraftsConfig config,
								 KafkaSinks kafkaSinks) {
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.kafkaSinks = kafkaSinks;
		this.observerDateTime = observerDateTime;
		this.config = config;
		this.uuidExtractor = new ReportObserverUUIDExtractor();
	}

	@Topic("${kafkaTopics.observertcReportDrafts.topicName}")
	public void receive(List<ReportDraft> reportDrafts) {
		if (reportDrafts.size() < 1) {
			return;
		}
		Iterator<ReportDraft> it = reportDrafts.iterator();
		for (; it.hasNext(); ) {
			ReportDraft reportDraft = it.next();
			if (reportDraft == null || reportDraft.report == null) {
				logger.warn("Null rerportDraft or report occured in evaluation. skipping");
				continue;
			}

			UUID observerUUID = this.uuidExtractor.apply(reportDraft.report);
			this.process(observerUUID, reportDraft);
		}

	}


	private void process(UUID observerUUID, ReportDraft reportDraft) {
		LocalDateTime now = this.observerDateTime.now();
		if (reportDraft.drafted == null) {
			logger.warn("ReportDraft drafted ts should have been not null. Please check preliminary injection to the topic and debug " +
					"the missing assignment");
			reportDraft.drafted = now;
		}
		reportDraft.processed = now;
		long elapsedTimeInS = ChronoUnit.SECONDS.between(reportDraft.drafted, now);
		boolean invalid = this.config.TTL < elapsedTimeInS;
		if (invalid) {
			logger.warn("A ReportDraft is circulating in the system longer than {}s. We drop it. {}", this.config.TTL, reportDraft.toString());
			return;
		}

		boolean forward = this.shouldForward(reportDraft);
		if (forward) {
			this.kafkaSinks.sendReport(observerUUID, reportDraft.report);
		} else {
			this.kafkaSinks.sendReportDraft(observerUUID, reportDraft);
		}
	}

	public boolean shouldForward(ReportDraft reportDraft) {
		Report report = reportDraft.report;
		boolean result;
		switch (report.type) {
			case INITIATED_CALL:
				result = this.initiatedCallReportIsValid(reportDraft);
				break;
			case FINISHED_CALL:
				result = this.finishedCallReportIsValid(reportDraft);
				break;
			default:
				result = true;
				break;
		}
		return result;
	}

	private boolean initiatedCallReportIsValid(ReportDraft reportDraft) {
		InitiatedCallReport initiatedCallReport = (InitiatedCallReport) reportDraft.report;
		LocalDateTime firstJoinedPC = null;
		Iterator<PeerconnectionsRecord> it = this.peerConnectionsRepository.findByCallUUID(initiatedCallReport.callUUID).iterator();
		for (; it.hasNext(); ) {
			PeerconnectionsRecord record = it.next();
			if (record.getDetached() != null) {
				continue;
			}
			if (record.getJoined() == null) {
				logger.warn("The joined timestamp for the PC {} is null. This should have not been happened, there is no way we can use " +
						"now the joined timestamp anywhere.");
				continue;
			}
			if (firstJoinedPC == null || record.getJoined().compareTo(firstJoinedPC) < 0) {
				firstJoinedPC = record.getJoined();
			}
		}
		if (firstJoinedPC == null) {
			return false;
		}
		if (firstJoinedPC.compareTo(initiatedCallReport.initiated) < 0) {
			Report newReport = InitiatedCallReport.of(
					initiatedCallReport.observerUUID,
					initiatedCallReport.callUUID,
					firstJoinedPC
			);
			reportDraft.report = newReport;
		}
		return true;
	}

	private boolean finishedCallReportIsValid(ReportDraft reportDraft) {
		FinishedCallReport finishedCallReport = (FinishedCallReport) reportDraft.report;
		LocalDateTime lastDetachedPC = null;
		Iterator<PeerconnectionsRecord> it = this.peerConnectionsRepository.findByCallUUID(finishedCallReport.callUUID).iterator();
		for (; it.hasNext(); ) {
			PeerconnectionsRecord record = it.next();
			if (record.getDetached() == null) {
				continue;
			}
			if (lastDetachedPC == null || 0 < record.getDetached().compareTo(lastDetachedPC)) {
				lastDetachedPC = record.getJoined();
			}
		}
		if (lastDetachedPC == null) {
			return false;
		}
		if (finishedCallReport.finished.compareTo(lastDetachedPC) < 0) {
			Report newReport = FinishedCallReport.of(
					finishedCallReport.observerUUID,
					finishedCallReport.callUUID,
					lastDetachedPC
			);
			reportDraft.report = newReport;
		}
		return true;
	}

}
