package org.observertc.webrtc.observer.evaluators;

import io.micrometer.core.annotation.Timed;
import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.webrtc.observer.common.UUIDAdapter;
import org.observertc.webrtc.observer.common.Utils;
import org.observertc.webrtc.observer.configs.ObserverConfig;
import org.observertc.webrtc.observer.repositories.tasks.FetchSfuRelationsTask;
import org.observertc.webrtc.observer.samples.*;
import org.observertc.webrtc.schemas.reports.SFUTransportReport;
import org.observertc.webrtc.schemas.reports.SfuInboundRtpPadReport;
import org.observertc.webrtc.schemas.reports.SfuOutboundRtpPadReport;
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
//    private static final String _____missingFromSample____ = "I did not find it in the SfuSample";
//    private static final String _____missingByBound____ = "I did not find it in to be bound from the observed sample?";
    private Subject<SFUTransportReport> sfuTransportReportSubject = PublishSubject.create();
    private Subject<SfuInboundRtpPadReport> sfuRtpSourceReportSubject = PublishSubject.create();
    private Subject<SfuOutboundRtpPadReport> sfuRtpSinkReportSubject = PublishSubject.create();
    private Subject<SfuSctpStreamReport> sfuSctpStreamSubject = PublishSubject.create();

    public Observable<SFUTransportReport> getSfuTransportReport() {
        return this.sfuTransportReportSubject;
    }
    public Observable<SfuInboundRtpPadReport> getSfuRtpSourceReport() {return this.sfuRtpSourceReportSubject; }
    public Observable<SfuOutboundRtpPadReport> getSfuRtpSinkReport() {return this.sfuRtpSinkReportSubject; }

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
        List<SfuInboundRtpPadReport> inboundRtpPadReports = new LinkedList<>();
        List<SfuOutboundRtpPadReport> outboundRtpPadReports = new LinkedList<>();
        List<SfuSctpStreamReport> sctpStreamReports = new LinkedList<>();
        for (SfuSamples sfuSamples : collectedSfuSamples) {
            UUID sfuId = sfuSamples.getSfuId();
            for (ObservedSfuSample observedSfuSample: sfuSamples) {
                SfuSample sfuSample = observedSfuSample.getSfuSample();
                SfuSampleVisitor.streamTransports(sfuSample)
                        .map(sfuTransport -> {
                            return this.createSfuTransportReport(
                                    sfuId,
                                    observedSfuSample,
                                    sfuTransport
                            );

                        })
                        .filter(Objects::nonNull)
                        .forEach(transportReports::add);

                SfuSampleVisitor.streamInboundRtpPads(sfuSample)
                        .filter(inboundRtpStream -> Utils.nullOrFalse(inboundRtpStream.skipMeasurements))
                        .map(inboundRtpStream -> {
//                            UUID trackId = null;
//                            UUID clientId = null;
                            UUID rtpStreamId = UUIDAdapter.tryParseOrNull(inboundRtpStream.rtpStreamId);
                            UUID callId = report.rtpStreamIdToCallIds.get(rtpStreamId);
                            return this.createSfuInboundRtpPadReport(
                                    sfuId,
                                    observedSfuSample,
                                    inboundRtpStream,
                                    callId
                            );

                        })
                        .filter(Objects::nonNull)
                        .forEach(inboundRtpPadReports::add);

                SfuSampleVisitor.streamOutboundRtpPads(sfuSample)
                        .filter(outboundRtpPad -> Utils.nullOrFalse(outboundRtpPad.skipMeasurements))
                        .map(outboundRtpStream -> {
                            UUID rtpStreamId = UUIDAdapter.tryParseOrNull(outboundRtpStream.rtpStreamId);
                            UUID callId = report.rtpStreamIdToCallIds.get(rtpStreamId);
                            return this.createSfuOutboundRtpPadReport(
                                    sfuId,
                                    observedSfuSample,
                                    outboundRtpStream,
                                    callId
                            );

                        })
                        .filter(Objects::nonNull)
                        .forEach(outboundRtpPadReports::add);

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
            transportReports.stream().forEach(this.sfuTransportReportSubject::onNext);
            inboundRtpPadReports.stream().forEach(this.sfuRtpSourceReportSubject::onNext);
            outboundRtpPadReports.stream().forEach(this.sfuRtpSinkReportSubject::onNext);
            sctpStreamReports.stream().forEach(this.sfuSctpStreamSubject::onNext);
        }
    }

    private SFUTransportReport createSfuTransportReport(
            UUID sfuId,
            ObservedSfuSample observedSfuSample,
            SfuSample.SfuTransport sfuTransport
    ) {
        String sfuIdStr = UUIDAdapter.toStringOrNull(sfuId);
        try {
            var result = SFUTransportReport.newBuilder()

                    /* Report MetaFields */
                    .setServiceId(observedSfuSample.getServiceId())
                    .setMediaUnitId(observedSfuSample.getMediaUnitId())
                    .setMarker(observedSfuSample.getMarker())
                    .setTimestamp(observedSfuSample.getTimestamp())


                    /* Report Fields */
                    .setTransportId(sfuTransport.transportId)
                    .setSfuId(sfuIdStr)
                    .setCallId(null)

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

    private SfuInboundRtpPadReport createSfuInboundRtpPadReport(
            UUID sfuId,
            ObservedSfuSample observedSfuSample,
            SfuSample.SfuInboundRtpPad sfuRtpSource,
            UUID callId
    ) {
        String callIdStr = UUIDAdapter.toStringOrNull(callId);
        try {
            var result = SfuInboundRtpPadReport.newBuilder()
                    /* Report MetaFields */
                    /* .setServiceId() // not given */
                    .setServiceId(observedSfuSample.getServiceId())
                    .setMediaUnitId(observedSfuSample.getMediaUnitId())
                    .setSfuId(sfuId.toString())
                    .setMarker(observedSfuSample.getMarker())
                    .setTimestamp(observedSfuSample.getTimestamp())

                    /* Report Fields */
                    .setTransportId(sfuRtpSource.transportId)
                    .setRtpStreamId(sfuRtpSource.rtpStreamId)
                    .setSsrc(sfuRtpSource.ssrc)
                    .setPadId(sfuRtpSource.padId)
                    .setTrackId(null) // not implemented yet
                    .setClientId(null) // not implemented yet
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

    private SfuOutboundRtpPadReport createSfuOutboundRtpPadReport(
            UUID sfuId,
            ObservedSfuSample observedSfuSample,
            SfuSample.SfuOutboundRtpPad sfuRtpSink,
            UUID callId
    ) {
        String callIdStr = UUIDAdapter.toStringOrNull(callId);
        try {
            var result = SfuOutboundRtpPadReport.newBuilder()

                    /* Report MetaFields */
                    /* .setServiceId() // not given */
                    .setServiceId(observedSfuSample.getServiceId())
                    .setMediaUnitId(observedSfuSample.getMediaUnitId())
                    .setSfuId(sfuId.toString())
                    .setMarker(observedSfuSample.getMarker())
                    .setTimestamp(observedSfuSample.getTimestamp())

                    /* Report Fields */
                    .setTransportId(sfuRtpSink.transportId)
                    .setRtpStreamId(sfuRtpSink.rtpStreamId)
                    .setSsrc(sfuRtpSink.ssrc)
                    .setTrackId(null) // not implemented yet
                    .setClientId(null) // not implemented yet
                    .setPadId(sfuRtpSink.padId)
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
                    .setPacketsLost(sfuRtpSink.packetsLost)
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
                    .setServiceId(observedSfuSample.getServiceId())
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
        collectedSfuSamples.stream()
                .flatMap(SfuSamples::stream)
                .forEach(observedSfuSample -> {
                    SfuSampleVisitor.streamOutboundRtpPads(observedSfuSample.getSfuSample())
                            .map(t -> UUIDAdapter.tryParse(t.rtpStreamId))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(sfuStreamIds::add);


                    SfuSampleVisitor.streamInboundRtpPads(observedSfuSample.getSfuSample())
                            .map(t -> UUIDAdapter.tryParse(t.rtpStreamId))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(sfuStreamIds::add);
                });
        var task = fetchSfuRelationsTaskProvider.get()
                .whereSfuRtpPadIds(sfuStreamIds)
                ;

        if (!task.execute().succeeded()) {
            logger.warn("Cannot match inbound tracks to outbound tracks, because the task execution is failed");
            return FetchSfuRelationsTask.EMPTY_REPORT;
        }
        return task.getResult();
    }
}
