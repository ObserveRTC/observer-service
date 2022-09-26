/** Generated Code, Do not edit! */

package org.observertc.schemas.v200.protobuf;

import org.observertc.schemas.samples.Samples;
import org.observertc.schemas.samples.Samples.ClientSample;
import org.observertc.schemas.samples.Samples.ClientSample.*;
import org.observertc.schemas.samples.Samples.Controls;
import org.observertc.schemas.samples.Samples.SfuSample;
import org.observertc.schemas.samples.Samples.SfuSample.*;
import org.observertc.schemas.samples.Samples.TurnSample;
import org.observertc.schemas.samples.Samples.TurnSample.TurnPeerAllocation;
import org.observertc.schemas.samples.Samples.TurnSample.TurnSession;

import java.util.function.Function;

public class ProtobufSamplesMapper implements Function<ProtobufSamples.Samples, Samples> {

	@Override
	public Samples apply(ProtobufSamples.Samples source) {
		if (source == null) return null;
		var result = new Samples();
		if (source.hasControls()) {
			var srcItem0 = source.getControls();
			var dstItem0 = new Controls();
			if (srcItem0.hasClose()) {
				dstItem0.close = srcItem0.getClose();
			}
			if (srcItem0.hasAccessClaim()) {
				dstItem0.accessClaim = srcItem0.getAccessClaim();
			}
			result.controls = dstItem0;
		}
		if (0 < source.getClientSamplesCount()) {
			result.clientSamples = new ClientSample[ source.getClientSamplesCount()];
			var clientSamplesIndex = 0;
			for (var srcItem0 : source.getClientSamplesList()) {
				var dstItem0 = new ClientSample();
				if (srcItem0.hasCallId()) {
					dstItem0.callId = srcItem0.getCallId();
				}
				if (srcItem0.hasClientId()) {
					dstItem0.clientId = srcItem0.getClientId();
				}
				if (srcItem0.hasSampleSeq()) {
					dstItem0.sampleSeq = srcItem0.getSampleSeq();
				}
				if (srcItem0.hasRoomId()) {
					dstItem0.roomId = srcItem0.getRoomId();
				}
				if (srcItem0.hasUserId()) {
					dstItem0.userId = srcItem0.getUserId();
				}
				if (srcItem0.hasEngine()) {
					var srcItem1 = srcItem0.getEngine();
					var dstItem1 = new Engine();
					if (srcItem1.hasName()) {
						dstItem1.name = srcItem1.getName();
					}
					if (srcItem1.hasVersion()) {
						dstItem1.version = srcItem1.getVersion();
					}
					dstItem0.engine = dstItem1;
				}
				if (srcItem0.hasPlatform()) {
					var srcItem1 = srcItem0.getPlatform();
					var dstItem1 = new Platform();
					if (srcItem1.hasType()) {
						dstItem1.type = srcItem1.getType();
					}
					if (srcItem1.hasVendor()) {
						dstItem1.vendor = srcItem1.getVendor();
					}
					if (srcItem1.hasModel()) {
						dstItem1.model = srcItem1.getModel();
					}
					dstItem0.platform = dstItem1;
				}
				if (srcItem0.hasBrowser()) {
					var srcItem1 = srcItem0.getBrowser();
					var dstItem1 = new Browser();
					if (srcItem1.hasName()) {
						dstItem1.name = srcItem1.getName();
					}
					if (srcItem1.hasVersion()) {
						dstItem1.version = srcItem1.getVersion();
					}
					dstItem0.browser = dstItem1;
				}
				if (srcItem0.hasOs()) {
					var srcItem1 = srcItem0.getOs();
					var dstItem1 = new OperationSystem();
					if (srcItem1.hasName()) {
						dstItem1.name = srcItem1.getName();
					}
					if (srcItem1.hasVersion()) {
						dstItem1.version = srcItem1.getVersion();
					}
					if (srcItem1.hasVersionName()) {
						dstItem1.versionName = srcItem1.getVersionName();
					}
					dstItem0.os = dstItem1;
				}
				if (0 < srcItem0.getMediaConstraintsCount()) {
					dstItem0.mediaConstraints = srcItem0.getMediaConstraintsList().toArray(new String[0]);
				}
				if (0 < srcItem0.getMediaDevicesCount()) {
					dstItem0.mediaDevices = new MediaDevice[ srcItem0.getMediaDevicesCount()];
					var mediaDevicesIndex = 0;
					for (var srcItem1 : srcItem0.getMediaDevicesList()) {
						var dstItem1 = new MediaDevice();
						if (srcItem1.hasId()) {
							dstItem1.id = srcItem1.getId();
						}
						if (srcItem1.hasKind()) {
							dstItem1.kind = srcItem1.getKind();
						}
						if (srcItem1.hasLabel()) {
							dstItem1.label = srcItem1.getLabel();
						}
						dstItem0.mediaDevices[mediaDevicesIndex++] = dstItem1;
					}
				}
				if (0 < srcItem0.getUserMediaErrorsCount()) {
					dstItem0.userMediaErrors = srcItem0.getUserMediaErrorsList().toArray(new String[0]);
				}
				if (0 < srcItem0.getExtensionStatsCount()) {
					dstItem0.extensionStats = new ExtensionStat[ srcItem0.getExtensionStatsCount()];
					var extensionStatsIndex = 0;
					for (var srcItem1 : srcItem0.getExtensionStatsList()) {
						var dstItem1 = new ExtensionStat();
						if (srcItem1.hasType()) {
							dstItem1.type = srcItem1.getType();
						}
						if (srcItem1.hasPayload()) {
							dstItem1.payload = srcItem1.getPayload();
						}
						dstItem0.extensionStats[extensionStatsIndex++] = dstItem1;
					}
				}
				if (0 < srcItem0.getIceServersCount()) {
					dstItem0.iceServers = srcItem0.getIceServersList().toArray(new String[0]);
				}
				if (0 < srcItem0.getLocalSDPsCount()) {
					dstItem0.localSDPs = srcItem0.getLocalSDPsList().toArray(new String[0]);
				}
				if (0 < srcItem0.getPcTransportsCount()) {
					dstItem0.pcTransports = new PeerConnectionTransport[ srcItem0.getPcTransportsCount()];
					dstItem0.iceCandidatePairs = new IceCandidatePair[ srcItem0.getPcTransportsCount()];
					var pcTransportsIndex = 0;
					for (var srcItem1 : srcItem0.getPcTransportsList()) {
						var dstItem1 = new PeerConnectionTransport();
						var dstItem1_iceCandidatePair = new IceCandidatePair();
						if (srcItem1.hasPeerConnectionId()) {
							dstItem1.peerConnectionId = srcItem1.getPeerConnectionId();
						}
						if (srcItem1.hasPacketsSent()) {
							dstItem1.packetsSent = srcItem1.getPacketsSent();
						}
						if (srcItem1.hasPacketsReceived()) {
							dstItem1.packetsReceived = srcItem1.getPacketsReceived();
						}
						if (srcItem1.hasBytesSent()) {
							dstItem1.bytesSent = srcItem1.getBytesSent();
						}
						if (srcItem1.hasBytesReceived()) {
							dstItem1.bytesReceived = srcItem1.getBytesReceived();
						}
						if (srcItem1.hasIceRole()) {
							dstItem1.iceRole = srcItem1.getIceRole();
						}
						if (srcItem1.hasIceLocalUsernameFragment()) {
							dstItem1.iceLocalUsernameFragment = srcItem1.getIceLocalUsernameFragment();
						}
						if (srcItem1.hasDtlsState()) {
							dstItem1.dtlsState = srcItem1.getDtlsState();
						}
						if (srcItem1.hasIceState()) {
							dstItem1.iceState = srcItem1.getIceState();
						}
						if (srcItem1.hasTlsVersion()) {
							dstItem1.tlsVersion = srcItem1.getTlsVersion();
						}
						if (srcItem1.hasDtlsCipher()) {
							dstItem1.dtlsCipher = srcItem1.getDtlsCipher();
						}
						if (srcItem1.hasSrtpCipher()) {
							dstItem1.srtpCipher = srcItem1.getSrtpCipher();
						}
						if (srcItem1.hasTlsGroup()) {
							dstItem1.tlsGroup = srcItem1.getTlsGroup();
						}
						if (srcItem1.hasSelectedCandidatePairChanges()) {
							dstItem1.selectedCandidatePairChanges = srcItem1.getSelectedCandidatePairChanges();
						}
						if (srcItem1.hasCandidatePairState()) {
							dstItem1_iceCandidatePair.state = srcItem1.getCandidatePairState();
						}
						if (srcItem1.hasCandidatePairPacketsSent()) {
							dstItem1_iceCandidatePair.packetsSent = srcItem1.getCandidatePairPacketsSent();
						}
						if (srcItem1.hasCandidatePairPacketsReceived()) {
							dstItem1_iceCandidatePair.packetsReceived = srcItem1.getCandidatePairPacketsReceived();
						}
						if (srcItem1.hasCandidatePairBytesSent()) {
							dstItem1_iceCandidatePair.bytesSent = srcItem1.getCandidatePairBytesSent();
						}
						if (srcItem1.hasCandidatePairBytesReceived()) {
							dstItem1_iceCandidatePair.bytesReceived = srcItem1.getCandidatePairBytesReceived();
						}
						if (srcItem1.hasCandidatePairLastPacketSentTimestamp()) {
							dstItem1_iceCandidatePair.lastPacketSentTimestamp = srcItem1.getCandidatePairLastPacketSentTimestamp();
						}
						if (srcItem1.hasCandidatePairLastPacketReceivedTimestamp()) {
							dstItem1_iceCandidatePair.lastPacketReceivedTimestamp = srcItem1.getCandidatePairLastPacketReceivedTimestamp();
						}
						if (srcItem1.hasCandidatePairTotalRoundTripTime()) {
							dstItem1_iceCandidatePair.totalRoundTripTime = srcItem1.getCandidatePairTotalRoundTripTime();
						}
						if (srcItem1.hasCandidatePairCurrentRoundTripTime()) {
							dstItem1_iceCandidatePair.currentRoundTripTime = srcItem1.getCandidatePairCurrentRoundTripTime();
						}
						if (srcItem1.hasCandidatePairAvailableOutgoingBitrate()) {
							dstItem1_iceCandidatePair.availableOutgoingBitrate = srcItem1.getCandidatePairAvailableOutgoingBitrate();
						}
						if (srcItem1.hasCandidatePairAvailableIncomingBitrate()) {
							dstItem1_iceCandidatePair.availableIncomingBitrate = srcItem1.getCandidatePairAvailableIncomingBitrate();
						}
						if (srcItem1.hasCandidatePairRequestsReceived()) {
							dstItem1_iceCandidatePair.requestsReceived = srcItem1.getCandidatePairRequestsReceived();
						}
						if (srcItem1.hasCandidatePairRequestsSent()) {
							dstItem1_iceCandidatePair.requestsSent = srcItem1.getCandidatePairRequestsSent();
						}
						if (srcItem1.hasCandidatePairResponsesReceived()) {
							dstItem1_iceCandidatePair.responsesReceived = srcItem1.getCandidatePairResponsesReceived();
						}
						if (srcItem1.hasCandidatePairResponsesSent()) {
							dstItem1_iceCandidatePair.responsesSent = srcItem1.getCandidatePairResponsesSent();
						}
						if (srcItem1.hasCandidatePairConsentRequestsSent()) {
							dstItem1_iceCandidatePair.consentRequestsSent = srcItem1.getCandidatePairConsentRequestsSent();
						}
						if (srcItem1.hasCandidatePairBytesDiscardedOnSend()) {
							dstItem1_iceCandidatePair.bytesDiscardedOnSend = srcItem1.getCandidatePairBytesDiscardedOnSend();
						}
						if (srcItem1.hasCandidatePairPacketsDiscardedOnSend()) {
							dstItem1_iceCandidatePair.packetsDiscardedOnSend = srcItem1.getCandidatePairPacketsDiscardedOnSend();
						}

						dstItem0.pcTransports[pcTransportsIndex] = dstItem1;
						dstItem0.iceCandidatePairs[pcTransportsIndex++] = dstItem1_iceCandidatePair;
					}
				}
				if (0 < srcItem0.getMediaSourcesCount()) {
					dstItem0.mediaSources = new MediaSourceStat[ srcItem0.getMediaSourcesCount()];
					var mediaSourcesIndex = 0;
					for (var srcItem1 : srcItem0.getMediaSourcesList()) {
						var dstItem1 = new MediaSourceStat();
						if (srcItem1.hasTrackIdentifier()) {
							dstItem1.trackIdentifier = srcItem1.getTrackIdentifier();
						}
						if (srcItem1.hasKind()) {
							dstItem1.kind = srcItem1.getKind();
						}
						if (srcItem1.hasRelayedSource()) {
							dstItem1.relayedSource = srcItem1.getRelayedSource();
						}
						if (srcItem1.hasAudioLevel()) {
							dstItem1.audioLevel = srcItem1.getAudioLevel();
						}
						if (srcItem1.hasTotalAudioEnergy()) {
							dstItem1.totalAudioEnergy = srcItem1.getTotalAudioEnergy();
						}
						if (srcItem1.hasTotalSamplesDuration()) {
							dstItem1.totalSamplesDuration = srcItem1.getTotalSamplesDuration();
						}
						if (srcItem1.hasEchoReturnLoss()) {
							dstItem1.echoReturnLoss = srcItem1.getEchoReturnLoss();
						}
						if (srcItem1.hasEchoReturnLossEnhancement()) {
							dstItem1.echoReturnLossEnhancement = srcItem1.getEchoReturnLossEnhancement();
						}
						if (srcItem1.hasWidth()) {
							dstItem1.width = srcItem1.getWidth();
						}
						if (srcItem1.hasHeight()) {
							dstItem1.height = srcItem1.getHeight();
						}
						if (srcItem1.hasFrames()) {
							dstItem1.frames = srcItem1.getFrames();
						}
						if (srcItem1.hasFramesPerSecond()) {
							dstItem1.framesPerSecond = srcItem1.getFramesPerSecond();
						}
						dstItem0.mediaSources[mediaSourcesIndex++] = dstItem1;
					}
				}
				if (0 < srcItem0.getCodecsCount()) {
					dstItem0.codecs = new MediaCodecStats[ srcItem0.getCodecsCount()];
					var codecsIndex = 0;
					for (var srcItem1 : srcItem0.getCodecsList()) {
						var dstItem1 = new MediaCodecStats();
						if (srcItem1.hasPayloadType()) {
							dstItem1.payloadType = srcItem1.getPayloadType();
						}
						if (srcItem1.hasCodecType()) {
							dstItem1.codecType = srcItem1.getCodecType();
						}
						if (srcItem1.hasMimeType()) {
							dstItem1.mimeType = srcItem1.getMimeType();
						}
						if (srcItem1.hasClockRate()) {
							dstItem1.clockRate = srcItem1.getClockRate();
						}
						if (srcItem1.hasChannels()) {
							dstItem1.channels = srcItem1.getChannels();
						}
						if (srcItem1.hasSdpFmtpLine()) {
							dstItem1.sdpFmtpLine = srcItem1.getSdpFmtpLine();
						}
						dstItem0.codecs[codecsIndex++] = dstItem1;
					}
				}
				if (0 < srcItem0.getCertificatesCount()) {
					dstItem0.certificates = new Certificate[ srcItem0.getCertificatesCount()];
					var certificatesIndex = 0;
					for (var srcItem1 : srcItem0.getCertificatesList()) {
						var dstItem1 = new Certificate();
						if (srcItem1.hasFingerprint()) {
							dstItem1.fingerprint = srcItem1.getFingerprint();
						}
						if (srcItem1.hasFingerprintAlgorithm()) {
							dstItem1.fingerprintAlgorithm = srcItem1.getFingerprintAlgorithm();
						}
						if (srcItem1.hasBase64Certificate()) {
							dstItem1.base64Certificate = srcItem1.getBase64Certificate();
						}
						if (srcItem1.hasIssuerCertificateId()) {
							dstItem1.issuerCertificateId = srcItem1.getIssuerCertificateId();
						}
						dstItem0.certificates[certificatesIndex++] = dstItem1;
					}
				}
				if (0 < srcItem0.getInboundAudioTracksCount()) {
					dstItem0.inboundAudioTracks = new InboundAudioTrack[ srcItem0.getInboundAudioTracksCount()];
					var inboundAudioTracksIndex = 0;
					for (var srcItem1 : srcItem0.getInboundAudioTracksList()) {
						var dstItem1 = new InboundAudioTrack();
						if (srcItem1.hasTrackId()) {
							dstItem1.trackId = srcItem1.getTrackId();
						}
						if (srcItem1.hasPeerConnectionId()) {
							dstItem1.peerConnectionId = srcItem1.getPeerConnectionId();
						}
						if (srcItem1.hasRemoteClientId()) {
							dstItem1.remoteClientId = srcItem1.getRemoteClientId();
						}
						if (srcItem1.hasSfuSinkId()) {
							dstItem1.sfuSinkId = srcItem1.getSfuSinkId();
						}
						if (srcItem1.hasSsrc()) {
							dstItem1.ssrc = srcItem1.getSsrc();
						}
						if (srcItem1.hasPacketsReceived()) {
							dstItem1.packetsReceived = srcItem1.getPacketsReceived();
						}
						if (srcItem1.hasPacketsLost()) {
							dstItem1.packetsLost = srcItem1.getPacketsLost();
						}
						if (srcItem1.hasJitter()) {
							dstItem1.jitter = srcItem1.getJitter();
						}
						if (srcItem1.hasPacketsDiscarded()) {
							dstItem1.packetsDiscarded = srcItem1.getPacketsDiscarded();
						}
						if (srcItem1.hasLastPacketReceivedTimestamp()) {
							dstItem1.lastPacketReceivedTimestamp = srcItem1.getLastPacketReceivedTimestamp();
						}
						if (srcItem1.hasHeaderBytesReceived()) {
							dstItem1.headerBytesReceived = srcItem1.getHeaderBytesReceived();
						}
						if (srcItem1.hasFecPacketsReceived()) {
							dstItem1.fecPacketsReceived = srcItem1.getFecPacketsReceived();
						}
						if (srcItem1.hasFecPacketsDiscarded()) {
							dstItem1.fecPacketsDiscarded = srcItem1.getFecPacketsDiscarded();
						}
						if (srcItem1.hasBytesReceived()) {
							dstItem1.bytesReceived = srcItem1.getBytesReceived();
						}
						if (srcItem1.hasNackCount()) {
							dstItem1.nackCount = srcItem1.getNackCount();
						}
						if (srcItem1.hasTotalProcessingDelay()) {
							dstItem1.totalProcessingDelay = srcItem1.getTotalProcessingDelay();
						}
						if (srcItem1.hasEstimatedPlayoutTimestamp()) {
							dstItem1.estimatedPlayoutTimestamp = srcItem1.getEstimatedPlayoutTimestamp();
						}
						if (srcItem1.hasJitterBufferDelay()) {
							dstItem1.jitterBufferDelay = srcItem1.getJitterBufferDelay();
						}
						if (srcItem1.hasJitterBufferEmittedCount()) {
							dstItem1.jitterBufferEmittedCount = srcItem1.getJitterBufferEmittedCount();
						}
						if (srcItem1.hasDecoderImplementation()) {
							dstItem1.decoderImplementation = srcItem1.getDecoderImplementation();
						}
						if (srcItem1.hasTotalSamplesReceived()) {
							dstItem1.totalSamplesReceived = srcItem1.getTotalSamplesReceived();
						}
						if (srcItem1.hasConcealedSamples()) {
							dstItem1.concealedSamples = srcItem1.getConcealedSamples();
						}
						if (srcItem1.hasSilentConcealedSamples()) {
							dstItem1.silentConcealedSamples = srcItem1.getSilentConcealedSamples();
						}
						if (srcItem1.hasConcealmentEvents()) {
							dstItem1.concealmentEvents = srcItem1.getConcealmentEvents();
						}
						if (srcItem1.hasInsertedSamplesForDeceleration()) {
							dstItem1.insertedSamplesForDeceleration = srcItem1.getInsertedSamplesForDeceleration();
						}
						if (srcItem1.hasRemovedSamplesForAcceleration()) {
							dstItem1.removedSamplesForAcceleration = srcItem1.getRemovedSamplesForAcceleration();
						}
						if (srcItem1.hasPacketsSent()) {
							dstItem1.packetsSent = srcItem1.getPacketsSent();
						}
						if (srcItem1.hasBytesSent()) {
							dstItem1.bytesSent = srcItem1.getBytesSent();
						}
						if (srcItem1.hasRemoteTimestamp()) {
							dstItem1.remoteTimestamp = srcItem1.getRemoteTimestamp();
						}
						if (srcItem1.hasReportsSent()) {
							dstItem1.reportsSent = srcItem1.getReportsSent();
						}
						if (srcItem1.hasRoundTripTime()) {
							dstItem1.roundTripTime = srcItem1.getRoundTripTime();
						}
						if (srcItem1.hasTotalRoundTripTime()) {
							dstItem1.totalRoundTripTime = srcItem1.getTotalRoundTripTime();
						}
						if (srcItem1.hasRoundTripTimeMeasurements()) {
							dstItem1.roundTripTimeMeasurements = srcItem1.getRoundTripTimeMeasurements();
						}
						dstItem0.inboundAudioTracks[inboundAudioTracksIndex++] = dstItem1;
					}
				}
				if (0 < srcItem0.getInboundVideoTracksCount()) {
					dstItem0.inboundVideoTracks = new InboundVideoTrack[ srcItem0.getInboundVideoTracksCount()];
					var inboundVideoTracksIndex = 0;
					for (var srcItem1 : srcItem0.getInboundVideoTracksList()) {
						var dstItem1 = new InboundVideoTrack();
						if (srcItem1.hasTrackId()) {
							dstItem1.trackId = srcItem1.getTrackId();
						}
						if (srcItem1.hasPeerConnectionId()) {
							dstItem1.peerConnectionId = srcItem1.getPeerConnectionId();
						}
						if (srcItem1.hasRemoteClientId()) {
							dstItem1.remoteClientId = srcItem1.getRemoteClientId();
						}
						if (srcItem1.hasSfuSinkId()) {
							dstItem1.sfuSinkId = srcItem1.getSfuSinkId();
						}
						if (srcItem1.hasSsrc()) {
							dstItem1.ssrc = srcItem1.getSsrc();
						}
						if (srcItem1.hasPacketsReceived()) {
							dstItem1.packetsReceived = srcItem1.getPacketsReceived();
						}
						if (srcItem1.hasPacketsLost()) {
							dstItem1.packetsLost = srcItem1.getPacketsLost();
						}
						if (srcItem1.hasJitter()) {
							dstItem1.jitter = srcItem1.getJitter();
						}
						if (srcItem1.hasPacketsDiscarded()) {
							dstItem1.packetsDiscarded = srcItem1.getPacketsDiscarded();
						}
						if (srcItem1.hasLastPacketReceivedTimestamp()) {
							dstItem1.lastPacketReceivedTimestamp = srcItem1.getLastPacketReceivedTimestamp();
						}
						if (srcItem1.hasHeaderBytesReceived()) {
							dstItem1.headerBytesReceived = srcItem1.getHeaderBytesReceived();
						}
						if (srcItem1.hasFecPacketsReceived()) {
							dstItem1.fecPacketsReceived = srcItem1.getFecPacketsReceived();
						}
						if (srcItem1.hasFecPacketsDiscarded()) {
							dstItem1.fecPacketsDiscarded = srcItem1.getFecPacketsDiscarded();
						}
						if (srcItem1.hasBytesReceived()) {
							dstItem1.bytesReceived = srcItem1.getBytesReceived();
						}
						if (srcItem1.hasNackCount()) {
							dstItem1.nackCount = srcItem1.getNackCount();
						}
						if (srcItem1.hasTotalProcessingDelay()) {
							dstItem1.totalProcessingDelay = srcItem1.getTotalProcessingDelay();
						}
						if (srcItem1.hasEstimatedPlayoutTimestamp()) {
							dstItem1.estimatedPlayoutTimestamp = srcItem1.getEstimatedPlayoutTimestamp();
						}
						if (srcItem1.hasJitterBufferDelay()) {
							dstItem1.jitterBufferDelay = srcItem1.getJitterBufferDelay();
						}
						if (srcItem1.hasJitterBufferEmittedCount()) {
							dstItem1.jitterBufferEmittedCount = srcItem1.getJitterBufferEmittedCount();
						}
						if (srcItem1.hasDecoderImplementation()) {
							dstItem1.decoderImplementation = srcItem1.getDecoderImplementation();
						}
						if (srcItem1.hasFramesDropped()) {
							dstItem1.framesDropped = srcItem1.getFramesDropped();
						}
						if (srcItem1.hasFramesDecoded()) {
							dstItem1.framesDecoded = srcItem1.getFramesDecoded();
						}
						if (srcItem1.hasKeyFramesDecoded()) {
							dstItem1.keyFramesDecoded = srcItem1.getKeyFramesDecoded();
						}
						if (srcItem1.hasFrameWidth()) {
							dstItem1.frameWidth = srcItem1.getFrameWidth();
						}
						if (srcItem1.hasFrameHeight()) {
							dstItem1.frameHeight = srcItem1.getFrameHeight();
						}
						if (srcItem1.hasFramesPerSecond()) {
							dstItem1.framesPerSecond = srcItem1.getFramesPerSecond();
						}
						if (srcItem1.hasQpSum()) {
							dstItem1.qpSum = srcItem1.getQpSum();
						}
						if (srcItem1.hasTotalDecodeTime()) {
							dstItem1.totalDecodeTime = srcItem1.getTotalDecodeTime();
						}
						if (srcItem1.hasTotalInterFrameDelay()) {
							dstItem1.totalInterFrameDelay = srcItem1.getTotalInterFrameDelay();
						}
						if (srcItem1.hasTotalSquaredInterFrameDelay()) {
							dstItem1.totalSquaredInterFrameDelay = srcItem1.getTotalSquaredInterFrameDelay();
						}
						if (srcItem1.hasFirCount()) {
							dstItem1.firCount = srcItem1.getFirCount();
						}
						if (srcItem1.hasPliCount()) {
							dstItem1.pliCount = srcItem1.getPliCount();
						}
						if (srcItem1.hasFramesReceived()) {
							dstItem1.framesReceived = srcItem1.getFramesReceived();
						}
						if (srcItem1.hasPacketsSent()) {
							dstItem1.packetsSent = srcItem1.getPacketsSent();
						}
						if (srcItem1.hasBytesSent()) {
							dstItem1.bytesSent = srcItem1.getBytesSent();
						}
						if (srcItem1.hasRemoteTimestamp()) {
							dstItem1.remoteTimestamp = srcItem1.getRemoteTimestamp();
						}
						if (srcItem1.hasReportsSent()) {
							dstItem1.reportsSent = srcItem1.getReportsSent();
						}
						if (srcItem1.hasRoundTripTime()) {
							dstItem1.roundTripTime = srcItem1.getRoundTripTime();
						}
						if (srcItem1.hasTotalRoundTripTime()) {
							dstItem1.totalRoundTripTime = srcItem1.getTotalRoundTripTime();
						}
						if (srcItem1.hasRoundTripTimeMeasurements()) {
							dstItem1.roundTripTimeMeasurements = srcItem1.getRoundTripTimeMeasurements();
						}
						dstItem0.inboundVideoTracks[inboundVideoTracksIndex++] = dstItem1;
					}
				}
				if (0 < srcItem0.getOutboundAudioTracksCount()) {
					dstItem0.outboundAudioTracks = new OutboundAudioTrack[ srcItem0.getOutboundAudioTracksCount()];
					var outboundAudioTracksIndex = 0;
					for (var srcItem1 : srcItem0.getOutboundAudioTracksList()) {
						var dstItem1 = new OutboundAudioTrack();
						if (srcItem1.hasTrackId()) {
							dstItem1.trackId = srcItem1.getTrackId();
						}
						if (srcItem1.hasPeerConnectionId()) {
							dstItem1.peerConnectionId = srcItem1.getPeerConnectionId();
						}
						if (srcItem1.hasSfuStreamId()) {
							dstItem1.sfuStreamId = srcItem1.getSfuStreamId();
						}
						if (srcItem1.hasSsrc()) {
							dstItem1.ssrc = srcItem1.getSsrc();
						}
						if (srcItem1.hasPacketsSent()) {
							dstItem1.packetsSent = srcItem1.getPacketsSent();
						}
						if (srcItem1.hasBytesSent()) {
							dstItem1.bytesSent = srcItem1.getBytesSent();
						}
						if (srcItem1.hasRid()) {
							dstItem1.rid = srcItem1.getRid();
						}
						if (srcItem1.hasHeaderBytesSent()) {
							dstItem1.headerBytesSent = srcItem1.getHeaderBytesSent();
						}
						if (srcItem1.hasRetransmittedPacketsSent()) {
							dstItem1.retransmittedPacketsSent = srcItem1.getRetransmittedPacketsSent();
						}
						if (srcItem1.hasRetransmittedBytesSent()) {
							dstItem1.retransmittedBytesSent = srcItem1.getRetransmittedBytesSent();
						}
						if (srcItem1.hasTargetBitrate()) {
							dstItem1.targetBitrate = srcItem1.getTargetBitrate();
						}
						if (srcItem1.hasTotalEncodedBytesTarget()) {
							dstItem1.totalEncodedBytesTarget = srcItem1.getTotalEncodedBytesTarget();
						}
						if (srcItem1.hasTotalPacketSendDelay()) {
							dstItem1.totalPacketSendDelay = srcItem1.getTotalPacketSendDelay();
						}
						if (srcItem1.hasAverageRtcpInterval()) {
							dstItem1.averageRtcpInterval = srcItem1.getAverageRtcpInterval();
						}
						if (srcItem1.hasNackCount()) {
							dstItem1.nackCount = srcItem1.getNackCount();
						}
						if (srcItem1.hasEncoderImplementation()) {
							dstItem1.encoderImplementation = srcItem1.getEncoderImplementation();
						}
						if (srcItem1.hasPacketsReceived()) {
							dstItem1.packetsReceived = srcItem1.getPacketsReceived();
						}
						if (srcItem1.hasPacketsLost()) {
							dstItem1.packetsLost = srcItem1.getPacketsLost();
						}
						if (srcItem1.hasJitter()) {
							dstItem1.jitter = srcItem1.getJitter();
						}
						if (srcItem1.hasRoundTripTime()) {
							dstItem1.roundTripTime = srcItem1.getRoundTripTime();
						}
						if (srcItem1.hasTotalRoundTripTime()) {
							dstItem1.totalRoundTripTime = srcItem1.getTotalRoundTripTime();
						}
						if (srcItem1.hasFractionLost()) {
							dstItem1.fractionLost = srcItem1.getFractionLost();
						}
						if (srcItem1.hasRoundTripTimeMeasurements()) {
							dstItem1.roundTripTimeMeasurements = srcItem1.getRoundTripTimeMeasurements();
						}
						if (srcItem1.hasRelayedSource()) {
							dstItem1.relayedSource = srcItem1.getRelayedSource();
						}
						if (srcItem1.hasAudioLevel()) {
							dstItem1.audioLevel = srcItem1.getAudioLevel();
						}
						if (srcItem1.hasTotalAudioEnergy()) {
							dstItem1.totalAudioEnergy = srcItem1.getTotalAudioEnergy();
						}
						if (srcItem1.hasTotalSamplesDuration()) {
							dstItem1.totalSamplesDuration = srcItem1.getTotalSamplesDuration();
						}
						if (srcItem1.hasEchoReturnLoss()) {
							dstItem1.echoReturnLoss = srcItem1.getEchoReturnLoss();
						}
						if (srcItem1.hasEchoReturnLossEnhancement()) {
							dstItem1.echoReturnLossEnhancement = srcItem1.getEchoReturnLossEnhancement();
						}
						dstItem0.outboundAudioTracks[outboundAudioTracksIndex++] = dstItem1;
					}
				}
				if (0 < srcItem0.getOutboundVideoTracksCount()) {
					dstItem0.outboundVideoTracks = new OutboundVideoTrack[ srcItem0.getOutboundVideoTracksCount()];
					var outboundVideoTracksIndex = 0;
					for (var srcItem1 : srcItem0.getOutboundVideoTracksList()) {
						var dstItem1 = new OutboundVideoTrack();
						if (srcItem1.hasTrackId()) {
							dstItem1.trackId = srcItem1.getTrackId();
						}
						if (srcItem1.hasPeerConnectionId()) {
							dstItem1.peerConnectionId = srcItem1.getPeerConnectionId();
						}
						if (srcItem1.hasSfuStreamId()) {
							dstItem1.sfuStreamId = srcItem1.getSfuStreamId();
						}
						if (srcItem1.hasSsrc()) {
							dstItem1.ssrc = srcItem1.getSsrc();
						}
						if (srcItem1.hasPacketsSent()) {
							dstItem1.packetsSent = srcItem1.getPacketsSent();
						}
						if (srcItem1.hasBytesSent()) {
							dstItem1.bytesSent = srcItem1.getBytesSent();
						}
						if (srcItem1.hasRid()) {
							dstItem1.rid = srcItem1.getRid();
						}
						if (srcItem1.hasHeaderBytesSent()) {
							dstItem1.headerBytesSent = srcItem1.getHeaderBytesSent();
						}
						if (srcItem1.hasRetransmittedPacketsSent()) {
							dstItem1.retransmittedPacketsSent = srcItem1.getRetransmittedPacketsSent();
						}
						if (srcItem1.hasRetransmittedBytesSent()) {
							dstItem1.retransmittedBytesSent = srcItem1.getRetransmittedBytesSent();
						}
						if (srcItem1.hasTargetBitrate()) {
							dstItem1.targetBitrate = srcItem1.getTargetBitrate();
						}
						if (srcItem1.hasTotalEncodedBytesTarget()) {
							dstItem1.totalEncodedBytesTarget = srcItem1.getTotalEncodedBytesTarget();
						}
						if (srcItem1.hasTotalPacketSendDelay()) {
							dstItem1.totalPacketSendDelay = srcItem1.getTotalPacketSendDelay();
						}
						if (srcItem1.hasAverageRtcpInterval()) {
							dstItem1.averageRtcpInterval = srcItem1.getAverageRtcpInterval();
						}
						if (srcItem1.hasNackCount()) {
							dstItem1.nackCount = srcItem1.getNackCount();
						}
						if (srcItem1.hasFirCount()) {
							dstItem1.firCount = srcItem1.getFirCount();
						}
						if (srcItem1.hasPliCount()) {
							dstItem1.pliCount = srcItem1.getPliCount();
						}
						if (srcItem1.hasEncoderImplementation()) {
							dstItem1.encoderImplementation = srcItem1.getEncoderImplementation();
						}
						if (srcItem1.hasFrameWidth()) {
							dstItem1.frameWidth = srcItem1.getFrameWidth();
						}
						if (srcItem1.hasFrameHeight()) {
							dstItem1.frameHeight = srcItem1.getFrameHeight();
						}
						if (srcItem1.hasFramesPerSecond()) {
							dstItem1.framesPerSecond = srcItem1.getFramesPerSecond();
						}
						if (srcItem1.hasFramesSent()) {
							dstItem1.framesSent = srcItem1.getFramesSent();
						}
						if (srcItem1.hasHugeFramesSent()) {
							dstItem1.hugeFramesSent = srcItem1.getHugeFramesSent();
						}
						if (srcItem1.hasFramesEncoded()) {
							dstItem1.framesEncoded = srcItem1.getFramesEncoded();
						}
						if (srcItem1.hasKeyFramesEncoded()) {
							dstItem1.keyFramesEncoded = srcItem1.getKeyFramesEncoded();
						}
						if (srcItem1.hasQpSum()) {
							dstItem1.qpSum = srcItem1.getQpSum();
						}
						if (srcItem1.hasTotalEncodeTime()) {
							dstItem1.totalEncodeTime = srcItem1.getTotalEncodeTime();
						}
						if (srcItem1.hasQualityLimitationDurationNone()) {
							dstItem1.qualityLimitationDurationNone = srcItem1.getQualityLimitationDurationNone();
						}
						if (srcItem1.hasQualityLimitationDurationCPU()) {
							dstItem1.qualityLimitationDurationCPU = srcItem1.getQualityLimitationDurationCPU();
						}
						if (srcItem1.hasQualityLimitationDurationBandwidth()) {
							dstItem1.qualityLimitationDurationBandwidth = srcItem1.getQualityLimitationDurationBandwidth();
						}
						if (srcItem1.hasQualityLimitationDurationOther()) {
							dstItem1.qualityLimitationDurationOther = srcItem1.getQualityLimitationDurationOther();
						}
						if (srcItem1.hasQualityLimitationReason()) {
							dstItem1.qualityLimitationReason = srcItem1.getQualityLimitationReason();
						}
						if (srcItem1.hasQualityLimitationResolutionChanges()) {
							dstItem1.qualityLimitationResolutionChanges = srcItem1.getQualityLimitationResolutionChanges();
						}
						if (srcItem1.hasPacketsReceived()) {
							dstItem1.packetsReceived = srcItem1.getPacketsReceived();
						}
						if (srcItem1.hasPacketsLost()) {
							dstItem1.packetsLost = srcItem1.getPacketsLost();
						}
						if (srcItem1.hasJitter()) {
							dstItem1.jitter = srcItem1.getJitter();
						}
						if (srcItem1.hasRoundTripTime()) {
							dstItem1.roundTripTime = srcItem1.getRoundTripTime();
						}
						if (srcItem1.hasTotalRoundTripTime()) {
							dstItem1.totalRoundTripTime = srcItem1.getTotalRoundTripTime();
						}
						if (srcItem1.hasFractionLost()) {
							dstItem1.fractionLost = srcItem1.getFractionLost();
						}
						if (srcItem1.hasRoundTripTimeMeasurements()) {
							dstItem1.roundTripTimeMeasurements = srcItem1.getRoundTripTimeMeasurements();
						}
						if (srcItem1.hasFramesDropped()) {
							dstItem1.framesDropped = srcItem1.getFramesDropped();
						}
						if (srcItem1.hasRelayedSource()) {
							dstItem1.relayedSource = srcItem1.getRelayedSource();
						}
						if (srcItem1.hasWidth()) {
							dstItem1.width = srcItem1.getWidth();
						}
						if (srcItem1.hasHeight()) {
							dstItem1.height = srcItem1.getHeight();
						}
						if (srcItem1.hasFrames()) {
							dstItem1.frames = srcItem1.getFrames();
						}
						dstItem0.outboundVideoTracks[outboundVideoTracksIndex++] = dstItem1;
					}
				}
				if (0 < srcItem0.getIceLocalCandidatesCount()) {
					dstItem0.iceLocalCandidates = new IceLocalCandidate[ srcItem0.getIceLocalCandidatesCount()];
					var iceLocalCandidatesIndex = 0;
					for (var srcItem1 : srcItem0.getIceLocalCandidatesList()) {
						var dstItem1 = new IceLocalCandidate();
						if (srcItem1.hasPeerConnectionId()) {
							dstItem1.peerConnectionId = srcItem1.getPeerConnectionId();
						}
						if (srcItem1.hasId()) {
							dstItem1.id = srcItem1.getId();
						}
						if (srcItem1.hasAddress()) {
							dstItem1.address = srcItem1.getAddress();
						}
						if (srcItem1.hasPort()) {
							dstItem1.port = srcItem1.getPort();
						}
						if (srcItem1.hasProtocol()) {
							dstItem1.protocol = srcItem1.getProtocol();
						}
						if (srcItem1.hasCandidateType()) {
							dstItem1.candidateType = srcItem1.getCandidateType();
						}
						if (srcItem1.hasPriority()) {
							dstItem1.priority = srcItem1.getPriority();
						}
						if (srcItem1.hasUrl()) {
							dstItem1.url = srcItem1.getUrl();
						}
						if (srcItem1.hasRelayProtocol()) {
							dstItem1.relayProtocol = srcItem1.getRelayProtocol();
						}
						dstItem0.iceLocalCandidates[iceLocalCandidatesIndex++] = dstItem1;
					}
				}
				if (0 < srcItem0.getIceRemoteCandidatesCount()) {
					dstItem0.iceRemoteCandidates = new IceRemoteCandidate[ srcItem0.getIceRemoteCandidatesCount()];
					var iceRemoteCandidatesIndex = 0;
					for (var srcItem1 : srcItem0.getIceRemoteCandidatesList()) {
						var dstItem1 = new IceRemoteCandidate();
						if (srcItem1.hasPeerConnectionId()) {
							dstItem1.peerConnectionId = srcItem1.getPeerConnectionId();
						}
						if (srcItem1.hasId()) {
							dstItem1.id = srcItem1.getId();
						}
						if (srcItem1.hasAddress()) {
							dstItem1.address = srcItem1.getAddress();
						}
						if (srcItem1.hasPort()) {
							dstItem1.port = srcItem1.getPort();
						}
						if (srcItem1.hasProtocol()) {
							dstItem1.protocol = srcItem1.getProtocol();
						}
						if (srcItem1.hasCandidateType()) {
							dstItem1.candidateType = srcItem1.getCandidateType();
						}
						if (srcItem1.hasPriority()) {
							dstItem1.priority = srcItem1.getPriority();
						}
						if (srcItem1.hasUrl()) {
							dstItem1.url = srcItem1.getUrl();
						}
						if (srcItem1.hasRelayProtocol()) {
							dstItem1.relayProtocol = srcItem1.getRelayProtocol();
						}
						dstItem0.iceRemoteCandidates[iceRemoteCandidatesIndex++] = dstItem1;
					}
				}
				if (0 < srcItem0.getDataChannelsCount()) {
					dstItem0.dataChannels = new DataChannel[ srcItem0.getDataChannelsCount()];
					var dataChannelsIndex = 0;
					for (var srcItem1 : srcItem0.getDataChannelsList()) {
						var dstItem1 = new DataChannel();
						if (srcItem1.hasPeerConnectionId()) {
							dstItem1.peerConnectionId = srcItem1.getPeerConnectionId();
						}
						if (srcItem1.hasId()) {
							try {
								dstItem1.dataChannelIdentifier = Integer.parseInt(srcItem1.getId());
							} catch (NumberFormatException e) {
								dstItem1.dataChannelIdentifier = -1;
							}
						}
						if (srcItem1.hasLabel()) {
							dstItem1.label = srcItem1.getLabel();
						}
						if (srcItem1.hasProtocol()) {
							dstItem1.protocol = srcItem1.getProtocol();
						}
						if (srcItem1.hasDataChannelIdentifier()) {
							dstItem1.dataChannelIdentifier = srcItem1.getDataChannelIdentifier();
						}
						if (srcItem1.hasState()) {
							dstItem1.state = srcItem1.getState();
						}
						if (srcItem1.hasBytesSent()) {
							dstItem1.bytesSent = srcItem1.getBytesSent();
						}
						if (srcItem1.hasBytesReceived()) {
							dstItem1.bytesReceived = srcItem1.getBytesReceived();
						}
						dstItem0.dataChannels[dataChannelsIndex++] = dstItem1;
					}
				}
				if (srcItem0.hasTimestamp()) {
					dstItem0.timestamp = srcItem0.getTimestamp();
				}
				if (srcItem0.hasTimeZoneOffsetInHours()) {
					dstItem0.timeZoneOffsetInHours = srcItem0.getTimeZoneOffsetInHours();
				}
				if (srcItem0.hasMarker()) {
					dstItem0.marker = srcItem0.getMarker();
				}
				result.clientSamples[clientSamplesIndex++] = dstItem0;
			}
		}
		if (0 < source.getSfuSamplesCount()) {
			result.sfuSamples = new SfuSample[ source.getSfuSamplesCount()];
			var sfuSamplesIndex = 0;
			for (var srcItem0 : source.getSfuSamplesList()) {
				var dstItem0 = new SfuSample();
				if (srcItem0.hasSfuId()) {
					dstItem0.sfuId = srcItem0.getSfuId();
				}
				if (srcItem0.hasTimestamp()) {
					dstItem0.timestamp = srcItem0.getTimestamp();
				}
				if (srcItem0.hasTimeZoneOffsetInHours()) {
					dstItem0.timeZoneOffsetInHours = srcItem0.getTimeZoneOffsetInHours();
				}
				if (srcItem0.hasMarker()) {
					dstItem0.marker = srcItem0.getMarker();
				}
				if (0 < srcItem0.getTransportsCount()) {
					dstItem0.transports = new SfuTransport[ srcItem0.getTransportsCount()];
					var transportsIndex = 0;
					for (var srcItem1 : srcItem0.getTransportsList()) {
						var dstItem1 = new SfuTransport();
						if (srcItem1.hasNoReport()) {
							dstItem1.noReport = srcItem1.getNoReport();
						}
						if (srcItem1.hasTransportId()) {
							dstItem1.transportId = srcItem1.getTransportId();
						}
						if (srcItem1.hasInternal()) {
							dstItem1.internal = srcItem1.getInternal();
						}
						if (srcItem1.hasDtlsState()) {
							dstItem1.dtlsState = srcItem1.getDtlsState();
						}
						if (srcItem1.hasIceState()) {
							dstItem1.iceState = srcItem1.getIceState();
						}
						if (srcItem1.hasSctpState()) {
							dstItem1.sctpState = srcItem1.getSctpState();
						}
						if (srcItem1.hasIceRole()) {
							dstItem1.iceRole = srcItem1.getIceRole();
						}
						if (srcItem1.hasLocalAddress()) {
							dstItem1.localAddress = srcItem1.getLocalAddress();
						}
						if (srcItem1.hasLocalPort()) {
							dstItem1.localPort = srcItem1.getLocalPort();
						}
						if (srcItem1.hasProtocol()) {
							dstItem1.protocol = srcItem1.getProtocol();
						}
						if (srcItem1.hasRemoteAddress()) {
							dstItem1.remoteAddress = srcItem1.getRemoteAddress();
						}
						if (srcItem1.hasRemotePort()) {
							dstItem1.remotePort = srcItem1.getRemotePort();
						}
						if (srcItem1.hasRtpBytesReceived()) {
							dstItem1.rtpBytesReceived = srcItem1.getRtpBytesReceived();
						}
						if (srcItem1.hasRtpBytesSent()) {
							dstItem1.rtpBytesSent = srcItem1.getRtpBytesSent();
						}
						if (srcItem1.hasRtpPacketsReceived()) {
							dstItem1.rtpPacketsReceived = srcItem1.getRtpPacketsReceived();
						}
						if (srcItem1.hasRtpPacketsSent()) {
							dstItem1.rtpPacketsSent = srcItem1.getRtpPacketsSent();
						}
						if (srcItem1.hasRtpPacketsLost()) {
							dstItem1.rtpPacketsLost = srcItem1.getRtpPacketsLost();
						}
						if (srcItem1.hasRtxBytesReceived()) {
							dstItem1.rtxBytesReceived = srcItem1.getRtxBytesReceived();
						}
						if (srcItem1.hasRtxBytesSent()) {
							dstItem1.rtxBytesSent = srcItem1.getRtxBytesSent();
						}
						if (srcItem1.hasRtxPacketsReceived()) {
							dstItem1.rtxPacketsReceived = srcItem1.getRtxPacketsReceived();
						}
						if (srcItem1.hasRtxPacketsSent()) {
							dstItem1.rtxPacketsSent = srcItem1.getRtxPacketsSent();
						}
						if (srcItem1.hasRtxPacketsLost()) {
							dstItem1.rtxPacketsLost = srcItem1.getRtxPacketsLost();
						}
						if (srcItem1.hasRtxPacketsDiscarded()) {
							dstItem1.rtxPacketsDiscarded = srcItem1.getRtxPacketsDiscarded();
						}
						if (srcItem1.hasSctpBytesReceived()) {
							dstItem1.sctpBytesReceived = srcItem1.getSctpBytesReceived();
						}
						if (srcItem1.hasSctpBytesSent()) {
							dstItem1.sctpBytesSent = srcItem1.getSctpBytesSent();
						}
						if (srcItem1.hasSctpPacketsReceived()) {
							dstItem1.sctpPacketsReceived = srcItem1.getSctpPacketsReceived();
						}
						if (srcItem1.hasSctpPacketsSent()) {
							dstItem1.sctpPacketsSent = srcItem1.getSctpPacketsSent();
						}
						dstItem0.transports[transportsIndex++] = dstItem1;
					}
				}
				if (0 < srcItem0.getInboundRtpPadsCount()) {
					dstItem0.inboundRtpPads = new SfuInboundRtpPad[ srcItem0.getInboundRtpPadsCount()];
					var inboundRtpPadsIndex = 0;
					for (var srcItem1 : srcItem0.getInboundRtpPadsList()) {
						var dstItem1 = new SfuInboundRtpPad();
						if (srcItem1.hasNoReport()) {
							dstItem1.noReport = srcItem1.getNoReport();
						}
						if (srcItem1.hasTransportId()) {
							dstItem1.transportId = srcItem1.getTransportId();
						}
						if (srcItem1.hasInternal()) {
							dstItem1.internal = srcItem1.getInternal();
						}
						if (srcItem1.hasStreamId()) {
							dstItem1.streamId = srcItem1.getStreamId();
						}
						if (srcItem1.hasPadId()) {
							dstItem1.padId = srcItem1.getPadId();
						}
						if (srcItem1.hasSsrc()) {
							dstItem1.ssrc = srcItem1.getSsrc();
						}
						if (srcItem1.hasMediaType()) {
							dstItem1.mediaType = srcItem1.getMediaType();
						}
						if (srcItem1.hasPayloadType()) {
							dstItem1.payloadType = srcItem1.getPayloadType();
						}
						if (srcItem1.hasMimeType()) {
							dstItem1.mimeType = srcItem1.getMimeType();
						}
						if (srcItem1.hasClockRate()) {
							dstItem1.clockRate = srcItem1.getClockRate();
						}
						if (srcItem1.hasSdpFmtpLine()) {
							dstItem1.sdpFmtpLine = srcItem1.getSdpFmtpLine();
						}
						if (srcItem1.hasRid()) {
							dstItem1.rid = srcItem1.getRid();
						}
						if (srcItem1.hasRtxSsrc()) {
							dstItem1.rtxSsrc = srcItem1.getRtxSsrc();
						}
						if (srcItem1.hasTargetBitrate()) {
							dstItem1.targetBitrate = srcItem1.getTargetBitrate();
						}
						if (srcItem1.hasVoiceActivityFlag()) {
							dstItem1.voiceActivityFlag = srcItem1.getVoiceActivityFlag();
						}
						if (srcItem1.hasFirCount()) {
							dstItem1.firCount = srcItem1.getFirCount();
						}
						if (srcItem1.hasPliCount()) {
							dstItem1.pliCount = srcItem1.getPliCount();
						}
						if (srcItem1.hasNackCount()) {
							dstItem1.nackCount = srcItem1.getNackCount();
						}
						if (srcItem1.hasSliCount()) {
							dstItem1.sliCount = srcItem1.getSliCount();
						}
						if (srcItem1.hasPacketsLost()) {
							dstItem1.packetsLost = srcItem1.getPacketsLost();
						}
						if (srcItem1.hasPacketsReceived()) {
							dstItem1.packetsReceived = srcItem1.getPacketsReceived();
						}
						if (srcItem1.hasPacketsDiscarded()) {
							dstItem1.packetsDiscarded = srcItem1.getPacketsDiscarded();
						}
						if (srcItem1.hasPacketsRepaired()) {
							dstItem1.packetsRepaired = srcItem1.getPacketsRepaired();
						}
						if (srcItem1.hasPacketsFailedDecryption()) {
							dstItem1.packetsFailedDecryption = srcItem1.getPacketsFailedDecryption();
						}
						if (srcItem1.hasPacketsDuplicated()) {
							dstItem1.packetsDuplicated = srcItem1.getPacketsDuplicated();
						}
						if (srcItem1.hasFecPacketsReceived()) {
							dstItem1.fecPacketsReceived = srcItem1.getFecPacketsReceived();
						}
						if (srcItem1.hasFecPacketsDiscarded()) {
							dstItem1.fecPacketsDiscarded = srcItem1.getFecPacketsDiscarded();
						}
						if (srcItem1.hasBytesReceived()) {
							dstItem1.bytesReceived = srcItem1.getBytesReceived();
						}
						if (srcItem1.hasRtcpSrReceived()) {
							dstItem1.rtcpSrReceived = srcItem1.getRtcpSrReceived();
						}
						if (srcItem1.hasRtcpRrSent()) {
							dstItem1.rtcpRrSent = srcItem1.getRtcpRrSent();
						}
						if (srcItem1.hasRtxPacketsReceived()) {
							dstItem1.rtxPacketsReceived = srcItem1.getRtxPacketsReceived();
						}
						if (srcItem1.hasRtxPacketsDiscarded()) {
							dstItem1.rtxPacketsDiscarded = srcItem1.getRtxPacketsDiscarded();
						}
						if (srcItem1.hasFramesReceived()) {
							dstItem1.framesReceived = srcItem1.getFramesReceived();
						}
						if (srcItem1.hasFramesDecoded()) {
							dstItem1.framesDecoded = srcItem1.getFramesDecoded();
						}
						if (srcItem1.hasKeyFramesDecoded()) {
							dstItem1.keyFramesDecoded = srcItem1.getKeyFramesDecoded();
						}
						if (srcItem1.hasFractionLost()) {
							dstItem1.fractionLost = srcItem1.getFractionLost();
						}
						if (srcItem1.hasJitter()) {
							dstItem1.jitter = srcItem1.getJitter();
						}
						if (srcItem1.hasRoundTripTime()) {
							dstItem1.roundTripTime = srcItem1.getRoundTripTime();
						}
						dstItem0.inboundRtpPads[inboundRtpPadsIndex++] = dstItem1;
					}
				}
				if (0 < srcItem0.getOutboundRtpPadsCount()) {
					dstItem0.outboundRtpPads = new SfuOutboundRtpPad[ srcItem0.getOutboundRtpPadsCount()];
					var outboundRtpPadsIndex = 0;
					for (var srcItem1 : srcItem0.getOutboundRtpPadsList()) {
						var dstItem1 = new SfuOutboundRtpPad();
						if (srcItem1.hasNoReport()) {
							dstItem1.noReport = srcItem1.getNoReport();
						}
						if (srcItem1.hasTransportId()) {
							dstItem1.transportId = srcItem1.getTransportId();
						}
						if (srcItem1.hasInternal()) {
							dstItem1.internal = srcItem1.getInternal();
						}
						if (srcItem1.hasStreamId()) {
							dstItem1.streamId = srcItem1.getStreamId();
						}
						if (srcItem1.hasSinkId()) {
							dstItem1.sinkId = srcItem1.getSinkId();
						}
						if (srcItem1.hasPadId()) {
							dstItem1.padId = srcItem1.getPadId();
						}
						if (srcItem1.hasSsrc()) {
							dstItem1.ssrc = srcItem1.getSsrc();
						}
						if (srcItem1.hasCallId()) {
							dstItem1.callId = srcItem1.getCallId();
						}
						if (srcItem1.hasClientId()) {
							dstItem1.clientId = srcItem1.getClientId();
						}
						if (srcItem1.hasTrackId()) {
							dstItem1.trackId = srcItem1.getTrackId();
						}
						if (srcItem1.hasMediaType()) {
							dstItem1.mediaType = srcItem1.getMediaType();
						}
						if (srcItem1.hasPayloadType()) {
							dstItem1.payloadType = srcItem1.getPayloadType();
						}
						if (srcItem1.hasMimeType()) {
							dstItem1.mimeType = srcItem1.getMimeType();
						}
						if (srcItem1.hasClockRate()) {
							dstItem1.clockRate = srcItem1.getClockRate();
						}
						if (srcItem1.hasSdpFmtpLine()) {
							dstItem1.sdpFmtpLine = srcItem1.getSdpFmtpLine();
						}
						if (srcItem1.hasRid()) {
							dstItem1.rid = srcItem1.getRid();
						}
						if (srcItem1.hasRtxSsrc()) {
							dstItem1.rtxSsrc = srcItem1.getRtxSsrc();
						}
						if (srcItem1.hasTargetBitrate()) {
							dstItem1.targetBitrate = srcItem1.getTargetBitrate();
						}
						if (srcItem1.hasVoiceActivityFlag()) {
							dstItem1.voiceActivityFlag = srcItem1.getVoiceActivityFlag();
						}
						if (srcItem1.hasFirCount()) {
							dstItem1.firCount = srcItem1.getFirCount();
						}
						if (srcItem1.hasPliCount()) {
							dstItem1.pliCount = srcItem1.getPliCount();
						}
						if (srcItem1.hasNackCount()) {
							dstItem1.nackCount = srcItem1.getNackCount();
						}
						if (srcItem1.hasSliCount()) {
							dstItem1.sliCount = srcItem1.getSliCount();
						}
						if (srcItem1.hasPacketsLost()) {
							dstItem1.packetsLost = srcItem1.getPacketsLost();
						}
						if (srcItem1.hasPacketsSent()) {
							dstItem1.packetsSent = srcItem1.getPacketsSent();
						}
						if (srcItem1.hasPacketsDiscarded()) {
							dstItem1.packetsDiscarded = srcItem1.getPacketsDiscarded();
						}
						if (srcItem1.hasPacketsRetransmitted()) {
							dstItem1.packetsRetransmitted = srcItem1.getPacketsRetransmitted();
						}
						if (srcItem1.hasPacketsFailedEncryption()) {
							dstItem1.packetsFailedEncryption = srcItem1.getPacketsFailedEncryption();
						}
						if (srcItem1.hasPacketsDuplicated()) {
							dstItem1.packetsDuplicated = srcItem1.getPacketsDuplicated();
						}
						if (srcItem1.hasFecPacketsSent()) {
							dstItem1.fecPacketsSent = srcItem1.getFecPacketsSent();
						}
						if (srcItem1.hasFecPacketsDiscarded()) {
							dstItem1.fecPacketsDiscarded = srcItem1.getFecPacketsDiscarded();
						}
						if (srcItem1.hasBytesSent()) {
							dstItem1.bytesSent = srcItem1.getBytesSent();
						}
						if (srcItem1.hasRtcpSrSent()) {
							dstItem1.rtcpSrSent = srcItem1.getRtcpSrSent();
						}
						if (srcItem1.hasRtcpRrReceived()) {
							dstItem1.rtcpRrReceived = srcItem1.getRtcpRrReceived();
						}
						if (srcItem1.hasRtxPacketsSent()) {
							dstItem1.rtxPacketsSent = srcItem1.getRtxPacketsSent();
						}
						if (srcItem1.hasRtxPacketsDiscarded()) {
							dstItem1.rtxPacketsDiscarded = srcItem1.getRtxPacketsDiscarded();
						}
						if (srcItem1.hasFramesSent()) {
							dstItem1.framesSent = srcItem1.getFramesSent();
						}
						if (srcItem1.hasFramesEncoded()) {
							dstItem1.framesEncoded = srcItem1.getFramesEncoded();
						}
						if (srcItem1.hasKeyFramesEncoded()) {
							dstItem1.keyFramesEncoded = srcItem1.getKeyFramesEncoded();
						}
						if (srcItem1.hasFractionLost()) {
							dstItem1.fractionLost = srcItem1.getFractionLost();
						}
						if (srcItem1.hasJitter()) {
							dstItem1.jitter = srcItem1.getJitter();
						}
						if (srcItem1.hasRoundTripTime()) {
							dstItem1.roundTripTime = srcItem1.getRoundTripTime();
						}
						dstItem0.outboundRtpPads[outboundRtpPadsIndex++] = dstItem1;
					}
				}
				if (0 < srcItem0.getSctpChannelsCount()) {
					dstItem0.sctpChannels = new SfuSctpChannel[ srcItem0.getSctpChannelsCount()];
					var sctpChannelsIndex = 0;
					for (var srcItem1 : srcItem0.getSctpChannelsList()) {
						var dstItem1 = new SfuSctpChannel();
						if (srcItem1.hasNoReport()) {
							dstItem1.noReport = srcItem1.getNoReport();
						}
						if (srcItem1.hasTransportId()) {
							dstItem1.transportId = srcItem1.getTransportId();
						}
						if (srcItem1.hasStreamId()) {
							dstItem1.streamId = srcItem1.getStreamId();
						}
						if (srcItem1.hasChannelId()) {
							dstItem1.channelId = srcItem1.getChannelId();
						}
						if (srcItem1.hasLabel()) {
							dstItem1.label = srcItem1.getLabel();
						}
						if (srcItem1.hasProtocol()) {
							dstItem1.protocol = srcItem1.getProtocol();
						}
						if (srcItem1.hasSctpSmoothedRoundTripTime()) {
							dstItem1.sctpSmoothedRoundTripTime = srcItem1.getSctpSmoothedRoundTripTime();
						}
						if (srcItem1.hasSctpCongestionWindow()) {
							dstItem1.sctpCongestionWindow = srcItem1.getSctpCongestionWindow();
						}
						if (srcItem1.hasSctpReceiverWindow()) {
							dstItem1.sctpReceiverWindow = srcItem1.getSctpReceiverWindow();
						}
						if (srcItem1.hasSctpMtu()) {
							dstItem1.sctpMtu = srcItem1.getSctpMtu();
						}
						if (srcItem1.hasSctpUnackData()) {
							dstItem1.sctpUnackData = srcItem1.getSctpUnackData();
						}
						if (srcItem1.hasMessageReceived()) {
							dstItem1.messageReceived = srcItem1.getMessageReceived();
						}
						if (srcItem1.hasMessageSent()) {
							dstItem1.messageSent = srcItem1.getMessageSent();
						}
						if (srcItem1.hasBytesReceived()) {
							dstItem1.bytesReceived = srcItem1.getBytesReceived();
						}
						if (srcItem1.hasBytesSent()) {
							dstItem1.bytesSent = srcItem1.getBytesSent();
						}
						dstItem0.sctpChannels[sctpChannelsIndex++] = dstItem1;
					}
				}
				if (0 < srcItem0.getExtensionStatsCount()) {
					dstItem0.extensionStats = new SfuExtensionStats[ srcItem0.getExtensionStatsCount()];
					var extensionStatsIndex = 0;
					for (var srcItem1 : srcItem0.getExtensionStatsList()) {
						var dstItem1 = new SfuExtensionStats();
						if (srcItem1.hasType()) {
							dstItem1.type = srcItem1.getType();
						}
						if (srcItem1.hasPayload()) {
							dstItem1.payload = srcItem1.getPayload();
						}
						dstItem0.extensionStats[extensionStatsIndex++] = dstItem1;
					}
				}
				result.sfuSamples[sfuSamplesIndex++] = dstItem0;
			}
		}
		if (0 < source.getTurnSamplesCount()) {
			result.turnSamples = new TurnSample[ source.getTurnSamplesCount()];
			var turnSamplesIndex = 0;
			for (var srcItem0 : source.getTurnSamplesList()) {
				var dstItem0 = new TurnSample();
				if (srcItem0.hasServerId()) {
					dstItem0.serverId = srcItem0.getServerId();
				}
				if (0 < srcItem0.getAllocationsCount()) {
					dstItem0.allocations = new TurnPeerAllocation[ srcItem0.getAllocationsCount()];
					var allocationsIndex = 0;
					for (var srcItem1 : srcItem0.getAllocationsList()) {
						var dstItem1 = new TurnPeerAllocation();
						if (srcItem1.hasPeerId()) {
							dstItem1.peerId = srcItem1.getPeerId();
						}
						if (srcItem1.hasSessionId()) {
							dstItem1.sessionId = srcItem1.getSessionId();
						}
						if (srcItem1.hasRelayedAddress()) {
							dstItem1.relayedAddress = srcItem1.getRelayedAddress();
						}
						if (srcItem1.hasRelayedPort()) {
							dstItem1.relayedPort = srcItem1.getRelayedPort();
						}
						if (srcItem1.hasTransportProtocol()) {
							dstItem1.transportProtocol = srcItem1.getTransportProtocol();
						}
						if (srcItem1.hasPeerAddress()) {
							dstItem1.peerAddress = srcItem1.getPeerAddress();
						}
						if (srcItem1.hasPeerPort()) {
							dstItem1.peerPort = srcItem1.getPeerPort();
						}
						if (srcItem1.hasSendingBitrate()) {
							dstItem1.sendingBitrate = srcItem1.getSendingBitrate();
						}
						if (srcItem1.hasReceivingBitrate()) {
							dstItem1.receivingBitrate = srcItem1.getReceivingBitrate();
						}
						if (srcItem1.hasSentBytes()) {
							dstItem1.sentBytes = srcItem1.getSentBytes();
						}
						if (srcItem1.hasReceivedBytes()) {
							dstItem1.receivedBytes = srcItem1.getReceivedBytes();
						}
						if (srcItem1.hasSentPackets()) {
							dstItem1.sentPackets = srcItem1.getSentPackets();
						}
						if (srcItem1.hasReceivedPackets()) {
							dstItem1.receivedPackets = srcItem1.getReceivedPackets();
						}
						dstItem0.allocations[allocationsIndex++] = dstItem1;
					}
				}
				if (0 < srcItem0.getSessionsCount()) {
					dstItem0.sessions = new TurnSession[ srcItem0.getSessionsCount()];
					var sessionsIndex = 0;
					for (var srcItem1 : srcItem0.getSessionsList()) {
						var dstItem1 = new TurnSession();
						if (srcItem1.hasSessionId()) {
							dstItem1.sessionId = srcItem1.getSessionId();
						}
						if (srcItem1.hasRealm()) {
							dstItem1.realm = srcItem1.getRealm();
						}
						if (srcItem1.hasUsername()) {
							dstItem1.username = srcItem1.getUsername();
						}
						if (srcItem1.hasClientId()) {
							dstItem1.clientId = srcItem1.getClientId();
						}
						if (srcItem1.hasStarted()) {
							dstItem1.started = srcItem1.getStarted();
						}
						if (srcItem1.hasNonceExpirationTime()) {
							dstItem1.nonceExpirationTime = srcItem1.getNonceExpirationTime();
						}
						if (srcItem1.hasServerAddress()) {
							dstItem1.serverAddress = srcItem1.getServerAddress();
						}
						if (srcItem1.hasServerPort()) {
							dstItem1.serverPort = srcItem1.getServerPort();
						}
						if (srcItem1.hasTransportProtocol()) {
							dstItem1.transportProtocol = srcItem1.getTransportProtocol();
						}
						if (srcItem1.hasClientAddress()) {
							dstItem1.clientAddress = srcItem1.getClientAddress();
						}
						if (srcItem1.hasClientPort()) {
							dstItem1.clientPort = srcItem1.getClientPort();
						}
						if (srcItem1.hasSendingBitrate()) {
							dstItem1.sendingBitrate = srcItem1.getSendingBitrate();
						}
						if (srcItem1.hasReceivingBitrate()) {
							dstItem1.receivingBitrate = srcItem1.getReceivingBitrate();
						}
						if (srcItem1.hasSentBytes()) {
							dstItem1.sentBytes = srcItem1.getSentBytes();
						}
						if (srcItem1.hasReceivedBytes()) {
							dstItem1.receivedBytes = srcItem1.getReceivedBytes();
						}
						if (srcItem1.hasSentPackets()) {
							dstItem1.sentPackets = srcItem1.getSentPackets();
						}
						if (srcItem1.hasReceivedPackets()) {
							dstItem1.receivedPackets = srcItem1.getReceivedPackets();
						}
						dstItem0.sessions[sessionsIndex++] = dstItem1;
					}
				}
				result.turnSamples[turnSamplesIndex++] = dstItem0;
			}
		}
		return result;
	}
}