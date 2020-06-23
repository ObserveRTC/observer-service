package com.observertc.gatekeeper.webrtcstat.processors;

import com.observertc.gatekeeper.webrtcstat.micrometer.WebRTCStatsReporter;
import com.observertc.gatekeeper.webrtcstat.repositories.CallMapRepository;
import com.observertc.gatekeeper.webrtcstat.repositories.SSRCMapRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.inject.Singleton;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SSRCBasedCallIDCleanPunctuator implements Punctuator {

	private static final Logger logger = LoggerFactory.getLogger(SSRCBasedCallIDCleanPunctuator.class);

	private final SSRCMapRepository ssrcMapRepository;
	private final CallMapRepository callMapRepository;
	private final WebRTCStatsReporter webRTCStatsReporter;

	public SSRCBasedCallIDCleanPunctuator(SSRCMapRepository ssrcMapRepository,
										  CallMapRepository callMapRepository,
										  WebRTCStatsReporter webRTCStatsReporter) {
		this.ssrcMapRepository = ssrcMapRepository;
		this.callMapRepository = callMapRepository;
		this.webRTCStatsReporter = webRTCStatsReporter;
	}

	public void init(ProcessorContext context) {

	}

	@Override
	public void punctuate(long timestamp) {
		LocalDateTime expiration = LocalDateTime.now().minus(30, ChronoUnit.SECONDS);
		Map<UUID, Integer> designatedToDelete = new HashMap<>();
		Set<UUID> peerConnections = new HashSet<>();
		Map<UUID, UUID> callsToObservers = new HashMap<>();
		this.ssrcMapRepository.retrieveSSRCObserverPeerConnectionCallIDsOlderThan(expiration, ssrcObserverPeerConnectionCallIDTuple -> {
			Long SSRC = ssrcObserverPeerConnectionCallIDTuple.getValue0();
			UUID observer = ssrcObserverPeerConnectionCallIDTuple.getValue1();
			UUID peerConnectionUUID = ssrcObserverPeerConnectionCallIDTuple.getValue2();
			UUID callUUID = ssrcObserverPeerConnectionCallIDTuple.getValue3();
			if (callUUID == null) {
				logger.error("Call was not defined for SSRC: {}, observer: {}, peerConnection: {}", SSRC, observer, peerConnectionUUID);
				peerConnections.add(peerConnectionUUID);
				return;
			}
			if (peerConnections.contains(peerConnectionUUID)) {
				return;
			}
			if (!callsToObservers.containsKey(callUUID)) {
				callsToObservers.put(callUUID, observer);
			}
			Integer participantsDesignatedToDelete = designatedToDelete.getOrDefault(callUUID, 0);
			designatedToDelete.put(callUUID, participantsDesignatedToDelete + 1);
			peerConnections.add(peerConnectionUUID);

		});

		this.ssrcMapRepository.removePeerConnections(peerConnections);
		Map<UUID, Integer> participantsPerCalls = this.callMapRepository.retrieveParticipantsPerCalls(peerConnections);
		Iterator<Map.Entry<UUID, Integer>> designatedToDeleteIt = designatedToDelete.entrySet().iterator();
		for (; designatedToDeleteIt.hasNext(); ) {
			Map.Entry<UUID, Integer> entry = designatedToDeleteIt.next();
			UUID callUUID = entry.getKey();
			if (callUUID == null) {
				logger.error("A call, which has no ID is designated to be removed");
				continue;
			}
			Integer designatedParticipants = entry.getValue();
			Integer actualParticipants = participantsPerCalls.get(callUUID);
			UUID observerUUID = callsToObservers.get(callUUID);
			if (actualParticipants == null) {
				logger.warn("The actual number of participants for call {} is null.  The system equals it into the designated number of " +
								"participants to not to mess up the delete process, but something is wrong",
						callUUID);
				actualParticipants = designatedParticipants;
			}
			if (observerUUID == null) {
				logger.warn("The observerUUID for call {} is null.", callUUID);
				observerUUID = UUID.fromString("noOne");
			}

			if (designatedParticipants < actualParticipants) {
				continue;
			} else if (actualParticipants < designatedParticipants) {
				logger.warn("The actual number of participants is smaller than the number of participants designated to be deleted for " +
								"call {}.  The system equals it into the designated number of " +
								"participants to not to mess up the delete process, but something is wrong.",
						callUUID);
				designatedParticipants = actualParticipants;
			}
			this.webRTCStatsReporter.incrementDetachedPeerConnections(observerUUID, callUUID, designatedParticipants);
			if (designatedParticipants == actualParticipants) {
				this.webRTCStatsReporter.incrementNumberOfFinishedCalls(observerUUID);
			}
		}

		this.callMapRepository.removePeerConnections(peerConnections);
	}

}
