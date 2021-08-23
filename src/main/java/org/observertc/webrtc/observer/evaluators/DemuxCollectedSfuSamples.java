package org.observertc.webrtc.observer.evaluators;

import io.micrometer.core.annotation.Timed;
import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.repositories.tasks.FetchSfuRelationsTask;
import org.observertc.webrtc.observer.samples.*;
import org.observertc.webrtc.schemas.reports.SFUTransportReport;
import org.observertc.webrtc.schemas.reports.SfuRTPSinkReport;
import org.observertc.webrtc.schemas.reports.SfuRTPSourceReport;
import org.observertc.webrtc.schemas.reports.SfuSctpStreamReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;

import static org.observertc.webrtc.observer.micrometer.ExposedMetrics.OBSERVERTC_EVALUATORS_DEMUX_COLLECTED_SFU_SAMPLES_TIME;

@Prototype
public class DemuxCollectedSfuSamples implements Consumer<CollectedSfuSamples> {
    private static final Logger logger = LoggerFactory.getLogger(DemuxCollectedSfuSamples.class);
//    private static final String _____missingFromSample____ = "I did not find it in the ClientSample";
//    private static final String _____missingByBound____ = "I did not find it in to be bound from the observed sample?";
    private Subject<SFUTransportReport> sfuTransportReportSubject = PublishSubject.create();
    private Subject<SfuRTPSourceReport> sfuRtpSourceReportSubject = PublishSubject.create();
    private Subject<SfuRTPSinkReport> sfuRtpSinkReportSubject = PublishSubject.create();
    private Subject<SfuSctpStreamReport> sfuSctpStreamSubject = PublishSubject.create();

    public Observable<SFUTransportReport> getSfuTransportReport() {
        return this.sfuTransportReportSubject;
    }
    public Observable<SfuRTPSourceReport> getSfuRtpSourceReport() {return this.sfuRtpSourceReportSubject; }
    public Observable<SfuRTPSinkReport> getSfuRtpSinkReport() {return this.sfuRtpSinkReportSubject; }

    public Observable<SfuSctpStreamReport> getSctpStreamReport() {return this.sfuSctpStreamSubject; }


    @Inject
    ObserverConfig observerConfig;

    @Inject
    Provider<FetchSfuRelationsTask> fetchSfuRelationsTaskProvider;

    @PostConstruct
    void setup() {

    }

    @Override
    @Timed(value = OBSERVERTC_EVALUATORS_DEMUX_COLLECTED_SFU_SAMPLES_TIME)
    public void accept(CollectedSfuSamples collectedSfuSamples) throws Throwable {
        try {
            this.doAccept(collectedSfuSamples);
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred while demultiplexing collected samples", ex);
        }
    }

