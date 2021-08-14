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
import org.observertc.webrtc.schemas.reports.SfuInboundRTPStreamReport;
import org.observertc.webrtc.schemas.reports.SfuOutboundRTPStreamReport;
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
    private Subject<SfuInboundRTPStreamReport> sfuInboundRtpStreamReportSubject = PublishSubject.create();
    private Subject<SfuOutboundRTPStreamReport> sfuOutboundRtpStreamReportSubject = PublishSubject.create();
    private Subject<SfuSctpStreamReport> sfuSctpStreamSubject = PublishSubject.create();

    public Observable<SFUTransportReport> getSfuTransportReport() {
        return this.sfuTransportReportSubject;
    }
    public Observable<SfuInboundRTPStreamReport> getSfuInboundRtpStreamReport() {return this.sfuInboundRtpStreamReportSubject; }
    public Observable<SfuOutboundRTPStreamReport> getSfuOutboundRtpStreamReport() {return this.sfuOutboundRtpStreamReportSubject; }

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
        List<SfuInboundRTPStreamReport> inboundRTPStreamReports = new LinkedList<>();
        List<SfuOutboundRTPStreamReport> outboundRTPStreamReports = new LinkedList<>();
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

                SfuSampleVisitor.streamInboundRtpStreams(sfuSample)
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
                            return this.createSfuInboundRtpStreamReport(
                                    sfuId,
                                    observedSfuSample,
                                    inboundRtpStream,
                                    trackId,
                                    clientId,
                                    callId
                            );

                        })
                        .filter(Objects::nonNull)
                        .forEach(inboundRTPStreamReports::add);

                SfuSampleVisitor.streamOutboundRtpStreams(sfuSample)
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
                            return this.createSfuOutboundRtpStreamReport(
                                    sfuId,
                                    observedSfuSample,
                                    outboundRtpStream,
                                    trackId,
                                    clientId,
                                    callId
                            );

                        })
                        .filter(Objects::nonNull)
                        .forEach(outboundRTPStreamReports::add);

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
            if (observerConfig.outboundReports.reportSfuInboundRtpStreams) {
                inboundRTPStreamReports.stream().forEach(this.sfuInboundRtpStreamReportSubject::onNext);
            }
            if (observerConfig.outboundReports.reportSfuOutboundRtpStreams) {
                outboundRTPStreamReports.stream().forEach(this.sfuOutboundRtpStreamReportSubject::onNext);
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

    private SfuInboundRTPStreamReport createSfuInboundRtpStreamReport(
            UUID sfuId,
            ObservedSfuSample observedSfuSample,
            SfuSample.SfuInboundRtpStream sfuInboundRtpStream,
            UUID trackId,
            UUID clientId,
            UUID callId
    ) {
        String trackIdStr = UUIDAdapter.toStringOrNull(trackId);
        String clientIdStr = UUIDAdapter.toStringOrNull(clientId);
        String callIdStr = UUIDAdapter.toStringOrNull(callId);
        try {
            var result = SfuInboundRTPStreamReport.newBuilder()
                    /* Report MetaFields */
                    /* .setServiceId() // not given */
                    .setMediaUnitId(observedSfuSample.getMediaUnitId())
                    .setSfuId(sfuId.toString())
                    .setMarker(observedSfuSample.getMarker())
                    .setTimestamp(observedSfuSample.getTimestamp())

                    /* Report Fields */
                    .setTransportId(sfuInboundRtpStream.transportId)
                    .setStreamId(sfuInboundRtpStream.streamId)
                    .setSsrc(sfuInboundRtpStream.ssrc)
                    .setTrackId(trackIdStr)
                    .setClientId(clientIdStr)
                    .setCallId(callIdStr)

                    /* RTP Stats */
                    .setMediaType(sfuInboundRtpStream.mediaType)
                    .setPayloadType(sfuInboundRtpStream.payloadType)
                    .setMimeType(sfuInboundRtpStream.mimeType)
                    .setClockRate(sfuInboundRtpStream.clockRate)
                    .setSdpFmtpLine(sfuInboundRtpStream.sdpFmtpLine)
                    .setRid(sfuInboundRtpStream.rid)
                    .setRtxSsrc(sfuInboundRtpStream.rtxSsrc)
                    .setTargetBitrate(sfuInboundRtpStream.targetBitrate)
                    .setVoiceActivityFlag(sfuInboundRtpStream.voiceActivityFlag)
                    .setFirCount(sfuInboundRtpStream.firCount)
                    .setPliCount(sfuInboundRtpStream.pliCount)
                    .setNackCount(sfuInboundRtpStream.nackCount)
                    .setSliCount(sfuInboundRtpStream.sliCount)
                    .setPacketsLost(sfuInboundRtpStream.packetsLost)
                    .setPacketsReceived(sfuInboundRtpStream.packetsReceived)
                    .setPacketsDiscarded(sfuInboundRtpStream.packetsDiscarded)
                    .setPacketsRepaired(sfuInboundRtpStream.packetsRepaired)
                    .setPacketsFailedDecryption(sfuInboundRtpStream.packetsFailedDecryption)
                    .setFecPacketsReceived(sfuInboundRtpStream.fecPacketsReceived)
                    .setFecPacketsDiscarded(sfuInboundRtpStream.fecPacketsDiscarded)
                    .setBytesReceived(sfuInboundRtpStream.bytesReceived)
                    .setRtcpSrReceived(sfuInboundRtpStream.rtcpSrReceived)
                    .setRtcpRrSent(sfuInboundRtpStream.rtcpRrSent)
                    .setRtxPacketsReceived(sfuInboundRtpStream.rtxPacketsReceived)
                    .setRtxPacketsDiscarded(sfuInboundRtpStream.rtxPacketsDiscarded)
                    .setFramesReceived(sfuInboundRtpStream.framesReceived)
                    .setFramesDecoded(sfuInboundRtpStream.framesDecoded)
                    .setKeyFramesDecoded(sfuInboundRtpStream.keyFramesDecoded)
                    .setFractionLost(sfuInboundRtpStream.fractionLost)
                    .setJitter(sfuInboundRtpStream.jitter)
                    .setRoundTripTime(sfuInboundRtpStream.roundTripTime)
                    .setAttachments(sfuInboundRtpStream.attachments)

                    .build();
            return result;
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while building PeerConnectionTransport report", ex);
            return null;
        }

    }

    private SfuOutboundRTPStreamReport createSfuOutboundRtpStreamReport(
            UUID sfuId,
            ObservedSfuSample observedSfuSample,
            SfuSample.SfuOutboundRtpStream sfuOutboundRtpStream,
            UUID trackId,
            UUID clientId,
            UUID callId
    ) {
        String trackIdStr = UUIDAdapter.toStringOrNull(trackId);
        String clientIdStr = UUIDAdapter.toStringOrNull(clientId);
        String callIdStr = UUIDAdapter.toStringOrNull(callId);
        try {
            var result = SfuOutboundRTPStreamReport.newBuilder()

                    /* Report MetaFields */
                    /* .setServiceId() // not given */
                    .setMediaUnitId(observedSfuSample.getMediaUnitId())
                    .setSfuId(sfuId.toString())
                    .setMarker(observedSfuSample.getMarker())
                    .setTimestamp(observedSfuSample.getTimestamp())

                    /* Report Fields */
                    .setTransportId(sfuOutboundRtpStream.transportId)
                    .setStreamId(sfuOutboundRtpStream.streamId)
                    .setSsrc(sfuOutboundRtpStream.ssrc)
                    .setTrackId(trackIdStr)
                    .setClientId(clientIdStr)
                    .setCallId(callIdStr)

                    /* RTP Stats */
                    .setMediaType(sfuOutboundRtpStream.mediaType)
                    .setPayloadType(sfuOutboundRtpStream.payloadType)
                    .setMimeType(sfuOutboundRtpStream.mimeType)
                    .setClockRate(sfuOutboundRtpStream.clockRate)
                    .setSdpFmtpLine(sfuOutboundRtpStream.sdpFmtpLine)
                    .setRid(sfuOutboundRtpStream.rid)
                    .setRtxSsrc(sfuOutboundRtpStream.rtxSsrc)
                    .setTargetBitrate(sfuOutboundRtpStream.targetBitrate)
                    .setVoiceActivityFlag(sfuOutboundRtpStream.voiceActivityFlag)
                    .setFirCount(sfuOutboundRtpStream.firCount)
                    .setPliCount(sfuOutboundRtpStream.pliCount)
                    .setNackCount(sfuOutboundRtpStream.nackCount)
                    .setSliCount(sfuOutboundRtpStream.sliCount)
                    .setPacketsLost(null)
                    .setPacketsSent(sfuOutboundRtpStream.packetsSent)
                    .setPacketsDiscarded(sfuOutboundRtpStream.packetsDiscarded)
                    .setPacketsRetransmitted(sfuOutboundRtpStream.packetsRetransmitted)
                    .setPacketsFailedEncryption(sfuOutboundRtpStream.packetsFailedEncryption)
                    .setPacketsDuplicated(sfuOutboundRtpStream.packetsDuplicated)
                    .setFecPacketsSent(sfuOutboundRtpStream.fecPacketsSent)
                    .setFecPacketsDiscarded(sfuOutboundRtpStream.fecPacketsDiscarded)
                    .setBytesSent(sfuOutboundRtpStream.bytesSent)
                    .setRtcpSrSent(sfuOutboundRtpStream.rtcpSrSent)
                    .setRtcpRrReceived(sfuOutboundRtpStream.rtcpRrReceived)
                    .setRtxPacketsSent(sfuOutboundRtpStream.rtxPacketsSent)
                    .setRtxPacketsDiscarded(sfuOutboundRtpStream.rtxPacketsDiscarded)
                    .setFramesSent(sfuOutboundRtpStream.framesSent)
                    .setFramesEncoded(sfuOutboundRtpStream.framesEncoded)
                    .setKeyFramesEncoded(sfuOutboundRtpStream.keyFramesEncoded)
                    .setAttachments(sfuOutboundRtpStream.attachments)

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
                    SfuSampleVisitor.streamInboundRtpStreams(observedSfuSample.getSfuSample())
                            .map(t -> UUIDAdapter.tryParse(t.streamId))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(sfuStreamIds::add);


                    SfuSampleVisitor.streamOutboundRtpStreams(observedSfuSample.getSfuSample())
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
