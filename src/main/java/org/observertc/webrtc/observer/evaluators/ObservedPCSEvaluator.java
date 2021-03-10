package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.ObserverConfig;
import org.observertc.webrtc.observer.common.IPAddressConverterProvider;
import org.observertc.webrtc.observer.dto.PeerConnectionSampleVisitor;
import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.evaluators.valueadapters.EnumConverter;
import org.observertc.webrtc.observer.evaluators.valueadapters.NumberConverter;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.observertc.webrtc.schemas.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.observertc.webrtc.observer.evaluators.Pipeline.REPORT_VERSION_NUMBER;

@Singleton
public class ObservedPCSEvaluator implements Observer<ObservedPCS> {

    private static final Logger logger = LoggerFactory.getLogger(ObservedPCSEvaluator.class);

    private final Subject<Report> ICELocalCandidateReports = PublishSubject.create();
    private final Subject<Report> ICERemoteCandidateReports = PublishSubject.create();
    private final Subject<Report> ICECandidatePairReports = PublishSubject.create();
    private final Subject<Report> outboundRTPReports = PublishSubject.create();
    private final Subject<Report> inboundRTPReports = PublishSubject.create();
    private final Subject<Report> remoteInboundRTPReports = PublishSubject.create();
    private final Subject<Report> userMediaErrorReports = PublishSubject.create();
    private final Subject<Report> mediaSourceReports = PublishSubject.create();
    private final Subject<Report> trackReports = PublishSubject.create();
    private final Subject<Report> extensionReports = PublishSubject.create();
    private AtomicReference<Disposable> disposable = new AtomicReference<>(null);
    private PeerConnectionSampleVisitor<ObservedPCS> processor;

    @Inject
    ObserverConfig.OutboundReportsConfig config;

    @Inject
    EnumConverter enumConverter;

    @Inject
    NumberConverter numberConverter;

    private final Function<String, String> ipAddressConverter;

    public ObservedPCSEvaluator(IPAddressConverterProvider ipAddressConverterProvider) {
        this.ipAddressConverter = ipAddressConverterProvider.provide();
    }


    @PostConstruct
    void setup() {
        this.processor = this.makeProcessor();
    }

    @PreDestroy
    void teardown() {

    }

    public Observable<Report> getICELocalCandidateReports() {
        return this.ICELocalCandidateReports;
    }

    public Observable<Report> getICERemoteCandidateReports() {
        return this.ICERemoteCandidateReports;
    }

    public Observable<Report> getICECandidatePairReports() {
        return this.ICECandidatePairReports;
    }

    public Observable<Report> getInboundRTPReports() {
        return this.inboundRTPReports;
    }

    public Observable<Report> getOutboundRTPReports() {
        return this.outboundRTPReports;
    }

    public Observable<Report> getRemoteInboundRTPReports() {
        return this.remoteInboundRTPReports;
    }

    public Observable<Report> getMediaSourceReports() {
        return this.mediaSourceReports;
    }

    public Observable<Report> getTrackReports() {
        return this.trackReports;
    }

    public Observable<Report> getUserMediaErrorReports() {
        return this.userMediaErrorReports;
    }

    public Observable<Report> getExtensionReports() {
        return this.extensionReports;
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        this.disposable.set(d);
    }

    @Override
    public void onNext(@NonNull ObservedPCS sample) {

        this.processor.accept(sample, sample.peerConnectionSample);
    }

    @Override
    public void onError(@NonNull Throwable e) {
        logger.error("Error occured during process", e);
    }

    @Override
    public void onComplete() {

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
        final BiConsumer<ObservedPCS, PeerConnectionSample.ExtensionStat> extensionStatsReporter = this.makeExtensionStatsReporter();
        final BiConsumer<ObservedPCS, PeerConnectionSample.MediaDeviceInfo> mediaDeviceReporter = this.makeMediaDeviceReporter();
        final BiConsumer<ObservedPCS, PeerConnectionSample.ClientDetails> clientDetailsReporter = this.makeClientDetailsReporter();
        return new PeerConnectionSampleVisitor<ObservedPCS>() {
            @Override
            public void visitExtensionStat(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.ExtensionStat subject) {
                extensionStatsReporter.accept(obj, subject);
            }

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

            @Override
            public void visitMediaDeviceInfo(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.MediaDeviceInfo deviceInfo) {
                mediaDeviceReporter.accept(obj, deviceInfo);
            }

            @Override
            public void visitClientDetails(ObservedPCS obj, PeerConnectionSample sample, PeerConnectionSample.ClientDetails clientDetails) {
                clientDetailsReporter.accept(obj, clientDetails);
            }
        };
    }