    public void doAccept(CollectedSfuSamples collectedSfuSamples) throws Throwable {
        FetchSfuRelationsTask.Report report = this.fetchSfuRelations(collectedSfuSamples);

        List<SFUTransportReport> transportReports = new LinkedList<>();
        List<SfuRTPSourceReport> rtpSourceReports = new LinkedList<>();
        List<SfuRTPSinkReport> rtpSinkReports = new LinkedList<>();
        List<SfuSctpStreamReport> sctpStreamReports = new LinkedList<>();
        for (SfuSamples sfuSamples : collectedSfuSamples) {
            UUID sfuId = sfuSamples.getSfuId();
            for (ObservedSfuSample observedSfuSample: sfuSamples) {
                SfuSample sfuSample = observedSfuSample.getSfuSample();
                SfuSampleVisitor.streamTransports(sfuSample)
                        .map(sfuTransport -> {
                            UUID transportId = UUIDAdapter.tryParseOrNull(sfuTransport.transportId);
                            UUID callId = report.transportToCallIds.get(transportId);
                            return this.createSfuTransportReport(
                                    sfuId,
                                    observedSfuSample,
                                    sfuTransport,
                                    callId
                            );

                        })
                        .filter(Objects::nonNull)
                        .forEach(transportReports::add);

                SfuSampleVisitor.streamRtpSources(sfuSample)
                        .map(inboundRtpStream -> {
                            UUID trackId = null;
                            UUID clientId = null;
                            UUID callId = null;
                            UUID sfuStreamId = UUIDAdapter.tryParseOrNull(inboundRtpStream.streamId);
                            FetchSfuRelationsTask.SfuStreamRelations relations = report.sfuStreamRelations.get(sfuStreamId);
                            if (Objects.nonNull(relations)) {
                                trackId = relations.trackId;
                                clientId = relations.clientId;
                                callId = relations.callId;
                            }
                            return this.createSfuRtpSourceReport(
                                    sfuId,
                                    observedSfuSample,
                                    inboundRtpStream,
                                    trackId,
                                    clientId,
                                    callId
                            );

                        })
                        .filter(Objects::nonNull)
                        .forEach(rtpSourceReports::add);

                SfuSampleVisitor.streamRtpSinks(sfuSample)
                        .map(outboundRtpStream -> {
                            UUID trackId = null;
                            UUID clientId = null;
                            UUID callId = null;
                            UUID sfuStreamId = UUIDAdapter.tryParseOrNull(outboundRtpStream.streamId);
                            FetchSfuRelationsTask.SfuStreamRelations relations = report.sfuStreamRelations.get(sfuStreamId);
                            if (Objects.nonNull(relations)) {
                                trackId = relations.trackId;
                                clientId = relations.clientId;
                                callId = relations.callId;
                            }
                            return this.createSfuRtpSinkReport(
                                    sfuId,
                                    observedSfuSample,
                                    outboundRtpStream,
                                    trackId,
                                    clientId,
                                    callId
                            );

                        })
                        .filter(Objects::nonNull)
                        .forEach(rtpSinkReports::add);

                SfuSampleVisitor.streamSctpStreams(sfuSample)
                        .map(sctpStream -> {
                            return this.createSfuSctpStreamReport(
                                    sfuId,
                                    observedSfuSample,
                                    sctpStream
                            );

                        })
                        .filter(Objects::nonNull)
                        .forEach(sctpStreamReports::add);
            }
        }
        synchronized (this) {
            if (observerConfig.outboundReports.reportSfuTransports) {
                transportReports.stream().forEach(this.sfuTransportReportSubject::onNext);
            }
            if (observerConfig.outboundReports.reportSfuRtpSources) {
                rtpSourceReports.stream().forEach(this.sfuRtpSourceReportSubject::onNext);
            }
            if (observerConfig.outboundReports.reportSfuRtpSinks) {
                rtpSinkReports.stream().forEach(this.sfuRtpSinkReportSubject::onNext);
            }
            if (observerConfig.outboundReports.reportSfuSctpStreams) {
                sctpStreamReports.stream().forEach(this.sfuSctpStreamSubject::onNext);
            }
        }
    }

