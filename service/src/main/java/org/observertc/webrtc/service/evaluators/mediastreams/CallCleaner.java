package org.observertc.webrtc.service.evaluators.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.observertc.webrtc.common.UUIDAdapter;
import org.observertc.webrtc.common.reports.DetachedPeerConnectionReport;
import org.observertc.webrtc.common.reports.FinishedCallReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.EvaluatorsConfig;
import org.observertc.webrtc.service.ObserverTimeZoneId;
import org.observertc.webrtc.service.jooq.enums.PeerconnectionsState;
import org.observertc.webrtc.service.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.service.repositories.ActiveStreamsRepository;
import org.observertc.webrtc.service.repositories.PeerConnectionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class CallCleaner implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(CallCleaner.class);
	private final EvaluatorsConfig.ActiveStreamsConfig config;
	private ProcessorContext context;
	private LocalDateTime lastUpdate;
	private final ObserverTimeZoneId observerTimeZoneId;
	private final ActiveStreamsRepository activeStreamsRepository;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private ReportsBuffer reportsBuffer;
	private int run = 0;

	public CallCleaner(
			EvaluatorsConfig.ActiveStreamsConfig config,
			ActiveStreamsRepository activeStreamsRepository,
			PeerConnectionsRepository peerConnectionsRepository,

			ObserverTimeZoneId observerTimeZoneId) {
		this.config = config;
		this.activeStreamsRepository = activeStreamsRepository;
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.observerTimeZoneId = observerTimeZoneId;
	}

	public void init(ProcessorContext context, ReportsBuffer reportsBuffer) {
		this.context = context;
		this.reportsBuffer = reportsBuffer;
	}

	public void setLastUpdate(LocalDateTime value) {
		this.lastUpdate = value;
	}

	@Override
	public void run() {
		if (this.run < this.config.waitingPeriods) {
			++this.run;
			return;
		}

		LocalDateTime threshold = this.getMaxIdleThreshold();
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
			this.context.forward(observerUUID, detachedPeerConnectionReport);
			record.store();
			Optional<PeerconnectionsRecord> joinedPCHolder =
					this.peerConnectionsRepository.findByCallUUIDBytes(record.getCalluuid()).filter(r -> r.getState().equals(PeerconnectionsState.joined)).findFirst();

			if (joinedPCHolder.isPresent()) {
				continue;
			}

			//finished call
			Report finishedCallReport = FinishedCallReport.of(observerUUID, callUUID, record.getUpdated());
			this.reportsBuffer.accept(finishedCallReport);
			this.activeStreamsRepository.deleteByCallUUIDBytes(record.getCalluuid());
		}

		++this.run;
	}

	private LocalDateTime getMaxIdleThreshold() {
		LocalDateTime now = LocalDateTime.now(this.observerTimeZoneId.getZoneId());
		LocalDateTime result = this.lastUpdate;
		if (result == null) {
			Optional<PeerconnectionsRecord> lastJoinedPCHolder = this.peerConnectionsRepository.getLastJoinedPC();
			if (!lastJoinedPCHolder.isPresent()) {
				logger.info("No new peer connection happened in the last {} periods, and no previous joined PC updated field can be used," +
						" thus the threshold to declare PC detached is based on wall clock", this.run);
				result = now;
			} else {
				PeerconnectionsRecord lastJoinedPC = lastJoinedPCHolder.get();
				LocalDateTime lastUpdate = lastJoinedPC.getUpdated();
				result = lastUpdate;
			}
		}

		if (result.compareTo(now.minusSeconds(this.config.maxAllowedUpdateGapInS)) < 0) {
			logger.info("The last updated PC updated time ({}) is older than the actual wall clock time minus the max allowed time gap in" +
					"seconds {}, thereby the " +
					"actual wall clock is used as thresholds for detached PCs", this.lastUpdate, this.config.maxAllowedUpdateGapInS);
			result = now;
		}

		return result.minusSeconds(this.config.maxIdleTimeInS);
	}

}
