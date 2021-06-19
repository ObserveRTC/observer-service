//package org.observertc.webrtc.observer.evaluators.pcSampleToReportsV3;
//
//import io.reactivex.rxjava3.core.Observable;
//import io.reactivex.rxjava3.subjects.PublishSubject;
//import io.reactivex.rxjava3.subjects.Subject;
//import org.observertc.webrtc.observer.common.TimeLimitedMap;
//import org.observertc.webrtc.observer.ObserverConfig;
//import org.observertc.webrtc.observer.configs.ObserverConfigDispatcher;
//import org.observertc.webrtc.observer.dto.CallDTO;
//import org.observertc.webrtc.observer.dto.PeerConnectionDTO;
//import org.observertc.webrtc.observer.entities.CallEntity;
//import org.observertc.webrtc.observer.entities.PeerConnectionEntity;
//import org.observertc.webrtc.schemas.reports.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.annotation.PostConstruct;
//import javax.inject.Inject;
//import javax.inject.Singleton;
//import java.time.Duration;
//import java.util.Objects;
//import java.util.UUID;
//
//import static org.observertc.webrtc.observer.evaluators.pcSampleToReportsV2.Pipeline.REPORT_VERSION_NUMBER;
//
//@Singleton
//public class CallUUIDs {
//    private static final Logger logger = LoggerFactory.getLogger(CallUUIDs.class);
//
//    private Subject<ClientMetaReport> clientMetaReports = PublishSubject.create();
//
//    public Observable<ClientMetaReport> getClientMetaReports() {
//        return clientMetaReports;
//    }
//
//    private TimeLimitedMap<UUID, UUID> pcsToCalls;
//
//    @Inject
//    ObserverConfigDispatcher configDispatcher;
//
//    ObserverConfig.EvaluatorsConfig config;
//
//    @PostConstruct
//    void setup() {
//        this.config = configDispatcher.getConfig().evaluators;
//        var maxIdleTimeInS = this.config.peerConnectionMaxIdleTimeInS;
//        this.pcsToCalls = new TimeLimitedMap<UUID, UUID>(Duration.ofSeconds(maxIdleTimeInS)).withRemovedCb(this::onRemovedCallUUID);
//    }
//
//    public UUID getCallUUID(UUID pcUUID) {
//        return null;
//    }
//
//    private void onRemovedCallUUID(UUID callUUID) {
//
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
//                .build();
//
//        if (!this.config.impairablePCsCallName.equals(pcEntity.peerConnection.callName)) {
//            logger.info("Peer Connection {} is registered to Call {}.", pcState.peerConnectionUUID, callUUID);
//        }
//
//        pcEntity = this.calls.addPeerConnection(pcEntity);
//
//        try {
//            JoinedPeerConnection joinedPC = JoinedPeerConnection.newBuilder()
//                    .setBrowserId(pcEntity.peerConnection.browserId)
//                    .setMediaUnitId(pcEntity.peerConnection.mediaUnitId)
//                    .setTimeZoneId(pcState.timeZoneId)
//                    .setCallUUID(pcEntity.callUUID.toString())
//                    .setPeerConnectionUUID(pcEntity.peerConnection.peerConnectionUUID.toString())
//                    .build();
//
//            Report report = Report.newBuilder()
//                    .setVersion(REPORT_VERSION_NUMBER)
//                    .setServiceUUID(pcEntity.serviceUUID.toString())
//                    .setServiceName(pcEntity.peerConnection.serviceName)
//                    .setMarker(pcEntity.peerConnection.marker)
//                    .setType(ReportType.JOINED_PEER_CONNECTION)
//                    .setTimestamp(pcEntity.peerConnection.joined)
//                    .setPayload(joinedPC)
//                    .build();
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
//        try {
//            Object payload = InitiatedCall.newBuilder()
//                    .setCallUUID(callEntity.call.callUUID.toString())
//                    .setCallName(callEntity.call.callName)
//                    .build();
//            Report report = Report.newBuilder()
//                    .setVersion(REPORT_VERSION_NUMBER)
//                    .setServiceUUID(callEntity.call.serviceUUID.toString())
//                    .setServiceName(callEntity.call.serviceName)
//                    .setMarker(callEntity.call.marker)
//                    .setType(ReportType.INITIATED_CALL)
//                    .setTimestamp(callEntity.call.initiated)
//                    .setPayload(payload)
//                    .build();
//        } finally {
//            return callEntity;
//        }
//    }
//}
