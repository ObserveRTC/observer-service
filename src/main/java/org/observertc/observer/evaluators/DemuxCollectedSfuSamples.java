package org.observertc.observer.evaluators;

import io.micrometer.core.annotation.Timed;
import io.micronaut.context.annotation.Prototype;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.observertc.observer.reports.Report;
import org.observertc.observer.common.UUIDAdapter;
import org.observertc.observer.common.Utils;
import org.observertc.observer.configs.ObserverConfig;
import org.observertc.observer.dto.SfuSinkDTO;
import org.observertc.observer.dto.SfuStreamDTO;
import org.observertc.observer.repositories.tasks.FetchSfuRelationsTask;
import org.observertc.observer.samples.CollectedSfuSamples;
import org.observertc.observer.samples.ObservedSfuSample;
import org.observertc.observer.samples.SfuSampleVisitor;
import org.observertc.observer.samples.SfuSamples;
import org.observertc.schemas.reports.SFUTransportReport;
import org.observertc.schemas.reports.SfuInboundRtpPadReport;
import org.observertc.schemas.reports.SfuOutboundRtpPadReport;
import org.observertc.schemas.reports.SfuSctpStreamReport;
import org.observertc.schemas.samples.Samples.SfuSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;

import static org.observertc.observer.micrometer.ExposedMetrics.OBSERVERTC_EVALUATORS_DEMUX_COLLECTED_SFU_SAMPLES_TIME;

@Prototype
public class DemuxCollectedSfuSamples implements Consumer<CollectedSfuSamples> {
    private static final Logger logger = LoggerFactory.getLogger(DemuxCollectedSfuSamples.class);
//    private static final String _____missingFromSample____ = "I did not find it in the SfuSample";
//    private static final String _____missingByBound____ = "I did not find it in to be bound from the observed sample?";
    private Subject<List<Report>> reportSubject = PublishSubject.create();

    public Observable<List<Report>> getObservableReport() {return this.reportSubject; }


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

