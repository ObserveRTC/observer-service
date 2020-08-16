package org.observertc.webrtc.service.evaluators.mediastreams;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;
import javax.inject.Singleton;
import org.observertc.webrtc.service.EvaluatorsConfig;
import org.observertc.webrtc.service.ObserverTimeZoneId;
import org.observertc.webrtc.service.jooq.tables.records.PeerconnectionsRecord;
import org.observertc.webrtc.service.repositories.PeerConnectionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class MaxIdleThresholdProvider implements Supplier<Optional<LocalDateTime>> {

	private static final Logger logger = LoggerFactory.getLogger(MaxIdleThresholdProvider.class);
	private final EvaluatorsConfig.CallCleanerConfig config;
	private final ObserverTimeZoneId observerTimeZoneId;
	private final PeerConnectionsRepository peerConnectionsRepository;
	private final LocalDateTime initiated;
	private volatile boolean noJoinedPCLogged = false;
	private volatile boolean joinedPCIsTooOldLogged = false;

	public MaxIdleThresholdProvider(
			EvaluatorsConfig.CallCleanerConfig config,
			PeerConnectionsRepository peerConnectionsRepository,
			ObserverTimeZoneId observerTimeZoneId) {
		this.config = config;
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.observerTimeZoneId = observerTimeZoneId;
		this.initiated = LocalDateTime.now(this.observerTimeZoneId.getZoneId());
	}

	@Override
	public Optional<LocalDateTime> get() {
		LocalDateTime now = LocalDateTime.now(this.observerTimeZoneId.getZoneId());
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

		if (lastUpdate.compareTo(now.minusSeconds(this.config.streamMaxAllowedGapInS)) < 0) {
			if (this.joinedPCIsTooOldLogged) {
				logger.info("The last updated PC updated time ({}) is older than the actual wall clock time minus the max allowed time gap in" +
						"seconds {}, thereby the " +
						"actual wall clock is used as thresholds for detached PCs", lastUpdate, this.config.streamMaxAllowedGapInS);
				this.joinedPCIsTooOldLogged = true;
			}
			return Optional.of(now.minusSeconds(this.config.streamMaxIdleTimeInS));
		}
		this.joinedPCIsTooOldLogged = false;
		return Optional.of(now.minusSeconds(this.config.streamMaxIdleTimeInS));
	}

}
