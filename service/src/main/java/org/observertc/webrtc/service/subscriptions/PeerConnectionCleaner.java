package org.observertc.webrtc.service.subscriptions;

import io.micronaut.scheduling.annotation.Scheduled;
import java.time.LocalDateTime;
import javax.inject.Singleton;
import org.observertc.webrtc.service.EvaluatorsConfig;
import org.observertc.webrtc.service.ObserverTimeZoneId;
import org.observertc.webrtc.service.repositories.PeerConnectionsRepository;

@Singleton
public class PeerConnectionCleaner {

	private final PeerConnectionsRepository peerConnectionsRepository;
	private final EvaluatorsConfig.ActiveStreamsConfig config;
	private final ObserverTimeZoneId observerTimeZoneId;

	public PeerConnectionCleaner(
			PeerConnectionsRepository peerConnectionsRepository,
			EvaluatorsConfig.ActiveStreamsConfig config,
			ObserverTimeZoneId observerTimeZoneId) {
		this.peerConnectionsRepository = peerConnectionsRepository;
		this.config = config;
		this.observerTimeZoneId = observerTimeZoneId;
	}

	@Scheduled(initialDelay = "20m", fixedRate = "60m")
	void execute() {
		LocalDateTime now = LocalDateTime.now(this.observerTimeZoneId.getZoneId());
		LocalDateTime threshold = now.minusDays(this.config.retentionTimeInDays);
		this.peerConnectionsRepository.deleteDetachedPCsUpdatedLessThan(threshold);
	}
}
