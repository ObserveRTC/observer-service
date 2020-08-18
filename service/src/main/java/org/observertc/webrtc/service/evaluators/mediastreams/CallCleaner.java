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
import org.observertc.webrtc.service.ObserverKafkaSinks;
import org.observertc.webrtc.service.ObserverTimeZoneId;
import org.observertc.webrtc.service.jooq.enums.PeerconnectionsState;
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
	private final ObserverTimeZoneId observerTimeZoneId;
	private final ActiveStreamsRepository activeStreamsRepository;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private final ObserverKafkaSinks observerKafkaSinks;
	private final MaxIdleThresholdProvider maxIdleThresholdProvider;
	private final ReportsBuffer reportsBuffer;
	private final SentReportsRepository sentReportsRepository;

	public CallCleaner(
			EvaluatorsConfig config,
			SentReportsRepository sentReportsRepository,
			ActiveStreamsRepository activeStreamsRepository,
			PeerConnectionsRepository peerConnectionsRepository,
			MaxIdleThresholdProvider maxIdleThresholdProvider,
			ObserverKafkaSinks observerKafkaSinks,
			ObserverTimeZoneId observerTimeZoneId,
			ReportsBuffer reportsBuffer) {
		this.config = config.callCleaner;
		this.activeStreamsRepository = activeStreamsRepository;
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.observerTimeZoneId = observerTimeZoneId;
		this.maxIdleThresholdProvider = maxIdleThresholdProvider;
		this.observerKafkaSinks = observerKafkaSinks;
		this.reportsBuffer = reportsBuffer;
		this.sentReportsRepository = sentReportsRepository;
	}

	@Scheduled(initialDelay = "10m", fixedRate = "60m")
	void deleteExpiredPCs() {
		LocalDateTime now = LocalDateTime.now(this.observerTimeZoneId.getZoneId());
		LocalDateTime threshold = now.minusDays(this.config.pcRetentionTimeInDays);
		try {
			this.peerConnectionsRepository.deleteDetachedPCsUpdatedLessThan(threshold);
			this.sentReportsRepository.deleteReportedOlderThan(threshold);
		} catch (Exception ex) {
			logger.error("Cannot execute remove old PCs", ex);
		}
	}

	@Scheduled(initialDelay = "2m", fixedRate = "2m")
	void reportExpiredPCs() {
		try {
			this.cleanCalls();
		} catch (Exception ex) {
			logger.error("Cannot execute remove old PCs", ex);
		}
		this.reportsBuffer.process();
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
			record.setState(PeerconnectionsState.detached);
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
			this.reportsBuffer.accept(detachedPeerConnectionReport);
			record.store();
			Optional<PeerconnectionsRecord> detachedPCHolder =
					this.peerConnectionsRepository.findByCallUUIDBytes(record.getCalluuid()).filter(r -> r.getState().equals(PeerconnectionsState.joined)).findFirst();

			if (detachedPCHolder.isPresent()) {
				continue;
			}

			//finished call
			Report finishedCallReport = FinishedCallReport.of(observerUUID, callUUID, record.getUpdated());
			this.reportsBuffer.accept(finishedCallReport);
			this.activeStreamsRepository.deleteByCallUUIDBytes(record.getCalluuid());
		}
	}
}
