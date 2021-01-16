package org.observertc.webrtc.observer.evaluators;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.observertc.webrtc.observer.dto.v20200114.PeerConnectionSample;
import org.observertc.webrtc.observer.samples.ObservedPCS;
import org.observertc.webrtc.schemas.reports.*;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;

@MicronautTest
class ObservedPCSEvaluatorTest {

    static TestInputsGenerator generator = new TestInputsGenerator();

    @Inject
    Provider<ObservedPCSEvaluator> subject;

    @Test
    public void shouldValidICELocalCandidateReports() {

        // Given
        List<Report> reportRecords = new LinkedList<>();
        ObservedPCSEvaluator observedPCSObserver = subject.get();
        observedPCSObserver.getICELocalCandidateReports().subscribe(reportRecords::add);

        // When
        ObservedPCS observedPCS = generator.makeObservedPCS();
        Observable.just(observedPCS)
                .subscribe(observedPCSObserver);

        // Then
        Assertions.assertFalse(reportRecords.isEmpty());
        reportRecords.stream().forEach(reportRecord -> validateICELocalCandidate(observedPCS, reportRecord));
    }

    private void validateICELocalCandidate(ObservedPCS observedPCS, Report report) {
        Assertions.assertEquals(ReportType.ICE_LOCAL_CANDIDATE, report.getType());
        ICELocalCandidate reportCandidate = (ICELocalCandidate) report.getPayload();

        Assertions.assertNotNull(observedPCS.peerConnectionSample, "The sample is null");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.iceStats, "No ICEStats");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.iceStats.localCandidates, "No Local ICE candidates");

        Optional<PeerConnectionSample.ICELocalCandidate> statCandidateHolder = Arrays.asList(observedPCS.peerConnectionSample.iceStats.localCandidates)
                .stream()
                .filter(candidate -> reportCandidate.getCandidateId().equals(candidate.id))
                .findFirst();

        Assertions.assertTrue(statCandidateHolder.isPresent());
        PeerConnectionSample.ICELocalCandidate iceLocalCandidate = statCandidateHolder.get();

        Assertions.assertEquals(iceLocalCandidate.port, reportCandidate.getPort());
        Assertions.assertEquals(iceLocalCandidate.priority, reportCandidate.getPriority());
        Assertions.assertEquals(iceLocalCandidate.deleted, reportCandidate.getDeleted());
        Assertions.assertEquals(iceLocalCandidate.transportId, reportCandidate.getTransportID());
        assertEnumValueEquals(iceLocalCandidate.candidateType, reportCandidate.getCandidateType());
        assertEnumValueEquals(iceLocalCandidate.networkType, reportCandidate.getNetworkType());
        Assertions.assertEquals(iceLocalCandidate.id, reportCandidate.getCandidateId());
        Assertions.assertEquals(iceLocalCandidate.ip, reportCandidate.getIpLSH());
        Assertions.assertEquals(iceLocalCandidate.isRemote, reportCandidate.getIsRemote());
        assertEnumValueEquals(iceLocalCandidate.protocol, reportCandidate.getProtocol());

    }

    private<S extends Enum, R extends Enum> boolean assertEnumValueEquals(S enum1, R enum2) {
        if (Objects.isNull(enum1)) {
            return Objects.isNull(enum2);
        }
        if (Objects.isNull(enum2)) {
            return Objects.isNull(enum1);
        }
        String value1 = enum1.name().toLowerCase();
        String value2 = enum2.name().toLowerCase();
        return value1.equals(value2);
    }

    @Test
    public void shouldValidICERemoteCandidateReports() {

        // Given
        List<Report> reportRecords = new LinkedList<>();
        ObservedPCSEvaluator observedPCSObserver = subject.get();
        observedPCSObserver.getICERemoteCandidateReports().subscribe(reportRecords::add);

        // When
        ObservedPCS observedPCS = generator.makeObservedPCS();
        Observable.just(observedPCS)
                .subscribe(observedPCSObserver);

        // Then
        Assertions.assertFalse(reportRecords.isEmpty());
        reportRecords.stream().forEach(reportRecord -> validateICERemoteCandidate(observedPCS, reportRecord));
    }

    private void validateICERemoteCandidate(ObservedPCS observedPCS, Report report) {
        Assertions.assertEquals(ReportType.ICE_REMOTE_CANDIDATE, report.getType());
        ICERemoteCandidate reportCandidate = (ICERemoteCandidate) report.getPayload();

        Assertions.assertNotNull(observedPCS.peerConnectionSample, "The sample is null");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.iceStats, "No ICEStats");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.iceStats.remoteCandidates, "No Local ICE candidates");

        Optional<PeerConnectionSample.ICERemoteCandidate> statCandidateHolder = Arrays.asList(observedPCS.peerConnectionSample.iceStats.remoteCandidates)
                .stream()
                .filter(candidate -> reportCandidate.getCandidateId().equals(candidate.id))
                .findFirst();

        Assertions.assertTrue(statCandidateHolder.isPresent());
        PeerConnectionSample.ICERemoteCandidate iceRemoteCandidate = statCandidateHolder.get();

        Assertions.assertEquals(iceRemoteCandidate.port, reportCandidate.getPort());
        Assertions.assertEquals(iceRemoteCandidate.priority, reportCandidate.getPriority());
        Assertions.assertEquals(iceRemoteCandidate.deleted, reportCandidate.getDeleted());
        Assertions.assertEquals(iceRemoteCandidate.transportId, reportCandidate.getTransportID());
        assertEnumValueEquals(iceRemoteCandidate.candidateType, reportCandidate.getCandidateType());
        Assertions.assertEquals(iceRemoteCandidate.id, reportCandidate.getCandidateId());
        Assertions.assertEquals(iceRemoteCandidate.ip, reportCandidate.getIpLSH());
        Assertions.assertEquals(iceRemoteCandidate.isRemote, reportCandidate.getIsRemote());
        assertEnumValueEquals(iceRemoteCandidate.protocol, reportCandidate.getProtocol());
    }

    @Test
    public void shouldValidICECandidatePairReports() {

        // Given
        List<Report> reportRecords = new LinkedList<>();
        ObservedPCSEvaluator observedPCSObserver = subject.get();
        observedPCSObserver.getICECandidatePairReports().subscribe(reportRecords::add);

        // When
        ObservedPCS observedPCS = generator.makeObservedPCS();
        Observable.just(observedPCS)
                .subscribe(observedPCSObserver);

        // Then
        Assertions.assertFalse(reportRecords.isEmpty());
        reportRecords.stream().forEach(reportRecord -> validateICECandidatePair(observedPCS, reportRecord));
    }

    private void validateICECandidatePair(ObservedPCS observedPCS, Report report) {
        Assertions.assertEquals(ReportType.ICE_CANDIDATE_PAIR, report.getType());
        ICECandidatePair reportCandidate = (ICECandidatePair) report.getPayload();

        Assertions.assertNotNull(observedPCS.peerConnectionSample, "The sample is null");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.iceStats, "No ICEStats");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.iceStats.candidatePairs, "No Local ICE candidates");

        Optional<PeerConnectionSample.ICECandidatePair> statCandidateHolder = Arrays.asList(observedPCS.peerConnectionSample.iceStats.candidatePairs)
                .stream()
                .filter(candidate -> reportCandidate.getCandidatePairId().equals(candidate.id))
                .findFirst();

        Assertions.assertTrue(statCandidateHolder.isPresent());
        PeerConnectionSample.ICECandidatePair iceCandidatePair = statCandidateHolder.get();

        Assertions.assertEquals(iceCandidatePair.priority, reportCandidate.getPriority());
        Assertions.assertEquals(iceCandidatePair.transportId, reportCandidate.getTransportID());
        Assertions.assertEquals(iceCandidatePair.availableOutgoingBitrate, reportCandidate.getAvailableOutgoingBitrate());
        Assertions.assertEquals(iceCandidatePair.localCandidateId, reportCandidate.getLocalCandidateID());
        Assertions.assertEquals(iceCandidatePair.bytesReceived, reportCandidate.getBytesReceived());
        Assertions.assertEquals(iceCandidatePair.bytesSent, reportCandidate.getBytesSent());
        Assertions.assertEquals(iceCandidatePair.consentRequestsSent, reportCandidate.getConsentRequestsSent());
        Assertions.assertEquals(iceCandidatePair.currentRoundTripTime, reportCandidate.getCurrentRoundTripTime());
        Assertions.assertEquals(iceCandidatePair.id, reportCandidate.getCandidatePairId());
        Assertions.assertEquals(iceCandidatePair.nominated, reportCandidate.getNominated());
        Assertions.assertEquals(iceCandidatePair.remoteCandidateId, reportCandidate.getRemoteCandidateID());
        Assertions.assertEquals(iceCandidatePair.requestsReceived, reportCandidate.getRequestsReceived());
        Assertions.assertEquals(iceCandidatePair.responsesSent, reportCandidate.getResponsesSent());
        assertEnumValueEquals(iceCandidatePair.state, reportCandidate.getState());
        Assertions.assertEquals(iceCandidatePair.totalRoundTripTime, reportCandidate.getTotalRoundTripTime());
        Assertions.assertEquals(iceCandidatePair.writable, reportCandidate.getWritable());
        Assertions.assertEquals(iceCandidatePair.requestsSent, reportCandidate.getRequestsSent());
        Assertions.assertEquals(iceCandidatePair.responsesReceived, reportCandidate.getResponsesReceived());
    }

    @Test
    public void shouldValidInboundRTPReports() {

        // Given
        List<Report> reportRecords = new LinkedList<>();
        ObservedPCSEvaluator observedPCSObserver = subject.get();
        observedPCSObserver.getInboundRTPReports().subscribe(reportRecords::add);

        // When
        ObservedPCS observedPCS = generator.makeObservedPCS();
        Observable.just(observedPCS)
                .subscribe(observedPCSObserver);

        // Then
        Assertions.assertFalse(reportRecords.isEmpty());
        reportRecords.stream().forEach(reportRecord -> validateInboundRTPReport(observedPCS, reportRecord));
    }

    private void validateInboundRTPReport(ObservedPCS observedPCS, Report report) {
        Assertions.assertEquals(ReportType.INBOUND_RTP, report.getType());
        InboundRTP statCandidate = (InboundRTP) report.getPayload();

        Assertions.assertNotNull(observedPCS.peerConnectionSample, "The sample is null");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.senderStats, "No SenderStats");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.receiverStats, "No ReceiverStats");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.senderStats.inboundRTPStats, "No inboundRTPStats");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.receiverStats.inboundRTPStats, "No inboundRTPStats");

        Optional<PeerConnectionSample.InboundRTPStreamStats> rtcStatHolder = Arrays.asList(observedPCS.peerConnectionSample.senderStats.inboundRTPStats, observedPCS.peerConnectionSample.receiverStats.inboundRTPStats)
                .stream()
                .flatMap(Arrays::stream)
                .filter(rtcStat -> statCandidate.getId().equals(rtcStat.id))
                .findFirst();

        Assertions.assertTrue(rtcStatHolder.isPresent());
        PeerConnectionSample.InboundRTPStreamStats rtcStat = rtcStatHolder.get();

        Assertions.assertEquals(rtcStat.id, statCandidate.getId());
        Assertions.assertEquals(rtcStat.packetsLost, statCandidate.getPacketsLost());
        Assertions.assertEquals(rtcStat.bytesReceived, statCandidate.getBytesReceived());
        Assertions.assertEquals(rtcStat.headerBytesReceived, statCandidate.getHeaderBytesReceived());
        Assertions.assertEquals(rtcStat.estimatedPlayoutTimestamp, statCandidate.getEstimatedPlayoutTimestamp());
        Assertions.assertEquals(rtcStat.fecPacketsDiscarded, statCandidate.getFecPacketsDiscarded());
        Assertions.assertEquals(rtcStat.fecPacketsReceived, statCandidate.getFecPacketsReceived());
        Assertions.assertEquals(rtcStat.firCount, statCandidate.getFirCount());
        Assertions.assertEquals(rtcStat.framesDecoded, statCandidate.getFramesDecoded());

        Assertions.assertEquals(rtcStat.keyFramesDecoded, statCandidate.getKeyFramesDecoded());
        Assertions.assertEquals(rtcStat.nackCount, statCandidate.getNackCount());
        Assertions.assertEquals(rtcStat.pliCount, statCandidate.getPliCount());
        Assertions.assertEquals(rtcStat.packetsReceived, statCandidate.getPacketsReceived());
        Assertions.assertEquals(rtcStat.jitter, statCandidate.getJitter());
        Assertions.assertEquals(rtcStat.qpSum, statCandidate.getQpSum());
        Assertions.assertEquals(rtcStat.ssrc, statCandidate.getSsrc());
        Assertions.assertEquals(rtcStat.totalDecodeTime, statCandidate.getTotalDecodeTime());
        Assertions.assertEquals(rtcStat.totalInterFrameDelay, statCandidate.getTotalInterFrameDelay());
        Assertions.assertEquals(rtcStat.totalSquaredInterFrameDelay, statCandidate.getTotalSquaredInterFrameDelay());

        Assertions.assertEquals(rtcStat.lastPacketReceivedTimestamp, statCandidate.getLastPacketReceivedTimestamp());
        Assertions.assertEquals(rtcStat.codecId, statCandidate.getCodecId());
        Assertions.assertEquals(rtcStat.trackId, statCandidate.getTrackId());
        Assertions.assertEquals(rtcStat.transportId, statCandidate.getTransportId());
        Assertions.assertEquals(rtcStat.isRemote, statCandidate.getIsRemote());
        assertEnumValueEquals(rtcStat.mediaType, statCandidate.getMediaType());
        Assertions.assertEquals(rtcStat.decoderImplementation, statCandidate.getDecoderImplementation());

    }

    @Test
    public void shouldValidOutboundRTPReports() {

        // Given
        List<Report> reportRecords = new LinkedList<>();
        ObservedPCSEvaluator observedPCSObserver = subject.get();
        observedPCSObserver.getOutboundRTPReports().subscribe(reportRecords::add);

        // When
        ObservedPCS observedPCS = generator.makeObservedPCS();
        Observable.just(observedPCS)
                .subscribe(observedPCSObserver);

        // Then
        Assertions.assertFalse(reportRecords.isEmpty());
        reportRecords.stream().forEach(reportRecord -> validateOutboundRTPReport(observedPCS, reportRecord));
    }

    private void validateOutboundRTPReport(ObservedPCS observedPCS, Report report) {
        Assertions.assertEquals(ReportType.OUTBOUND_RTP, report.getType());
        OutboundRTP statCandidate = (OutboundRTP) report.getPayload();

        Assertions.assertNotNull(observedPCS.peerConnectionSample, "The sample is null");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.senderStats, "No SenderStats");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.receiverStats, "No ReceiverStats");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.senderStats.outboundRTPStats, "No outboundRTPStats");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.receiverStats.outboundRTPStats, "No outboundRTPStats");

        Optional<PeerConnectionSample.OutboundRTPStreamStats> rtcStatHolder = Arrays.asList(observedPCS.peerConnectionSample.senderStats.outboundRTPStats, observedPCS.peerConnectionSample.receiverStats.outboundRTPStats)
                .stream()
                .flatMap(Arrays::stream)
                .filter(rtcStat -> statCandidate.getId().equals(rtcStat.id))
                .findFirst();

        Assertions.assertTrue(rtcStatHolder.isPresent());
        PeerConnectionSample.OutboundRTPStreamStats rtcStat = rtcStatHolder.get();

        Assertions.assertEquals(rtcStat.id, statCandidate.getId());
        Assertions.assertEquals(rtcStat.bytesSent, statCandidate.getBytesSent());
        Assertions.assertEquals(rtcStat.headerBytesSent, statCandidate.getHeaderBytesSent());
        Assertions.assertEquals(rtcStat.framesEncoded, statCandidate.getFramesEncoded());
        Assertions.assertEquals(rtcStat.qualityLimitationResolutionChanges, statCandidate.getQualityLimitationResolutionChanges());
        assertEnumValueEquals(rtcStat.qualityLimitationReason, statCandidate.getQualityLimitationReason());
        Assertions.assertEquals(rtcStat.firCount, statCandidate.getFirCount());
        Assertions.assertEquals(rtcStat.mediaSourceId, statCandidate.getMediaSourceID());
        assertEnumValueEquals(rtcStat.mediaType, statCandidate.getMediaType());
        Assertions.assertEquals(rtcStat.remoteId, statCandidate.getRemoteID());

        Assertions.assertEquals(rtcStat.keyFramesEncoded, statCandidate.getKeyFramesEncoded());
        Assertions.assertEquals(rtcStat.nackCount, statCandidate.getNackCount());
        Assertions.assertEquals(rtcStat.pliCount, statCandidate.getPliCount());
        Assertions.assertEquals(rtcStat.packetsSent, statCandidate.getPacketsSent());
        Assertions.assertEquals(rtcStat.qpSum, statCandidate.getQpSum());
        Assertions.assertEquals(rtcStat.ssrc, statCandidate.getSsrc());
        Assertions.assertEquals(rtcStat.totalEncodedBytesTarget, statCandidate.getTotalEncodedBytesTarget());
        Assertions.assertEquals(rtcStat.totalEncodeTime, statCandidate.getTotalEncodeTime());
        Assertions.assertEquals(rtcStat.totalPacketSendDelay, statCandidate.getTotalPacketSendDelay());

        Assertions.assertEquals(rtcStat.codecId, statCandidate.getCodecID());
        Assertions.assertEquals(rtcStat.trackId, statCandidate.getTrackID());
        Assertions.assertEquals(rtcStat.transportId, statCandidate.getTransportID());
        Assertions.assertEquals(rtcStat.isRemote, statCandidate.getIsRemote());
        assertEnumValueEquals(rtcStat.mediaType, statCandidate.getMediaType());
        Assertions.assertEquals(rtcStat.encoderImplementation, statCandidate.getEncoderImplementation());
    }

    @Test
    public void shouldValidRemoteInboundRTPReports() {

        // Given
        List<Report> reportRecords = new LinkedList<>();
        ObservedPCSEvaluator observedPCSObserver = subject.get();
        observedPCSObserver.getRemoteInboundRTPReports().subscribe(reportRecords::add);

        // When
        ObservedPCS observedPCS = generator.makeObservedPCS();
        Observable.just(observedPCS)
                .subscribe(observedPCSObserver);

        // Then
        Assertions.assertFalse(reportRecords.isEmpty());
        reportRecords.stream().forEach(reportRecord -> validateRemoteInboundRTPReport(observedPCS, reportRecord));
    }

    private void validateRemoteInboundRTPReport(ObservedPCS observedPCS, Report report) {
        Assertions.assertEquals(ReportType.REMOTE_INBOUND_RTP, report.getType());
        RemoteInboundRTP statCandidate = (RemoteInboundRTP) report.getPayload();

        Assertions.assertNotNull(observedPCS.peerConnectionSample, "The sample is null");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.senderStats, "No SenderStats");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.receiverStats, "No ReceiverStats");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.senderStats.remoteInboundRTPStats, "No remoteInboundRTPStats");
        Assertions.assertNotNull(observedPCS.peerConnectionSample.receiverStats.remoteInboundRTPStats, "No remoteInboundRTPStats");

        Optional<PeerConnectionSample.RemoteInboundRTPStreamStats> rtcStatHolder = Arrays.asList(observedPCS.peerConnectionSample.senderStats.remoteInboundRTPStats, observedPCS.peerConnectionSample.receiverStats.remoteInboundRTPStats)
                .stream()
                .flatMap(Arrays::stream)
                .filter(rtcStat -> statCandidate.getId().equals(rtcStat.id))
                .findFirst();

        Assertions.assertTrue(rtcStatHolder.isPresent());
        PeerConnectionSample.RemoteInboundRTPStreamStats rtcStat = rtcStatHolder.get();

        Assertions.assertEquals(rtcStat.id, statCandidate.getId());
        Assertions.assertEquals(rtcStat.packetsLost, statCandidate.getPacketsLost());
        Assertions.assertEquals(rtcStat.jitter, statCandidate.getJitter(), 0.0001);
        Assertions.assertEquals(rtcStat.ssrc, statCandidate.getSsrc());
        Assertions.assertEquals(rtcStat.codecId, statCandidate.getCodecID());
        Assertions.assertEquals(rtcStat.transportId, statCandidate.getTransportID());
        assertEnumValueEquals(rtcStat.mediaType, statCandidate.getMediaType());
        Assertions.assertEquals(rtcStat.localId, statCandidate.getLocalID());
        assertEnumValueEquals(rtcStat.mediaType, statCandidate.getMediaType());
    }

    @Test
    public void shouldValidTrackReports() {

        // Given
        List<Report> reportRecords = new LinkedList<>();
        ObservedPCSEvaluator observedPCSObserver = subject.get();
        observedPCSObserver.getTrackReports().subscribe(reportRecords::add);

        // When
        ObservedPCS observedPCS = generator.makeObservedPCS();
        Observable.just(observedPCS)
                .subscribe(observedPCSObserver);

        // Then
        Assertions.assertFalse(reportRecords.isEmpty());
    }

    @Test
    public void shouldValidMediaSourceReports() {

        // Given
        List<Report> reportRecords = new LinkedList<>();
        ObservedPCSEvaluator observedPCSObserver = subject.get();
        observedPCSObserver.getMediaSourceReports().subscribe(reportRecords::add);

        // When
        ObservedPCS observedPCS = generator.makeObservedPCS();
        Observable.just(observedPCS)
                .subscribe(observedPCSObserver);

        // Then
        Assertions.assertFalse(reportRecords.isEmpty());
    }

    @Test
    public void shouldValidUserMediaErrorReports() {

        // Given
        List<Report> reportRecords = new LinkedList<>();
        ObservedPCSEvaluator observedPCSObserver = subject.get();
        observedPCSObserver.getUserMediaErrorReports().subscribe(reportRecords::add);

        // When
        ObservedPCS observedPCS = generator.makeObservedPCS();
        Observable.just(observedPCS)
                .subscribe(observedPCSObserver);

        // Then
        Assertions.assertFalse(reportRecords.isEmpty());
    }

}