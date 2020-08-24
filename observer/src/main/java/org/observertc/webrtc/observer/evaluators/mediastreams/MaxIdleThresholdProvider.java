package org.observertc.webrtc.observer.evaluators.mediastreams;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Supplier;
import javax.inject.Singleton;
import org.observertc.webrtc.observer.EvaluatorsConfig;
import org.observertc.webrtc.observer.ObserverDateTime;
import org.observertc.webrtc.observer.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.observer.repositories.PeerConnectionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class MaxIdleThresholdProvider implements Supplier<Optional<LocalDateTime>> {

	private static final Logger logger = LoggerFactory.getLogger(MaxIdleThresholdProvider.class);
	private final EvaluatorsConfig.CallCleanerConfig config;
	private final ObserverDateTime observerDateTime;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private volatile boolean noJoinedPCLogged = false;
	private volatile boolean joinedPCIsTooOldLogged = false;

	public MaxIdleThresholdProvider(
			EvaluatorsConfig.CallCleanerConfig config,
			PeerConnectionsRepository peerConnectionsRepository,
			ObserverDateTime observerDateTime) {
		this.config = config;
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.observerDateTime = observerDateTime;
	}

	@Override
	public Optional<LocalDateTime> get() {
		LocalDateTime now = this.observerDateTime.now();
		Optional<PeerconnectionsRecord> lastJoinedPCHolder = this.peerConnectionsRepository.getLastJoinedPC();
		if (!lastJoinedPCHolder.isPresent()) {
			if (!this.noJoinedPCLogged) {
				logger.info("No previous joined PC updated field can be used," +
						" thus the threshold to declare PC detached is based on wall clock");
				this.noJoinedPCLogged = true;
			}
			return Optional.of(now.minusSeconds(this.config.streamMaxIdleTimeInS));
		}
		this.noJoinedPCLogged = false;
		PeerconnectionsRecord lastJoinedPC = lastJoinedPCHolder.get();
		LocalDateTime lastUpdate = lastJoinedPC.getUpdated();
		long elapsedTimeInS = ChronoUnit.SECONDS.between(lastUpdate, now);
		if (this.config.streamMaxAllowedGapInS < elapsedTimeInS) {
			if (this.joinedPCIsTooOldLogged) {
				logger.info("The last updated PC updated time ({}) is older than the actual wall clock time minus the max allowed time gap in" +
						"seconds {}, thereby the " +
						"actual wall clock is used as thresholds for detached PCs", lastUpdate, this.config.streamMaxAllowedGapInS);
				this.joinedPCIsTooOldLogged = true;
			}
			return Optional.of(now.minusSeconds(this.config.streamMaxIdleTimeInS));
		}
		this.joinedPCIsTooOldLogged = false;
		return Optional.of(lastUpdate.minusSeconds(this.config.streamMaxIdleTimeInS));
	}

}
