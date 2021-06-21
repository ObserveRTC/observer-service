package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.samples.*;
import org.observertc.webrtc.schemas.reports.PcTransportReport;

import java.util.UUID;

public class ReportPeerConnectionTransports implements Consumer<CollectedCallSamples> {

    private Subject<PcTransportReport> reports = PublishSubject.create();

    @Override
    public void accept(CollectedCallSamples collectedCallSamples) throws Throwable {
        for (CallSamples callSamples : collectedCallSamples) {
            for (ClientSamples clientSamples: callSamples) {
                ObservedSample observedSample = clientSamples;
                for (ClientSample clientSample : clientSamples) {
                    this.createPeerConnectionTransportReport(
                            callSamples.getCallId(),
                            observedSample,
                            clientSample,
                    );


                }
            }
        }
    }

    private void createPeerConnectionTransportReport(
        UUID callId,
        ObservedClientSample observedSample,
        ClientSample clientSample
    ) {
        ClientSampleVisitor.streamPeerConnectionTransports(clientSample)
                .map(peerConnectionTransport ->
                    PcTransportReport.newBuilder()
                            .setServiceId(observedSample.getServiceId())
                            .setMediaUnitId(observedSample.getMediaUnitId())
                            .setAvailableIncomingBitrate(peerConnectionTransport.candidatePairAvailableIncomingBitrate)

                        .build()
                );
    }
}
