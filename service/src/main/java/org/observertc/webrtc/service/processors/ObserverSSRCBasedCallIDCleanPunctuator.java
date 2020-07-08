package org.observertc.webrtc.service.processors;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
import org.observertc.webrtc.common.reports.DetachedPeerConnection;
import org.observertc.webrtc.common.reports.FinishedCall;
import org.observertc.webrtc.common.reportsink.CallReports;
import org.observertc.webrtc.common.reportsink.ReportService;
import org.observertc.webrtc.service.micrometer.ObserverSSRCPeerConnectionSampleProcessReporter;
import org.observertc.webrtc.service.micrometer.WebRTCStatsReporter;
import org.observertc.webrtc.service.reportsink.ReportServiceProvider;
import org.observertc.webrtc.service.repositories.CallPeerConnectionsRepository;
import org.observertc.webrtc.service.repositories.PeerConnectionSSRCsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ObserverSSRCBasedCallIDCleanPunctuator implements Punctuator {

	private static final Logger logger = LoggerFactory.getLogger(ObserverSSRCBasedCallIDCleanPunctuator.class);

	private final PeerConnectionSSRCsRepository peerConnectionSSRCsRepository;
	private final CallPeerConnectionsRepository callPeerConnectionsRepository;
	private final WebRTCStatsReporter webRTCStatsReporter;
	private final ObserverSSRCPeerConnectionSampleProcessReporter observerSSRCPeerConnectionSampleProcessReporter;
	private final int expirationTimeInS;
	private final ReportService reportService;

	public ObserverSSRCBasedCallIDCleanPunctuator(
			PeerConnectionSSRCsRepository peerConnectionSSRCsRepository,
			CallPeerConnectionsRepository callPeerConnectionsRepository,
			WebRTCStatsReporter webRTCStatsReporter,
			ReportServiceProvider reportServiceProvider,
			ObserverSSRCPeerConnectionSampleProcessReporter observerSSRCPeerConnectionSampleProcessReporter) {
		this.observerSSRCPeerConnectionSampleProcessReporter = observerSSRCPeerConnectionSampleProcessReporter;
		this.expirationTimeInS = 30;
		this.reportService = reportServiceProvider.getReportService();
		this.peerConnectionSSRCsRepository = peerConnectionSSRCsRepository;
		this.callPeerConnectionsRepository = callPeerConnectionsRepository;
		this.webRTCStatsReporter = webRTCStatsReporter;
	}

	public void init(ProcessorContext context) {

	}

	/**
	 * This is the trigger method initiate the process of cleaning the calls
	 *
	 * @param timestamp
	 */
	@Override
	public void punctuate(long timestamp) {
		Instant started = Instant.now();
		try {
			this.punctuateProcess(timestamp);
		} catch (Exception ex) {
			logger.error("An exception occured during execution", ex);
		} finally {
			Duration duration = Duration.between(Instant.now(), started);
			this.observerSSRCPeerConnectionSampleProcessReporter.setCallCleaningExecutionTime(duration);
		}
	}

	public void punctuateProcess(long timestamp) {
		CallReports callReports = this.reportService.getCallReports();
		LocalDateTime expiration = LocalDateTime.now().minus(this.expirationTimeInS, ChronoUnit.SECONDS);
		Set<UUID> expiredPeerConnections = new HashSet<>();
		Set<UUID> callUUIDs = new HashSet<>();
		Map<UUID, UUID> pcToObservers = new HashMap<>();
		Map<UUID, LocalDateTime> pcLastUpdated = new HashMap<>();
		Map<UUID, Set<UUID>> callToExpiredPeerConnections = new HashMap<>();
		Map<UUID, Set<UUID>> callToPeerConnections = new HashMap<>();
		this.peerConnectionSSRCsRepository.getSSRCMapEntriesOlderThan(expiration,
				ssrcMapEntry -> {
					expiredPeerConnections.add(ssrcMapEntry.peerConnectionUUID);
					pcToObservers.put(ssrcMapEntry.peerConnectionUUID, ssrcMapEntry.observerUUID);
					LocalDateTime lastUpdated = pcLastUpdated.get(ssrcMapEntry.peerConnectionUUID);
					if (lastUpdated == null || lastUpdated.compareTo(ssrcMapEntry.updated) < 0) {
						lastUpdated = ssrcMapEntry.updated;
					}
					pcLastUpdated.put(ssrcMapEntry.peerConnectionUUID, lastUpdated);
				});
		logger.info("The following peer connections have not received any update in the last {} seconds. {}", this.expirationTimeInS,
				String.join(", ", expiredPeerConnections.stream().map(Object::toString).collect(Collectors.toList())));
		this.callPeerConnectionsRepository.getCallMapsForPeerConnectionUUIDs(() -> expiredPeerConnections.iterator(), callMapEntry -> {
			callUUIDs.add(callMapEntry.callUUID);
			Set<UUID> callExpiredPeerConnections = callToExpiredPeerConnections.getOrDefault(callMapEntry.callUUID, new HashSet<>());
			callExpiredPeerConnections.add(callMapEntry.peerConnectionUUID);
			callToExpiredPeerConnections.put(callMapEntry.callUUID, callExpiredPeerConnections);
		});
		this.callPeerConnectionsRepository.getCallMapsForCallUUIDs(() -> callUUIDs.iterator(), callMapEntry -> {
			UUID peerConnectionUUID = callMapEntry.peerConnectionUUID;
			UUID callUUID = callMapEntry.callUUID;
			Set<UUID> callPeerConnections = callToPeerConnections.getOrDefault(callUUID, new HashSet<>());
			callPeerConnections.add(peerConnectionUUID);
			callToPeerConnections.put(callUUID, callPeerConnections);
		});

		Iterator<Map.Entry<UUID, Set<UUID>>> callsToExpiredPeerConnectionsIterator = callToExpiredPeerConnections.entrySet().iterator();
		for (; callsToExpiredPeerConnectionsIterator.hasNext(); ) {
			Map.Entry<UUID, Set<UUID>> entry = callsToExpiredPeerConnectionsIterator.next();
			UUID callUUID = entry.getKey();
			UUID observerUUID = null;
			LocalDateTime lastUpdateOfCall = null;
			Set<UUID> expiredPeerConnectionsSet = entry.getValue();
			Iterator<UUID> expiredPeerConnectionsSetIterator = expiredPeerConnectionsSet.iterator();
			for (; expiredPeerConnectionsSetIterator.hasNext(); ) {
				UUID expiredPeerConnection = expiredPeerConnectionsSetIterator.next();
				LocalDateTime lastUpdate = pcLastUpdated.get(expiredPeerConnection);
				if (lastUpdate == null) {
					logger.warn("Something went wrong with the last update tracking. Peer connection: {}, call: {}",
							expiredPeerConnection, callUUID);
					lastUpdate = LocalDateTime.now();
				} else {
					if (lastUpdateOfCall == null || 0 < lastUpdateOfCall.compareTo(lastUpdate)) {
						lastUpdateOfCall = lastUpdate;
					}
				}
				UUID observerUUIDCandidate = pcToObservers.get(expiredPeerConnection);
				if (observerUUIDCandidate == null) {
					logger.warn("Something went wrong retrieving the  observer for peer connection {}, call {}", expiredPeerConnection,
							callUUID);
				} else {
					if (observerUUID != null && !observerUUID.equals(observerUUIDCandidate)) {
						logger.warn("It is not allowed (yet) to have more than one observer for the same call. " +
										"observers: {}, {}; call: {}, expired peer connection: {}", observerUUID, observerUUIDCandidate, callUUID
								, expiredPeerConnection);
					}
					observerUUID = observerUUIDCandidate;
				}
				DetachedPeerConnection detachedPeerConnection = this.makeDetachedPeerConnection(observerUUID, callUUID,
						expiredPeerConnection, lastUpdate);
				callReports.detachedPeerConnection(detachedPeerConnection);
			}
			Set<UUID> allPeerConnectionsForTheCall = callToPeerConnections.get(callUUID);
			int callPeerConnectionsTotal = 0;
			if (allPeerConnectionsForTheCall != null) {
				callPeerConnectionsTotal = allPeerConnectionsForTheCall.size();
			}
			int callPeerConnectionsExpired = 0;
			if (expiredPeerConnectionsSet != null) {
				callPeerConnectionsExpired = expiredPeerConnectionsSet.size();
			}
			if (callPeerConnectionsTotal <= callPeerConnectionsExpired) {
				if (callPeerConnectionsTotal < callPeerConnectionsExpired) {
					logger.warn("The number of expired peer connections is higher than the total number of peer connecttion for the call." +
							" HOW? call: {}", callUUID);
				}
				if (lastUpdateOfCall == null) {
					lastUpdateOfCall = LocalDateTime.now();
				}
				FinishedCall finishedCall = this.makeFinishedCall(observerUUID, callUUID, lastUpdateOfCall);
				callReports.finishedCall(finishedCall);
			}
		}
		this.callPeerConnectionsRepository.deleteByIds(() -> expiredPeerConnections.iterator());
		this.peerConnectionSSRCsRepository.removePeerConnections(expiredPeerConnections);
	}

	private DetachedPeerConnection makeDetachedPeerConnection(UUID observerUUID, UUID callUUID, UUID peerConnectionUUID, LocalDateTime detached) {
		return new DetachedPeerConnection() {
			@Override
			public UUID getObserverUUID() {
				return observerUUID;
			}

			@Override
			public UUID getPeerConnectionUUID() {
				return peerConnectionUUID;
			}

			@Override
			public LocalDateTime getTimestamp() {
				return detached;
			}

			@Override
			public UUID getCallUUID() {
				return callUUID;
			}
		};
	}

	private FinishedCall makeFinishedCall(UUID observerUUID, UUID callUUID, LocalDateTime finished) {
		return new FinishedCall() {
			@Override
			public UUID getObserverUUID() {
				return observerUUID;
			}

			@Override
			public UUID getCallUUID() {
				return callUUID;
			}

			@Override
			public LocalDateTime getTimestamp() {
				return finished;
			}
		};
	}
}
