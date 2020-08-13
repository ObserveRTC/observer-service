package org.observertc.webrtc.service.evaluators.mediastreams;

import io.micronaut.context.annotation.Prototype;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.Punctuator;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.EvaluatorsConfig;
import org.observertc.webrtc.service.dto.RTCStatsBiTransformer;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.evaluators.WebExtrAppSampleIteratorProvider;
import org.observertc.webrtc.service.evaluators.valueadapters.NumberConverter;
import org.observertc.webrtc.service.samples.WebExtrAppSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class ActiveStreamsEvaluator implements Punctuator, Transformer<UUID, WebExtrAppSample, KeyValue<UUID, Report>>, BiConsumer<WebExtrAppSample,
		RTCStats> {

	private static final Logger logger = LoggerFactory.getLogger(ActiveStreamsEvaluator.class);
	private final EvaluatorsConfig.ActiveStreamsConfig config;
	private final Map<UUID, MediaStreamUpdate> updates;
	private ProcessorContext context;
	private final RTCStatsBiTransformer<WebExtrAppSample, Void> updater;
	private LocalDateTime lastUpdate;

	private final CallCleaner callCleaner;
	private final ReportsBuffer reportsBuffer;

	private final MediaStreamUpdateProcessor mediaStreamUpdateProcessor;

	public ActiveStreamsEvaluator(
			EvaluatorsConfig.ActiveStreamsConfig config,
			MediaStreamUpdateProcessor mediaStreamUpdateProcessor,
			CallCleaner callCleaner,
			ReportsBuffer reportsBuffer) {
		this.config = config;
		this.callCleaner = callCleaner;
		this.reportsBuffer = reportsBuffer;
		this.mediaStreamUpdateProcessor = mediaStreamUpdateProcessor;
		this.updater = this.makeRTCStatsProcessor();
		this.updates = new HashMap<>();
	}

	@Override
	public void init(ProcessorContext context) {
		this.context = context;
		this.reportsBuffer.init(this.context);
		this.mediaStreamUpdateProcessor.init(this.context, this.reportsBuffer);
		this.callCleaner.init(this.context, this.reportsBuffer);
		int updatePeriodInS = this.config.updatePeriodInS;
		context.schedule(Duration.ofSeconds(updatePeriodInS), PunctuationType.WALL_CLOCK_TIME, this);
	}

	@Override
	public KeyValue<UUID, Report> transform(UUID peerConnectionUUID, WebExtrAppSample sample) {
		Iterator<RTCStats> it = WebExtrAppSampleIteratorProvider.RTCStatsIt(sample);
		for (; it.hasNext(); ) {
			RTCStats rtcStatsItem = it.next();
			this.accept(sample, rtcStatsItem);
		}
		return null;
	}

	@Override
	public void accept(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
		this.updater.transform(rtcStats, webExtrAppSample);
	}

	@Override
	public void close() {

	}

	public int getUpdatePeriodInS() {
		return this.config.updatePeriodInS;
	}

	@Override
	public void punctuate(long timestamp) {
		if (0 < this.updates.size()) {
			Deque<MediaStreamUpdate> updateQ = new LinkedList<>();
			this.updates.values().stream().forEach(updateQ::add);
			this.mediaStreamUpdateProcessor.accept(updateQ);
			this.updates.clear();
		}
		this.callCleaner.setLastUpdate(this.lastUpdate);
		this.callCleaner.run();
		this.reportsBuffer.process();
	}

	private RTCStatsBiTransformer<WebExtrAppSample, Void> makeRTCStatsProcessor() {
		final Function<WebExtrAppSample, MediaStreamUpdate> updateMaker = (webExtrAppSample) -> {
			return MediaStreamUpdate.of(
					webExtrAppSample.observerUUID,
					webExtrAppSample.peerConnectionUUID,
					webExtrAppSample.timestamp,
					webExtrAppSample.peerConnectionSample.getBrowserId(),
					webExtrAppSample.sampleTimeZoneID

			);
		};
		final BiConsumer<WebExtrAppSample, RTCStats> updater = (webExtrAppSample, rtcStats) -> {
			MediaStreamUpdate mediaStreamUpdate = updates.getOrDefault(
					webExtrAppSample.peerConnectionUUID,
					updateMaker.apply(webExtrAppSample)
			);
			Long SSRC = NumberConverter.toLong(rtcStats.getSsrc());
			mediaStreamUpdate.add(SSRC, webExtrAppSample.timestamp);
			updates.put(webExtrAppSample.peerConnectionUUID, mediaStreamUpdate);
			if (mediaStreamUpdate.updated != null) {
				if (lastUpdate == null || lastUpdate.compareTo(mediaStreamUpdate.updated) < 0) {
					lastUpdate = mediaStreamUpdate.updated;
				}
			}
		};
		return new RTCStatsBiTransformer<WebExtrAppSample, Void>() {
			@Override
			public Void processInboundRTP(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
				updater.accept(webExtrAppSample, rtcStats);
				return null;
			}

			@Override
			public Void processOutboundRTP(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
				updater.accept(webExtrAppSample, rtcStats);
				return null;
			}

			@Override
			public Void processRemoteInboundRTP(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
				updater.accept(webExtrAppSample, rtcStats);
				return null;
			}

			@Override
			public Void processTrack(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
				return null;
			}

			@Override
			public Void processMediaSource(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
				return null;
			}

			@Override
			public Void processCandidatePair(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
				return null;
			}
		};
	}
}
