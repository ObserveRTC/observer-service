package org.observertc.webrtc.observer.evaluators;

import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Prototype;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import org.observertc.webrtc.common.reports.ICECandidatePairReport;
import org.observertc.webrtc.common.reports.ICELocalCandidateReport;
import org.observertc.webrtc.common.reports.ICERemoteCandidateReport;
import org.observertc.webrtc.common.reports.Report;
import org.observertc.webrtc.observer.EvaluatorsConfig;
import org.observertc.webrtc.observer.KafkaSinks;
import org.observertc.webrtc.observer.dto.ICEStatsBiConsumer;
import org.observertc.webrtc.observer.dto.RTCStatsBiTransformer;
import org.observertc.webrtc.observer.dto.webextrapp.CandidatePair;
import org.observertc.webrtc.observer.dto.webextrapp.LocalCandidate;
import org.observertc.webrtc.observer.dto.webextrapp.ObserveRTCCIceStats;
import org.observertc.webrtc.observer.dto.webextrapp.RTCStats;
import org.observertc.webrtc.observer.dto.webextrapp.RemoteCandidate;
import org.observertc.webrtc.observer.evaluators.ipflags.IPAddressConverter;
import org.observertc.webrtc.observer.evaluators.ipflags.IPFlags;
import org.observertc.webrtc.observer.evaluators.valueadapters.CandidateNetworkTypeConverter;
import org.observertc.webrtc.observer.evaluators.valueadapters.CandidatePairStateConverter;
import org.observertc.webrtc.observer.evaluators.valueadapters.InboundRTPConverter;
import org.observertc.webrtc.observer.evaluators.valueadapters.MediaSourceConverter;
import org.observertc.webrtc.observer.evaluators.valueadapters.NumberConverter;
import org.observertc.webrtc.observer.evaluators.valueadapters.OutboundRTPConverter;
import org.observertc.webrtc.observer.evaluators.valueadapters.ProtocolTypeConverter;
import org.observertc.webrtc.observer.evaluators.valueadapters.RemoteInboundRTPConverter;
import org.observertc.webrtc.observer.evaluators.valueadapters.TrackConverter;
import org.observertc.webrtc.observer.samples.MediaStreamSample;
import org.observertc.webrtc.observer.samples.WebExtrAppSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KafkaListener(
		groupId = "observertc-webrtc-observer-WebExtrAppSamplesEvaluator",
		threads = 4
)
@Prototype
public class WebExtrAppSamplesEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(WebExtrAppSamplesEvaluator.class);
	private final RTCStatsBiTransformer<WebExtrAppSample, Report> rtcStatsBiTransformer;
	private final ICEStatsBiConsumer<WebExtrAppSample> iceStatsBiConsumer;
	private final EvaluatorsConfig.SampleTransformerConfig config;
	private final SentReportsChecker sentReportsChecker;
	private final IPFlags ipFlags;
	private final IPAddressConverter ipAddressConverter;

	private final KafkaSinks kafkaSinks;

	public WebExtrAppSamplesEvaluator(EvaluatorsConfig.SampleTransformerConfig config,
									  SentReportsChecker sentReportsChecker,
									  KafkaSinks kafkaSinks,
									  IPFlags ipFlags) {
		this.config = config;
		this.rtcStatsBiTransformer = this.makeRTCStatsEvaluator();
		this.iceStatsBiConsumer = this.makeICEStatsEvaluator();
		this.sentReportsChecker = sentReportsChecker;
		this.ipFlags = ipFlags;
		this.ipAddressConverter = new IPAddressConverter();
		this.kafkaSinks = kafkaSinks;
	}


	@Topic("${kafkaTopics.webExtrAppSamples.topicName}")
	public void receive(@KafkaKey UUID peerConnectionUUID, WebExtrAppSample sample) {
		Iterator<RTCStats> it = WebExtrAppSampleIteratorProvider.RTCStatsIt(sample);
		for (; it.hasNext(); ) {
			RTCStats rtcStatsItem = it.next();
			Report report = this.rtcStatsBiTransformer.transform(rtcStatsItem, sample);
			if (report != null) {
				kafkaSinks.sendReport(sample.observerUUID, report);
			}
		}
		ObserveRTCCIceStats iceStats = sample.peerConnectionSample.getIceStats();
		this.iceStatsBiConsumer.accept(iceStats, sample);
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
				Report result = mediaSourceReportTransformer.apply(webExtrAppSample, rtcStats);
//				if (!sentReportsChecker.isSent(result)) {
//					return result;
//				} else {
//					return null;
//				}
				return result;
			}

			@Override
			public Report processCandidatePair(WebExtrAppSample webExtrAppSample, RTCStats rtcStats) {
				return null;
			}
		};
	}

	private ICEStatsBiConsumer<WebExtrAppSample> makeICEStatsEvaluator() {
		final BiConsumer<WebExtrAppSample, CandidatePair> candidatePairReporter = this.makeCandidatePairReporter();
		final BiConsumer<WebExtrAppSample, LocalCandidate> localCandidateReporter = this.makeLocalCandidateReporter();
		final BiConsumer<WebExtrAppSample, RemoteCandidate> remoteCandidateReporter = this.makeRemoteCandidateReporter();
		return new ICEStatsBiConsumer<WebExtrAppSample>() {
			@Override
			public void processRemoteCandidate(WebExtrAppSample sample, RemoteCandidate remoteCandidate) {
				remoteCandidateReporter.accept(sample, remoteCandidate);
			}

			@Override
			public void processLocalCandidate(WebExtrAppSample sample, LocalCandidate localCandidate) {
				localCandidateReporter.accept(sample, localCandidate);
			}

			@Override
			public void processCandidatePair(WebExtrAppSample sample, CandidatePair candidatePair) {
				candidatePairReporter.accept(sample, candidatePair);
			}
		};
	}

	private String getIPFlag(String ip) {
		if (ip == null) {
			return null;
		}
		Optional<InetAddress> inetAddressHolder = this.ipAddressConverter.apply(ip);
		if (!inetAddressHolder.isPresent()) {
			return null;
		}
		InetAddress inetAddress = inetAddressHolder.get();
		Optional<String> ipFlagHolder = this.ipFlags.apply(inetAddress);
		if (!ipFlagHolder.isPresent()) {
			return null;
		}
		return ipFlagHolder.get();
	}

	private BiConsumer<WebExtrAppSample, RemoteCandidate> makeRemoteCandidateReporter() {
		if (!this.config.reportRemoteCandidates) {
			return (webExtrAppSample, remoteCandidate) -> {

			};
		}
		BiConsumer<WebExtrAppSample, RemoteCandidate> result = (sample, remoteCandidate) -> {
			String ipFlag = getIPFlag(remoteCandidate.getIP());
			Report iceRemoteCandidateReport = ICERemoteCandidateReport.of(sample.observerUUID,
					sample.peerConnectionUUID,
					remoteCandidate.getID(),
					sample.timestamp,
					remoteCandidate.getDeleted(),
					null, // Add IP LSH later!
					ipFlag,
					NumberConverter.toInt(remoteCandidate.getPort()),
					NumberConverter.toLong(remoteCandidate.getPriority()),
					ProtocolTypeConverter.fromDTOProtocolType(remoteCandidate.getProtocol())
			);
			if (!sentReportsChecker.isSent(iceRemoteCandidateReport)) {
				kafkaSinks.sendReport(sample.observerUUID, iceRemoteCandidateReport);
			}
		};
		return result;
	}

	private BiConsumer<WebExtrAppSample, LocalCandidate> makeLocalCandidateReporter() {
		if (!this.config.reportLocalCandidates) {
			return (webExtrAppSample, localCandidate) -> {

			};
		}

		BiConsumer<WebExtrAppSample, LocalCandidate> result = (sample, localCandidate) -> {
			String ipFlag = getIPFlag(localCandidate.getIP());
			Report iceLocalCandidateReport = ICELocalCandidateReport.of(sample.observerUUID,
					sample.peerConnectionUUID,
					localCandidate.getID(),
					sample.timestamp,
					localCandidate.getDeleted(),
					null, // Add IP LSH later!
					ipFlag,
					CandidateNetworkTypeConverter.fromDTONetworkType(localCandidate.getNetworkType()),
					NumberConverter.toInt(localCandidate.getPort()),
					NumberConverter.toLong(localCandidate.getPriority()),
					ProtocolTypeConverter.fromDTOProtocolType(localCandidate.getProtocol())
			);
			if (!sentReportsChecker.isSent(iceLocalCandidateReport)) {
				kafkaSinks.sendReport(sample.observerUUID, iceLocalCandidateReport);
			}
		};
		return result;
	}

	private BiConsumer<WebExtrAppSample, CandidatePair> makeCandidatePairReporter() {
		if (!this.config.reportCandidatePairs) {
			return (webExtrAppSample, candidatePair) -> {

			};
		}
		BiConsumer<WebExtrAppSample, CandidatePair> result = (sample, candidatePair) -> {
			Report iceCandidatePairReport = ICECandidatePairReport.of(sample.observerUUID,
					sample.peerConnectionUUID,
					candidatePair.getID(),
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
			if (!sentReportsChecker.isSent(iceCandidatePairReport)) {
				kafkaSinks.sendReport(sample.observerUUID, iceCandidatePairReport);
			}
		};
		return result;
	}

	private BiFunction<WebExtrAppSample, RTCStats, Report> makeOutboundRTPReportTransformer() {
		BiFunction<WebExtrAppSample, RTCStats, Report> result = (webExtrAppSample, rtcStats) -> null;
		if (!this.config.reportOutboundRTPs) {
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
		if (!this.config.reportInboundRTPs) {
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
		if (!this.config.reportRemoteInboundRTPs) {
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
		if (!this.config.reportMediaSources) {
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
