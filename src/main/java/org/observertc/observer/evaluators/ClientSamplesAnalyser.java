package org.observertc.observer.evaluators;

import io.micronaut.context.BeanProvider;
import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import jakarta.inject.Inject;
import org.observertc.observer.common.JsonUtils;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.evaluators.depots.*;
import org.observertc.observer.events.CallMetaType;
import org.observertc.observer.metrics.ClientSamplesMetrics;
import org.observertc.observer.metrics.EvaluatorMetrics;
import org.observertc.observer.reports.Report;
import org.observertc.observer.repositories.tasks.MatchTracks;
import org.observertc.observer.samples.ClientSampleVisitor;
import org.observertc.observer.samples.ObservedClientSamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

@Prototype
public class ClientSamplesAnalyser implements Consumer<ObservedClientSamples> {
    private static final Logger logger = LoggerFactory.getLogger(ClientSamplesAnalyser.class);
    private static final String METRIC_COMPONENT_NAME = ClientSamplesAnalyser.class.getSimpleName();

    @Inject
    EvaluatorMetrics exposedMetrics;

    @Inject
    BeanProvider<MatchTracks> matchTracks;

    @Inject
    ObserverConfig.EvaluatorsConfig.ClientSamplesAnalyserConfig config;

    @Inject
    ClientSamplesMetrics clientSamplesMetrics;

    private Subject<List<Report>> output = PublishSubject.create();
    private final PeerConnectionTransportReportsDepot peerConnectionTransportReportsDepot = new PeerConnectionTransportReportsDepot();
    private final IceCandidatePairReportsDepot iceCandidatePairReportsDepot = new IceCandidatePairReportsDepot();
    private final InboundAudioReportsDepot inboundAudioReportsDepot = new InboundAudioReportsDepot();
    private final InboundVideoReportsDepot inboundVideoReportsDepot = new InboundVideoReportsDepot();
    private final OutboundAudioReportsDepot outboundAudioReportsDepot = new OutboundAudioReportsDepot();
    private final OutboundVideoReportsDepot outboundVideoReportsDepot = new OutboundVideoReportsDepot();
    private final ClientDataChannelReportsDepot clientDataChannelReportsDepot = new ClientDataChannelReportsDepot();
    private final CallMetaReportsDepot callMetaReportsDepot = new CallMetaReportsDepot();
    private final ClientExtensionReportsDepot clientExtensionReportsDepot = new ClientExtensionReportsDepot();
    private final CustomCallEventReportsDepot customCallEventReportsDepot = new CustomCallEventReportsDepot();

    public Observable<List<Report>> observableReports() {
        return this.output;
    }

    public void accept(ObservedClientSamples observedClientSamples) {
        if (observedClientSamples == null) {
            return;
        }
        if (observedClientSamples.isEmpty()) {
            this.output.onNext(Collections.emptyList());
            return;
        }
        Instant started = Instant.now();
        try {
            this.process(observedClientSamples);
        } finally {
            this.exposedMetrics.addTaskExecutionTime(METRIC_COMPONENT_NAME, started, Instant.now());
        }
    }


