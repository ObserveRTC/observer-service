public class ProtobufSamplesReader implements Function<ProtobufSamples, Samples> {

	@Override
	public Samples accept(ProtobufSamples source) {
		var result = new Samples();
		if (source.hasMeta()) {
			result.meta = new [object Object]();
			if ( source.meta.hasSchemaVersion()) {
				result.meta.schemaVersion =  source.meta.getSchemaVersion();
			}
		}
		if (source.hasControlFlags()) {
			result.controlFlags = new [object Object]();
			if ( source.controlFlags.hasClose()) {
				result.controlFlags.close =  source.controlFlags.getClose();
			}
		}
		if (source.hasClientSamples()) {
			for (var srcItem0 : source.getClientSamplesList()) {
				var dstItem0 = new [object Object]();
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
					dstItem0.engine = new [object Object]();
					if ( srcItem0.engine.hasName()) {
						dstItem0.engine.name =  srcItem0.engine.getName();
					}
					if ( srcItem0.engine.hasVersion()) {
						dstItem0.engine.version =  srcItem0.engine.getVersion();
					}
				}
				if (srcItem0.hasPlatform()) {
					dstItem0.platform = new [object Object]();
					if ( srcItem0.platform.hasType()) {
						dstItem0.platform.type =  srcItem0.platform.getType();
					}
					if ( srcItem0.platform.hasVendor()) {
						dstItem0.platform.vendor =  srcItem0.platform.getVendor();
					}
					if ( srcItem0.platform.hasModel()) {
						dstItem0.platform.model =  srcItem0.platform.getModel();
					}
				}
				if (srcItem0.hasBrowser()) {
					dstItem0.browser = new [object Object]();
					if ( srcItem0.browser.hasName()) {
						dstItem0.browser.name =  srcItem0.browser.getName();
					}
					if ( srcItem0.browser.hasVersion()) {
						dstItem0.browser.version =  srcItem0.browser.getVersion();
					}
				}
				if (srcItem0.hasOs()) {
					dstItem0.os = new [object Object]();
					if ( srcItem0.os.hasName()) {
						dstItem0.os.name =  srcItem0.os.getName();
					}
					if ( srcItem0.os.hasVersion()) {
						dstItem0.os.version =  srcItem0.os.getVersion();
					}
					if ( srcItem0.os.hasVersionName()) {
						dstItem0.os.versionName =  srcItem0.os.getVersionName();
					}
				}
				if (srcItem0.hasMediaConstraints()) {
					dstItem0.mediaConstraints = getMediaConstraintsList().toArray(new string[0]);
				}
				if (srcItem0.hasMediaDevices()) {
					for (var srcItem1 : srcItem0.getMediaDevicesList()) {
						var dstItem1 = new [object Object]();
						if (srcItem1.hasId()) {
							dstItem1.id = srcItem1.getId();
						}
						if (srcItem1.hasKind()) {
							dstItem1.kind = srcItem1.getKind();
						}
						if (srcItem1.hasLabel()) {
							dstItem1.label = srcItem1.getLabel();
						}
					}
				}
				if (srcItem0.hasUserMediaErrors()) {
					dstItem0.userMediaErrors = getUserMediaErrorsList().toArray(new string[0]);
				}
				if (srcItem0.hasExtensionStats()) {
					for (var srcItem1 : srcItem0.getExtensionStatsList()) {
						var dstItem1 = new [object Object]();
						if (srcItem1.hasType()) {
							dstItem1.type = srcItem1.getType();
						}
						if (srcItem1.hasPayload()) {
							dstItem1.payload = srcItem1.getPayload();
						}
					}
				}
				if (srcItem0.hasIceServers()) {
					dstItem0.iceServers = getIceServersList().toArray(new string[0]);
				}
				if (srcItem0.hasPcTransports()) {
					for (var srcItem1 : srcItem0.getPcTransportsList()) {
						var dstItem1 = new [object Object]();
						if (srcItem1.hasPeerConnectionId()) {
							dstItem1.peerConnectionId = srcItem1.getPeerConnectionId();
						}
						if (srcItem1.hasLabel()) {
							dstItem1.label = srcItem1.getLabel();
						}
						if (srcItem1.hasDataChannelsOpened()) {
							dstItem1.dataChannelsOpened = srcItem1.getDataChannelsOpened();
						}
						if (srcItem1.hasDataChannelsClosed()) {
							dstItem1.dataChannelsClosed = srcItem1.getDataChannelsClosed();
						}
						if (srcItem1.hasDataChannelsRequested()) {
							dstItem1.dataChannelsRequested = srcItem1.getDataChannelsRequested();
						}
						if (srcItem1.hasDataChannelsAccepted()) {
							dstItem1.dataChannelsAccepted = srcItem1.getDataChannelsAccepted();
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
						if (srcItem1.hasLocalAddress()) {
							dstItem1.localAddress = srcItem1.getLocalAddress();
						}
						if (srcItem1.hasLocalPort()) {
							dstItem1.localPort = srcItem1.getLocalPort();
						}
						if (srcItem1.hasLocalProtocol()) {
							dstItem1.localProtocol = srcItem1.getLocalProtocol();
						}
						if (srcItem1.hasLocalCandidateType()) {
							dstItem1.localCandidateType = srcItem1.getLocalCandidateType();
						}
						if (srcItem1.hasLocalCandidateICEServerUrl()) {
							dstItem1.localCandidateICEServerUrl = srcItem1.getLocalCandidateICEServerUrl();
						}
						if (srcItem1.hasLocalCandidateRelayProtocol()) {
							dstItem1.localCandidateRelayProtocol = srcItem1.getLocalCandidateRelayProtocol();
						}
						if (srcItem1.hasRemoteAddress()) {
							dstItem1.remoteAddress = srcItem1.getRemoteAddress();
						}
						if (srcItem1.hasRemotePort()) {
							dstItem1.remotePort = srcItem1.getRemotePort();
						}
						if (srcItem1.hasRemoteProtocol()) {
							dstItem1.remoteProtocol = srcItem1.getRemoteProtocol();
						}
						if (srcItem1.hasRemoteCandidateType()) {
							dstItem1.remoteCandidateType = srcItem1.getRemoteCandidateType();
						}
						if (srcItem1.hasRemoteCandidateICEServerUrl()) {
							dstItem1.remoteCandidateICEServerUrl = srcItem1.getRemoteCandidateICEServerUrl();
						}
						if (srcItem1.hasRemoteCandidateRelayProtocol()) {
							dstItem1.remoteCandidateRelayProtocol = srcItem1.getRemoteCandidateRelayProtocol();
						}
						if (srcItem1.hasCandidatePairState()) {
							dstItem1.candidatePairState = srcItem1.getCandidatePairState();
						}
						if (srcItem1.hasCandidatePairPacketsSent()) {
							dstItem1.candidatePairPacketsSent = srcItem1.getCandidatePairPacketsSent();
						}
						if (srcItem1.hasCandidatePairPacketsReceived()) {
							dstItem1.candidatePairPacketsReceived = srcItem1.getCandidatePairPacketsReceived();
						}
						if (srcItem1.hasCandidatePairBytesSent()) {
							dstItem1.candidatePairBytesSent = srcItem1.getCandidatePairBytesSent();
						}
						if (srcItem1.hasCandidatePairBytesReceived()) {
							dstItem1.candidatePairBytesReceived = srcItem1.getCandidatePairBytesReceived();
						}
						if (srcItem1.hasCandidatePairLastPacketSentTimestamp()) {
							dstItem1.candidatePairLastPacketSentTimestamp = srcItem1.getCandidatePairLastPacketSentTimestamp();
						}
						if (srcItem1.hasCandidatePairLastPacketReceivedTimestamp()) {
							dstItem1.candidatePairLastPacketReceivedTimestamp = srcItem1.getCandidatePairLastPacketReceivedTimestamp();
						}
						if (srcItem1.hasCandidatePairFirstRequestTimestamp()) {
							dstItem1.candidatePairFirstRequestTimestamp = srcItem1.getCandidatePairFirstRequestTimestamp();
						}
						if (srcItem1.hasCandidatePairLastRequestTimestamp()) {
							dstItem1.candidatePairLastRequestTimestamp = srcItem1.getCandidatePairLastRequestTimestamp();
						}
						if (srcItem1.hasCandidatePairLastResponseTimestamp()) {
							dstItem1.candidatePairLastResponseTimestamp = srcItem1.getCandidatePairLastResponseTimestamp();
						}
						if (srcItem1.hasCandidatePairTotalRoundTripTime()) {
							dstItem1.candidatePairTotalRoundTripTime = srcItem1.getCandidatePairTotalRoundTripTime();
						}
						if (srcItem1.hasCandidatePairCurrentRoundTripTime()) {
							dstItem1.candidatePairCurrentRoundTripTime = srcItem1.getCandidatePairCurrentRoundTripTime();
						}
						if (srcItem1.hasCandidatePairAvailableOutgoingBitrate()) {
							dstItem1.candidatePairAvailableOutgoingBitrate = srcItem1.getCandidatePairAvailableOutgoingBitrate();
						}
						if (srcItem1.hasCandidatePairAvailableIncomingBitrate()) {
							dstItem1.candidatePairAvailableIncomingBitrate = srcItem1.getCandidatePairAvailableIncomingBitrate();
						}
						if (srcItem1.hasCandidatePairCircuitBreakerTriggerCount()) {
							dstItem1.candidatePairCircuitBreakerTriggerCount = srcItem1.getCandidatePairCircuitBreakerTriggerCount();
						}
						if (srcItem1.hasCandidatePairRequestsReceived()) {
							dstItem1.candidatePairRequestsReceived = srcItem1.getCandidatePairRequestsReceived();
						}
						if (srcItem1.hasCandidatePairRequestsSent()) {
							dstItem1.candidatePairRequestsSent = srcItem1.getCandidatePairRequestsSent();
						}
						if (srcItem1.hasCandidatePairResponsesReceived()) {
							dstItem1.candidatePairResponsesReceived = srcItem1.getCandidatePairResponsesReceived();
						}
						if (srcItem1.hasCandidatePairResponsesSent()) {
							dstItem1.candidatePairResponsesSent = srcItem1.getCandidatePairResponsesSent();
						}
						if (srcItem1.hasCandidatePairRetransmissionReceived()) {
							dstItem1.candidatePairRetransmissionReceived = srcItem1.getCandidatePairRetransmissionReceived();
						}
						if (srcItem1.hasCandidatePairRetransmissionSent()) {
							dstItem1.candidatePairRetransmissionSent = srcItem1.getCandidatePairRetransmissionSent();
						}
						if (srcItem1.hasCandidatePairConsentRequestsSent()) {
							dstItem1.candidatePairConsentRequestsSent = srcItem1.getCandidatePairConsentRequestsSent();
						}
						if (srcItem1.hasCandidatePairConsentExpiredTimestamp()) {
							dstItem1.candidatePairConsentExpiredTimestamp = srcItem1.getCandidatePairConsentExpiredTimestamp();
						}
						if (srcItem1.hasCandidatePairBytesDiscardedOnSend()) {
							dstItem1.candidatePairBytesDiscardedOnSend = srcItem1.getCandidatePairBytesDiscardedOnSend();
						}
						if (srcItem1.hasCandidatePairPacketsDiscardedOnSend()) {
							dstItem1.candidatePairPacketsDiscardedOnSend = srcItem1.getCandidatePairPacketsDiscardedOnSend();
						}
						if (srcItem1.hasCandidatePairRequestBytesSent()) {
							dstItem1.candidatePairRequestBytesSent = srcItem1.getCandidatePairRequestBytesSent();
						}
						if (srcItem1.hasCandidatePairConsentRequestBytesSent()) {
							dstItem1.candidatePairConsentRequestBytesSent = srcItem1.getCandidatePairConsentRequestBytesSent();
						}
						if (srcItem1.hasCandidatePairResponseBytesSent()) {
							dstItem1.candidatePairResponseBytesSent = srcItem1.getCandidatePairResponseBytesSent();
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
					}
				}
				if (srcItem0.hasMediaSources()) {
					for (var srcItem1 : srcItem0.getMediaSourcesList()) {
						var dstItem1 = new [object Object]();
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
						if (srcItem1.hasBitDepth()) {
							dstItem1.bitDepth = srcItem1.getBitDepth();
						}
						if (srcItem1.hasFrames()) {
							dstItem1.frames = srcItem1.getFrames();
						}
						if (srcItem1.hasFramesPerSecond()) {
							dstItem1.framesPerSecond = srcItem1.getFramesPerSecond();
						}
					}
				}
				if (srcItem0.hasCodecs()) {
					for (var srcItem1 : srcItem0.getCodecsList()) {
						var dstItem1 = new [object Object]();
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
					}
				}
				if (srcItem0.hasCertificates()) {
					for (var srcItem1 : srcItem0.getCertificatesList()) {
						var dstItem1 = new [object Object]();
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
					}
				}
				if (srcItem0.hasInboundAudioTracks()) {
					for (var srcItem1 : srcItem0.getInboundAudioTracksList()) {
						var dstItem1 = new [object Object]();
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
						if (srcItem1.hasPacketsRepaired()) {
							dstItem1.packetsRepaired = srcItem1.getPacketsRepaired();
						}
						if (srcItem1.hasBurstPacketsLost()) {
							dstItem1.burstPacketsLost = srcItem1.getBurstPacketsLost();
						}
						if (srcItem1.hasBurstPacketsDiscarded()) {
							dstItem1.burstPacketsDiscarded = srcItem1.getBurstPacketsDiscarded();
						}
						if (srcItem1.hasBurstLossCount()) {
							dstItem1.burstLossCount = srcItem1.getBurstLossCount();
						}
						if (srcItem1.hasBurstDiscardCount()) {
							dstItem1.burstDiscardCount = srcItem1.getBurstDiscardCount();
						}
						if (srcItem1.hasBurstLossRate()) {
							dstItem1.burstLossRate = srcItem1.getBurstLossRate();
						}
						if (srcItem1.hasBurstDiscardRate()) {
							dstItem1.burstDiscardRate = srcItem1.getBurstDiscardRate();
						}
						if (srcItem1.hasGapLossRate()) {
							dstItem1.gapLossRate = srcItem1.getGapLossRate();
						}
						if (srcItem1.hasGapDiscardRate()) {
							dstItem1.gapDiscardRate = srcItem1.getGapDiscardRate();
						}
						if (srcItem1.hasLastPacketReceivedTimestamp()) {
							dstItem1.lastPacketReceivedTimestamp = srcItem1.getLastPacketReceivedTimestamp();
						}
						if (srcItem1.hasAverageRtcpInterval()) {
							dstItem1.averageRtcpInterval = srcItem1.getAverageRtcpInterval();
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
						if (srcItem1.hasPacketsFailedDecryption()) {
							dstItem1.packetsFailedDecryption = srcItem1.getPacketsFailedDecryption();
						}
						if (srcItem1.hasPacketsDuplicated()) {
							dstItem1.packetsDuplicated = srcItem1.getPacketsDuplicated();
						}
						if (srcItem1.hasPerDscpPacketsReceived()) {
							dstItem1.perDscpPacketsReceived = srcItem1.getPerDscpPacketsReceived();
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
						if (srcItem1.hasVoiceActivityFlag()) {
							dstItem1.voiceActivityFlag = srcItem1.getVoiceActivityFlag();
						}
						if (srcItem1.hasTotalSamplesReceived()) {
							dstItem1.totalSamplesReceived = srcItem1.getTotalSamplesReceived();
						}
						if (srcItem1.hasTotalSamplesDecoded()) {
							dstItem1.totalSamplesDecoded = srcItem1.getTotalSamplesDecoded();
						}
						if (srcItem1.hasSamplesDecodedWithSilk()) {
							dstItem1.samplesDecodedWithSilk = srcItem1.getSamplesDecodedWithSilk();
						}
						if (srcItem1.hasSamplesDecodedWithCelt()) {
							dstItem1.samplesDecodedWithCelt = srcItem1.getSamplesDecodedWithCelt();
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
						if (srcItem1.hasEnded()) {
							dstItem1.ended = srcItem1.getEnded();
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
						if (srcItem1.hasChannels()) {
							dstItem1.channels = srcItem1.getChannels();
						}
						if (srcItem1.hasSdpFmtpLine()) {
							dstItem1.sdpFmtpLine = srcItem1.getSdpFmtpLine();
						}
					}
				}
				if (srcItem0.hasInboundVideoTracks()) {
					for (var srcItem1 : srcItem0.getInboundVideoTracksList()) {
						var dstItem1 = new [object Object]();
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
						if (srcItem1.hasPacketsRepaired()) {
							dstItem1.packetsRepaired = srcItem1.getPacketsRepaired();
						}
						if (srcItem1.hasBurstPacketsLost()) {
							dstItem1.burstPacketsLost = srcItem1.getBurstPacketsLost();
						}
						if (srcItem1.hasBurstPacketsDiscarded()) {
							dstItem1.burstPacketsDiscarded = srcItem1.getBurstPacketsDiscarded();
						}
						if (srcItem1.hasBurstLossCount()) {
							dstItem1.burstLossCount = srcItem1.getBurstLossCount();
						}
						if (srcItem1.hasBurstDiscardCount()) {
							dstItem1.burstDiscardCount = srcItem1.getBurstDiscardCount();
						}
						if (srcItem1.hasBurstLossRate()) {
							dstItem1.burstLossRate = srcItem1.getBurstLossRate();
						}
						if (srcItem1.hasBurstDiscardRate()) {
							dstItem1.burstDiscardRate = srcItem1.getBurstDiscardRate();
						}
						if (srcItem1.hasGapLossRate()) {
							dstItem1.gapLossRate = srcItem1.getGapLossRate();
						}
						if (srcItem1.hasGapDiscardRate()) {
							dstItem1.gapDiscardRate = srcItem1.getGapDiscardRate();
						}
						if (srcItem1.hasLastPacketReceivedTimestamp()) {
							dstItem1.lastPacketReceivedTimestamp = srcItem1.getLastPacketReceivedTimestamp();
						}
						if (srcItem1.hasAverageRtcpInterval()) {
							dstItem1.averageRtcpInterval = srcItem1.getAverageRtcpInterval();
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
						if (srcItem1.hasPacketsFailedDecryption()) {
							dstItem1.packetsFailedDecryption = srcItem1.getPacketsFailedDecryption();
						}
						if (srcItem1.hasPacketsDuplicated()) {
							dstItem1.packetsDuplicated = srcItem1.getPacketsDuplicated();
						}
						if (srcItem1.hasPerDscpPacketsReceived()) {
							dstItem1.perDscpPacketsReceived = srcItem1.getPerDscpPacketsReceived();
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
						if (srcItem1.hasPartialFramesLost()) {
							dstItem1.partialFramesLost = srcItem1.getPartialFramesLost();
						}
						if (srcItem1.hasFullFramesLost()) {
							dstItem1.fullFramesLost = srcItem1.getFullFramesLost();
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
						if (srcItem1.hasFrameBitDepth()) {
							dstItem1.frameBitDepth = srcItem1.getFrameBitDepth();
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
						if (srcItem1.hasSliCount()) {
							dstItem1.sliCount = srcItem1.getSliCount();
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
						if (srcItem1.hasEnded()) {
							dstItem1.ended = srcItem1.getEnded();
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
						if (srcItem1.hasChannels()) {
							dstItem1.channels = srcItem1.getChannels();
						}
						if (srcItem1.hasSdpFmtpLine()) {
							dstItem1.sdpFmtpLine = srcItem1.getSdpFmtpLine();
						}
					}
				}
				if (srcItem0.hasOutboundAudioTracks()) {
					for (var srcItem1 : srcItem0.getOutboundAudioTracksList()) {
						var dstItem1 = new [object Object]();
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
						if (srcItem1.hasRtxSsrc()) {
							dstItem1.rtxSsrc = srcItem1.getRtxSsrc();
						}
						if (srcItem1.hasRid()) {
							dstItem1.rid = srcItem1.getRid();
						}
						if (srcItem1.hasLastPacketSentTimestamp()) {
							dstItem1.lastPacketSentTimestamp = srcItem1.getLastPacketSentTimestamp();
						}
						if (srcItem1.hasHeaderBytesSent()) {
							dstItem1.headerBytesSent = srcItem1.getHeaderBytesSent();
						}
						if (srcItem1.hasPacketsDiscardedOnSend()) {
							dstItem1.packetsDiscardedOnSend = srcItem1.getPacketsDiscardedOnSend();
						}
						if (srcItem1.hasBytesDiscardedOnSend()) {
							dstItem1.bytesDiscardedOnSend = srcItem1.getBytesDiscardedOnSend();
						}
						if (srcItem1.hasFecPacketsSent()) {
							dstItem1.fecPacketsSent = srcItem1.getFecPacketsSent();
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
						if (srcItem1.hasPerDscpPacketsSent()) {
							dstItem1.perDscpPacketsSent = srcItem1.getPerDscpPacketsSent();
						}
						if (srcItem1.hasNackCount()) {
							dstItem1.nackCount = srcItem1.getNackCount();
						}
						if (srcItem1.hasEncoderImplementation()) {
							dstItem1.encoderImplementation = srcItem1.getEncoderImplementation();
						}
						if (srcItem1.hasTotalSamplesSent()) {
							dstItem1.totalSamplesSent = srcItem1.getTotalSamplesSent();
						}
						if (srcItem1.hasSamplesEncodedWithSilk()) {
							dstItem1.samplesEncodedWithSilk = srcItem1.getSamplesEncodedWithSilk();
						}
						if (srcItem1.hasSamplesEncodedWithCelt()) {
							dstItem1.samplesEncodedWithCelt = srcItem1.getSamplesEncodedWithCelt();
						}
						if (srcItem1.hasVoiceActivityFlag()) {
							dstItem1.voiceActivityFlag = srcItem1.getVoiceActivityFlag();
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
						if (srcItem1.hasPacketsRepaired()) {
							dstItem1.packetsRepaired = srcItem1.getPacketsRepaired();
						}
						if (srcItem1.hasBurstPacketsLost()) {
							dstItem1.burstPacketsLost = srcItem1.getBurstPacketsLost();
						}
						if (srcItem1.hasBurstPacketsDiscarded()) {
							dstItem1.burstPacketsDiscarded = srcItem1.getBurstPacketsDiscarded();
						}
						if (srcItem1.hasBurstLossCount()) {
							dstItem1.burstLossCount = srcItem1.getBurstLossCount();
						}
						if (srcItem1.hasBurstDiscardCount()) {
							dstItem1.burstDiscardCount = srcItem1.getBurstDiscardCount();
						}
						if (srcItem1.hasBurstLossRate()) {
							dstItem1.burstLossRate = srcItem1.getBurstLossRate();
						}
						if (srcItem1.hasBurstDiscardRate()) {
							dstItem1.burstDiscardRate = srcItem1.getBurstDiscardRate();
						}
						if (srcItem1.hasGapLossRate()) {
							dstItem1.gapLossRate = srcItem1.getGapLossRate();
						}
						if (srcItem1.hasGapDiscardRate()) {
							dstItem1.gapDiscardRate = srcItem1.getGapDiscardRate();
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
						if (srcItem1.hasReportsReceived()) {
							dstItem1.reportsReceived = srcItem1.getReportsReceived();
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
						if (srcItem1.hasEnded()) {
							dstItem1.ended = srcItem1.getEnded();
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
						if (srcItem1.hasChannels()) {
							dstItem1.channels = srcItem1.getChannels();
						}
						if (srcItem1.hasSdpFmtpLine()) {
							dstItem1.sdpFmtpLine = srcItem1.getSdpFmtpLine();
						}
					}
				}
				if (srcItem0.hasOutboundVideoTracks()) {
					for (var srcItem1 : srcItem0.getOutboundVideoTracksList()) {
						var dstItem1 = new [object Object]();
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
						if (srcItem1.hasRtxSsrc()) {
							dstItem1.rtxSsrc = srcItem1.getRtxSsrc();
						}
						if (srcItem1.hasRid()) {
							dstItem1.rid = srcItem1.getRid();
						}
						if (srcItem1.hasLastPacketSentTimestamp()) {
							dstItem1.lastPacketSentTimestamp = srcItem1.getLastPacketSentTimestamp();
						}
						if (srcItem1.hasHeaderBytesSent()) {
							dstItem1.headerBytesSent = srcItem1.getHeaderBytesSent();
						}
						if (srcItem1.hasPacketsDiscardedOnSend()) {
							dstItem1.packetsDiscardedOnSend = srcItem1.getPacketsDiscardedOnSend();
						}
						if (srcItem1.hasBytesDiscardedOnSend()) {
							dstItem1.bytesDiscardedOnSend = srcItem1.getBytesDiscardedOnSend();
						}
						if (srcItem1.hasFecPacketsSent()) {
							dstItem1.fecPacketsSent = srcItem1.getFecPacketsSent();
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
						if (srcItem1.hasPerDscpPacketsSent()) {
							dstItem1.perDscpPacketsSent = srcItem1.getPerDscpPacketsSent();
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
						if (srcItem1.hasSliCount()) {
							dstItem1.sliCount = srcItem1.getSliCount();
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
						if (srcItem1.hasFrameBitDepth()) {
							dstItem1.frameBitDepth = srcItem1.getFrameBitDepth();
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
						if (srcItem1.hasFramesDiscardedOnSend()) {
							dstItem1.framesDiscardedOnSend = srcItem1.getFramesDiscardedOnSend();
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
						if (srcItem1.hasPacketsDiscarded()) {
							dstItem1.packetsDiscarded = srcItem1.getPacketsDiscarded();
						}
						if (srcItem1.hasPacketsRepaired()) {
							dstItem1.packetsRepaired = srcItem1.getPacketsRepaired();
						}
						if (srcItem1.hasBurstPacketsLost()) {
							dstItem1.burstPacketsLost = srcItem1.getBurstPacketsLost();
						}
						if (srcItem1.hasBurstPacketsDiscarded()) {
							dstItem1.burstPacketsDiscarded = srcItem1.getBurstPacketsDiscarded();
						}
						if (srcItem1.hasBurstLossCount()) {
							dstItem1.burstLossCount = srcItem1.getBurstLossCount();
						}
						if (srcItem1.hasBurstDiscardCount()) {
							dstItem1.burstDiscardCount = srcItem1.getBurstDiscardCount();
						}
						if (srcItem1.hasBurstLossRate()) {
							dstItem1.burstLossRate = srcItem1.getBurstLossRate();
						}
						if (srcItem1.hasBurstDiscardRate()) {
							dstItem1.burstDiscardRate = srcItem1.getBurstDiscardRate();
						}
						if (srcItem1.hasGapLossRate()) {
							dstItem1.gapLossRate = srcItem1.getGapLossRate();
						}
						if (srcItem1.hasGapDiscardRate()) {
							dstItem1.gapDiscardRate = srcItem1.getGapDiscardRate();
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
						if (srcItem1.hasReportsReceived()) {
							dstItem1.reportsReceived = srcItem1.getReportsReceived();
						}
						if (srcItem1.hasRoundTripTimeMeasurements()) {
							dstItem1.roundTripTimeMeasurements = srcItem1.getRoundTripTimeMeasurements();
						}
						if (srcItem1.hasFramesDropped()) {
							dstItem1.framesDropped = srcItem1.getFramesDropped();
						}
						if (srcItem1.hasPartialFramesLost()) {
							dstItem1.partialFramesLost = srcItem1.getPartialFramesLost();
						}
						if (srcItem1.hasFullFramesLost()) {
							dstItem1.fullFramesLost = srcItem1.getFullFramesLost();
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
						if (srcItem1.hasBitDepth()) {
							dstItem1.bitDepth = srcItem1.getBitDepth();
						}
						if (srcItem1.hasFrames()) {
							dstItem1.frames = srcItem1.getFrames();
						}
						if (srcItem1.hasEnded()) {
							dstItem1.ended = srcItem1.getEnded();
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
						if (srcItem1.hasChannels()) {
							dstItem1.channels = srcItem1.getChannels();
						}
						if (srcItem1.hasSdpFmtpLine()) {
							dstItem1.sdpFmtpLine = srcItem1.getSdpFmtpLine();
						}
					}
				}
				if (srcItem0.hasIceLocalCandidates()) {
					for (var srcItem1 : srcItem0.getIceLocalCandidatesList()) {
						var dstItem1 = new [object Object]();
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
					}
				}
				if (srcItem0.hasIceRemoteCandidates()) {
					for (var srcItem1 : srcItem0.getIceRemoteCandidatesList()) {
						var dstItem1 = new [object Object]();
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
					}
				}
				if (srcItem0.hasDataChannels()) {
					for (var srcItem1 : srcItem0.getDataChannelsList()) {
						var dstItem1 = new [object Object]();
						if (srcItem1.hasPeerConnectionId()) {
							dstItem1.peerConnectionId = srcItem1.getPeerConnectionId();
						}
						if (srcItem1.hasId()) {
							dstItem1.id = srcItem1.getId();
						}
						if (srcItem1.hasLabel()) {
							dstItem1.label = srcItem1.getLabel();
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
						if (srcItem1.hasDataChannelIdentifier()) {
							dstItem1.dataChannelIdentifier = srcItem1.getDataChannelIdentifier();
						}
						if (srcItem1.hasState()) {
							dstItem1.state = srcItem1.getState();
						}
						if (srcItem1.hasMessagesSent()) {
							dstItem1.messagesSent = srcItem1.getMessagesSent();
						}
						if (srcItem1.hasBytesSent()) {
							dstItem1.bytesSent = srcItem1.getBytesSent();
						}
						if (srcItem1.hasMessagesReceived()) {
							dstItem1.messagesReceived = srcItem1.getMessagesReceived();
						}
						if (srcItem1.hasBytesReceived()) {
							dstItem1.bytesReceived = srcItem1.getBytesReceived();
						}
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
			}
		}
		if (source.hasSfuSamples()) {
			for (var srcItem0 : source.getSfuSamplesList()) {
				var dstItem0 = new [object Object]();
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
				if (srcItem0.hasTransports()) {
					for (var srcItem1 : srcItem0.getTransportsList()) {
						var dstItem1 = new [object Object]();
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
					}
				}
				if (srcItem0.hasInboundRtpPads()) {
					for (var srcItem1 : srcItem0.getInboundRtpPadsList()) {
						var dstItem1 = new [object Object]();
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
					}
				}
				if (srcItem0.hasOutboundRtpPads()) {
					for (var srcItem1 : srcItem0.getOutboundRtpPadsList()) {
						var dstItem1 = new [object Object]();
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
					}
				}
				if (srcItem0.hasSctpChannels()) {
					for (var srcItem1 : srcItem0.getSctpChannelsList()) {
						var dstItem1 = new [object Object]();
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
					}
				}
				if (srcItem0.hasExtensionStats()) {
					for (var srcItem1 : srcItem0.getExtensionStatsList()) {
						var dstItem1 = new [object Object]();
						if (srcItem1.hasType()) {
							dstItem1.type = srcItem1.getType();
						}
						if (srcItem1.hasPayload()) {
							dstItem1.payload = srcItem1.getPayload();
						}
					}
				}
			}
		}
		if (source.hasTurnSamples()) {
			for (var srcItem0 : source.getTurnSamplesList()) {
				var dstItem0 = new [object Object]();
				if (srcItem0.hasServerId()) {
					dstItem0.serverId = srcItem0.getServerId();
				}
				if (srcItem0.hasSessions()) {
					for (var srcItem1 : srcItem0.getSessionsList()) {
						var dstItem1 = new [object Object]();
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
						if (srcItem1.hasClientTransportProtocol()) {
							dstItem1.clientTransportProtocol = srcItem1.getClientTransportProtocol();
						}
						if (srcItem1.hasRelayTransportProtocol()) {
							dstItem1.relayTransportProtocol = srcItem1.getRelayTransportProtocol();
						}
						if (srcItem1.hasServerAddress()) {
							dstItem1.serverAddress = srcItem1.getServerAddress();
						}
						if (srcItem1.hasServerPort()) {
							dstItem1.serverPort = srcItem1.getServerPort();
						}
						if (srcItem1.hasPeerAddress()) {
							dstItem1.peerAddress = srcItem1.getPeerAddress();
						}
						if (srcItem1.hasPeerPort()) {
							dstItem1.peerPort = srcItem1.getPeerPort();
						}
						if (srcItem1.hasAverageSendingBitrateToClient()) {
							dstItem1.averageSendingBitrateToClient = srcItem1.getAverageSendingBitrateToClient();
						}
						if (srcItem1.hasAverageReceivingBitrateFromClient()) {
							dstItem1.averageReceivingBitrateFromClient = srcItem1.getAverageReceivingBitrateFromClient();
						}
						if (srcItem1.hasReceivedBytesFromClient()) {
							dstItem1.receivedBytesFromClient = srcItem1.getReceivedBytesFromClient();
						}
						if (srcItem1.hasSentBytesToClient()) {
							dstItem1.sentBytesToClient = srcItem1.getSentBytesToClient();
						}
						if (srcItem1.hasReceivedPacketsFromClient()) {
							dstItem1.receivedPacketsFromClient = srcItem1.getReceivedPacketsFromClient();
						}
						if (srcItem1.hasSentPacketsToClient()) {
							dstItem1.sentPacketsToClient = srcItem1.getSentPacketsToClient();
						}
						if (srcItem1.hasAverageSendingBitrateToPeer()) {
							dstItem1.averageSendingBitrateToPeer = srcItem1.getAverageSendingBitrateToPeer();
						}
						if (srcItem1.hasAverageReceivingBitrateFromPeer()) {
							dstItem1.averageReceivingBitrateFromPeer = srcItem1.getAverageReceivingBitrateFromPeer();
						}
						if (srcItem1.hasReceivedBytesFromPeer()) {
							dstItem1.receivedBytesFromPeer = srcItem1.getReceivedBytesFromPeer();
						}
						if (srcItem1.hasSentBytesToPeer()) {
							dstItem1.sentBytesToPeer = srcItem1.getSentBytesToPeer();
						}
						if (srcItem1.hasReceivedPacketsFromPeer()) {
							dstItem1.receivedPacketsFromPeer = srcItem1.getReceivedPacketsFromPeer();
						}
						if (srcItem1.hasSentPacketsToPeer()) {
							dstItem1.sentPacketsToPeer = srcItem1.getSentPacketsToPeer();
						}
					}
				}
			}
		}
	}
}