    private SFUTransportReport createSfuTransportReport(
            UUID sfuId,
            ObservedSfuSample observedSfuSample,
            SfuSample.SfuTransport sfuTransport,
            UUID callId
    ) {
        String sfuIdStr = UUIDAdapter.toStringOrNull(sfuId);
        String callIdStr = UUIDAdapter.toStringOrNull(callId);

        try {
            var result = SFUTransportReport.newBuilder()

                    /* Report MetaFields */
                    .setMediaUnitId(observedSfuSample.getMediaUnitId())
                    .setMarker(observedSfuSample.getMarker())
                    .setTimestamp(observedSfuSample.getTimestamp())


                    /* Report Fields */
                    .setTransportId(sfuTransport.transportId)
                    .setSfuId(sfuIdStr)
                    .setCallId(callIdStr)

                    /* Transport stats */
                    .setDtlsState(sfuTransport.dtlsState)
                    .setIceState(sfuTransport.iceState)
                    .setSctpState(sfuTransport.sctpState)
                    .setIceRole(sfuTransport.iceRole)
                    .setLocalAddress(sfuTransport.localAddress)
                    .setLocalPort(sfuTransport.localPort)
                    .setProtocol(sfuTransport.protocol)
                    .setRemoteAddress(sfuTransport.remoteAddress)
                    .setRemotePort(sfuTransport.remotePort)

                    /* RTP related stats */
                    .setRtpBytesReceived(sfuTransport.rtpBytesReceived)
                    .setRtpBytesSent(sfuTransport.rtpBytesSent)
                    .setRtpPacketsReceived(sfuTransport.rtpPacketsReceived)
                    .setRtpPacketsSent(sfuTransport.rtpPacketsSent)
                    .setRtpPacketsLost(sfuTransport.rtpPacketsLost)

                    /* RTX related stats */
                    .setRtxBytesReceived(sfuTransport.rtxBytesReceived)
                    .setRtxBytesSent(sfuTransport.rtxBytesSent)
                    .setRtxPacketsReceived(sfuTransport.rtxPacketsReceived)
                    .setRtxPacketsSent(sfuTransport.rtxPacketsSent)
//                    .setRtxPacketsLost(sfuTransport.rtxPacket)
                    .setRtxPacketsDiscarded(sfuTransport.rtxPacketsDiscarded)

                    /* SCTP related stats */
                    .setSctpBytesReceived(sfuTransport.sctpBytesReceived)
                    .setSctpBytesSent(sfuTransport.sctpBytesSent)
                    .setSctpPacketsReceived(sfuTransport.sctpPacketsReceived)
                    .setSctpPacketsSent(sfuTransport.sctpPacketsSent)

                    .build();
            return result;
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while building PeerConnectionTransport report", ex);
            return null;
        }

    }

    private SfuRTPSourceReport createSfuRtpSourceReport(
            UUID sfuId,
            ObservedSfuSample observedSfuSample,
            SfuSample.SfuRtpSource sfuRtpSource,
            UUID trackId,
            UUID clientId,
            UUID callId
    ) {
        String trackIdStr = UUIDAdapter.toStringOrNull(trackId);
        String clientIdStr = UUIDAdapter.toStringOrNull(clientId);
        String callIdStr = UUIDAdapter.toStringOrNull(callId);
        try {
            var result = SfuRTPSourceReport.newBuilder()
                    /* Report MetaFields */
                    /* .setServiceId() // not given */
                    .setMediaUnitId(observedSfuSample.getMediaUnitId())
                    .setSfuId(sfuId.toString())
                    .setMarker(observedSfuSample.getMarker())
                    .setTimestamp(observedSfuSample.getTimestamp())

                    /* Report Fields */
                    .setTransportId(sfuRtpSource.transportId)
                    .setStreamId(sfuRtpSource.streamId)
                    .setSourceId(sfuRtpSource.sourceId)
                    .setSsrc(sfuRtpSource.ssrc)
                    .setTrackId(trackIdStr)
                    .setClientId(clientIdStr)
                    .setCallId(callIdStr)

                    /* RTP Stats */
                    .setMediaType(sfuRtpSource.mediaType)
                    .setPayloadType(sfuRtpSource.payloadType)
                    .setMimeType(sfuRtpSource.mimeType)
                    .setClockRate(sfuRtpSource.clockRate)
                    .setSdpFmtpLine(sfuRtpSource.sdpFmtpLine)
                    .setRid(sfuRtpSource.rid)
                    .setRtxSsrc(sfuRtpSource.rtxSsrc)
                    .setTargetBitrate(sfuRtpSource.targetBitrate)
                    .setVoiceActivityFlag(sfuRtpSource.voiceActivityFlag)
                    .setFirCount(sfuRtpSource.firCount)
                    .setPliCount(sfuRtpSource.pliCount)
                    .setNackCount(sfuRtpSource.nackCount)
                    .setSliCount(sfuRtpSource.sliCount)
                    .setPacketsLost(sfuRtpSource.packetsLost)
                    .setPacketsReceived(sfuRtpSource.packetsReceived)
                    .setPacketsDiscarded(sfuRtpSource.packetsDiscarded)
                    .setPacketsRepaired(sfuRtpSource.packetsRepaired)
                    .setPacketsFailedDecryption(sfuRtpSource.packetsFailedDecryption)
                    .setFecPacketsReceived(sfuRtpSource.fecPacketsReceived)
                    .setFecPacketsDiscarded(sfuRtpSource.fecPacketsDiscarded)
                    .setBytesReceived(sfuRtpSource.bytesReceived)
                    .setRtcpSrReceived(sfuRtpSource.rtcpSrReceived)
                    .setRtcpRrSent(sfuRtpSource.rtcpRrSent)
                    .setRtxPacketsReceived(sfuRtpSource.rtxPacketsReceived)
                    .setRtxPacketsDiscarded(sfuRtpSource.rtxPacketsDiscarded)
                    .setFramesReceived(sfuRtpSource.framesReceived)
                    .setFramesDecoded(sfuRtpSource.framesDecoded)
                    .setKeyFramesDecoded(sfuRtpSource.keyFramesDecoded)
                    .setFractionLost(sfuRtpSource.fractionLost)
                    .setJitter(sfuRtpSource.jitter)
                    .setRoundTripTime(sfuRtpSource.roundTripTime)
                    .setAttachments(sfuRtpSource.attachments)

                    .build();
            return result;
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while building PeerConnectionTransport report", ex);
            return null;
        }

    }