    private BiConsumer<ObservedPCS, PeerConnectionSample.ClientDetails> makeClientDetailsReporter() {
        if (!this.config.enabled || !this.config.clientDetails) {
            return (observedPCS, subject) -> {

            };
        }

        return (observedPCS, subject) -> {
            String browserName = null;
            String browserVersion = null;
            if (Objects.nonNull(subject.browser)) {
                browserName = subject.browser.name;
                browserVersion = subject.browser.version;
            }
            String osName = null;
            String osVersion = null;
            if (Objects.nonNull(subject.os)) {
                osName = subject.os.name;
                osVersion = subject.os.version;
            }

            String engineName = null;
            String engineVersion = null;
            if (Objects.nonNull(subject.engine)) {
                engineName = subject.engine.name;
                engineVersion = subject.engine.version;
            }

            String platformModel = null;
            String platformType = null;
            String platformVendor = null;
            if (Objects.nonNull(subject.platform)) {
                platformModel = subject.platform.model;
                platformType = subject.platform.type;
                platformVendor = subject.platform.vendor;
            }
            PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
            ClientDetails clientDetails = ClientDetails.newBuilder()
                    .setMediaUnitId(observedPCS.mediaUnitId)
                    .setCallName(peerConnectionSample.callId)
                    .setUserId(peerConnectionSample.userId)
                    .setBrowserId(peerConnectionSample.browserId)
                    .setPeerConnectionUUID(peerConnectionSample.peerConnectionId)
                    .setBrowserName(browserName)
                    .setBrowserVersion(browserVersion)
                    .setOsName(osName)
                    .setOsVersion(osVersion)
                    .setEngineName(engineName)
                    .setEngineVersion(engineVersion)
                    .setPlatformModel(platformModel)
                    .setPlatformType(platformType)
                    .setPlatformVendor(platformVendor)
                    .build();
            Report reportRecord = makeReportRecord(observedPCS, observedPCS.serviceUUID, ReportType.CLIENT_DETAILS, clientDetails);
            this.extensionReports.onNext(reportRecord);
        };

    }

    private BiConsumer<ObservedPCS, PeerConnectionSample.MediaDeviceInfo> makeMediaDeviceReporter() {
        if (!this.config.enabled || !this.config.mediaDevices) {
            return (observedPCS, subject) -> {

            };
        }

        return (observedPCS, subject) -> {
            PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
            MediaDevice mediaDeviceInfo = MediaDevice.newBuilder()
                    .setMediaUnitId(observedPCS.mediaUnitId)
                    .setCallName(peerConnectionSample.callId)
                    .setUserId(peerConnectionSample.userId)
                    .setBrowserId(peerConnectionSample.browserId)
                    .setPeerConnectionUUID(peerConnectionSample.peerConnectionId)
                    .setDeviceId(subject.deviceId)
                    .setGroupId(subject.groupId)
                    .setKind(enumConverter.toMediaDeviceKind(subject.kind))
                    .setLabel(subject.label)
                    .build();
            Report reportRecord = makeReportRecord(observedPCS, observedPCS.serviceUUID, ReportType.MEDIA_DEVICE, mediaDeviceInfo);
            this.extensionReports.onNext(reportRecord);
        };
    }

    private BiConsumer<ObservedPCS, PeerConnectionSample.ExtensionStat> makeExtensionStatsReporter() {
        if (!this.config.enabled || !this.config.reportExtensions) {
            return (observedPCS, subject) -> {

            };
        }

        return (observedPCS, subject) -> {
            PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
            ExtensionReport extensionReport = ExtensionReport.newBuilder()
                    .setMediaUnitId(observedPCS.mediaUnitId)
                    .setCallName(peerConnectionSample.callId)
                    .setUserId(peerConnectionSample.userId)
                    .setBrowserId(peerConnectionSample.browserId)
                    .setPeerConnectionUUID(peerConnectionSample.peerConnectionId)
                    .setExtensionType(subject.extensionType)
                    .setPayload(subject.payload)
                    .build();
            Report reportRecord = makeReportRecord(observedPCS, observedPCS.serviceUUID, ReportType.EXTENSION, extensionReport);
            this.extensionReports.onNext(reportRecord);
        };
    }

    private BiConsumer<ObservedPCS, PeerConnectionSample.UserMediaError> makeUserMediaErrorReporter() {
        if (!this.config.enabled || !this.config.reportUserMediaErrors) {
            return (observedPCS, subject) -> {

            };
        }

        return (observedPCS, subject) -> {
            PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
            // peer connection sample uuid
            String pcUUIDStr = "Unknown";
            if (Objects.nonNull(peerConnectionSample.peerConnectionId)) {
                pcUUIDStr = peerConnectionSample.peerConnectionId;
            }
            String browserId = "Unknown";
            if (Objects.nonNull(peerConnectionSample.browserId)) {
                browserId = peerConnectionSample.browserId;
            }
            UserMediaError mediaSource = UserMediaError.newBuilder()
                    .setMediaUnitId(observedPCS.mediaUnitId)
                    .setCallName(peerConnectionSample.callId)
                    .setUserId(peerConnectionSample.userId)
                    .setBrowserId(browserId)
                    .setPeerConnectionUUID(pcUUIDStr)
                    .setMessage(subject.message)
                    .build();
            Report reportRecord = makeReportRecord(observedPCS, observedPCS.serviceUUID, ReportType.USER_MEDIA_ERROR, mediaSource);
            this.userMediaErrorReports.onNext(reportRecord);
        };
    }

