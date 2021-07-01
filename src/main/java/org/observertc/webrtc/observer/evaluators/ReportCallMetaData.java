package org.observertc.webrtc.observer.evaluators;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.CallMetaType;
import org.observertc.webrtc.observer.common.ObjectToString;
import org.observertc.webrtc.observer.samples.*;
import org.observertc.webrtc.schemas.reports.CallMetaReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Singleton
public class ReportCallMetaData implements Consumer<CollectedCallSamples> {

    private static final Logger logger = LoggerFactory.getLogger(ReportCallMetaData.class);

    Subject<CallMetaReport> reportSubject = PublishSubject.create();

    public Observable<CallMetaReport> observableCallMetaReports() {
        return this.reportSubject;
    }

    @Override
    public void accept(CollectedCallSamples collectedCallSamples) throws Throwable {
        List<CallMetaReport.Builder> reportBuilders = new LinkedList<>();
        for (CallSamples callSamples : collectedCallSamples) {
            var callId = callSamples.getCallId();
            for (ClientSamples clientSamples : callSamples) {
                var observedClientSample = clientSamples;
                for (ClientSample clientSample : clientSamples) {

                    Supplier<CallMetaReport.Builder> createCallMetaReport = () ->
                            prepareReport(callId, observedClientSample, clientSample);

                    // streamCertificates
                    ClientSampleVisitor.streamCertificates(clientSample)
                            .map(certificate -> this.createCertificateReportBuilder(createCallMetaReport.get(), certificate))
                            .forEach(reportBuilders::add);

                    // streamCodecs
                    ClientSampleVisitor.streamCodecs(clientSample)
                            .map(codec -> this.createCodecReportBuilder(createCallMetaReport.get(), codec))
                            .forEach(reportBuilders::add);

                    // streamIceLocalCandidates
                    ClientSampleVisitor.streamIceLocalCandidates(clientSample)
                            .map(iceLocalCandidate -> this.createIceLocalCandidateReportBuilder(createCallMetaReport.get(), iceLocalCandidate))
                            .forEach(reportBuilders::add);

                    // streamIceRemoteCandidates
                    ClientSampleVisitor.streamIceRemoteCandidates(clientSample)
                            .map(iceRemoteCandidate -> this.createIceRemoteCandidateReportBuilder(createCallMetaReport.get(), iceRemoteCandidate))
                            .forEach(reportBuilders::add);

                    // streamIceServers
                    ClientSampleVisitor.streamIceServers(clientSample)
                            .map(iceServer -> this.createIceServerReportBuilder(createCallMetaReport.get(), iceServer))
                            .forEach(reportBuilders::add);

                    // streamMediaConstraints
                    ClientSampleVisitor.streamMediaConstraints(clientSample)
                            .map(mediaConstraint -> this.createMediaConstraintReportBuilder(createCallMetaReport.get(), mediaConstraint))
                            .forEach(reportBuilders::add);

                    // streamMediaDevices
                    ClientSampleVisitor.streamMediaDevices(clientSample)
                            .map(mediaDevice -> this.createMediaDeviceReportBuilder(createCallMetaReport.get(), mediaDevice))
                            .forEach(reportBuilders::add);

                    // streamMediaSources
                    ClientSampleVisitor.streamMediaSources(clientSample)
                            .map(mediaSourceStat -> this.createMediaSourceReportBuilder(createCallMetaReport.get(), mediaSourceStat))
                            .forEach(reportBuilders::add);

                    // streamUserMediaErrors
                    ClientSampleVisitor.streamUserMediaErrors(clientSample)
                            .map(userMediaError -> this.createUserMediaErrorReportBuilder(createCallMetaReport.get(), userMediaError))
                            .forEach(reportBuilders::add);
                }
            }
        }

        reportBuilders.stream()
                .filter(this::validateMetaReportBuilder)
                .map(CallMetaReport.Builder::build)
                .forEach(this.reportSubject::onNext);
    }

    private CallMetaReport.Builder createCertificateReportBuilder(CallMetaReport.Builder builder, ClientSample.Certificate certificate) {
        String payload = ObjectToString.toString(certificate);
        return builder
                .setType(CallMetaType.CERTIFICATE.name())
                .setPayload(payload)
                ;
    }

    private CallMetaReport.Builder createCodecReportBuilder(CallMetaReport.Builder builder, ClientSample.Codec codec) {
        String payload = ObjectToString.toString(codec);
        return builder
                .setType(CallMetaType.CODEC.name())
                .setPayload(payload)
                ;
    }

    private CallMetaReport.Builder createIceLocalCandidateReportBuilder(CallMetaReport.Builder builder, ClientSample.ICELocalCandidate localCandidate) {
        String payload = ObjectToString.toString(localCandidate);
        return builder
                .setType(CallMetaType.ICE_LOCAL_CANDIDATE.name())
                .setPayload(payload)
                ;
    }

    private CallMetaReport.Builder createIceRemoteCandidateReportBuilder(CallMetaReport.Builder builder, ClientSample.ICERemoteCandidate remoteCandidate) {
        String payload = ObjectToString.toString(remoteCandidate);
        return builder
                .setType(CallMetaType.ICE_REMOTE_CANDIDATE.name())
                .setPayload(payload)
                ;
    }

    private CallMetaReport.Builder createIceServerReportBuilder(CallMetaReport.Builder builder, String iceServer) {
        return builder
                .setType(CallMetaType.ICE_SERVER.name())
                .setPayload(iceServer)
                ;
    }

    private CallMetaReport.Builder createMediaConstraintReportBuilder(CallMetaReport.Builder builder, String mediaConstraint) {
        return builder
                .setType(CallMetaType.MEDIA_CONSTRAINT.name())
                .setPayload(mediaConstraint)
                ;
    }

    private CallMetaReport.Builder createMediaDeviceReportBuilder(CallMetaReport.Builder builder, String mediaDevice) {
        return builder
                .setType(CallMetaType.MEDIA_DEVICE.name())
                .setPayload(mediaDevice)
                ;
    }

    private CallMetaReport.Builder createMediaSourceReportBuilder(CallMetaReport.Builder builder, ClientSample.MediaSourceStat mediaSource) {
        String payload = ObjectToString.toString(mediaSource);
        return builder
                .setType(CallMetaType.MEDIA_SOURCE.name())
                .setPayload(payload)
                ;
    }

    private CallMetaReport.Builder createUserMediaErrorReportBuilder(CallMetaReport.Builder builder, String userMediaError) {
        return builder
                .setType(CallMetaType.USER_MEDIA_ERROR.name())
                .setPayload(userMediaError)
                ;
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

    private boolean validateMetaReportBuilder(CallMetaReport.Builder builder) {
        // check if some info is invalid
        if (Objects.isNull(builder.getTimestamp())) {
            logger.warn("Builder validation failed: No timestamp");
            return false;
        }
        return true;
    }
}
