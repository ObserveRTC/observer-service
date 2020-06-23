package com.observertc.gatekeeper.webrtcstat.processors;

import com.observertc.gatekeeper.webrtcstat.micrometer.WebRTCStatsReporter;
import com.observertc.gatekeeper.webrtcstat.model.CallMapEntry;
import com.observertc.gatekeeper.webrtcstat.model.SSRCMapEntry;
import com.observertc.gatekeeper.webrtcstat.repositories.CallMapRepository;
import com.observertc.gatekeeper.webrtcstat.repositories.SSRCMapRepository;
import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Singleton;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;

@Singleton
public class SSRCMapEntriesProcessor implements Processor<UUID, SSRCMapEntry> {

	private final SSRCMapRepository ssrcMapRepository;
	private final CallMapRepository callMapRepository;
	private final WebRTCStatsReporter webRTCStatsReporter;
	private final SSRCBasedCallIdentifyPunctuator callIdentifyPunctuator;
	private final SSRCBasedCallIDCleanPunctuator callIDCleanPunctuator;
	private ProcessorContext context;


	public SSRCMapEntriesProcessor(SSRCMapRepository ssrcMapRepository,
								   CallMapRepository callMapRepository,
								   WebRTCStatsReporter webRTCStatsReporter,
								   SSRCBasedCallIdentifyPunctuator ssrcBasedCallIdentifyPunctuator,
								   SSRCBasedCallIDCleanPunctuator ssrcBasedCallIDCleanPunctuator) {
		this.ssrcMapRepository = ssrcMapRepository;
		this.callMapRepository = callMapRepository;
		this.webRTCStatsReporter = webRTCStatsReporter;
		this.callIdentifyPunctuator = ssrcBasedCallIdentifyPunctuator;
		this.callIDCleanPunctuator = ssrcBasedCallIDCleanPunctuator;
	}

	@Override
	public void init(ProcessorContext context) {
		// keep the processor context locally because we need it in punctuate() and commit()
		this.context = context;
		this.callIdentifyPunctuator.init(context);
		this.callIDCleanPunctuator.init(context);
		int updatePeriodInS = 10;
		this.context.schedule(Duration.ofSeconds(updatePeriodInS), PunctuationType.WALL_CLOCK_TIME, this.callIdentifyPunctuator);
		this.context.schedule(Duration.ofSeconds(updatePeriodInS), PunctuationType.WALL_CLOCK_TIME, this.callIDCleanPunctuator);
	}

	@Override
	public void close() {

	}

	@Override
	public void process(UUID pcUUID, SSRCMapEntry sample) {
		this.callIdentifyPunctuator.add(sample);
	}

	//	@Override
	public void oldProcess(UUID pcUUID, SSRCMapEntry sample) {
		this.ssrcMapRepository.update(sample);
		Iterable<UUID> peerConnections = this.ssrcMapRepository.getPeerConnections(sample.SSRC, sample.observerUUID);
		UUID callUUID = null;
		List<CallMapEntry> missingPeerConnections = new LinkedList<>();
		for (Iterator<UUID> it = peerConnections.iterator(); it.hasNext(); ) {
			UUID peerConnection = it.next();
			Optional<CallMapEntry> callMapEntryHolder = callMapRepository.findById(peerConnection);
			if (callMapEntryHolder.isPresent()) {
				UUID candidate = callMapEntryHolder.get().callUUID;
				if (callUUID != null && !callUUID.equals(candidate)) {
					// TODO: logging
				}
				callUUID = candidate;
			} else {
				CallMapEntry entry = new CallMapEntry();
				entry.peerConnectionUUID = peerConnection;
				entry.callUUID = null;
				missingPeerConnections.add(entry);
			}
		}

		int numberOfMissingPeerConnections = missingPeerConnections.size();
		if (0 < numberOfMissingPeerConnections) {
			if (callUUID == null) {
				callUUID = UUID.randomUUID();
				this.webRTCStatsReporter.incrementNumberOfInitiatedCalls(sample.observerUUID);
			}
			final UUID finalCallUUID = callUUID;
			missingPeerConnections.forEach(callMapEntry -> callMapEntry.callUUID = finalCallUUID);
			this.callMapRepository.saveAll(missingPeerConnections);
			this.webRTCStatsReporter.incrementJoinedPeerConnections(sample.observerUUID, callUUID, numberOfMissingPeerConnections);
		}
	}
}
