package org.observertc.webrtc.service.evaluators;

import io.micronaut.context.annotation.Prototype;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
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
import org.observertc.webrtc.service.evaluators.ipflags.IPAddressConverter;
import org.observertc.webrtc.service.evaluators.ipflags.IPFlags;
import org.observertc.webrtc.service.evaluators.valueadapters.CandidateNetworkTypeConverter;
import org.observertc.webrtc.service.evaluators.valueadapters.CandidatePairStateConverter;
import org.observertc.webrtc.service.evaluators.valueadapters.InboundRTPConverter;
import org.observertc.webrtc.service.evaluators.valueadapters.MediaSourceConverter;
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
	private final EvaluatorsConfig.SampleTransformerConfig config;
	private final SentReportsChecker sentReportsChecker;
	private final IPFlags ipFlags;
	private final IPAddressConverter ipAddressConverter;

	public WebExtrAppSamplesEvaluator(EvaluatorsConfig.SampleTransformerConfig config,
									  SentReportsChecker sentReportsChecker,
									  IPFlags ipFlags) {
		this.config = config;
		this.rtcStatsBiTransformer = this.makeRTCStatsEvaluator();
		this.iceStatsBiConsumer = this.makeICEStatsEvaluator();
		this.sentReportsChecker = sentReportsChecker;
		this.ipFlags = ipFlags;
		this.ipAddressConverter = new IPAddressConverter();
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
				Report result = mediaSourceReportTransformer.apply(webExtrAppSample, rtcStats);
				if (!sentReportsChecker.isSent(result)) {
					return result;
				} else {
					return null;
				}
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
				context.forward(sample.observerUUID, iceRemoteCandidateReport);
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
				context.forward(sample.observerUUID, iceLocalCandidateReport);
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
				context.forward(sample.observerUUID, iceCandidatePairReport);
			}
		};
		return result;
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
