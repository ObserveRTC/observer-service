package org.observertc.webrtc.observer.service.processors;

import org.observertc.webrtc.observer.service.micrometer.ObserverSSRCPeerConnectionSampleProcessReporter;
import org.observertc.webrtc.observer.service.micrometer.WebRTCStatsReporter;
import org.observertc.webrtc.observer.service.model.CallPeerConnectionsEntry;
import org.observertc.webrtc.observer.service.model.PeerConnectionSSRCsEntry;
import org.observertc.webrtc.observer.service.repositories.CallPeerConnectionsRepository;
import org.observertc.webrtc.observer.service.repositories.PeerConnectionSSRCsRepository;
import org.observertc.webrtc.observer.service.samples.ObserverSSRCPeerConnectionSample;
import io.micronaut.context.annotation.Prototype;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class ObserverSSRCBasedCallIdentifyPunctuator implements Punctuator {

	private static final Logger logger = LoggerFactory.getLogger(ObserverSSRCBasedCallIdentifyPunctuator.class);

	private final PeerConnectionSSRCsRepository peerConnectionSSRCsRepository;
	private final CallPeerConnectionsRepository callPeerConnectionsRepository;
	private final WebRTCStatsReporter webRTCStatsReporter;

	private final Map<UUID, Pair<LocalDateTime, LocalDateTime>> pcUpdated;
	private final Map<UUID, Set<Pair<UUID, Long>>> pcToObserverSSRCs;
	private final Map<Pair<UUID, Long>, Set<UUID>> observerSSRCToPCs;
	private final ObserverSSRCPeerConnectionSampleProcessReporter observerSSRCPeerConnectionSampleProcessReporter;
//	private final Map<SSRCMapEntry, LocalDateTime> ssrcMapEntries;

	public ObserverSSRCBasedCallIdentifyPunctuator(PeerConnectionSSRCsRepository peerConnectionSSRCsRepository,
												   CallPeerConnectionsRepository callPeerConnectionsRepository,
												   ObserverSSRCPeerConnectionSampleProcessReporter observerSSRCPeerConnectionSampleProcessReporter,
												   WebRTCStatsReporter webRTCStatsReporter) {
		this.peerConnectionSSRCsRepository = peerConnectionSSRCsRepository;
		this.callPeerConnectionsRepository = callPeerConnectionsRepository;
		this.webRTCStatsReporter = webRTCStatsReporter;
		this.observerSSRCPeerConnectionSampleProcessReporter = observerSSRCPeerConnectionSampleProcessReporter;
		this.pcUpdated = new HashMap<>();
		this.pcToObserverSSRCs = new HashMap<>();
		this.observerSSRCToPCs = new HashMap<>();
	}


	public void init(ProcessorContext context) {


	}

	public void add(ObserverSSRCPeerConnectionSample sample) {
		UUID peerConnectionUUID = sample.peerConnectionUUID;
		Pair<UUID, Long> observerSSRC = Pair.with(sample.observerUUID, sample.SSRC);

		Set<UUID> observerSSRCPeerConnections = this.observerSSRCToPCs.getOrDefault(observerSSRC, new HashSet<>());
		observerSSRCPeerConnections.add(peerConnectionUUID);
		this.observerSSRCToPCs.put(observerSSRC, observerSSRCPeerConnections);

		Set<Pair<UUID, Long>> peerConnectionObserverSSRCs = this.pcToObserverSSRCs.getOrDefault(peerConnectionUUID, new HashSet<>());
		peerConnectionObserverSSRCs.add(observerSSRC);
		this.pcToObserverSSRCs.put(peerConnectionUUID, peerConnectionObserverSSRCs);

		LocalDateTime now = LocalDateTime.now();
		Pair<LocalDateTime, LocalDateTime> createdUpdated =
				this.pcUpdated.getOrDefault(peerConnectionUUID, Pair.with(now, now));
		createdUpdated.setAt1(now);
		this.pcUpdated.put(peerConnectionUUID, createdUpdated);
	}

	/**
	 * This is the trigger method initiate the process of cleaning the calls
	 *
	 * @param timestamp
	 */
	@Override
	public void punctuate(long timestamp) {
		this.observerSSRCPeerConnectionSampleProcessReporter.setBufferSize(this.observerSSRCToPCs.size());
		Instant started = Instant.now();
		try {
			this.punctuateProcess(timestamp);
		} catch (Exception ex) {
			logger.error("An exception occured during execution", ex);
		} finally {
			Duration duration = Duration.between(Instant.now(), started);
			this.observerSSRCPeerConnectionSampleProcessReporter.setCallIdentificationExecutionTime(duration);
		}
	}

	public void punctuateProcess(long timestamp) {
		if (this.observerSSRCToPCs.size() < 1) {
			return;
		}
		List<PeerConnectionSSRCsEntry> peerConnectionSSRCsEntryList = this.observerSSRCToPCs
				.entrySet().stream().flatMap(observerSSRCPCEntry -> {
					Pair<UUID, Long> observerSSRC = observerSSRCPCEntry.getKey();
					UUID observerUUID = observerSSRC.getValue0();
					Long SSRC = observerSSRC.getValue1();
					Set<UUID> peerConnections = observerSSRCPCEntry.getValue();
					return peerConnections.stream().map(peerConnection -> {
						PeerConnectionSSRCsEntry peerConnectionSSRCsEntry = new PeerConnectionSSRCsEntry();
						peerConnectionSSRCsEntry.peerConnectionUUID = peerConnection;
						peerConnectionSSRCsEntry.observerUUID = observerUUID;
						peerConnectionSSRCsEntry.SSRC = SSRC;
						Pair<LocalDateTime, LocalDateTime> createdUpdated = pcUpdated.get(peerConnection);
						peerConnectionSSRCsEntry.updated = createdUpdated.getValue1();
						return peerConnectionSSRCsEntry;
					});
				}).collect(Collectors.toList());
		Map<UUID, UUID> pcToCall = new HashMap<>();
		Set<UUID> addedPCs = new HashSet<>();
		Set<UUID> callUUIDs = new HashSet<>();
		this.peerConnectionSSRCsRepository.saveAll(peerConnectionSSRCsEntryList);
		this.callPeerConnectionsRepository.fetchByIds(() -> this.pcToObserverSSRCs.keySet().iterator(), callMapEntry -> {
			callUUIDs.add(callMapEntry.callUUID);
		});
		this.callPeerConnectionsRepository.getCallMapsForCallUUIDs(() -> callUUIDs.iterator(), callMapEntry -> {
			pcToCall.put(callMapEntry.peerConnectionUUID, callMapEntry.callUUID);
		});

		Iterator<Map.Entry<Pair<UUID, Long>, Set<UUID>>> observerSSRCToPCIt = this.observerSSRCToPCs.entrySet().iterator();
		for (; observerSSRCToPCIt.hasNext(); ) {
			Map.Entry<Pair<UUID, Long>, Set<UUID>> entry = observerSSRCToPCIt.next();
			Pair<UUID, Long> observerSSRC = entry.getKey();
			Set<UUID> peerConnections = entry.getValue();
			Iterator<UUID> peerConnectionsIterator = peerConnections.iterator();
			UUID callUUID = null;
			Set<UUID> joinedPeerConnections = new HashSet<>();
			for (; peerConnectionsIterator.hasNext(); ) {
				UUID peerConnectionUUID = peerConnectionsIterator.next();
				UUID callUUIDCandidate = pcToCall.get(peerConnectionUUID);
				if (callUUIDCandidate == null) {
					joinedPeerConnections.add(peerConnectionUUID);
					continue;
				}
				if (callUUID == null) {
					callUUID = callUUIDCandidate;
					continue;
				} else if (!callUUIDCandidate.equals(callUUID)) {
					logger.warn("Call UUID ({}, {}) mismatch for the same Observer, SSRC pair: <{},{}>",
							callUUID, callUUIDCandidate, observerSSRC.getValue0(), observerSSRC.getValue1());
				}
			}
			// if everybody has already been joined, the callUUID is null!
			if (joinedPeerConnections.size() < 1) { // everybody has already joined
				continue;
			}
			if (callUUID == null) { // This is a new call!
				callUUID = UUID.randomUUID();
				UUID firstPc = joinedPeerConnections.stream()
						.filter(pcUpdated::containsKey)
						.min(new Comparator<UUID>() {
							@Override
							public int compare(UUID o1, UUID o2) {
								Pair<LocalDateTime, LocalDateTime> createdUpdated1 = pcUpdated.get(o1);
								Pair<LocalDateTime, LocalDateTime> createdUpdated2 = pcUpdated.get(o2);
								LocalDateTime d1 = createdUpdated1.getValue0();
								LocalDateTime d2 = createdUpdated2.getValue0();
								return d1.compareTo(d2);
							}
						}).get();
				Pair<LocalDateTime, LocalDateTime> createdUpdated = pcUpdated.get(firstPc);
				LocalDateTime initiated = createdUpdated.getValue0();
				this.webRTCStatsReporter.incrementInitiatedCalls(observerSSRC.getValue0(), callUUID, initiated);
			}
			Iterator<UUID> joinedPeerConnectionsIterator = joinedPeerConnections.iterator();
			for (; joinedPeerConnectionsIterator.hasNext(); ) {
				UUID joinedPeerConnection = joinedPeerConnectionsIterator.next();
				Pair<LocalDateTime, LocalDateTime> createdUpdated = pcUpdated.get(joinedPeerConnection);
				if (createdUpdated == null) {
					logger.info("A peer connection {}, which did not joined before appeared in a new call {}", joinedPeerConnection, callUUID);
					createdUpdated = Pair.with(LocalDateTime.now(), LocalDateTime.now());
				}
				LocalDateTime joined = createdUpdated.getValue0();
				this.webRTCStatsReporter.incrementJoinedPeerConnections(observerSSRC.getValue0(), callUUID, joinedPeerConnection,
						joined);
				pcToCall.put(joinedPeerConnection, callUUID);
				addedPCs.add(joinedPeerConnection);
			}
		}
		// Save added pcs
		Iterable<CallPeerConnectionsEntry> callMapEntryIterable = () -> addedPCs.stream().map(peerConnectionUUID -> {
			CallPeerConnectionsEntry callPeerConnectionsEntry = new CallPeerConnectionsEntry();
			callPeerConnectionsEntry.peerConnectionUUID = peerConnectionUUID;
			callPeerConnectionsEntry.callUUID = pcToCall.get(peerConnectionUUID);
			return callPeerConnectionsEntry;
		}).iterator();
		this.callPeerConnectionsRepository.saveAll(callMapEntryIterable);
		this.observerSSRCToPCs.clear();
		this.pcToObserverSSRCs.clear();
		this.pcUpdated.clear();
	}


}
