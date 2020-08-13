package org.observertc.webrtc.service.evaluators.purgatory;

import java.time.Duration;
import java.util.Iterator;
import java.util.UUID;
import java.util.function.BiFunction;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.EvaluatorsConfig;
import org.observertc.webrtc.service.dto.RTCStatsBiTransformer;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.evaluators.mediastreams.ActiveStreamsEvaluator;
import org.observertc.webrtc.service.evaluators.WebExtrAppSampleIteratorProvider;
import org.observertc.webrtc.service.evaluators.valueadapters.InboundRTPConverter;
import org.observertc.webrtc.service.evaluators.valueadapters.OutboundRTPConverter;
import org.observertc.webrtc.service.evaluators.valueadapters.RemoteInboundRTPConverter;
import org.observertc.webrtc.service.samples.MediaStreamSample;
import org.observertc.webrtc.service.samples.WebExtrAppSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediaStatsEvaluator implements Transformer<UUID, WebExtrAppSample, KeyValue<UUID, Report>> {

	private static final Logger logger = LoggerFactory.getLogger(MediaStatsEvaluator.class);
	private final RTCStatsBiTransformer<WebExtrAppSample, Report> rtcStatsBiTransformer;
	private ProcessorContext context;
	private final EvaluatorsConfig.RTCStatsConfig config;
	private final ActiveStreamsEvaluator activeStreamsEvaluator;

	public MediaStatsEvaluator(EvaluatorsConfig.RTCStatsConfig config, ActiveStreamsEvaluator activeStreamsEvaluator) {
		this.activeStreamsEvaluator = activeStreamsEvaluator;
		this.config = config;
		this.rtcStatsBiTransformer = this.makeRTCStatsEvaluator();
	}

	@Override
	public void init(ProcessorContext context) {
		this.context = context;
		this.activeStreamsEvaluator.init(context);
		int updatePeriodInS = this.activeStreamsEvaluator.getUpdatePeriodInS();
		context.schedule(Duration.ofSeconds(updatePeriodInS), PunctuationType.WALL_CLOCK_TIME, this.activeStreamsEvaluator);
	}

	@Override
	public void close() {

	}

	@Override
	public KeyValue<UUID, Report> transform(UUID peerConnectionUUID, WebExtrAppSample sample) {
		Iterator<RTCStats> it = WebExtrAppSampleIteratorProvider.RTCStatsIt(sample);
		for (; it.hasNext(); ) {
			RTCStats rtcStatsItem = it.next();
			this.activeStreamsEvaluator.accept(sample, rtcStatsItem);
			Report report = this.rtcStatsBiTransformer.transform(rtcStatsItem, sample);
			if (report != null) {
				this.context.forward(sample.observerUUID, report);
			}
		}
		return null;
	}

	private RTCStatsBiTransformer<WebExtrAppSample, Report> makeRTCStatsEvaluator() {
		final BiFunction<WebExtrAppSample, RTCStats, Report> inboundRTPReportTransformer = this.makeInboundRTPReportTransformer();
		final BiFunction<WebExtrAppSample, RTCStats, Report> outboundRTPReportTransformer = this.makeOutboundRTPReportTransformer();
		final BiFunction<WebExtrAppSample, RTCStats, Report> remoteInboundRTPReportTransformer = this.makeRemoteInboundRTPReportTransformer();

		return new RTCStatsBiTransformer<WebExtrAppSample, Report>() {
			@Override
			public Report processInboundRTP(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
				return inboundRTPReportTransformer.apply(webExtrAppSample, rtcStats);
			}

			@Override
			public Report processOutboundRTP(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
				return outboundRTPReportTransformer.apply(webExtrAppSample, rtcStats);
			}

			@Override
			public Report processRemoteInboundRTP(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
				return remoteInboundRTPReportTransformer.apply(webExtrAppSample, rtcStats);
			}

			@Override
			public Report processTrack(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
				return null;
			}

			@Override
			public Report processMediaSource(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
				return null;
			}

			@Override
			public Report processCandidatePair(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
				return null;
			}
		};
	}

	private BiFunction<WebExtrAppSample, RTCStats, Report> makeOutboundRTPReportTransformer() {
		BiFunction<WebExtrAppSample, RTCStats, Report> result = (webExtrAppSample, rtcStats) -> null;
		if (!this.config.reportOutboundRTP) {
			return result;
		}
		BiFunction<WebExtrAppSample, RTCStats, MediaStreamSample> mediaStreamSampleConverter = this.makeMediaStreamConverter();
		OutboundRTPConverter reportConverter = new OutboundRTPConverter();
		result = (webExtrAppSample, rtcStats) -> {
			MediaStreamSample mediaStreamSample = mediaStreamSampleConverter.apply(webExtrAppSample, rtcStats);
			return reportConverter.apply(mediaStreamSample);
		};
		return result;
	}

	private BiFunction<WebExtrAppSample, RTCStats, Report> makeInboundRTPReportTransformer() {
		BiFunction<WebExtrAppSample, RTCStats, Report> result = (webExtrAppSample, rtcStats) -> null;
		if (!this.config.reportInboundRTP) {
			return result;
		}
		BiFunction<WebExtrAppSample, RTCStats, MediaStreamSample> mediaStreamSampleConverter = this.makeMediaStreamConverter();
		InboundRTPConverter reportConverter = new InboundRTPConverter();
		result = (webExtrAppSample, rtcStats) -> {
			MediaStreamSample mediaStreamSample = mediaStreamSampleConverter.apply(webExtrAppSample, rtcStats);
			return reportConverter.apply(mediaStreamSample);
		};
		return result;
	}

	private BiFunction<WebExtrAppSample, RTCStats, Report> makeRemoteInboundRTPReportTransformer() {
		BiFunction<WebExtrAppSample, RTCStats, Report> result = (webExtrAppSample, rtcStats) -> null;
		if (!this.config.reportRemoteInboundRTP) {
			return result;
		}
		BiFunction<WebExtrAppSample, RTCStats, MediaStreamSample> mediaStreamSampleConverter = this.makeMediaStreamConverter();
		RemoteInboundRTPConverter reportConverter = new RemoteInboundRTPConverter();
		result = (webExtrAppSample, rtcStats) -> {
			MediaStreamSample mediaStreamSample = mediaStreamSampleConverter.apply(webExtrAppSample, rtcStats);
			return reportConverter.apply(mediaStreamSample);
		};
		return result;
	}

	private BiFunction<WebExtrAppSample, RTCStats, MediaStreamSample> makeMediaStreamConverter() {
		return new BiFunction<WebExtrAppSample, RTCStats, MediaStreamSample>() {
			@Override
			public MediaStreamSample apply(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
				MediaStreamSample result = MediaStreamSample.of(
						webExtrAppSample.observerUUID,
						webExtrAppSample.peerConnectionUUID,
						webExtrAppSample.peerConnectionSample.getBrowserId(),
						webExtrAppSample.sampleTimeZoneID,
						rtcStats,
						webExtrAppSample.timestamp
				);
				return result;
			}
		};
	}

}
