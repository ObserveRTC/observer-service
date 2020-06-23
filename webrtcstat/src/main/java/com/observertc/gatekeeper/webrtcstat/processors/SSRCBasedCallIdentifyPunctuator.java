package com.observertc.gatekeeper.webrtcstat.processors;

import com.observertc.gatekeeper.webrtcstat.micrometer.WebRTCStatsReporter;
import com.observertc.gatekeeper.webrtcstat.model.CallMapEntry;
import com.observertc.gatekeeper.webrtcstat.model.SSRCMapEntry;
import com.observertc.gatekeeper.webrtcstat.repositories.CallMapRepository;
import com.observertc.gatekeeper.webrtcstat.repositories.SSRCMapRepository;
import io.micronaut.context.annotation.Prototype;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
import org.javatuples.Pair;
import org.javatuples.Triplet;

@Prototype
public class SSRCBasedCallIdentifyPunctuator implements Punctuator {

	private final SSRCMapRepository ssrcMapRepository;
	private final CallMapRepository callMapRepository;
	private final WebRTCStatsReporter webRTCStatsReporter;
	private final Map<Triplet<Long, UUID, UUID>, SSRCMapEntry> ssrcObserverPeerConnections;
//	private final Map<SSRCMapEntry, LocalDateTime> ssrcMapEntries;

	public SSRCBasedCallIdentifyPunctuator(SSRCMapRepository ssrcMapRepository,
										   CallMapRepository callMapRepository,
										   WebRTCStatsReporter webRTCStatsReporter) {
		this.ssrcMapRepository = ssrcMapRepository;
		this.callMapRepository = callMapRepository;
		this.webRTCStatsReporter = webRTCStatsReporter;
		this.ssrcObserverPeerConnections = new HashMap<>();
	}


	public void init(ProcessorContext context) {

	}

	public void add(SSRCMapEntry ssrcMapEntry) {
		this.ssrcObserverPeerConnections.put(
				Triplet.with(ssrcMapEntry.SSRC, ssrcMapEntry.observerUUID, ssrcMapEntry.peerConnectionUUID),
				ssrcMapEntry
		);
	}

	/**
	 * This is the trigger method initiate the process of cleaning the calls
	 *
	 * @param timestamp
	 */

	@Override
	public void punctuate(long timestamp) {
		if (this.ssrcObserverPeerConnections.size() < 1) {
			return;
		}
		this.ssrcMapRepository.saveAll(ssrcObserverPeerConnections.values());
		Iterable<Triplet<Long, UUID, Set<CallMapEntry>>> callMapsIterable = this.ssrcMapRepository
				.getPeerConnections(() -> ssrcObserverPeerConnections.keySet().stream().map(triplet -> Pair.with(triplet.getValue0(),
						triplet.getValue1())).iterator());
		Iterator<Triplet<Long, UUID, Set<CallMapEntry>>> callMapsIterator = callMapsIterable.iterator();
		Map<UUID, UUID> pcToCallMap = new HashMap<>();
		for (; callMapsIterator.hasNext(); ) {
			Triplet<Long, UUID, Set<CallMapEntry>> ssrcObserverCallMaps = callMapsIterator.next();
			UUID observer = ssrcObserverCallMaps.getValue1();
			Set<CallMapEntry> providedCallMapEntries = ssrcObserverCallMaps.getValue2();
			UUID callUUID = null;
			Set<CallMapEntry> missingCallMaps = new HashSet<>();
			for (Iterator<CallMapEntry> callMapEntryIterator = providedCallMapEntries.iterator(); callMapEntryIterator.hasNext(); ) {
				CallMapEntry callMapEntry = callMapEntryIterator.next();
				if (pcToCallMap.containsKey(callMapEntry.peerConnectionUUID)) {
					continue;
				}
				if (callMapEntry.callUUID != null) {
					if (callUUID != null) {
						if (!callMapEntry.callUUID.equals(callUUID)) {
							// TODO: log it!
						}
					} else {
						callUUID = callMapEntry.callUUID;
					}
				} else {
					missingCallMaps.add(callMapEntry);
				}
			}
			boolean newCall = false;
			boolean alreadyHandled = false;
			if (callUUID == null) {
				newCall = true;
				callUUID = UUID.randomUUID();
			}
			int joinedParticipants = 0;
			for (Iterator<CallMapEntry> callMapEntryIterator = missingCallMaps.iterator(); callMapEntryIterator.hasNext(); ++joinedParticipants) {
				CallMapEntry callMapEntry = callMapEntryIterator.next();
				if (pcToCallMap.containsKey(callMapEntry.peerConnectionUUID)) {
					alreadyHandled = true;
				}
				callMapEntry.callUUID = callUUID;
				pcToCallMap.put(callMapEntry.peerConnectionUUID, callUUID);
			}
			if (alreadyHandled) {
				continue;
			}
			if (0 < joinedParticipants) {
				webRTCStatsReporter.incrementJoinedPeerConnections(observer, callUUID, joinedParticipants);
				if (newCall) {
					webRTCStatsReporter.incrementNumberOfInitiatedCalls(observer);
				}
			}

		}

		if (0 < pcToCallMap.size()) {
			this.callMapRepository.saveAll(() ->
					pcToCallMap.entrySet().stream().map(entry -> {
						CallMapEntry callMapEntry = new CallMapEntry();
						callMapEntry.peerConnectionUUID = entry.getKey();
						callMapEntry.callUUID = entry.getValue();
						return callMapEntry;
					}).iterator());
		}

		this.ssrcObserverPeerConnections.clear();
	}

}