    private SfuRTPSinkReport createSfuRtpSinkReport(
            UUID sfuId,
            ObservedSfuSample observedSfuSample,
            SfuSample.SfuRtpSink sfuRtpSink,
            UUID trackId,
            UUID clientId,
            UUID callId
    ) {
        String trackIdStr = UUIDAdapter.toStringOrNull(trackId);
        String clientIdStr = UUIDAdapter.toStringOrNull(clientId);
        String callIdStr = UUIDAdapter.toStringOrNull(callId);
        try {
            var result = SfuRTPSinkReport.newBuilder()

                    /* Report MetaFields */
                    /* .setServiceId() // not given */
                    .setMediaUnitId(observedSfuSample.getMediaUnitId())
                    .setSfuId(sfuId.toString())
                    .setMarker(observedSfuSample.getMarker())
                    .setTimestamp(observedSfuSample.getTimestamp())

                    /* Report Fields */
                    .setTransportId(sfuRtpSink.transportId)
                    .setStreamId(sfuRtpSink.streamId)
                    .setSinkId(sfuRtpSink.sinkId)
                    .setSsrc(sfuRtpSink.ssrc)
                    .setTrackId(trackIdStr)
                    .setClientId(clientIdStr)
                    .setCallId(callIdStr)

                    /* RTP Stats */
                    .setMediaType(sfuRtpSink.mediaType)
                    .setPayloadType(sfuRtpSink.payloadType)
                    .setMimeType(sfuRtpSink.mimeType)
                    .setClockRate(sfuRtpSink.clockRate)
                    .setSdpFmtpLine(sfuRtpSink.sdpFmtpLine)
                    .setRid(sfuRtpSink.rid)
                    .setRtxSsrc(sfuRtpSink.rtxSsrc)
                    .setTargetBitrate(sfuRtpSink.targetBitrate)
                    .setVoiceActivityFlag(sfuRtpSink.voiceActivityFlag)
                    .setFirCount(sfuRtpSink.firCount)
                    .setPliCount(sfuRtpSink.pliCount)
                    .setNackCount(sfuRtpSink.nackCount)
                    .setSliCount(sfuRtpSink.sliCount)
                    .setPacketsLost(null)
                    .setPacketsSent(sfuRtpSink.packetsSent)
                    .setPacketsDiscarded(sfuRtpSink.packetsDiscarded)
                    .setPacketsRetransmitted(sfuRtpSink.packetsRetransmitted)
                    .setPacketsFailedEncryption(sfuRtpSink.packetsFailedEncryption)
                    .setPacketsDuplicated(sfuRtpSink.packetsDuplicated)
                    .setFecPacketsSent(sfuRtpSink.fecPacketsSent)
                    .setFecPacketsDiscarded(sfuRtpSink.fecPacketsDiscarded)
                    .setBytesSent(sfuRtpSink.bytesSent)
                    .setRtcpSrSent(sfuRtpSink.rtcpSrSent)
                    .setRtcpRrReceived(sfuRtpSink.rtcpRrReceived)
                    .setRtxPacketsSent(sfuRtpSink.rtxPacketsSent)
                    .setRtxPacketsDiscarded(sfuRtpSink.rtxPacketsDiscarded)
                    .setFramesSent(sfuRtpSink.framesSent)
                    .setFramesEncoded(sfuRtpSink.framesEncoded)
                    .setKeyFramesEncoded(sfuRtpSink.keyFramesEncoded)
                    .setAttachments(sfuRtpSink.attachments)

                    .build();
            return result;
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while building PeerConnectionTransport report", ex);
            return null;
        }

    }

