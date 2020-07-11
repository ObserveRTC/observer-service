package org.observertc.webrtc.service.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.Punctuator;
import org.javatuples.Pair;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.micrometer.ObserverSSRCPeerConnectionSampleProcessReporter;
import org.observertc.webrtc.service.model.PeerConnectionSSRCsEntry;
import org.observertc.webrtc.service.repositories.CallPeerConnectionsRepository;
import org.observertc.webrtc.service.repositories.PeerConnectionSSRCsRepository;
import org.observertc.webrtc.service.samples.MediaStreamKey;
import org.observertc.webrtc.service.samples.ObserveRTCMediaStreamStatsSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class PeerConnectionSSRCsUpdater implements Punctuator {

	private static final Logger logger = LoggerFactory.getLogger(PeerConnectionSSRCsUpdater.class);

	private ProcessorContext context;

	private final PeerConnectionSSRCsRepository peerConnectionSSRCsRepository;
	private final Map<MediaStreamKey, Pair<LocalDateTime, LocalDateTime>> updates;
	//	private Map<UUID, Pair<UUID, LocalDateTime>> updatedPeerConnections;
	private BiConsumer<UUID, Pair<UUID, LocalDateTime>> updatedPeerConnections;

	//	private final Map<SSRCMapEntry, LocalDateTime> ssrcMapEntries;
	public PeerConnectionSSRCsUpdater(PeerConnectionSSRCsRepository peerConnectionSSRCsRepository,
									  CallPeerConnectionsRepository callPeerConnectionsRepository,
									  ObserverSSRCPeerConnectionSampleProcessReporter observerSSRCPeerConnectionSampleProcessReporter) {
		this.peerConnectionSSRCsRepository = peerConnectionSSRCsRepository;
		this.updates = new HashMap<>();
//		this.updatedPeerConnections = new HashMap<>();
	}


	public void init(ProcessorContext context) {
		this.context = context;

	}

	public void setPeerConnectionSSRCConsumer(BiConsumer<UUID, Pair<UUID, LocalDateTime>> updatedPeerConnections) {
		this.updatedPeerConnections = updatedPeerConnections;
	}

	/**
	 * Assumption: RTCStats cannot be null!
	 *
	 * @param peerConnectionUUID
	 * @param sample
	 */
	public void add(UUID peerConnectionUUID, ObserveRTCMediaStreamStatsSample sample) {
		RTCStats rtcStats = sample.rtcStats;
		if (rtcStats.getSsrc() == null) {
			logger.warn("SSRC cannot be null. {}", rtcStats.toString());
			return;
		}
		if (sample.observerUUID == null) {
			logger.warn("ObserverUUID for sample cannot be null {}", sample.toString());
			return;
		}
		if (sample.sampled == null) {
			logger.warn("timestamp for sample cannot be null {}", sample.toString());
			return;
		}
		Long SSRC = sample.rtcStats.getSsrc().longValue();
		UUID observerUUID = sample.observerUUID;
		MediaStreamKey mediaStreamKey = MediaStreamKey.of(observerUUID, peerConnectionUUID, SSRC);

		Pair<LocalDateTime, LocalDateTime> sampled = this.updates.getOrDefault(mediaStreamKey, Pair.with(sample.sampled, sample.sampled));
		sampled.setAt1(sample.sampled);
		this.updates.put(mediaStreamKey, sampled);
	}

	/**
	 * This is the trigger method initiate the process of cleaning the calls
	 *
	 * @param timestamp
	 */
	@Override
	public void punctuate(long timestamp) {
		try {
			this.doPunctuate(timestamp);
		} catch (Exception ex) {
			logger.error("Call Report process is failed", ex);
		}
	}

	/**
	 * The actual process we execute
	 *
	 * @param timestamp
	 */
	private void doPunctuate(long timestamp) {
		if (this.updates.size() < 1) {
			return;
		}
		Iterable<PeerConnectionSSRCsEntry> entities = () ->
				this.updates.entrySet().stream()
						.map(entry -> {
							PeerConnectionSSRCsEntry mappedEntry = new PeerConnectionSSRCsEntry();
							MediaStreamKey mediaStreamKey = entry.getKey();
							Pair<LocalDateTime, LocalDateTime> sampled = entry.getValue();
							LocalDateTime firstUpdate = sampled.getValue0();
							LocalDateTime lastUpdate = sampled.getValue1();
							mappedEntry.observerUUID = mediaStreamKey.observerUUID;
							mappedEntry.peerConnectionUUID = mediaStreamKey.peerConnectionUUID;
							mappedEntry.SSRC = mediaStreamKey.SSRC;
							mappedEntry.updated = lastUpdate;
							if (this.updatedPeerConnections != null) {
								this.updatedPeerConnections.accept(mappedEntry.peerConnectionUUID, Pair.with(mappedEntry.observerUUID, firstUpdate));
							}
							return mappedEntry;
						}).iterator();
		this.peerConnectionSSRCsRepository.saveAll(entities);
		this.updates.clear();
	}


}