        List<Report> reports = new LinkedList<>();
        for (SfuSamples sfuSamples : collectedSfuSamples) {
            UUID sfuId = sfuSamples.getSfuId();
            for (ObservedSfuSample observedSfuSample: sfuSamples) {
                SfuSample sfuSample = observedSfuSample.getSfuSample();
                SfuSampleVisitor.streamTransports(sfuSample)
                        .filter(sfuTransport -> Utils.nullOrFalse(sfuTransport.noReport))
                        .map(sfuTransport -> {
                            return this.createSfuTransportReport(
                                    sfuId,
                                    observedSfuSample,
                                    sfuTransport
                            );

                        })
                        .filter(Objects::nonNull)
                        .map(Report::fromSfuTransportReport)
                        .forEach(reports::add);

                SfuSampleVisitor.streamInboundRtpPads(sfuSample)
                        .filter(inboundRtpStream -> Utils.nullOrFalse(inboundRtpStream.noReport))
                        .map(inboundRtpStream -> {
                            UUID sfuStreamId = UUIDAdapter.tryParseOrNull(inboundRtpStream.streamId);
                            SfuStreamDTO sfuStreamDTO = report.sfuStreams.get(sfuStreamId);
                            return this.createSfuInboundRtpPadReport(
                                    sfuId,
                                    observedSfuSample,
                                    inboundRtpStream,
                                    sfuStreamDTO
                            );

                        })
                        .filter(Objects::nonNull)
                        .map(Report::fromSfuInboundRtpPadReport)
                        .forEach(reports::add);

                SfuSampleVisitor.streamOutboundRtpPads(sfuSample)
                        .filter(outboundRtpPad -> Utils.nullOrFalse(outboundRtpPad.noReport))
                        .map(outboundRtpStream -> {
                            UUID sfuSinkId = UUIDAdapter.tryParseOrNull(outboundRtpStream.sinkId);
                            SfuSinkDTO sfuSinkDTO = report.sfuSinks.get(sfuSinkId);
                            return this.createSfuOutboundRtpPadReport(
                                    sfuId,
                                    observedSfuSample,
                                    outboundRtpStream,
                                    sfuSinkDTO
                            );

                        })
                        .filter(Objects::nonNull)
                        .map(Report::fromSfuOutboundRtpPadReport)
                        .forEach(reports::add);

                SfuSampleVisitor.streamSctpStreams(sfuSample)
                        .map(sctpStream -> {
                            return this.createSfuSctpStreamReport(
                                    sfuId,
                                    observedSfuSample,
                                    sctpStream
                            );

                        })
                        .filter(Objects::nonNull)
                        .map(Report::fromSfuSctpStreamReport)
                        .forEach(reports::add);
            }
        }
        synchronized (this) {
            this.reportSubject.onNext(reports);
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
            SfuSample.SfuInboundRtpPad sfuInboundRtpPad,
            SfuStreamDTO sfuStreamDTO
    ) {
        String callId = null, clientId = null, trackId = null;
        if (Objects.nonNull(sfuStreamDTO)) {
            callId = UUIDAdapter.toStringOrNull(sfuStreamDTO.callId);
            clientId = UUIDAdapter.toStringOrNull(sfuStreamDTO.clientId);
            trackId = UUIDAdapter.toStringOrNull(sfuStreamDTO.trackId);
        }
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
                    .setTransportId(sfuInboundRtpPad.transportId)
                    .setSfuStreamId(sfuInboundRtpPad.streamId)

                    .setSsrc(sfuInboundRtpPad.ssrc)
                    .setRtpPadId(sfuInboundRtpPad.padId)
                    .setTrackId(trackId)
                    .setClientId(clientId)
                    .setCallId(callId)

                    /* RTP Stats */
                    .setMediaType(sfuInboundRtpPad.mediaType)
                    .setPayloadType(sfuInboundRtpPad.payloadType)
                    .setMimeType(sfuInboundRtpPad.mimeType)
                    .setClockRate(sfuInboundRtpPad.clockRate)
                    .setSdpFmtpLine(sfuInboundRtpPad.sdpFmtpLine)
                    .setRid(sfuInboundRtpPad.rid)
                    .setRtxSsrc(sfuInboundRtpPad.rtxSsrc)
                    .setTargetBitrate(sfuInboundRtpPad.targetBitrate)
                    .setVoiceActivityFlag(sfuInboundRtpPad.voiceActivityFlag)
                    .setFirCount(sfuInboundRtpPad.firCount)
                    .setPliCount(sfuInboundRtpPad.pliCount)
                    .setNackCount(sfuInboundRtpPad.nackCount)
                    .setSliCount(sfuInboundRtpPad.sliCount)
                    .setPacketsLost(sfuInboundRtpPad.packetsLost)
                    .setPacketsReceived(sfuInboundRtpPad.packetsReceived)
                    .setPacketsDiscarded(sfuInboundRtpPad.packetsDiscarded)
                    .setPacketsRepaired(sfuInboundRtpPad.packetsRepaired)
                    .setPacketsFailedDecryption(sfuInboundRtpPad.packetsFailedDecryption)
                    .setFecPacketsReceived(sfuInboundRtpPad.fecPacketsReceived)
                    .setFecPacketsDiscarded(sfuInboundRtpPad.fecPacketsDiscarded)
                    .setBytesReceived(sfuInboundRtpPad.bytesReceived)
                    .setRtcpSrReceived(sfuInboundRtpPad.rtcpSrReceived)
                    .setRtcpRrSent(sfuInboundRtpPad.rtcpRrSent)
                    .setRtxPacketsReceived(sfuInboundRtpPad.rtxPacketsReceived)
                    .setRtxPacketsDiscarded(sfuInboundRtpPad.rtxPacketsDiscarded)
                    .setFramesReceived(sfuInboundRtpPad.framesReceived)
                    .setFramesDecoded(sfuInboundRtpPad.framesDecoded)
                    .setKeyFramesDecoded(sfuInboundRtpPad.keyFramesDecoded)
                    .setFractionLost(sfuInboundRtpPad.fractionLost)
                    .setJitter(sfuInboundRtpPad.jitter)
                    .setRoundTripTime(sfuInboundRtpPad.roundTripTime)

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
            SfuSinkDTO sfuSinkDTO
    ) {
        String callId = null, clientId = null, trackId = null;
        if (Objects.nonNull(sfuSinkDTO)) {
            callId = UUIDAdapter.toStringOrNull(sfuSinkDTO.callId);
            clientId = UUIDAdapter.toStringOrNull(sfuSinkDTO.clientId);
            trackId = UUIDAdapter.toStringOrNull(sfuSinkDTO.trackId);
        }
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
                    .setSfuStreamId(sfuRtpSink.streamId)
                    .setSfuSinkId(sfuRtpSink.sinkId)
                    .setRtpPadId(sfuRtpSink.padId)
                    .setSsrc(sfuRtpSink.ssrc)

                    .setTrackId(trackId)
                    .setClientId(clientId)
                    .setCallId(callId)

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
            SfuSample.SfuSctpChannel sctpChannel
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
                    .setTransportId(sctpChannel.transportId)
//                    .setRoomId()
                    .setStreamId(sctpChannel.streamId)

                    /* SCTP Stats */
                    .setLabel(sctpChannel.label)
                    .setProtocol(sctpChannel.protocol)
                    .setSctpSmoothedRoundTripTime(sctpChannel.sctpSmoothedRoundTripTime)
                    .setSctpCongestionWindow(sctpChannel.sctpCongestionWindow)
                    .setSctpReceiverWindow(sctpChannel.sctpReceiverWindow)
                    .setSctpUnackData(sctpChannel.sctpUnackData)
                    .setMessageReceived(sctpChannel.messageReceived)
                    .setMessageSent(sctpChannel.messageSent)
                    .setBytesReceived(sctpChannel.bytesReceived)
                    .setBytesSent(sctpChannel.bytesSent)

                    .build();
            return result;
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while building PeerConnectionTransport report", ex);
            return null;
        }

    }

    private FetchSfuRelationsTask.Report fetchSfuRelations(CollectedSfuSamples collectedSfuSamples) {
        Set<UUID> rtpPadIds = new HashSet<>();
        collectedSfuSamples.stream()
                .flatMap(SfuSamples::stream)
                .forEach(observedSfuSample -> {
                    SfuSampleVisitor.streamOutboundRtpPads(observedSfuSample.getSfuSample())
                            .map(t -> UUIDAdapter.tryParse(t.padId))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(rtpPadIds::add);


                    SfuSampleVisitor.streamInboundRtpPads(observedSfuSample.getSfuSample())
                            .map(t -> UUIDAdapter.tryParse(t.padId))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(rtpPadIds::add);
                });
        var task = fetchSfuRelationsTaskProvider.get()
                .whereSfuRtpPadIds(rtpPadIds)
                ;

        if (!task.execute().succeeded()) {
            logger.warn("Cannot match inbound tracks to outbound tracks, because the task execution is failed");
            return FetchSfuRelationsTask.EMPTY_REPORT;
        }
        return task.getResult();
    }
}
