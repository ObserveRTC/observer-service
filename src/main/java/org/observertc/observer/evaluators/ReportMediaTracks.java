//package org.observertc.webrtc.observer.evaluators;
//
//import io.micronaut.context.annotation.Prototype;
//import io.reactivex.rxjava3.functions.Consumer;
//import org.observertc.webrtc.observer.repositories.tasks.FindRemoteClientIdsForMediaTrackKeys;
//import org.observertc.webrtc.observer.samples.*;
//import org.observertc.webrtc.schemas.reports.InboundAudioTrackReport;
//
//import javax.inject.Inject;
//import javax.inject.Provider;
//import java.util.Collections;
//import java.util.Map;
//import java.util.Set;
//import java.util.UUID;
//
//@Prototype
//public class ReportMediaTracks implements Consumer<CollectedCallSamples> {
//
//    @Inject
//    Provider<FindRemoteClientIdsForMediaTrackKeys> findRemoteClientIdsForMediaTrackKeysProvider;
//
//    @Override
//    public void accept(CollectedCallSamples collectedCallSamples) throws Throwable {
//        Set<MediaTrackId> inboundMediaTrackIds = collectedCallSamples.getInboundMediaTrackIds();
//        var findRemoteClientIds = findRemoteClientIdsForMediaTrackKeysProvider.get();
//        findRemoteClientIds
//                .whereMediaTrackIds(inboundMediaTrackIds)
//                .withUnmodifiableResult(true);
//        Map<MediaTrackId, UUID> inboundTrackIdToRemoteClientIds;
//        if (findRemoteClientIds.execute().succeeded()) {
//            inboundTrackIdToRemoteClientIds = findRemoteClientIds.getResult();
//        } else {
//            inboundTrackIdToRemoteClientIds = Collections.EMPTY_MAP;
//        }
//
//        for (CallSamples callSamples : collectedCallSamples) {
//            for (ClientSamples clientSamples: callSamples) {
//                ObservedSample observedSample = clientSamples;
//                for (ClientSample clientSample : clientSamples) {
//                    this.sendInboundAudioMediaTrackReports(
//                            callSamples.getCallId(),
//                            observedSample,
//                            clientSample
//                    );
//                }
//            }
//        }
//    }
//
//    private void sendInboundAudioMediaTrackReports(
//            UUID callId,
//            ObservedSample observedSample,
//            ClientSample clientSample
//    ) {
//        ClientSampleVisitor.streamInboundAudioTracks(clientSample).forEach(trackStat -> {
//            var report = InboundAudioTrackReport.newBuilder()
//                    .setServiceId(observedSample.getServiceId())
//                    .setMediaUnitId(observedSample.getMediaUnitId())
//
//                    .build();
//        });
//
//    }
//}
