package org.observertc.webrtc.observer.evaluators.mediastreams;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.common.reports.avro.FinishedCall;
import org.observertc.webrtc.common.reports.avro.InitiatedCall;
import org.observertc.webrtc.common.reports.avro.ReportType;
import org.observertc.webrtc.observer.EvaluatorsConfig;
import org.observertc.webrtc.observer.ReportDraftSink;
import org.observertc.webrtc.observer.ReportSink;
import org.observertc.webrtc.observer.evaluators.reportdrafts.AbstractReportDraftProcessor;
import org.observertc.webrtc.observer.evaluators.reportdrafts.FinishedCallReportDraft;
import org.observertc.webrtc.observer.evaluators.reportdrafts.InitiatedCallReportDraft;
import org.observertc.webrtc.observer.evaluators.reportdrafts.ReportDraft;
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
	private final ReportSink reportSink;
	private final EvaluatorsConfig.ReportDraftsConfig config;
	private final AbstractReportDraftProcessor processor;
	private final ReportDraftSink reportDraftSink;

	public ReportDraftsEvaluator(PeerConnectionsRepository peerConnectionsRepository,
								 EvaluatorsConfig.ReportDraftsConfig config,
								 ReportDraftSink reportDraftSink,
								 ReportSink reportSink) {
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.reportSink = reportSink;
		this.reportDraftSink = reportDraftSink;
		this.config = config;
		this.processor = this.makeProcessor();
	}

	@Topic("${kafkaTopics.observertcReportDrafts.topicName}")
	public void receive(List<ReportDraft> reportDrafts) {
		if (reportDrafts.size() < 1) {
			return;
		}
		Instant now = Instant.now();
		Iterator<ReportDraft> it = reportDrafts.iterator();
		for (; it.hasNext(); ) {
			ReportDraft reportDraft = it.next();
			if (reportDraft == null) {
				logger.warn("Null rerportDraft or report occured in evaluation. skipping");
				continue;
			}
			Instant created = Instant.ofEpochMilli(reportDraft.created);
			long elapsedSeconds = ChronoUnit.SECONDS.between(created, now);
			if (this.config.expirationTimeInS < elapsedSeconds) {
				logger.warn("ReportDraft {} is expired", reportDraft.toString());
				continue;
			}
			if (elapsedSeconds < this.config.minEnforcedTimeInS) {
				this.reportDraftSink.send(UUID.randomUUID(), reportDraft);
				continue;
			}
			this.processor.accept(reportDraft);
		}

	}

	private AbstractReportDraftProcessor makeProcessor() {
		return new AbstractReportDraftProcessor() {
			@Override
			public void processInitiatedCallReport(InitiatedCallReportDraft report) {
				evaluateInitiatedCallReport(report);
			}

			@Override
			public void processFinishedCallReport(FinishedCallReportDraft report) {
				evaluateFinishedCallReport(report);
			}
		};
	}


	private void evaluateInitiatedCallReport(InitiatedCallReportDraft initiatedCallReport) {
		PeerconnectionsRecord firstJoinedPC = null;
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
			if (firstJoinedPC == null) {
				firstJoinedPC = record;
			}
		}
		if (firstJoinedPC == null) {
			logger.warn("No first joined PC is found. ReportDraft is dropped. {}", initiatedCallReport.toString());
			return;
		}
		//  || record.getJoined().compareTo(firstJoinedPC) < 0
		String callUUIDStr;
		if (firstJoinedPC.getCalluuid() != null) {
			callUUIDStr = firstJoinedPC.getCalluuid().toString();
		} else {
			callUUIDStr = "NOT VALID UUID";
		}

		Object payload = InitiatedCall.newBuilder()
				.setCallUUID(callUUIDStr)
				.build();
		UUID serviceUUID = UUIDAdapter.toUUIDOrDefault(firstJoinedPC.getServiceuuid(), null);
		this.reportSink.sendReport(
				serviceUUID,
				firstJoinedPC.getProvidedcallid(),
				serviceUUID.toString(),
				ReportType.INITIATED_CALL,
				firstJoinedPC.getJoined(),
				payload
		);
	}

	private void evaluateFinishedCallReport(FinishedCallReportDraft finishedCallReport) {
		PeerconnectionsRecord lastDetachedPC = null;
		Iterator<PeerconnectionsRecord> it = this.peerConnectionsRepository.findByCallUUID(finishedCallReport.callUUID).iterator();
		for (; it.hasNext(); ) {
			PeerconnectionsRecord record = it.next();
			if (record.getDetached() == null) {
				continue;
			}
			if (lastDetachedPC == null) {
				lastDetachedPC = record;
			}
		}
		if (lastDetachedPC == null) {
			logger.warn("No last detached PC is found. ReportDraft is dropped. {}", finishedCallReport.toString());
			return;
		}

		String callUUIDStr;
		if (lastDetachedPC.getCalluuid() != null) {
			callUUIDStr = lastDetachedPC.getCalluuid().toString();
		} else {
			callUUIDStr = "NOT VALID UUID";
		}
		Object payload = FinishedCall.newBuilder()
				.setCallUUID(callUUIDStr)
				.build();
		UUID serviceUUID = UUIDAdapter.toUUIDOrDefault(lastDetachedPC.getServiceuuid(), null);
		this.reportSink.sendReport(
				serviceUUID,
				lastDetachedPC.getProvidedcallid(),
				serviceUUID.toString(),
				ReportType.FINISHED_CALL,
				lastDetachedPC.getUpdated(),
				payload
		);
	}

}