    private void process(ObservedClientSamples observedClientSamples) {
        if (observedClientSamples.isEmpty()) {
            return;
        }
        MatchTracks.Report matches;
        if (this.config.matchTracks) {
            var task = this.matchTracks.get()
                    .whereOutboundTrackIds(observedClientSamples.getOutboundTrackIds())
                    .whereInboundTrackIds(observedClientSamples.getInboundTrackIds())
                    ;
            if (!task.execute().succeeded()) {
                logger.warn("Interrupted execution of component due to unsuccessful task execution");
                return;
            }
            matches = task.getResult();
        } else {
            matches = MatchTracks.EMPTY_REPORT;
        }

        var inboundMatches = matches.inboundMatches;
        var peerConnectionLabels = new HashMap<String, String>();
        for (var observedClientSample : observedClientSamples) {
            var clientSample = observedClientSample.getClientSample();
            if (Objects.isNull(clientSample.callId)) {
                logger.warn("Cannot make a report from a sample callId is null. ServiceId: {}, MediaUnitId: {}, clientId: {}",
                        observedClientSample.getServiceId(),
                        observedClientSample.getMediaUnitId(),
                        clientSample.clientId
                );
                return;
            }
            if (Objects.isNull(clientSample)) continue;
            ClientSampleVisitor.streamPeerConnectionTransports(clientSample).forEach(peerConnectionTransport -> {
                this.peerConnectionTransportReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setPeerConnectionTransport(peerConnectionTransport)
                        .assemble();
                if (peerConnectionTransport.peerConnectionId != null && peerConnectionTransport.label != null) {
                    peerConnectionLabels.put(peerConnectionTransport.peerConnectionId, peerConnectionTransport.label);
                }
            });

            ClientSampleVisitor.streamIceCandidatePairs(clientSample).forEach(iceCandidatePair -> {
                this.iceCandidatePairReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setIceCandidatePair(iceCandidatePair)
                        .assemble();
                if (iceCandidatePair.peerConnectionId != null && iceCandidatePair.label != null) {
                    peerConnectionLabels.put(iceCandidatePair.peerConnectionId, iceCandidatePair.label);
                }
            });

            ClientSampleVisitor.streamInboundAudioTracks(clientSample).forEach(inboundAudioTrack -> {
                var match = inboundMatches.get(inboundAudioTrack.trackId);
                var peerConnectionLabel = Objects.nonNull(inboundAudioTrack.peerConnectionId) ? peerConnectionLabels.get(inboundAudioTrack.peerConnectionId) : null;
                if (Objects.nonNull(match)) {
                    this.inboundAudioReportsDepot
                            .setRemoteClientId(match.outboundClientId())
                            .setRemoteUserId(match.outboundUserId())
                            .setRemotePeerConnectionId(match.outboundPeerConnectionId())
                            .setRemoteTrackId(match.outboundTrackId())
                    ;
                } else if (config.dropUnmatchedReports) {
                    return;
                }
                this.inboundAudioReportsDepot
                        .setPeerConnectionLabel(peerConnectionLabel)
                        .setObservedClientSample(observedClientSample)
                        .setInboundAudioTrack(inboundAudioTrack)
                        .assemble();
            });

            ClientSampleVisitor.streamInboundVideoTracks(clientSample).forEach(inboundVideoTrack -> {
                var match = inboundMatches.get(inboundVideoTrack.trackId);
                var peerConnectionLabel = Objects.nonNull(inboundVideoTrack.peerConnectionId) ? peerConnectionLabels.get(inboundVideoTrack.peerConnectionId) : null;
                if (Objects.nonNull(match)) {
                    this.inboundVideoReportsDepot
                            .setRemoteClientId(match.outboundClientId())
                            .setRemoteUserId(match.outboundUserId())
                            .setRemotePeerConnectionId(match.outboundPeerConnectionId())
                            .setRemoteTrackId(match.outboundTrackId())
                    ;
                } else if (config.dropUnmatchedReports) {
                    return;
                }
                this.inboundVideoReportsDepot
                        .setPeerConnectionLabel(peerConnectionLabel)
                        .setObservedClientSample(observedClientSample)
                        .setInboundVideoTrack(inboundVideoTrack)
                        .assemble();
            });

            ClientSampleVisitor.streamOutboundAudioTracks(clientSample).forEach(outboundAudioTrack -> {
                var peerConnectionLabel = Objects.nonNull(outboundAudioTrack.peerConnectionId) ? peerConnectionLabels.get(outboundAudioTrack.peerConnectionId) : null;
                this.outboundAudioReportsDepot
                        .setPeerConnectionLabel(peerConnectionLabel)
                        .setObservedClientSample(observedClientSample)
                        .setOutboundAudioTrack(outboundAudioTrack)
                        .assemble();
            });

            ClientSampleVisitor.streamOutboundVideoTracks(clientSample).forEach(outboundVideoTrack -> {
                var peerConnectionLabel = Objects.nonNull(outboundVideoTrack.peerConnectionId) ? peerConnectionLabels.get(outboundVideoTrack.peerConnectionId) : null;
                this.outboundVideoReportsDepot
                        .setPeerConnectionLabel(peerConnectionLabel)
                        .setObservedClientSample(observedClientSample)
                        .setOutboundVideoTrack(outboundVideoTrack)
                        .assemble();
            });

            ClientSampleVisitor.streamDataChannels(clientSample).forEach(dataChannel -> {
                var peerConnectionLabel = Objects.nonNull(dataChannel.peerConnectionId) ? peerConnectionLabels.get(dataChannel.peerConnectionId) : null;
                this.clientDataChannelReportsDepot
                        .setPeerConnectionLabel(peerConnectionLabel)
                        .setObservedClientSample(observedClientSample)
                        .setDataChannel(dataChannel)
                        .assemble();
            });

            // operation system
            if (Objects.nonNull(clientSample.os)) {
                String payload = JsonUtils.objectToString(clientSample.os);
                this.callMetaReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setMetaType(CallMetaType.OPERATION_SYSTEM)
                        .setPayload(payload)
                        .assemble();
                this.clientSamplesMetrics.incrementOperationSystem(
                        clientSample.os.name,
                        clientSample.os.version
                );
            }

            // engine
            if (Objects.nonNull(clientSample.engine)) {
                String payload = JsonUtils.objectToString(clientSample.engine);
                this.callMetaReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setMetaType(CallMetaType.ENGINE)
                        .setPayload(payload)
                        .assemble();
            }

            // platform
            if (Objects.nonNull(clientSample.platform)) {
                String payload = JsonUtils.objectToString(clientSample.platform);
                this.callMetaReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setMetaType(CallMetaType.PLATFORM)
                        .setPayload(payload)
                        .assemble();
                this.clientSamplesMetrics.incrementPlatform(
                        clientSample.platform.vendor,
                        clientSample.platform.type,
                        clientSample.platform.model
                );
            }

            // browser
            if (Objects.nonNull(clientSample.browser)) {
                String payload = JsonUtils.objectToString(clientSample.browser);
                this.callMetaReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setMetaType(CallMetaType.BROWSER)
                        .setPayload(payload)
                        .assemble();
                this.clientSamplesMetrics.incrementBrowser(
                        clientSample.browser.name,
                        clientSample.browser.version
                );
            }

            // streamCertificates
            ClientSampleVisitor.streamCertificates(clientSample).forEach(certificate -> {
                String payload = JsonUtils.objectToString(certificate);
                this.callMetaReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setMetaType(CallMetaType.CERTIFICATE)
                        .setPayload(payload)
                        .assemble();
            });

            // streamCodecs
            ClientSampleVisitor.streamCodecs(clientSample).forEach(mediaCodecStats -> {
                String payload = JsonUtils.objectToString(mediaCodecStats);
                this.callMetaReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setMetaType(CallMetaType.CODEC)
                        .setPayload(payload)
                        .assemble();
            });

            // streamIceLocalCandidates
            ClientSampleVisitor.streamIceLocalCandidates(clientSample).forEach(iceLocalCandidate -> {
                String payload = JsonUtils.objectToString(iceLocalCandidate);
                this.callMetaReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setMetaType(CallMetaType.ICE_LOCAL_CANDIDATE)
                        .setPayload(payload)
                        .assemble();
            });

            // streamIceRemoteCandidates
            ClientSampleVisitor.streamIceRemoteCandidates(clientSample).forEach(iceRemoteCandidate -> {
                String payload = JsonUtils.objectToString(iceRemoteCandidate);
                this.callMetaReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setMetaType(CallMetaType.ICE_REMOTE_CANDIDATE)
                        .setPayload(payload)
                        .assemble();
            });

            // stream custom call events
            ClientSampleVisitor.streamCustomCallEvents(clientSample).forEach(customCallEvent -> {
                this.customCallEventReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setCustomCallEvent(customCallEvent)
                        .assemble();
            });

            // streamIceServers
            ClientSampleVisitor.streamIceServers(clientSample).forEach(iceServer -> {
                this.callMetaReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setMetaType(CallMetaType.ICE_SERVER)
                        .setPayload(iceServer)
                        .assemble();
            });

            // streamMediaConstraints
            ClientSampleVisitor.streamMediaConstraints(clientSample).forEach(mediaConstraint -> {
                this.callMetaReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setMetaType(CallMetaType.MEDIA_CONSTRAINT)
                        .setPayload(mediaConstraint)
                        .assemble();
            });

            // streamMediaDevices
            ClientSampleVisitor.streamMediaDevices(clientSample).forEach(mediaDevice -> {
                String payload = JsonUtils.objectToString(mediaDevice);
                this.callMetaReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setMetaType(CallMetaType.MEDIA_DEVICE)
                        .setPayload(payload)
                        .assemble();
            });

            // streamMediaSources
            ClientSampleVisitor.streamMediaSources(clientSample).forEach(mediaSourceStat -> {
                String payload = JsonUtils.objectToString(mediaSourceStat);
                this.callMetaReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setMetaType(CallMetaType.MEDIA_SOURCE)
                        .setPayload(payload)
                        .assemble();
            });

            // streamUserMediaErrors
            ClientSampleVisitor.streamUserMediaErrors(clientSample).forEach(userMediaError -> {
                this.callMetaReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setMetaType(CallMetaType.USER_MEDIA_ERROR)
                        .setPayload(userMediaError)
                        .assemble();
            });

            // localSDP
            if (Objects.nonNull(clientSample.localSDPs)) {
                var payload = String.join("\n", clientSample.localSDPs);
                this.callMetaReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setMetaType(CallMetaType.LOCAL_SDP)
                        .setPayload(payload)
                        .assemble();
            }

            // extension stats
            ClientSampleVisitor.streamExtensionStats(clientSample).forEach(extensionStat -> {
                if (!JsonUtils.isValidJsonString(extensionStat.payload)) {
                    logger.warn("The payload of the extension stat MUST be a valid json string. Report will not be created for message {}", JsonUtils.objectToString(extensionStat));
                    return;
                }
                this.clientExtensionReportsDepot
                        .setObservedClientSample(observedClientSample)
                        .setExtensionType(extensionStat.type)
                        .setPayload(extensionStat.payload)
                        .assemble();
            });
        }
        var reports = new LinkedList<Report>();
        this.peerConnectionTransportReportsDepot.get().stream().map(Report::fromPeerConnectionTransportReport).forEach(reports::add);
        this.iceCandidatePairReportsDepot.get().stream().map(Report::fromIceCandidatePairReport).forEach(reports::add);
        this.inboundAudioReportsDepot.get().stream().map(Report::fromInboundAudioTrackReport).forEach(reports::add);
        this.inboundVideoReportsDepot.get().stream().map(Report::fromInboundVideoTrackReport).forEach(reports::add);
        this.outboundAudioReportsDepot.get().stream().map(Report::fromOutboundAudioTrackReport).forEach(reports::add);
        this.outboundVideoReportsDepot.get().stream().map(Report::fromOutboundVideoTrackReport).forEach(reports::add);
        this.clientDataChannelReportsDepot.get().stream().map(Report::fromClientDataChannelReport).forEach(reports::add);
        this.callMetaReportsDepot.get().stream().map(Report::fromCallMetaReport).forEach(reports::add);
        this.clientExtensionReportsDepot.get().stream().map(Report::fromClientExtensionReport).forEach(reports::add);
        this.customCallEventReportsDepot.get().stream().map(Report::fromCallEventReport).forEach(reports::add);
        if (0 < reports.size()) {
            this.output.onNext(reports);
        }
    }

}
