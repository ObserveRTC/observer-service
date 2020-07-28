package org.observertc.webrtc.service.processors.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.Punctuator;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.observertc.webrtc.common.reports.InboundRTPReport;
import org.observertc.webrtc.common.reports.OutboundRTPReport;
import org.observertc.webrtc.common.reports.RemoteInboundRTPReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.ReportsConfig;
import org.observertc.webrtc.service.dto.MediaStreamSampleTransformer;
import org.observertc.webrtc.service.model.PeerConnectionSSRCsEntry;
import org.observertc.webrtc.service.processors.mediastreams.valueadapters.InboundRTPConverter;
import org.observertc.webrtc.service.processors.mediastreams.valueadapters.OutboundRTPConverter;
import org.observertc.webrtc.service.processors.mediastreams.valueadapters.RemoteInboundRTPConverter;
import org.observertc.webrtc.service.repositories.PeerConnectionSSRCsRepository;
import org.observertc.webrtc.service.samples.MediaStreamSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class MediaStreamTransformer implements Transformer<UUID, MediaStreamSample, KeyValue<UUID, Report>>, Punctuator {

	private static final Logger logger = LoggerFactory.getLogger(MediaStreamTransformer.class);

	private final CallsReporter callsReporter;
	private final ReportsConfig reportsConfig;
	private final Map<Triplet<UUID, UUID, Long>, Pair<LocalDateTime, LocalDateTime>> updates;
	private final PeerConnectionSSRCsRepository peerConnectionSSRCsRepository;
	private final Function<MediaStreamSample, Report> sampleProcessor;

	public MediaStreamTransformer(
			PeerConnectionSSRCsRepository peerConnectionSSRCsRepository,
			ReportsConfig reportsConfig,
			CallsReporter callsReporter) {
		this.peerConnectionSSRCsRepository = peerConnectionSSRCsRepository;
		this.callsReporter = callsReporter;
		this.reportsConfig = reportsConfig;
		if (this.reportsConfig.reportMediaSamples) {
			this.sampleProcessor = this.makeMediaStreamProcessor();
		} else {
			this.sampleProcessor = report -> null;
		}

		updates = new HashMap<>();
	}

	@Override
	public void init(ProcessorContext context) {
		this.callsReporter.init(context, this.reportsConfig.callReports);
		if (this.reportsConfig.callReports.enabled) {
			int reportPeriodInS = this.reportsConfig.callReports.reportPeriodInS;
			context.schedule(Duration.ofSeconds(reportPeriodInS), PunctuationType.WALL_CLOCK_TIME, this.callsReporter);
		}
		int updatePeriodInS = this.reportsConfig.callReports.updatePeriodInS;
		context.schedule(Duration.ofSeconds(updatePeriodInS), PunctuationType.WALL_CLOCK_TIME, this);
		logger.info("MediaStreamEvaluator is configured by  {}", this.reportsConfig.toString());

	}

	@Override
	public KeyValue<UUID, Report> transform(UUID peerConnectionUUID, MediaStreamSample sample) {
		Long SSRC = sample.rtcStats.getSsrc().longValue();
		Triplet<UUID, UUID, Long> updateKey = Triplet.with(sample.observerUUID, peerConnectionUUID, SSRC);
		LocalDateTime timestamp = sample.sampled;
		Pair<LocalDateTime, LocalDateTime> createdUpdated = this.updates.getOrDefault(updateKey, Pair.with(timestamp, timestamp));
		createdUpdated = createdUpdated.setAt1(timestamp);
		this.updates.put(updateKey, createdUpdated);
		Report report = this.sampleProcessor.apply(sample);
		if (report == null) {
			return null;
		}
		return new KeyValue<>(sample.observerUUID, report);
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

	private Function<MediaStreamSample, Report> makeMediaStreamProcessor() {
		RemoteInboundRTPConverter remoteInboundRTPConverter = new RemoteInboundRTPConverter();
		InboundRTPConverter inboundRTPConverter = new InboundRTPConverter();
		OutboundRTPConverter outboundRTPConverter = new OutboundRTPConverter();
		MediaStreamSampleTransformer<Report> transformer = new MediaStreamSampleTransformer<Report>() {
			@Override
			public Report processInboundRTP(MediaStreamSample sample) {
				InboundRTPReport result = inboundRTPConverter.apply(sample);
				return result;
			}

			@Override
			public Report processOutboundRTP(MediaStreamSample sample) {
				OutboundRTPReport result = outboundRTPConverter.apply(sample);
				return result;
			}

			@Override
			public Report processRemoteInboundRTP(MediaStreamSample sample) {
				RemoteInboundRTPReport result = remoteInboundRTPConverter.apply(sample);
				return result;
			}

			@Override
			public Report processTrack(MediaStreamSample sample) {
				return null;
			}

			@Override
			public Report processMediaSource(MediaStreamSample sample) {
				return null;
			}

			@Override
			public Report processCandidatePair(MediaStreamSample sample) {
				return null;
			}
		};
		return new Function<MediaStreamSample, Report>() {
			@Override
			public Report apply(MediaStreamSample sample) {
				if (sample.rtcStats == null) {
					return transformer.unprocessable(sample);
				}
				if (sample.rtcStats.getSsrc() == null) {
					return transformer.unprocessable(sample);
				}
				return transformer.transform(sample);
			}
		};
	}

}
