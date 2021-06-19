//package org.observertc.webrtc.observer.evaluators.pcSampleToReportsV3;
//
//import io.reactivex.rxjava3.annotations.NonNull;
//import io.reactivex.rxjava3.core.Observable;
//import io.reactivex.rxjava3.subjects.PublishSubject;
//import io.reactivex.rxjava3.subjects.Subject;
//import org.observertc.webrtc.observer.ObserverConfig;
//import org.observertc.webrtc.observer.dto.AbstractPeerConnectionSampleVisitor;
//import org.observertc.webrtc.observer.dto.CallDTO;
//import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
//import org.observertc.webrtc.observer.dto.PeerConnectionSampleVisitor;
//import org.observertc.webrtc.observer.dto.pcsamples.v20200114.PeerConnectionSample;
//import org.observertc.webrtc.observer.entities.CallEntity;
//import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
//import org.observertc.webrtc.observer.micrometer.ObserverMetrics;
//import org.observertc.webrtc.observer.repositories.CallsRepository;
//import org.observertc.webrtc.observer.repositories.tasks.UpdatePCSSRCsTask;
//import org.observertc.webrtc.observer.samples.ObservedPCS;
//import org.observertc.webrtc.schemas.reports.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.annotation.PostConstruct;
//import javax.inject.Inject;
//import javax.inject.Provider;
//import javax.inject.Singleton;
//import javax.validation.constraints.NotNull;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.*;
//import java.util.function.BiConsumer;
//
//import static org.observertc.webrtc.observer.evaluators.pcSampleToReportsV2.Pipeline.REPORT_VERSION_NUMBER;
//
//@Singleton
//public class ObservedPCSEvaluator implements io.reactivex.rxjava3.functions.Function<List<ObservedPCS>, ObservedPcState> {
//    private static final Logger logger = LoggerFactory.getLogger(ObservedPCSEvaluator.class);
//    private final Map<UUID, PCState> pcStates = new HashMap<>();
//    private PeerConnectionSampleVisitor<PCState> pcStateProcessor;
//    private ObservedPcState actual = new ObservedPcState();
//    private Subject<CallMetaReport> callMetaReports = PublishSubject.create();
//
//    public Observable<CallMetaReport> getCallMetaReports() {
//        return callMetaReports;
//    }
//
//    @Inject
//    ObserverMetrics observerMetrics;
//
//    @Inject
//    ObserverConfig.EvaluatorsConfig config;
//
//    @Inject
//    CallsRepository calls;
//
//    @Inject
//    Provider<UpdatePCSSRCsTask> updatePCsTaskProvider;
//
//    @PostConstruct
//    void setup() {
//        this.pcStateProcessor = this.makeSSRCExtractor();
//    }
//
//    @Override
//    public ObservedPcState apply(List<ObservedPCS> observedPCS) throws Throwable {
//        if (Objects.isNull(this.actual)) {
//            this.actual = new ObservedPcState();
//        }
//        ObservedPcState result = null;
//        try {
//            this.accept(observedPCS);
//            result = this.actual;
//        } catch (Exception ex) {
//            result = null;
//        } finally {
//            this.actual = new ObservedPcState();
//        }
//        return result;
//    }
//
//    public void accept(List<ObservedPCS> observedPCSamples) throws Throwable {
//        Map<UUID, PCState> activePCs = new HashMap<>();
//        Instant now = Instant.now();
//        for (ObservedPCS observedPCS : observedPCSamples) {
//            if (Objects.isNull(observedPCS.peerConnectionSample)) {
//                continue;
//            }
//            PCState pcState = this.pcStates.get(observedPCS.peerConnectionUUID);
//            if (Objects.isNull(pcState)) {
//                pcState = this.makePCState(observedPCS);
//                if (Objects.isNull(pcState)) {
//                    continue;
//                }
//                this.pcStates.put(observedPCS.peerConnectionUUID, pcState);
//            }
//            pcState.updated = observedPCS.timestamp;
//            pcState.touched = now;
//
//            this.pcStateProcessor.accept(pcState, observedPCS.peerConnectionSample);
//            if (pcState.SSRCs.size() < 1 && Objects.isNull(pcState.callName)) {
//                pcState.callName = this.config.impairablePCsCallName;
//                this.observerMetrics.incrementImpairedPCs(pcState.serviceName, pcState.mediaUnitID);
//            }
//            activePCs.put(observedPCS.peerConnectionUUID, pcState);
//        }
//
//        Map<UUID, PCState> expiredPCs = new HashMap<>();
//        Iterator<Map.Entry<UUID, PCState>> it = this.pcStates.entrySet().iterator();
//        while(it.hasNext()) {
//            Map.Entry<UUID, PCState> entry = it.next();
//            UUID pcUUID = entry.getKey();
//            if (activePCs.containsKey(pcUUID)) {
//                continue;
//            }
//            PCState pcState = entry.getValue();
//            if (Duration.between(pcState.touched, now).getSeconds() < config.peerConnectionMaxIdleTimeInS) {
//                continue;
//            }
//            expiredPCs.put(pcUUID, pcState);
//            it.remove();
//        }
//
//        if (0 < activePCs.size()) {
//            this.checkActivePcStates(activePCs);
//        }
//
//        if (0 < expiredPCs.size()) {
//            this.checkExpiredPcStates(expiredPCs);
//        }
//    }
//
//
//    private PCState makePCState(ObservedPCS observedPCS) {
//        PeerConnectionSample pcSample = observedPCS.peerConnectionSample;
//        if (Objects.isNull(pcSample)) {
//            return null;
//        }
//        PCState pcState = PCState.of(
//                observedPCS.serviceUUID,
//                observedPCS.peerConnectionUUID,
//                observedPCS.timestamp,
//                pcSample.browserId,
//                pcSample.callId,
//                observedPCS.timeZoneID,
//                pcSample.userId,
//                observedPCS.mediaUnitId,
//                observedPCS.serviceName,
//                observedPCS.marker
//        );
//        return pcState;
//    }
//
//    private PeerConnectionSampleVisitor<PCState> makeSSRCExtractor() {
//        BiConsumer<PCState, Long> ssrcConverter = (pcState, SSRC) -> {
//            if (Objects.isNull(SSRC)) {
//                return;
//            }
//            if (SSRC < 16) {
//                SSRC = (pcState.peerConnectionUUID.getMostSignificantBits() & 0xFFFFFFFE) + SSRC;
//            } else if( 10000 < SSRC && SSRC < 10016 ) {
//                SSRC = (pcState.peerConnectionUUID.getMostSignificantBits() & 0xFFF00000) + SSRC;
//            }
//            pcState.SSRCs.add(SSRC);
//        };
//
//        return new AbstractPeerConnectionSampleVisitor<PCState>() {
//            @Override
//            public void visitRemoteInboundRTP(PCState pcState, PeerConnectionSample sample, PeerConnectionSample.RemoteInboundRTPStreamStats subject) {
//                ssrcConverter.accept(pcState, subject.ssrc);
//            }
//
//            @Override
//            public void visitInboundRTP(PCState pcState, PeerConnectionSample sample, PeerConnectionSample.InboundRTPStreamStats subject) {
//                ssrcConverter.accept(pcState, subject.ssrc);
//            }
//
//            @Override
//            public void visitOutboundRTP(PCState pcState, PeerConnectionSample sample, PeerConnectionSample.OutboundRTPStreamStats subject) {
//                ssrcConverter.accept(pcState, subject.ssrc);
//            }
//
//            @Override
//            public void visitICELocalCandidate(PCState pcState, PeerConnectionSample sample, PeerConnectionSample.ICELocalCandidate subject) {
//                if (Objects.nonNull(subject.ip)) {
//
//                }
//            }
//
//            @Override
//            public void visitICERemoteCandidate(PCState pcState, PeerConnectionSample sample, PeerConnectionSample.ICERemoteCandidate subject) {
//                if (Objects.nonNull(subject.ip)) {
//                    pcState.remoteAddresses.add(subject.ip);
//                }
//            }
//        };
//    }
//
//    private void checkActivePcStates(Map<UUID, PCState> newPeerConnections) {
//        Queue<PCState> pcStates = new LinkedList<>();
//        pcStates.addAll(newPeerConnections.values());
//        while (!pcStates.isEmpty()) {
//            PCState pcState = pcStates.poll();
//            Optional<CallEntity> maybeCallEntity = this.calls.findCall(pcState.serviceUUID, pcState.callName, pcState.SSRCs);
//            CallEntity callEntity;
//            if (maybeCallEntity.isPresent()) {
//                callEntity = maybeCallEntity.get();
//            } else {
//                callEntity = this.addNewCall(pcState);
//            }
//
//            if (Objects.isNull(callEntity)) {
//                logger.warn("No Call Entity has been added");
//                return;
//            }
//
//            PeerConnectionEntity pcEntity = this.addNewPeerConnection(callEntity.call.callUUID, pcState);
//        }
//    }
//
//    private PeerConnectionEntity addNewPeerConnection(UUID callUUID, PCState pcState) {
//
//        PeerConnectionEntity pcEntity = PeerConnectionEntity.builder()
//                .withPCDTO(PeerConnectionDTO.of(
//                        pcState.serviceUUID,
//                        pcState.serviceName,
//                        pcState.mediaUnitID,
//                        callUUID,
//                        pcState.callName,
//                        pcState.peerConnectionUUID,
//                        pcState.userId,
//                        pcState.browserId,
//                        pcState.timeZoneId,
//                        pcState.created,
//                        pcState.marker)
//                )
//                .withSSRCs(pcState.SSRCs)
//                .withRemoteIPs(pcState.remoteAddresses)
//                .build();
//
//        if (!this.config.impairablePCsCallName.equals(pcEntity.peerConnection.callName)) {
//            logger.info("Peer Connection {} is registered to Call {}.", pcState.peerConnectionUUID, callUUID);
//        }
//
//        pcEntity = this.calls.addPeerConnection(pcEntity);
//
//        try {
//            var joinedPC = CallMetaReport
//                    .newBuilder()
//                    .setMarker(pcState.marker)
//                    .setCallUUID(callUUID.toString())
//                    .setRoomName(pcState.callName)
//                    .setUserId(pcState.userId)
//                    .setClientId(pcState.browserId)
//                    .setMetaType(ReportType.JOINED_PEER_CONNECTION.name())
//                    .setMetaPayload(null)
//                    .setSampleSeq(null)
//                    .setServiceName(pcState.serviceName)
//                    .setServiceUUID(pcState.serviceUUID.toString())
//                    .setTimestamp(pcState.created)
//                    .build();
//            this.callMetaReports.onNext(joinedPC);
//
//            if (pcState.SSRCs.size() < 1) {
//                this.reportNoSSRC(callUUID, pcState);
//            }
//
//            this.observerMetrics.incrementJoinedPCs(pcEntity.peerConnection.serviceName, pcEntity.peerConnection.mediaUnitId);
//        } finally {
//            return pcEntity;
//        }
//    }
//
//    private CallEntity addNewCall(PCState pcState) {
//        CallEntity callEntity;
//        try {
//            CallEntity candidate = CallEntity.builder()
//                    .withCallDTO(CallDTO.of(
//                            UUID.randomUUID(),
//                            pcState.serviceUUID,
//                            pcState.serviceName,
//                            pcState.created,
//                            pcState.callName,
//                            pcState.marker
//                    ))
//                    .build();
//            callEntity = this.calls.addCall(candidate);
//        } catch (Exception ex) {
//            logger.error("Unexpected exception occurred", ex);
//            return null;
//        }
//
//        if (Objects.isNull(callEntity)) {
//            logger.warn("CallEntity is null {}", pcState);
//            return null;
//        }
//
//        if (!this.config.impairablePCsCallName.equals(callEntity.call.callName)) {
//            logger.info("Call is registered with a uuid: {}", callEntity.call.callUUID);
//        }
//
//        this.actual.newPcToUUIDs.put(pcState.peerConnectionUUID, callEntity.call.callUUID);
//
//        try {
//            var initiatedCallReport = CallMetaReport
//                    .newBuilder()
//                    .setMarker(callEntity.call.marker)
//                    .setCallUUID(callEntity.call.callUUID.toString())
//                    .setRoomName(callEntity.call.callName)
//                    .setUserId(pcState.userId)
//                    .setClientId(pcState.browserId)
//                    .setMetaType(ReportType.INITIATED_CALL.toString())
//                    .setMetaPayload(null)
//                    .setSampleSeq(null)
//                    .setServiceName(callEntity.call.serviceName)
//                    .setServiceUUID(callEntity.call.serviceUUID.toString())
//                    .setTimestamp(callEntity.call.initiated)
//                    .build();
//            this.callMetaReports.onNext(initiatedCallReport);
//            this.observerMetrics.incrementInitiatedCall(callEntity.call.serviceName);
//        } finally {
//            return callEntity;
//        }
//    }
//
//
//    private void checkExpiredPcStates(@NonNull Map<UUID, PCState> expiredPCStates) throws Throwable{
//        if (expiredPCStates.size() < 1) {
//            return;
//        }
//        Queue<PCState> pcStates = new LinkedList<>();
//        pcStates.addAll(expiredPCStates.values());
//        while (!pcStates.isEmpty()) {
//            PCState pcState = pcStates.poll();
//
//            PeerConnectionEntity pcEntity = this.detachPeerConnection(pcState);
//            logger.info("Peer Connection {} is unregistered to Call {}.", pcState, pcEntity.callUUID);
//
//            if (Objects.isNull(pcEntity)) {
//                logger.warn("Detach process has failed {}", pcState);
//                continue;
//            }
//            this.actual.expiredPcUUIDs.add(pcEntity.pcUUID);
//
//            Optional<CallEntity> callEntityHolder = this.calls.findCall(pcEntity.callUUID);
//            if (!callEntityHolder.isPresent()) {
//                logger.warn("Peer connection {} does not belong to any call", pcEntity);
//                continue;
//            }
//            CallEntity callEntity = callEntityHolder.get();
//
//            if (0 < callEntity.peerConnections.size()) {
//                continue;
//            }
//
//            this.finnishCall(callEntity.call.callUUID, pcState.updated, pcState);
//            logger.info("Call is unregistered with a uuid: {}", callEntity);
//
//        }
//    }
//
//    private PeerConnectionEntity detachPeerConnection(@NotNull PCState pcState) {
//
//        PeerConnectionEntity pcEntity = this.calls.removePeerConnection(pcState.peerConnectionUUID);
//        try {
//            var detachedPc = CallMetaReport
//                    .newBuilder()
//                    .setMarker(pcState.marker)
//                    .setCallUUID(pcEntity.callUUID.toString())
//                    .setRoomName(pcState.callName)
//                    .setUserId(pcState.userId)
//                    .setClientId(pcState.browserId)
//                    .setMetaType(ReportType.DETACHED_PEER_CONNECTION.name())
//                    .setMetaPayload(null)
//                    .setSampleSeq(null)
//                    .setServiceName(pcState.serviceName)
//                    .setServiceUUID(pcState.serviceUUID.toString())
//                    .setTimestamp(pcState.created)
//                    .build();
//            this.callMetaReports.onNext(detachedPc);
//            this.observerMetrics.incrementDetachedPCs(pcEntity.peerConnection.serviceName, pcEntity.peerConnection.mediaUnitId);
//        } finally {
//            return pcEntity;
//        }
//    }
//
//    private void finnishCall(UUID callUUID, Long timestamp, PCState pcState) {
//
//        CallEntity callEntity = this.calls.removeCall(callUUID);
//
//        var finishedCall = CallMetaReport
//                .newBuilder()
//                .setMarker(callEntity.call.marker)
//                .setCallUUID(callEntity.call.callUUID.toString())
//                .setRoomName(callEntity.call.callName)
//                .setUserId(pcState.userId)
//                .setClientId(pcState.browserId)
//                .setMetaType(ReportType.FINISHED_CALL.name())
//                .setMetaPayload(null)
//                .setSampleSeq(null)
//                .setServiceName(pcState.serviceName)
//                .setServiceUUID(pcState.serviceUUID.toString())
//                .setTimestamp(pcState.created)
//                .build();
//        this.callMetaReports.onNext(finishedCall);
//        this.observerMetrics.incrementFinishedCall(callEntity.call.serviceName);
//        Long durationInMillis = timestamp - callEntity.call.initiated;
//        Duration callDuration = Duration.ofMillis(durationInMillis);
//        this.observerMetrics.addCallDuration(callEntity.call.serviceName, callDuration);
//    }
//
//    private void reportNoSSRC(UUID callUUID, PCState pcState) {
//        var noSSRCReport = CallMetaReport
//                .newBuilder()
//                .setMarker(pcState.marker)
//                .setCallUUID(callUUID.toString())
//                .setRoomName(pcState.callName)
//                .setUserId(pcState.userId)
//                .setClientId(pcState.browserId)
//                .setMetaType("CustomEvent")
//                .setMetaPayload("No SSRC is provided for this call")
//                .setSampleSeq(null)
//                .setServiceName(pcState.serviceName)
//                .setServiceUUID(pcState.serviceUUID.toString())
//                .setTimestamp(pcState.created)
//                .build();
//        this.callMetaReports.onNext(noSSRCReport);
//    }
//}
