package org.observertc.webrtc.service.evaluators.mediastreams;

import io.micronaut.scheduling.annotation.Scheduled;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Singleton;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.common.reports.DetachedPeerConnectionReport;
import org.observertc.webrtc.common.reports.FinishedCallReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.EvaluatorsConfig;
import org.observertc.webrtc.service.KafkaSinks;
import org.observertc.webrtc.service.ObserverDateTime;
import org.observertc.webrtc.service.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.service.repositories.ActiveStreamsRepository;
import org.observertc.webrtc.service.repositories.PeerConnectionsRepository;
import org.observertc.webrtc.service.repositories.SentReportsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CallCleaner {

	private static final Logger logger = LoggerFactory.getLogger(CallCleaner.class);
	private final EvaluatorsConfig.CallCleanerConfig config;
	private final ObserverDateTime observerDateTime;
	private final ActiveStreamsRepository activeStreamsRepository;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private final KafkaSinks kafkaSinks;
	private final MaxIdleThresholdProvider maxIdleThresholdProvider;
	private final SentReportsRepository sentReportsRepository;

	public CallCleaner(
			EvaluatorsConfig config,
			SentReportsRepository sentReportsRepository,
			ActiveStreamsRepository activeStreamsRepository,
			PeerConnectionsRepository peerConnectionsRepository,
			MaxIdleThresholdProvider maxIdleThresholdProvider,
			KafkaSinks kafkaSinks,
			ObserverDateTime observerDateTime) {
		this.config = config.callCleaner;
		this.activeStreamsRepository = activeStreamsRepository;
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.observerDateTime = observerDateTime;
		this.maxIdleThresholdProvider = maxIdleThresholdProvider;
		this.kafkaSinks = kafkaSinks;
		this.sentReportsRepository = sentReportsRepository;
	}

	@Scheduled(initialDelay = "10m", fixedRate = "60m")
	void deleteExpiredPCs() {
		LocalDateTime now = this.observerDateTime.now();
		LocalDateTime threshold = now.minusDays(this.config.pcRetentionTimeInDays);
		try {
			this.peerConnectionsRepository.deletePCsDetachedOlderThan(threshold);
			this.sentReportsRepository.deleteReportedOlderThan(threshold);
		} catch (Exception ex) {
			logger.error("Cannot execute remove old PCs", ex);
		}
	}

	@Scheduled(initialDelay = "1m", fixedRate = "2m")
	void reportExpiredPCs() {
		try {
			this.cleanCalls();
		} catch (Exception ex) {
			logger.error("Cannot execute remove old PCs", ex);
		}
	}

	private void cleanCalls() {
		Optional<LocalDateTime> thresholdHolder = this.maxIdleThresholdProvider.get();
		if (!thresholdHolder.isPresent()) {
			return;
		}
		LocalDateTime threshold = thresholdHolder.get();
		Iterator<PeerconnectionsRecord> it = this.peerConnectionsRepository.findJoinedPCsUpdatedLowerThan(threshold).iterator();
		for (; it.hasNext(); ) {
			PeerconnectionsRecord record = it.next();
			record.setDetached(record.getUpdated());
			record.setUpdated(record.getUpdated());

			UUID observerUUID = UUIDAdapter.toUUID(record.getObserveruuid());
			UUID callUUID = UUIDAdapter.toUUID(record.getCalluuid());
			UUID peerConnectionUUID = UUIDAdapter.toUUID(record.getPeerconnectionuuid());
			Report detachedPeerConnectionReport = DetachedPeerConnectionReport.of(
					observerUUID,
					callUUID,
					peerConnectionUUID,
					record.getBrowserid(),
					record.getUpdated());
			this.kafkaSinks.sendReport(observerUUID, detachedPeerConnectionReport);
			record.store();
			Optional<PeerconnectionsRecord> joinedPCHolder =
					this.peerConnectionsRepository.findJoinedPCsByCallUUIDBytes(record.getCalluuid()).findFirst();

			if (joinedPCHolder.isPresent()) {
				continue;
			}

			//finished call
			Report finishedCallReport = FinishedCallReport.of(observerUUID, callUUID, record.getUpdated());
			ReportDraft reportDraft = new ReportDraft(finishedCallReport, this.observerDateTime.now());
			this.kafkaSinks.sendReportDraft(observerUUID, reportDraft);
			this.activeStreamsRepository.deleteByCallUUIDBytes(record.getCalluuid());
		}
	}
}
