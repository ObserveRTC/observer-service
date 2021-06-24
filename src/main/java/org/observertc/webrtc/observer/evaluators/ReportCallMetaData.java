package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.CallMetaType;
import org.observertc.webrtc.observer.samples.*;
import org.observertc.webrtc.schemas.reports.CallMetaReport;

import javax.inject.Singleton;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@Singleton
public class ReportCallMetaData implements Consumer<CollectedCallSamples> {

    Subject<CallMetaReport> reportSubject = PublishSubject.create();

    public Observable<CallMetaReport> observableCallMetaReports() {
        return this.reportSubject;
    }

    @Override
    public void accept(CollectedCallSamples collectedCallSamples) throws Throwable {
        List<CallMetaReport.Builder> reportBuilders = new LinkedList<>();
        for (CallSamples callSamples : collectedCallSamples) {
            for (ClientSamples clientSamples : callSamples) {
                for (ClientSample clientSample : clientSamples) {
                    Function<CallMetaType, CallMetaReport.Builder> callMetaReport = type -> {
                        return prepareReport(callSamples.getCallId(), clientSamples, clientSample)
                                .setType(type.name());
                    };
                    ClientSampleVisitor.streamUserMediaErrors(clientSample)
                            .map(userMediaError -> {
                                return callMetaReport.apply(CallMetaType.USER_MEDIA_ERROR)
                                        .setPayload(userMediaError);
                            }).forEach(reportBuilders::add);

                    ClientSampleVisitor.streamCertificates(clientSample)
                            .map(certificate -> {
                                var payload = Objects.toString(certificate);
                                return callMetaReport.apply(CallMetaType.CERTIFICATE)
                                        .setPayload(payload);
                            }).forEach(reportBuilders::add);

                    ClientSampleVisitor.streamMediaDevices(clientSample)
                            .map(mediaDevice -> {
                                return callMetaReport.apply(CallMetaType.MEDIA_DEVICE)
                                        .setPayload(mediaDevice);
                            }).forEach(reportBuilders::add);
                }
            }
        }

        reportBuilders.stream()
                .map(CallMetaReport.Builder::build)
                .forEach(this.reportSubject::onNext);
    }

    private CallMetaReport.Builder prepareReport(UUID callId, ObservedSample observedSample, ClientSample clientSample) {
        Long now = Instant.now().toEpochMilli();
        return CallMetaReport.newBuilder()
                .setServiceId(observedSample.getServiceId())
                .setMediaUnitId(observedSample.getMediaUnitId())
                .setRoomId(clientSample.roomId)
                .setCallId(callId.toString())
                .setUserId(clientSample.userId)
                .setClientId(clientSample.clientId)
                .setTimestamp(now)
                .setSampleSeq(clientSample.sampleSeq)
                .setSampleTimestamp(clientSample.timestamp)
                .setMarker(clientSample.marker)
                ;

    }
}
