package org.observertc.webrtc.observer.evaluators.mediastreams;

import io.micronaut.scheduling.annotation.Scheduled;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Singleton;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.common.reports.avro.DetachedPeerConnection;
import org.observertc.webrtc.common.reports.avro.ReportType;
import org.observertc.webrtc.observer.EvaluatorsConfig;
import org.observertc.webrtc.observer.ReportDraftSink;
import org.observertc.webrtc.observer.ReportSink;
import org.observertc.webrtc.observer.evaluators.reportdrafts.FinishedCallReportDraft;
import org.observertc.webrtc.observer.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.observer.repositories.ActiveStreamsRepository;
import org.observertc.webrtc.observer.repositories.PeerConnectionsRepository;
import org.observertc.webrtc.observer.repositories.SentReportsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CallCleaner {

	private static final Logger logger = LoggerFactory.getLogger(CallCleaner.class);
	private final EvaluatorsConfig.CallCleanerConfig config;
	private final ActiveStreamsRepository activeStreamsRepository;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private final ReportDraftSink reportDraftSink;
	private final MaxIdleThresholdProvider maxIdleThresholdProvider;
	private final SentReportsRepository sentReportsRepository;
	private final ReportSink reportSink;

	public CallCleaner(
			EvaluatorsConfig config,
			SentReportsRepository sentReportsRepository,
			ActiveStreamsRepository activeStreamsRepository,
			PeerConnectionsRepository peerConnectionsRepository,
			MaxIdleThresholdProvider maxIdleThresholdProvider,
			ReportSink reportSink,
			ReportDraftSink reportDraftSink) {
		this.config = config.callCleaner;
		this.reportSink = reportSink;
		this.activeStreamsRepository = activeStreamsRepository;
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.maxIdleThresholdProvider = maxIdleThresholdProvider;
		this.reportDraftSink = reportDraftSink;
		this.sentReportsRepository = sentReportsRepository;
	}

	@Scheduled(initialDelay = "10m", fixedRate = "60m")
	void deleteExpiredPCs() {
		Long threshold = Instant.now().minus(this.config.pcRetentionTimeInDays, ChronoUnit.DAYS).toEpochMilli();
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
		Optional<Long> thresholdHolder = this.maxIdleThresholdProvider.get();
		if (!thresholdHolder.isPresent()) {
			return;
		}
		Long threshold = thresholdHolder.get();
		Iterator<PeerconnectionsRecord> it = this.peerConnectionsRepository.findJoinedPCsUpdatedLowerThan(threshold).iterator();
		for (; it.hasNext(); ) {
			PeerconnectionsRecord record = it.next();
			record.setDetached(record.getUpdated());
			record.setUpdated(record.getUpdated());
			record.store();

			UUID serviceUUID = UUIDAdapter.toUUID(record.getServiceuuid());
			UUID callUUID = UUIDAdapter.toUUID(record.getCalluuid());
			UUID peerConnectionUUID = UUIDAdapter.toUUID(record.getPeerconnectionuuid());

			Object payload = DetachedPeerConnection.newBuilder()
					.setCallUUID(callUUID.toString())
					.setUserID(record.getProvideduserid())
					.setMediaUnitID(record.getMediaunitid())
					.setPeerConnectionUUID(peerConnectionUUID.toString())
					.build();
			this.reportSink.sendReport(serviceUUID,
					record.getProvidedcallid(),
					serviceUUID.toString(),
					ReportType.DETACHED_PEER_CONNECTION,
					record.getUpdated(),
					payload);

			Optional<PeerconnectionsRecord> joinedPCHolder =
					this.peerConnectionsRepository.findJoinedPCsByCallUUIDBytes(record.getCalluuid()).findFirst();

			if (joinedPCHolder.isPresent()) {
				continue;
			}

			//finished call
			FinishedCallReportDraft reportDraft = FinishedCallReportDraft.of(serviceUUID, callUUID, record.getUpdated());
			this.reportDraftSink.send(serviceUUID, reportDraft);
			this.activeStreamsRepository.deleteByCallUUIDBytes(record.getCalluuid());
		}
	}
}
