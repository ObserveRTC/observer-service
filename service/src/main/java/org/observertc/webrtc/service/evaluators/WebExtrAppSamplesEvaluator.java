package org.observertc.webrtc.service.evaluators;

import io.micronaut.context.annotation.Prototype;
import java.util.Iterator;
import java.util.UUID;
import java.util.function.BiFunction;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.observertc.webrtc.common.reports.ICECandidatePairReport;
import org.observertc.webrtc.common.reports.ICELocalCandidateReport;
import org.observertc.webrtc.common.reports.ICERemoteCandidateReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.service.EvaluatorsConfig;
import org.observertc.webrtc.service.dto.ICEStatsBiConsumer;
import org.observertc.webrtc.service.dto.RTCStatsBiTransformer;
import org.observertc.webrtc.service.dto.webextrapp.CandidatePair;
import org.observertc.webrtc.service.dto.webextrapp.LocalCandidate;
import org.observertc.webrtc.service.dto.webextrapp.ObserveRTCCIceStats;
import org.observertc.webrtc.service.dto.webextrapp.RTCStats;
import org.observertc.webrtc.service.dto.webextrapp.RemoteCandidate;
import org.observertc.webrtc.service.evaluators.valueadapters.CandidatePairStateConverter;
import org.observertc.webrtc.service.evaluators.valueadapters.InboundRTPConverter;
import org.observertc.webrtc.service.evaluators.valueadapters.MediaSourceConverter;
import org.observertc.webrtc.service.evaluators.valueadapters.NetworkTypeConverter;
import org.observertc.webrtc.service.evaluators.valueadapters.NumberConverter;
import org.observertc.webrtc.service.evaluators.valueadapters.OutboundRTPConverter;
import org.observertc.webrtc.service.evaluators.valueadapters.ProtocolTypeConverter;
import org.observertc.webrtc.service.evaluators.valueadapters.RemoteInboundRTPConverter;
import org.observertc.webrtc.service.evaluators.valueadapters.TrackConverter;
import org.observertc.webrtc.service.samples.MediaStreamSample;
import org.observertc.webrtc.service.samples.WebExtrAppSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class WebExtrAppSamplesEvaluator implements Transformer<UUID, WebExtrAppSample, KeyValue<UUID, Report>> {

	private static final Logger logger = LoggerFactory.getLogger(WebExtrAppSamplesEvaluator.class);
	private final RTCStatsBiTransformer<WebExtrAppSample, Report> rtcStatsBiTransformer;
	private final ICEStatsBiConsumer<WebExtrAppSample> iceStatsBiConsumer;
	private ProcessorContext context;
	private final EvaluatorsConfig.RTCStatsConfig config;

	public WebExtrAppSamplesEvaluator(EvaluatorsConfig.RTCStatsConfig config) {
		this.config = config;
		this.rtcStatsBiTransformer = this.makeRTCStatsEvaluator();
		this.iceStatsBiConsumer = this.makeICEStatsEvaluator();
	}

	@Override
	public void init(ProcessorContext context) {
		this.context = context;
	}

	@Override
	public void close() {

	}

	@Override
	public KeyValue<UUID, Report> transform(UUID peerConnectionUUID, WebExtrAppSample sample) {
		Iterator<RTCStats> it = WebExtrAppSampleIteratorProvider.RTCStatsIt(sample);
		for (; it.hasNext(); ) {
			RTCStats rtcStatsItem = it.next();
			Report report = this.rtcStatsBiTransformer.transform(rtcStatsItem, sample);
			if (report != null) {
				this.context.forward(sample.observerUUID, report);
			}
		}
		ObserveRTCCIceStats iceStats = sample.peerConnectionSample.getIceStats();
		this.iceStatsBiConsumer.accept(iceStats, sample);
		return null;
	}

	private RTCStatsBiTransformer<WebExtrAppSample, Report> makeRTCStatsEvaluator() {
		final BiFunction<WebExtrAppSample, RTCStats, Report> inboundRTPReportTransformer = this.makeInboundRTPReportTransformer();
		final BiFunction<WebExtrAppSample, RTCStats, Report> outboundRTPReportTransformer = this.makeOutboundRTPReportTransformer();
		final BiFunction<WebExtrAppSample, RTCStats, Report> remoteInboundRTPReportTransformer = this.makeRemoteInboundRTPReportTransformer();
		final BiFunction<WebExtrAppSample, RTCStats, Report> mediaSourceReportTransformer = this.makeMediaSourceReportTransformer();
		final BiFunction<WebExtrAppSample, RTCStats, Report> trackReportTransformer = this.makeTrackReportTransformer();

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
				return trackReportTransformer.apply(webExtrAppSample, rtcStats);
			}

			@Override
			public Report processMediaSource(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
				return mediaSourceReportTransformer.apply(webExtrAppSample, rtcStats);
			}

			@Override
			public Report processCandidatePair(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
				return null;
			}
		};
	}

	private ICEStatsBiConsumer<WebExtrAppSample> makeICEStatsEvaluator() {
		return new ICEStatsBiConsumer<WebExtrAppSample>() {
			@Override
			public void processRemoteCandidate(WebExtrAppSample sample, RemoteCandidate remoteCandidate) {
				Report iceRemoteCandidateReport = ICERemoteCandidateReport.of(sample.observerUUID,
						sample.peerConnectionUUID,
						sample.timestamp,
						remoteCandidate.getDeleted(),
						null, // Add IP LSH later!
						NumberConverter.toInt(remoteCandidate.getPort()),
						NumberConverter.toLong(remoteCandidate.getPriority()),
						ProtocolTypeConverter.fromDTOProtocolType(remoteCandidate.getProtocol())
				);
				context.forward(sample.observerUUID, iceRemoteCandidateReport);
			}

			@Override
			public void processLocalCandidate(WebExtrAppSample sample, LocalCandidate localCandidate) {
				Report iceLocalCandidateReport = ICELocalCandidateReport.of(sample.observerUUID,
						sample.peerConnectionUUID,
						sample.timestamp,
						localCandidate.getDeleted(),
						null, // Add IP LSH later!
						NetworkTypeConverter.fromDTONetworkType(localCandidate.getNetworkType()),
						NumberConverter.toInt(localCandidate.getPort()),
						NumberConverter.toLong(localCandidate.getPriority()),
						ProtocolTypeConverter.fromDTOProtocolType(localCandidate.getProtocol())
				);
				context.forward(sample.observerUUID, iceLocalCandidateReport);
			}

			@Override
			public void processCandidatePair(WebExtrAppSample sample, CandidatePair candidatePair) {
				Report iceCandidatePairReport = ICECandidatePairReport.of(sample.observerUUID,
						sample.peerConnectionUUID,
						sample.timestamp,
						candidatePair.getNominated(),
						NumberConverter.toInt(candidatePair.getAvailableOutgoingBitrate()),
						NumberConverter.toInt(candidatePair.getBytesReceived()),
						NumberConverter.toInt(candidatePair.getBytesSent()),
						NumberConverter.toInt(candidatePair.getConsentRequestsSent()),
						candidatePair.getCurrentRoundTripTime(),
						NumberConverter.toInt(candidatePair.getPriority()),
						NumberConverter.toInt(candidatePair.getRequestsReceived()),
						NumberConverter.toInt(candidatePair.getRequestsSent()),
						NumberConverter.toInt(candidatePair.getResponsesReceived()),
						NumberConverter.toInt(candidatePair.getResponsesSent()),
						CandidatePairStateConverter.fromState(candidatePair.getState()),
						candidatePair.getTotalRoundTripTime(),
						candidatePair.getWritable()
				);
				context.forward(sample.observerUUID, iceCandidatePairReport);
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

	private BiFunction<WebExtrAppSample, RTCStats, Report> makeMediaSourceReportTransformer() {
		BiFunction<WebExtrAppSample, RTCStats, Report> result = (webExtrAppSample, rtcStats) -> null;
		if (!this.config.reportMediaSource) {
			return result;
		}
		BiFunction<WebExtrAppSample, RTCStats, MediaStreamSample> mediaStreamSampleConverter = this.makeMediaStreamConverter();
		MediaSourceConverter reportConverter = new MediaSourceConverter();
		result = (webExtrAppSample, rtcStats) -> {
			MediaStreamSample mediaStreamSample = mediaStreamSampleConverter.apply(webExtrAppSample, rtcStats);
			return reportConverter.apply(mediaStreamSample);
		};
		return result;
	}

	private BiFunction<WebExtrAppSample, RTCStats, Report> makeTrackReportTransformer() {
		BiFunction<WebExtrAppSample, RTCStats, Report> result = (webExtrAppSample, rtcStats) -> null;
		if (!this.config.reportTracks) {
			return result;
		}
		BiFunction<WebExtrAppSample, RTCStats, MediaStreamSample> mediaStreamSampleConverter = this.makeMediaStreamConverter();
		TrackConverter reportConverter = new TrackConverter();
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
