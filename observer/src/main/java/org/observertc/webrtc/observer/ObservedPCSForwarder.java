/*
 * Copyright  2020 Balazs Kreith
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.observertc.webrtc.observer;

import io.micronaut.context.annotation.Prototype;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.observertc.webrtc.observer.dto.PeerConnectionSampleVisitor;
import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.evaluators.valueadapters.EnumConverter;
import org.observertc.webrtc.observer.evaluators.valueadapters.NumberConverter;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.observertc.webrtc.schemas.reports.ICECandidatePair;
import org.observertc.webrtc.schemas.reports.ICELocalCandidate;
import org.observertc.webrtc.schemas.reports.ICERemoteCandidate;
import org.observertc.webrtc.schemas.reports.InboundRTP;
import org.observertc.webrtc.schemas.reports.MediaSource;
import org.observertc.webrtc.schemas.reports.OutboundRTP;
import org.observertc.webrtc.schemas.reports.RemoteInboundRTP;
import org.observertc.webrtc.schemas.reports.ReportType;
import org.observertc.webrtc.schemas.reports.Track;
import org.observertc.webrtc.schemas.reports.UserMediaError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
public class ObservedPCSForwarder {

	private static final Logger logger = LoggerFactory.getLogger(ObservedPCSForwarder.class);
	private final OutboundReportsConfig config;
	private final PeerConnectionSampleVisitor<ObservedPCS> processor;
	private final ReportSink reportSink;
	private final EnumConverter enumConverter;
	private final NumberConverter numberConverter;

	public ObservedPCSForwarder(OutboundReportsConfig config,
								EnumConverter enumConverter,
								NumberConverter numberConverter,
								ReportSink reportSink) {
		this.config = config;
		this.reportSink = reportSink;
		this.enumConverter = enumConverter;
		this.numberConverter = numberConverter;
		this.processor = this.makeProcessor();

	}

	public void forward(ObservedPCS sample) {
		this.processor.accept(sample, sample.peerConnectionSample);
	}

	public void forwardOnlyUserMediaError(ObservedPCS sample) {
		PeerConnectionSample peerConnectionSample = sample.peerConnectionSample;
		if (peerConnectionSample == null) {
			return;
		}
		PeerConnectionSample.UserMediaError[] userMediaErrors = peerConnectionSample.userMediaErrors;
		if (userMediaErrors == null) {
			return;
		}
		for (PeerConnectionSample.UserMediaError userMediaError : userMediaErrors) {
			this.processor.visitUserMediaError(sample, sample.peerConnectionSample, userMediaError);
		}
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
		final BiConsumer<ObservedPCS, PeerConnectionSample.RTCTrackStats> trackReporter = this.makeTrackReporter();
		final BiConsumer<ObservedPCS, PeerConnectionSample.MediaSourceStats> mediaSourceReporter = this.makeMediaSourceReporter();
		final BiConsumer<ObservedPCS, PeerConnectionSample.UserMediaError> userMediaErrorReporter = this.makeUserMediaErrorReporter();
		return new PeerConnectionSampleVisitor<ObservedPCS>() {
			@Override
			public void visitUserMediaError(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.UserMediaError subject) {
				userMediaErrorReporter.accept(obj, subject);
			}

			@Override
			public void visitMediaSource(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.MediaSourceStats subject) {
				mediaSourceReporter.accept(obj, subject);
			}

			@Override
			public void visitTrack(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.RTCTrackStats subject) {
				trackReporter.accept(obj, subject);
			}

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

	private BiConsumer<ObservedPCS, PeerConnectionSample.UserMediaError> makeUserMediaErrorReporter() {
		if (!this.config.reportUserMediaErrors) {
			return (observedPCS, subject) -> {

			};
		}

		return (observedPCS, subject) -> {
			PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
			UserMediaError mediaSource = UserMediaError.newBuilder()
					.setMediaUnitId(observedPCS.mediaUnitId)
					.setCallName(peerConnectionSample.callId)
					.setUserId(peerConnectionSample.userId)
					.setBrowserId(peerConnectionSample.browserId)
					.setPeerConnectionUUID(peerConnectionSample.peerConnectionId)
					.setMessage(subject.message)
					.build();
			sendReport(observedPCS, observedPCS.serviceUUID, ReportType.USER_MEDIA_ERROR, mediaSource);
		};
	}

	private BiConsumer<ObservedPCS, PeerConnectionSample.MediaSourceStats> makeMediaSourceReporter() {
		if (!this.config.reportMediaSources) {
			return (observedPCS, subject) -> {

			};
		}


		return (observedPCS, subject) -> {
			PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
			MediaSource mediaSource = MediaSource.newBuilder()
					.setMediaUnitId(observedPCS.mediaUnitId)
					.setCallName(peerConnectionSample.callId)
					.setUserId(peerConnectionSample.userId)
					.setBrowserId(peerConnectionSample.browserId)
					.setPeerConnectionUUID(peerConnectionSample.peerConnectionId)

					.setAudioLevel(subject.audioLevel)
					.setFramesPerSecond(subject.framesPerSecond)
					.setHeight(subject.height)
					.setMediaSourceId(subject.id)
					.setMediaType(enumConverter.toReportMediaType(subject.mediaType))
					.setTotalAudioEnergy(subject.totalAudioEnergy)
					.setTotalSamplesDuration(subject.totalSamplesDuration)
					.setTrackId(subject.trackId)
					.setWidth(subject.width)
					.build();
			sendReport(observedPCS, ReportType.MEDIA_SOURCE, mediaSource);
		};
	}

	private BiConsumer<ObservedPCS, PeerConnectionSample.RTCTrackStats> makeTrackReporter() {
		if (!this.config.reportTracks) {
			return (observedPCS, subject) -> {

			};
		}

		return (observedPCS, subject) -> {
			PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
			Track track = Track.newBuilder()
					.setMediaUnitId(observedPCS.mediaUnitId)
					.setCallName(peerConnectionSample.callId)
					.setUserId(peerConnectionSample.userId)
					.setBrowserId(peerConnectionSample.browserId)
					.setPeerConnectionUUID(peerConnectionSample.peerConnectionId)
//
					.setConcealedSamples(subject.concealedSamples)
					.setConcealmentEvents(subject.concealmentEvents)
					.setDetached(subject.detached)
					.setEnded(subject.ended)
					.setFramesDecoded(subject.framesDecoded)
					.setFramesDropped(subject.framesDropped)
					.setFramesReceived(subject.framesReceived)
					.setHugeFramesSent(subject.hugeFramesSent)
					.setTrackId(subject.id)
					.setInsertedSamplesForDeceleration(subject.insertedSamplesForDeceleration)
					.setJitterBufferDelay(subject.jitterBufferDelay)
					.setJitterBufferEmittedCount(subject.jitterBufferEmittedCount)
					.setMediaSourceID(subject.mediaSourceId)
					.setMediaType(enumConverter.toReportMediaType(subject.mediaType))
					.setRemoteSource(subject.remoteSource)
					.setRemovedSamplesForAcceleration(subject.removedSamplesForAcceleration)
					.setSamplesDuration(subject.samplesDuration)
					.setSilentConcealedSamples(subject.silentConcealedSamples)
					.setTotalSamplesReceived(subject.totalSamplesReceived)
					.build();

			sendReport(observedPCS, ReportType.TRACK, track);
		};
	}

	private BiConsumer<ObservedPCS, PeerConnectionSample.ICECandidatePair> makeICECandidatePairReporter() {
		if (!this.config.reportCandidatePairs) {
			return (observedPCS, subject) -> {

			};
		}
		return (observedPCS, subject) -> {
			PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
			ICECandidatePair candidatePair = ICECandidatePair.newBuilder()
					.setMediaUnitId(observedPCS.mediaUnitId)
					.setCallName(peerConnectionSample.callId)
					.setUserId(peerConnectionSample.userId)
					.setBrowserId(peerConnectionSample.browserId)
					.setPeerConnectionUUID(peerConnectionSample.peerConnectionId)
					.setCandidatePairId(subject.id)
					.setLocalCandidateID(subject.localCandidateId)
					.setRemoteCandidateID(subject.remoteCandidateId)
					.setAvailableOutgoingBitrate(subject.availableOutgoingBitrate)
					.setBytesReceived(subject.bytesReceived)
					.setBytesSent(subject.bytesSent)
					.setConsentRequestsSent(subject.consentRequestsSent)
					.setCurrentRoundTripTime(subject.currentRoundTripTime)
					.setNominated(subject.nominated)
					.setPriority(subject.priority)
					.setRequestsReceived(subject.requestsReceived)
					.setRequestsSent(subject.requestsSent)
					.setResponsesReceived(subject.responsesReceived)
					.setResponsesSent(subject.responsesSent)
					.setState(enumConverter.toICEState(subject.state))
					.setTotalRoundTripTime(subject.totalRoundTripTime)
					.setTransportID(subject.transportId)
					.setWritable(subject.writable)
					.build();
			sendReport(observedPCS, ReportType.ICE_CANDIDATE_PAIR, candidatePair);
		};
	}

	private BiConsumer<ObservedPCS, PeerConnectionSample.ICELocalCandidate> makeICELocalCandidateReporter() {
		if (!this.config.reportLocalCandidates) {
			return (observedPCS, subject) -> {

			};
		}

		return (observedPCS, subject) -> {
			PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
			String ipLSH = subject.ip;
			ICELocalCandidate localCandidate = ICELocalCandidate.newBuilder()
					.setMediaUnitId(observedPCS.mediaUnitId)
					.setCallName(peerConnectionSample.callId)
					.setUserId(peerConnectionSample.userId)
					.setBrowserId(peerConnectionSample.browserId)
					.setPeerConnectionUUID(peerConnectionSample.peerConnectionId)
					.setCandidateId(subject.id)
					.setCandidateType(enumConverter.toCandidateType(subject.candidateType))
					.setDeleted(subject.deleted)
					.setIpLSH(ipLSH)
					.setIsRemote(subject.isRemote)
					.setPort(subject.port)
					.setPriority(subject.priority)
					.setNetworkType(enumConverter.toNetworkType(subject.networkType))
					.setProtocol(enumConverter.toInternetProtocol(subject.protocol))
					.setTransportID(subject.transportId)
					.build();
			sendReport(observedPCS, ReportType.ICE_LOCAL_CANDIDATE, localCandidate);
		};
	}

	private BiConsumer<ObservedPCS, PeerConnectionSample.ICERemoteCandidate> makeICERemoteCandidateReporter() {
		if (!this.config.reportRemoteCandidates) {
			return (observedPCS, subject) -> {

			};
		}
		return (observedPCS, subject) -> {
			PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
			String ipLSH = subject.ip;
			ICERemoteCandidate remoteCandidate = ICERemoteCandidate.newBuilder()
					.setMediaUnitId(observedPCS.mediaUnitId)
					.setCallName(peerConnectionSample.callId)
					.setUserId(peerConnectionSample.userId)
					.setBrowserId(peerConnectionSample.browserId)
					.setPeerConnectionUUID(peerConnectionSample.peerConnectionId)
					.setCandidateId(subject.id)
					.setCandidateType(enumConverter.toCandidateType(subject.candidateType))
					.setDeleted(subject.deleted)
					.setIpLSH(ipLSH)
					.setIsRemote(subject.isRemote)
					.setPort(subject.port)
					.setPriority(subject.priority)
					.setProtocol(enumConverter.toInternetProtocol(subject.protocol))
					.setTransportID(subject.transportId)
					.build();
			sendReport(observedPCS, ReportType.ICE_REMOTE_CANDIDATE, remoteCandidate);
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
					.setMediaUnitId(observedPCS.mediaUnitId)
					.setCallName(peerConnectionSample.callId)
					.setUserId(peerConnectionSample.userId)
					.setBrowserId(peerConnectionSample.browserId)
					.setPeerConnectionUUID(peerConnectionSample.peerConnectionId)
					.setSsrc(subject.ssrc)
					.setCodecID(subject.codecId)
					.setId(subject.id)
					.setJitter(numberConverter.toFloat(subject.jitter))
					.setLocalID(subject.localId)
					.setMediaType(enumConverter.toReportMediaType(subject.mediaType))
					.setPacketsLost(subject.packetsLost)
					.setRoundTripTime(subject.roundTripTime)
					.setTransportID(subject.transportId)
					.build();
			sendReport(observedPCS, ReportType.REMOTE_INBOUND_RTP, remoteInboundRTP);
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
					.setMediaUnitId(observedPCS.mediaUnitId)
					.setCallName(peerConnectionSample.callId)
					.setUserId(peerConnectionSample.userId)
					.setBrowserId(peerConnectionSample.browserId)
					.setPeerConnectionUUID(peerConnectionSample.peerConnectionId)
					.setSsrc(subject.ssrc)
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
			sendReport(observedPCS, ReportType.INBOUND_RTP, inboundRTP);
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
					.setMediaUnitId(observedPCS.mediaUnitId)
					.setCallName(peerConnectionSample.callId)
					.setUserId(peerConnectionSample.userId)
					.setBrowserId(peerConnectionSample.browserId)
					.setPeerConnectionUUID(peerConnectionSample.peerConnectionId)
					.setSsrc(subject.ssrc)
					.setBytesSent(subject.bytesSent)
					.setCodecID(subject.codecId)
					.setEncoderImplementation(subject.encoderImplementation)
					.setFirCount(subject.firCount)
					.setFramesEncoded(subject.framesEncoded)
					.setHeaderBytesSent(subject.headerBytesSent)
					.setId(subject.id)
					.setIsRemote(subject.isRemote)
					.setKeyFramesEncoded(subject.keyFramesEncoded)
					.setMediaSourceID(subject.mediaSourceId)
					.setMediaType(enumConverter.toReportMediaType(subject.mediaType))
					.setNackCount(subject.nackCount)
					.setPacketsSent(subject.packetsSent)
					.setPliCount(subject.pliCount)
					.setQpSum(subject.qpSum)
					.setQualityLimitationReason(enumConverter.toQualityLimitationReason(subject.qualityLimitationReason))
					.setQualityLimitationResolutionChanges(subject.qualityLimitationResolutionChanges)
					.setRemoteID(subject.remoteId)
					.setRetransmittedBytesSent(subject.retransmittedBytesSent)
					.setRetransmittedPacketsSent(subject.retransmittedPacketsSent)
					.setTotalEncodedBytesTarget(subject.totalEncodedBytesTarget)
					.setTotalEncodeTime(subject.totalEncodeTime)
					.setTotalPacketSendDelay(subject.totalPacketSendDelay)
					.setTrackID(subject.trackId)
					.setTransportID(subject.transportId)
					.build();
			sendReport(observedPCS, ReportType.OUTBOUND_RTP, outboundRTP);
		};
	}

	private void sendReport(ObservedPCS observedPCS, ReportType reportType, Object payload) {
		this.sendReport(observedPCS, observedPCS.peerConnectionUUID, reportType, payload);
	}

	private void sendReport(ObservedPCS observedPCS, UUID kafkaKey, ReportType reportType, Object payload) {
		this.reportSink.sendReport(
				kafkaKey,
				observedPCS.serviceUUID,
				observedPCS.serviceName,
				observedPCS.marker,
				reportType,
				observedPCS.timestamp,
				payload
		);
	}
}
