package org.observertc.webrtc.service.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.Punctuator;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.model.PeerConnectionSSRCsEntry;
import org.observertc.webrtc.service.repositories.PeerConnectionSSRCsRepository;
import org.observertc.webrtc.service.samples.ObserveRTCMediaStreamStatsSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class CallsEvaluator implements Transformer<UUID, ObserveRTCMediaStreamStatsSample, KeyValue<UUID, Report>>, Punctuator {

	private static final Logger logger = LoggerFactory.getLogger(CallsEvaluator.class);

	private final CallsReporter callsReporter;
	private final MediaStreamEvaluatorConfiguration configuration;
	private final Map<Triplet<UUID, UUID, Long>, Pair<LocalDateTime, LocalDateTime>> updates;
	private final PeerConnectionSSRCsRepository peerConnectionSSRCsRepository;

	public CallsEvaluator(
			PeerConnectionSSRCsRepository peerConnectionSSRCsRepository,
			MediaStreamEvaluatorConfiguration configuration,
			CallsReporter callsReporter) {
		this.peerConnectionSSRCsRepository = peerConnectionSSRCsRepository;
		this.callsReporter = callsReporter;
		this.configuration = configuration;
		updates = new HashMap<>();
	}

	@Override
	public void init(ProcessorContext context) {
		this.callsReporter.init(context, this.configuration.callReports);
		if (this.configuration.callReports.enabled) {
			int reportPeriodInS = this.configuration.callReports.reportPeriodInS;
			context.schedule(Duration.ofSeconds(reportPeriodInS), PunctuationType.WALL_CLOCK_TIME, this.callsReporter);
		}
		int updatePeriodInS = this.configuration.updatePeriodInS;
		context.schedule(Duration.ofSeconds(updatePeriodInS), PunctuationType.WALL_CLOCK_TIME, this);
		logger.info("MediaStreamEvaluator is configured by  {}", this.configuration.toString());

	}

	@Override
	public KeyValue<UUID, Report> transform(UUID peerConnectionUUID, ObserveRTCMediaStreamStatsSample sample) {
		Long SSRC = sample.rtcStats.getSsrc().longValue();
		Triplet<UUID, UUID, Long> updateKey = Triplet.with(sample.observerUUID, peerConnectionUUID, SSRC);
		LocalDateTime timestamp = sample.sampled;
		Pair<LocalDateTime, LocalDateTime> createdUpdated = this.updates.getOrDefault(updateKey, Pair.with(timestamp, timestamp));
		createdUpdated.setAt1(timestamp);
		this.updates.put(updateKey, createdUpdated);
		return null;
	}

	@Override
	public void close() {

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
							Triplet<UUID, UUID, Long> observerPeerConnectionSSRC = entry.getKey();
							Pair<LocalDateTime, LocalDateTime> sampled = entry.getValue();
							LocalDateTime firstUpdate = sampled.getValue0();
							LocalDateTime lastUpdate = sampled.getValue1();
							mappedEntry.observerUUID = observerPeerConnectionSSRC.getValue0();
							mappedEntry.peerConnectionUUID = observerPeerConnectionSSRC.getValue1();
							mappedEntry.SSRC = observerPeerConnectionSSRC.getValue2();
							mappedEntry.updated = lastUpdate;
							this.callsReporter.accept(mappedEntry.peerConnectionUUID, Pair.with(mappedEntry.observerUUID, firstUpdate));
							return mappedEntry;
						}).iterator();
		this.peerConnectionSSRCsRepository.saveAll(entities);
		this.updates.clear();
	}

}