    private SfuSctpStreamReport createSfuSctpStreamReport(
            UUID sfuId,
            ObservedSfuSample observedSfuSample,
            SfuSample.SctpStream sctpStream
    ) {

        try {
            var result = SfuSctpStreamReport.newBuilder()

                    /* Report MetaFields */
                    .setMediaUnitId(observedSfuSample.getMediaUnitId())
                    .setSfuId(sfuId.toString())
                    .setMarker(observedSfuSample.getMarker())
                    .setTimestamp(observedSfuSample.getTimestamp())

                    /* Helper field */
//                    .setCallId(callId)
                    .setTransportId(sctpStream.transportId)
//                    .setRoomId()
                    .setStreamId(sctpStream.streamId)

                    /* SCTP Stats */
                    .setLabel(sctpStream.label)
                    .setProtocol(sctpStream.protocol)
                    .setSctpSmoothedRoundTripTime(sctpStream.sctpSmoothedRoundTripTime)
                    .setSctpCongestionWindow(sctpStream.sctpCongestionWindow)
                    .setSctpReceiverWindow(sctpStream.sctpReceiverWindow)
                    .setSctpUnackData(sctpStream.sctpUnackData)
                    .setMessageReceived(sctpStream.messageReceived)
                    .setMessageSent(sctpStream.messageSent)
                    .setBytesReceived(sctpStream.bytesReceived)
                    .setBytesSent(sctpStream.bytesSent)

                    .build();
            return result;
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while building PeerConnectionTransport report", ex);
            return null;
        }

    }

    private FetchSfuRelationsTask.Report fetchSfuRelations(CollectedSfuSamples collectedSfuSamples) {
        Set<UUID> sfuStreamIds = new HashSet<>();
        Set<UUID> sfuTransportIds = new HashSet<>();
        collectedSfuSamples.stream()
                .flatMap(SfuSamples::stream)
                .forEach(observedSfuSample -> {
                    SfuSampleVisitor.streamRtpSources(observedSfuSample.getSfuSample())
                            .map(t -> UUIDAdapter.tryParse(t.streamId))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(sfuStreamIds::add);


                    SfuSampleVisitor.streamRtpSinks(observedSfuSample.getSfuSample())
                            .map(t -> UUIDAdapter.tryParse(t.streamId))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(sfuStreamIds::add);

                    SfuSampleVisitor.streamTransports(observedSfuSample.getSfuSample())
                            .map(t -> UUIDAdapter.tryParse(t.transportId))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(sfuTransportIds::add);
                });
        var task = fetchSfuRelationsTaskProvider.get()
                .whereSfuStreamIds(sfuStreamIds)
                .whereSfuTransportIds(sfuTransportIds)
                ;

        if (!task.execute().succeeded()) {
            logger.warn("Cannot match inbound tracks to outbound tracks, because the task execution is failed");
            return FetchSfuRelationsTask.EMPTY_REPORT;
        }
        return task.getResult();
    }
}
