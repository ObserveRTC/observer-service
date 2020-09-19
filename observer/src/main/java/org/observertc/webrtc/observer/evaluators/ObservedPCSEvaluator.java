package org.observertc.webrtc.observer.evaluators;

import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Prototype;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.observertc.webrtc.common.reports.avro.ICECandidatePair;
import org.observertc.webrtc.common.reports.avro.ICELocalCandidate;
import org.observertc.webrtc.common.reports.avro.ICERemoteCandidate;
import org.observertc.webrtc.common.reports.avro.InboundRTP;
import org.observertc.webrtc.common.reports.avro.OutboundRTP;
import org.observertc.webrtc.common.reports.avro.RemoteInboundRTP;
import org.observertc.webrtc.common.reports.avro.Report;
import org.observertc.webrtc.observer.EvaluatorsConfig;
import org.observertc.webrtc.observer.KafkaSinks;
import org.observertc.webrtc.observer.ReportSink;
import org.observertc.webrtc.observer.dto.PeerConnectionSampleVisitor;
import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.evaluators.valueadapters.EnumConverter;
import org.observertc.webrtc.observer.evaluators.valueadapters.NumberConverter;
import org.observertc.webrtc.observer.repositories.SentReportsRepository;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@KafkaListener(
		groupId = "observertc-webrtc-observer-ObservedPCSEvaluator",
		threads = 4
)
@Prototype
public class ObservedPCSEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(ObservedPCSEvaluator.class);
	private final EvaluatorsConfig.SampleTransformerConfig config;
	private final PeerConnectionSampleVisitor<ObservedPCS> processor;
	private final ReportSink reportSink;
	private final EnumConverter enumConverter;
	private final NumberConverter numberConverter;
	private final SignatureMaker signatureMaker;
	private final SentReportsRepository sentReportsRepository;

	public ObservedPCSEvaluator(EvaluatorsConfig.SampleTransformerConfig config,
								EnumConverter enumConverter,
								NumberConverter numberConverter,
								SignatureMaker signatureMaker,
								SentReportsRepository sentReportsRepository,
								ReportSink reportSink,
								KafkaSinks kafkaSinks) {
		this.config = config;
		this.reportSink = reportSink;
		this.enumConverter = enumConverter;
		this.numberConverter = numberConverter;
		this.signatureMaker = signatureMaker;
		this.sentReportsRepository = sentReportsRepository;
		this.processor = this.makeProcessor();

	}


	@Topic("${kafkaTopics.observedPCS.topicName}")
	public void receive(@KafkaKey UUID peerConnectionUUID, ObservedPCS sample) {
		this.processor.accept(sample, sample.peerConnectionSample);
	}

	private PeerConnectionSampleVisitor<ObservedPCS> makeProcessor() {
		final BiConsumer<ObservedPCS, PeerConnectionSample.ICECandidatePair> ICECandidatePairReporter = this.makeICECandidatePairReporter();
		final BiConsumer<ObservedPCS, PeerConnectionSample.ICELocalCandidate> ICELocalCandidateReporter =
				this.makeICELocalCandidateReporter();
		final BiConsumer<ObservedPCS, PeerConnectionSample.ICERemoteCandidate> ICERemoteCandidateReporter =
				this.makeICERemoteCandidateReporter();
		final BiConsumer<ObservedPCS, PeerConnectionSample.InboundRTPStreamStats> inboundRTPReporter = this.makeInboundRTPReporter();
		final BiConsumer<ObservedPCS, PeerConnectionSample.RemoteInboundRTPStreamStats> remoteInboundRTPReporter =
				this.makeRemoteInboundRTPReporter();
		final BiConsumer<ObservedPCS, PeerConnectionSample.OutboundRTPStreamStats> outboundRTPReporter = this.makeOutboundRTPReporter();

		return new PeerConnectionSampleVisitor<ObservedPCS>() {
			@Override
			public void visitRemoteInboundRTP(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.RemoteInboundRTPStreamStats subject) {
				remoteInboundRTPReporter.accept(obj, subject);
			}

			@Override
			public void visitInboundRTP(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.InboundRTPStreamStats subject) {
				inboundRTPReporter.accept(obj, subject);
			}

			@Override
			public void visitOutboundRTP(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.OutboundRTPStreamStats subject) {
				outboundRTPReporter.accept(obj, subject);
			}

			@Override
			public void visitICECandidatePair(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.ICECandidatePair subject) {
				ICECandidatePairReporter.accept(obj, subject);
			}

			@Override
			public void visitICELocalCandidate(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.ICELocalCandidate subject) {
				ICELocalCandidateReporter.accept(obj, subject);
			}

			@Override
			public void visitICERemoteCandidate(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.ICERemoteCandidate subject) {
				ICERemoteCandidateReporter.accept(obj, subject);
			}
		};
	}

	private BiConsumer<ObservedPCS, PeerConnectionSample.ICECandidatePair> makeICECandidatePairReporter() {
		if (!this.config.reportCandidatePairs) {
			return (observedPCS, subject) -> {

			};
		}
		return (observedPCS, subject) -> {
			PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
			byte[] signature = signatureMaker.makeSignature(peerConnectionSample.peerConnectionID, subject.id, subject.transportID);
			if (sentReportsRepository.existsBySignature(signature)) {
				return;
			}
			ICECandidatePair candidatePair = ICECandidatePair.newBuilder()
					.setPeerConnectionUUID(peerConnectionSample.peerConnectionID)
					.setMediaUnit(observedPCS.mediaUnit)
					.setAvailableOutgoingBitrate(subject.availableOutgoingBitrate)
					.setBytesReceived(subject.bytesReceived)
					.setBytesSent(subject.bytesSent)
					.setConsentRequestsSent(subject.consentRequestsSent)
					.setCurrentRoundTripTime(subject.currentRoundTripTime)
					.setId(subject.id)
					.setLocalCandidateID(subject.localCandidateID)
					.setNominated(subject.nominated)
					.setPriority(subject.priority)
					.setRemoteCandidateID(subject.remoteCandidateID)
					.setRequestsReceived(subject.requestsReceived)
					.setRequestsSent(subject.requestsSent)
					.setResponsesReceived(subject.responsesReceived)
					.setResponsesSent(subject.responsesSent)
					.setState(enumConverter.toICEState(subject.state))
					.setTotalRoundTripTime(subject.totalRoundTripTime)
					.setTransportID(subject.transportID)
					.setWritable(subject.writable)
					.build();
			sendReport(observedPCS, candidatePair);
		};
	}

	private BiConsumer<ObservedPCS, PeerConnectionSample.ICELocalCandidate> makeICELocalCandidateReporter() {
		if (!this.config.reportLocalCandidates) {
			return (observedPCS, subject) -> {

			};
		}

		return (observedPCS, subject) -> {
			PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
			byte[] signature = signatureMaker.makeSignature(peerConnectionSample.peerConnectionID, subject.id, subject.transportID);
			if (sentReportsRepository.existsBySignature(signature)) {
				return;
			}

			ICELocalCandidate localCandidate = ICELocalCandidate.newBuilder()
					.setPeerConnectionUUID(peerConnectionSample.peerConnectionID)
					.setMediaUnit(observedPCS.mediaUnit)
					.setCandidateType(enumConverter.toCandidateType(subject.candidateType))
					.setDeleted(subject.deleted)
					.setId(subject.id)
					.setIp(subject.ip)
					.setIsRemote(subject.isRemote)
					.setPort(subject.port)
					.setPriority(subject.priority)
					.setNetworkType(enumConverter.toNetworkType(subject.protocol))
					.setTransportID(subject.transportID)
					.build();
			sendReport(observedPCS, localCandidate);
		};
	}

	private BiConsumer<ObservedPCS, PeerConnectionSample.ICERemoteCandidate> makeICERemoteCandidateReporter() {
		if (!this.config.reportRemoteCandidates) {
			return (observedPCS, subject) -> {

			};
		}
		return (observedPCS, subject) -> {
			PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
			byte[] signature = signatureMaker.makeSignature(peerConnectionSample.peerConnectionID, subject.id, subject.transportID);
			if (sentReportsRepository.existsBySignature(signature)) {
				return;
			}
			ICERemoteCandidate remoteCandidate = ICERemoteCandidate.newBuilder()
					.setPeerConnectionUUID(peerConnectionSample.peerConnectionID)
					.setMediaUnit(observedPCS.mediaUnit)
					.setCandidateType(enumConverter.toCandidateType(subject.candidateType))
					.setDeleted(subject.deleted)
					.setId(subject.id)
					.setIp(subject.ip)
					.setIsRemote(subject.isRemote)
					.setPort(subject.port)
					.setPriority(subject.priority)
					.setProtocol(enumConverter.toInternetProtocol(subject.protocol))
					.setTransportID(subject.transportID)
					.build();
			sendReport(observedPCS, remoteCandidate);
		};
	}

	private BiConsumer<ObservedPCS, PeerConnectionSample.RemoteInboundRTPStreamStats> makeRemoteInboundRTPReporter() {
		if (!this.config.reportRemoteInboundRTPs) {
			return (observedPCS, subject) -> {

			};
		}
		return (observedPCS, subject) -> {
			PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
			RemoteInboundRTP remoteInboundRTP = RemoteInboundRTP.newBuilder()
					.setPeerConnectionUUID(peerConnectionSample.peerConnectionID)
					.setMediaUnit(observedPCS.mediaUnit)
					.setCodecID(subject.codecID)
					.setId(subject.id)
					.setJitter(numberConverter.toFloat(subject.jitter))
					.setLocalID(subject.localID)
					.setMediaType(enumConverter.toReportMediaType(subject.mediaType))
					.setPacketsLost(subject.packetsLost)
					.setRoundTripTime(subject.roundTripTime)
					.setSsrc(subject.ssrc)
					.setTransportID(subject.transportID)
					.build();
			sendReport(observedPCS, remoteInboundRTP);
		};
	}

	private BiConsumer<ObservedPCS, PeerConnectionSample.InboundRTPStreamStats> makeInboundRTPReporter() {
		if (!this.config.reportInboundRTPs) {
			return (observedPCS, subject) -> {

			};
		}
		return (observedPCS, subject) -> {
			PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
			InboundRTP inboundRTP = InboundRTP.newBuilder()
					.setPeerConnectionUUID(peerConnectionSample.peerConnectionID)
					.setMediaUnit(observedPCS.mediaUnit)
					.setBytesReceived(subject.bytesReceived)
					.setCodecId(subject.codecId)
					.setDecoderImplementation(subject.decoderImplementation)
					.setEstimatedPlayoutTimestamp(subject.estimatedPlayoutTimestamp)
					.setFecPacketsDiscarded(subject.fecPacketsDiscarded)
					.setFecPacketsReceived(subject.fecPacketsReceived)
					.setFirCount(subject.firCount)
					.setFramesDecoded(subject.framesDecoded)
					.setHeaderBytesReceived(subject.headerBytesReceived)
					.setId(subject.id)
					.setIsRemote(subject.isRemote)
					.setJitter(subject.jitter)
					.setKeyFramesDecoded(subject.keyFramesDecoded)
					.setLastPacketReceivedTimestamp(subject.lastPacketReceivedTimestamp)
					.setMediaType(enumConverter.toReportMediaType(subject.mediaType))
					.setNackCount(subject.nackCount)
					.setPacketsLost(subject.packetsLost)
					.setPacketsReceived(subject.packetsReceived)
					.setPliCount(subject.pliCount)
					.setQpSum(subject.qpSum)
					.setSsrc(subject.ssrc)
					.setTotalDecodeTime(subject.totalDecodeTime)
					.setTotalInterFrameDelay(subject.totalInterFrameDelay)
					.setTotalSquaredInterFrameDelay(subject.totalSquaredInterFrameDelay)
					.setTrackId(subject.trackId)
					.setTransportId(subject.transportId)
					.build();
			sendReport(observedPCS, inboundRTP);
		};
	}

	private BiConsumer<ObservedPCS, PeerConnectionSample.OutboundRTPStreamStats> makeOutboundRTPReporter() {
		if (!this.config.reportOutboundRTPs) {
			return (observedPCS, subject) -> {

			};
		}

		return (observedPCS, subject) -> {
			PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
			OutboundRTP outboundRTP = OutboundRTP.newBuilder()
					.setPeerConnectionUUID(observedPCS.peerConnectionUUID.toString())
					.setMediaUnit(observedPCS.mediaUnit)
					.setBytesSent(subject.bytesSent)
					.setCodecID(subject.codecID)
					.setEncoderImplementation(subject.encoderImplementation)
					.setFirCount(subject.firCount)
					.setFramesEncoded(subject.framesEncoded)
					.setHeaderBytesSent(subject.headerBytesSent)
					.setId(subject.id)
					.setIsRemote(subject.isRemote)
					.setKeyFramesEncoded(subject.keyFramesEncoded)
					.setMediaSourceID(subject.mediaSourceID)
					.setMediaType(enumConverter.toReportMediaType(subject.mediaType))
					.setNackCount(subject.nackCount)
					.setPacketsSent(subject.packetsSent)
					.setPliCount(subject.pliCount)
					.setQpSum(subject.qpSum)
					.setQualityLimitationReason(enumConverter.toQualityLimitationReason(subject.qualityLimitationReason))
					.setQualityLimitationResolutionChanges(subject.qualityLimitationResolutionChanges)
					.setRemoteID(subject.remoteID)
					.setRetransmittedBytesSent(subject.retransmittedBytesSent)
					.setRetransmittedPacketsSent(subject.retransmittedPacketsSent)
					.setTotalEncodedBytesTarget(subject.totalEncodedBytesTarget)
					.setTotalEncodeTime(subject.totalEncodeTime)
					.setTotalPacketSendDelay(subject.totalPacketSendDelay)
					.setTrackID(subject.trackID)
					.setTransportID(subject.transportID)
					.build();
			sendReport(observedPCS, outboundRTP);
		};
	}

	private void sendReport(ObservedPCS observedPCS, Object payload) {
		DatumWriter<Report> writer = new SpecificDatumWriter<>(Report.class);
		
		Report report;
		byte[] data;
//		ByteArrayOutputStream stream = new ByteArrayOutputStream();
//		BinaryMessageEncoder<Report> encoder = Report.getEncoder();
//		try {
////			jsonEncoder = EncoderFactory.get().jsonEncoder(
////					Report.getClassSchema(), stream);
//			writer.write(report, encoder);
//			jsonEncoder.flush();
//			data = stream.toByteArray();
//		} catch (IOException e) {
//			logger.error("Serialization error:" + e.getMessage());
//		}
	}
}