    private BiConsumer<ObservedPCS, PeerConnectionSample.MediaSourceStats> makeMediaSourceReporter() {
        if (!this.config.enabled || !this.config.reportMediaSources) {
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
            Report reportRecord = makeReportRecord(observedPCS, ReportType.MEDIA_SOURCE, mediaSource);
            this.mediaSourceReports.onNext(reportRecord);
        };
    }

    private BiConsumer<ObservedPCS, PeerConnectionSample.RTCTrackStats> makeTrackReporter() {
        if (!this.config.enabled || !this.config.reportTracks) {
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
            Report reportRecord = makeReportRecord(observedPCS, ReportType.TRACK, track);
            this.trackReports.onNext(reportRecord);
        };
    }

    private BiConsumer<ObservedPCS, PeerConnectionSample.ICECandidatePair> makeICECandidatePairReporter() {
        if (!this.config.enabled || !this.config.reportCandidatePairs) {
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
            Report reportRecord = makeReportRecord(observedPCS, ReportType.ICE_CANDIDATE_PAIR, candidatePair);
            this.ICECandidatePairReports.onNext(reportRecord);
        };
    }

    private BiConsumer<ObservedPCS, PeerConnectionSample.ICELocalCandidate> makeICELocalCandidateReporter() {
        if (!this.config.enabled || !this.config.reportLocalCandidates) {
            return (observedPCS, subject) -> {

            };
        }

        return (observedPCS, subject) -> {
            PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
            String ipLSH = ipAddressConverter.apply(subject.ip);
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
            Report reportRecord = makeReportRecord(observedPCS, ReportType.ICE_LOCAL_CANDIDATE, localCandidate);
            this.ICELocalCandidateReports.onNext(reportRecord);
        };
    }

    private BiConsumer<ObservedPCS, PeerConnectionSample.ICERemoteCandidate> makeICERemoteCandidateReporter() {
        if (!this.config.enabled || !this.config.reportRemoteCandidates) {
            return (observedPCS, subject) -> {

            };
        }
        return (observedPCS, subject) -> {
            PeerConnectionSample peerConnectionSample = observedPCS.peerConnectionSample;
            String ipLSH = ipAddressConverter.apply(subject.ip);
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
            Report reportRecord = makeReportRecord(observedPCS, ReportType.ICE_REMOTE_CANDIDATE, remoteCandidate);
            this.ICERemoteCandidateReports.onNext(reportRecord);
        };
    }

    private BiConsumer<ObservedPCS, PeerConnectionSample.RemoteInboundRTPStreamStats> makeRemoteInboundRTPReporter() {
        if (!this.config.enabled || !this.config.reportRemoteInboundRTPs) {
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
            Report reportRecord = makeReportRecord(observedPCS, ReportType.REMOTE_INBOUND_RTP, remoteInboundRTP);
            this.remoteInboundRTPReports.onNext(reportRecord);
        };
    }

    private BiConsumer<ObservedPCS, PeerConnectionSample.InboundRTPStreamStats> makeInboundRTPReporter() {
        if (!this.config.enabled || !this.config.reportInboundRTPs) {
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
            Report reportRecord = makeReportRecord(observedPCS, ReportType.INBOUND_RTP, inboundRTP);
            this.inboundRTPReports.onNext(reportRecord);
        };
    }

    private BiConsumer<ObservedPCS, PeerConnectionSample.OutboundRTPStreamStats> makeOutboundRTPReporter() {
        if (!this.config.enabled || !this.config.reportOutboundRTPs) {
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
            Report reportRecord = makeReportRecord(observedPCS, ReportType.OUTBOUND_RTP, outboundRTP);
            this.outboundRTPReports.onNext(reportRecord);
        };
    }

    private Report makeReportRecord(ObservedPCS observedPCS, ReportType reportType, Object payload) {
        return this.makeReportRecord(observedPCS, observedPCS.peerConnectionUUID, reportType, payload);
    }

    private Report makeReportRecord(ObservedPCS observedPCS, UUID kafkaKey, ReportType reportType, Object payload) {
        return Report.newBuilder()
                .setVersion(REPORT_VERSION_NUMBER)
                .setServiceUUID(observedPCS.serviceUUID.toString())
                .setServiceName(observedPCS.serviceName)
                .setType(reportType)
                .setMarker(observedPCS.marker)
                .setTimestamp(observedPCS.timestamp)
                .setPayload(payload)
                .build();
    }
}
