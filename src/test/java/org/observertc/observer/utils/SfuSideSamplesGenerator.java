package org.observertc.observer.utils;

import org.observertc.schemas.samples.Samples;

import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.observertc.observer.utils.TestUtils.arrayOrNullFromList;
import static org.observertc.observer.utils.TestUtils.arrayOrNullFromQueue;

public class SfuSideSamplesGenerator implements Supplier<Samples> {

    private RandomGenerators randomGenerator = new RandomGenerators();
    private String callId = null;
    private String sfuId = UUID.randomUUID().toString();
    private String marker = null;
    private Integer timeZoneOffsetInHours = null;
    private Map<String, TransportSession> transports = new HashMap<>();
    private Map<String, RtpSession> inboundRtpPads = new HashMap<>();
    private Map<String, RtpSession> outboundRtpPads = new HashMap<>();
    private Map<String, DataChannelSession> sctpChannels = new HashMap<>();
    private Queue<Samples.SfuSample.SfuExtensionStats> addedExtensionStats = new LinkedList<>();

    public SfuSideSamplesGenerator setSfuId(String value) {
        this.sfuId = value;
        return this;
    }

    public SfuSideSamplesGenerator setMarker(String value) {
        this.marker = value;
        return this;
    }

    public SfuSideSamplesGenerator setTimeZoneOffsetInHours(Integer value) {
        this.timeZoneOffsetInHours = value;
        return this;
    }

    public SfuSideSamplesGenerator addExtensionStat() {
        var extensionStat = new Samples.SfuSample.SfuExtensionStats();
        extensionStat.type = this.randomGenerator.getRandomString();
        extensionStat.payload = this.randomGenerator.getRandomString(128);
        this.addedExtensionStats.add(extensionStat);
        return this;
    }

    public SfuSideSamplesGenerator addTransport(String value) {
        return this.addTransport(value, false);
    }

    public SfuSideSamplesGenerator addTransport(String value, boolean internal) {
        var session = new TransportSession(internal);
        this.transports.put(value, session);
        return this;
    }

    public SfuSideSamplesGenerator removeTransport(String value) {
        if (!this.transports.containsKey(value)) return this;
        this.inboundRtpPads.entrySet().stream()
                .filter(entry -> entry.getValue().transportId.equals(value))
                .map(entry -> entry.getKey())
                .collect(Collectors.toList())
                .stream()
                .forEach(this.inboundRtpPads::remove);

        this.outboundRtpPads.entrySet().stream()
                .filter(entry -> entry.getValue().transportId.equals(value))
                .map(entry -> entry.getKey())
                .collect(Collectors.toList())
                .stream()
                .forEach(this.outboundRtpPads::remove);

        this.sctpChannels.entrySet().stream()
                .filter(entry -> entry.getValue().transportId.equals(value))
                .map(entry -> entry.getKey())
                .collect(Collectors.toList())
                .stream()
                .forEach(this.sctpChannels::remove);
        return this;
    }

    public SfuSideSamplesGenerator addDataChannel(String transportId, String streamId, String channelId) {
        var session = new DataChannelSession(transportId, streamId);
        this.sctpChannels.put(channelId, session);
        return this;
    }

    public SfuSideSamplesGenerator removeDataChannel(String channelId) {
        this.sctpChannels.remove(channelId);
        return this;
    }

    public SfuSideSamplesGenerator addInboundRtpPad(String transportId, String rtpPadId, Long SSRC, String sfuStreamId) {
        return this.addInboundRtpPad(transportId, rtpPadId, SSRC, sfuStreamId, false);
    }

    public SfuSideSamplesGenerator addInboundRtpPad(String transportId, String rtpPadId, Long SSRC, String sfuStreamId, boolean internal) {
        if (!this.transports.containsKey(transportId)) {
            throw new RuntimeException("Add the peer connection id to the generator before you add any session related to it");
        }
        var rtpSession = new RtpSession(sfuStreamId, null, SSRC, transportId, rtpPadId, internal, null, null);
        this.inboundRtpPads.put(rtpPadId, rtpSession);
        return this;
    }

    public SfuSideSamplesGenerator removeInboundRtpPad(String rtpPadId) {
        this.inboundRtpPads.remove(rtpPadId);
        return this;
    }

    public SfuSideSamplesGenerator addOutboundRtpPad(String transportId, String rtpPadId, Long SSRC, String sfuStreamId, String sfuSinkId)  {
        return this.addOutboundRtpPad(transportId, rtpPadId, SSRC, sfuStreamId, sfuSinkId, false);
    }
    public SfuSideSamplesGenerator addOutboundRtpPad(String transportId, String rtpPadId, Long SSRC, String sfuStreamId, String sfuSinkId, boolean internal) {
        return this.addOutboundRtpPad(transportId, rtpPadId, SSRC, sfuStreamId, sfuSinkId, internal, null, null);
    }


    public SfuSideSamplesGenerator addOutboundRtpPad(String transportId, String rtpPadId, Long SSRC, String sfuStreamId, String sfuSinkId, boolean internal, String remoteClientId, String remoteTrackId) {
        if (!this.transports.containsKey(transportId)) {
            throw new RuntimeException("Add the peer connection id to the generator before you add any session related to it");
        }
        var rtpSession = new RtpSession(sfuStreamId, sfuSinkId, SSRC, transportId, rtpPadId, internal, remoteClientId, remoteTrackId);
        this.outboundRtpPads.put(rtpPadId, rtpSession);
        return this;
    }

    public SfuSideSamplesGenerator removeOutboundRtpPad(UUID rtpPadId) {
        this.outboundRtpPads.remove(rtpPadId);
        return this;
    }

    @Override
    public Samples get() {

        var transports = this.transports.entrySet().stream().map(entry -> {
            var transportId = entry.getKey();
            var session = entry.getValue();
            var sfuTransport = new Samples.SfuSample.SfuTransport();
            sfuTransport.noReport = false;
            sfuTransport.transportId = transportId;
            sfuTransport.internal = session.internal;
            sfuTransport.dtlsState = this.randomGenerator.getRandomDtlsState();
            sfuTransport.iceState = this.randomGenerator.getRandomIceState();
            sfuTransport.sctpState = this.randomGenerator.getRandomDataChannelState();
            sfuTransport.iceRole = this.randomGenerator.getRandomIceRole();
            sfuTransport.localAddress = this.randomGenerator.getRandomIPv4Address();
            sfuTransport.localPort = this.randomGenerator.getRandomPort();
            sfuTransport.protocol = this.randomGenerator.getRandomNetworkTransportProtocols();
            sfuTransport.remoteAddress = this.randomGenerator.getRandomIPv4Address();
            sfuTransport.remotePort = this.randomGenerator.getRandomPort();
            sfuTransport.rtpBytesReceived = this.randomGenerator.getRandomPositiveLong();
            sfuTransport.rtpBytesSent = this.randomGenerator.getRandomPositiveLong();
            sfuTransport.rtpPacketsReceived = this.randomGenerator.getRandomPositiveInteger();
            sfuTransport.rtpPacketsSent = this.randomGenerator.getRandomPositiveInteger();
            sfuTransport.rtpPacketsLost = this.randomGenerator.getRandomPositiveInteger();
            sfuTransport.rtxBytesReceived = this.randomGenerator.getRandomPositiveLong();
            sfuTransport.rtxBytesSent = this.randomGenerator.getRandomPositiveLong();
            sfuTransport.rtxPacketsReceived = this.randomGenerator.getRandomPositiveInteger();
            sfuTransport.rtxPacketsSent = this.randomGenerator.getRandomPositiveInteger();
            sfuTransport.rtxPacketsLost = this.randomGenerator.getRandomPositiveInteger();
            sfuTransport.rtxPacketsDiscarded = this.randomGenerator.getRandomPositiveInteger();
            sfuTransport.sctpBytesReceived = this.randomGenerator.getRandomPositiveLong();
            sfuTransport.sctpBytesSent = this.randomGenerator.getRandomPositiveLong();
            sfuTransport.sctpPacketsReceived = this.randomGenerator.getRandomPositiveInteger();
            sfuTransport.sctpPacketsSent = this.randomGenerator.getRandomPositiveInteger();
            return sfuTransport;
        }).collect(Collectors.toList());

        var inboundRtpPads = this.inboundRtpPads.entrySet().stream().map(entry -> {
            var rtpPadId = entry.getKey();
            var session = entry.getValue();
            var sfuInboundRtpPad = new Samples.SfuSample.SfuInboundRtpPad();
            sfuInboundRtpPad.noReport = false;
            sfuInboundRtpPad.transportId = session.transportId;
            sfuInboundRtpPad.internal = session.internal;
            sfuInboundRtpPad.streamId = session.sfuStreamId;
            sfuInboundRtpPad.padId = rtpPadId;
            sfuInboundRtpPad.ssrc = this.randomGenerator.getRandomSSRC();
            sfuInboundRtpPad.mediaType = this.randomGenerator.getRandomMediaKind();
            sfuInboundRtpPad.payloadType = this.randomGenerator.getRandomPayloadType();
            sfuInboundRtpPad.mimeType = this.randomGenerator.getRandomString();
            sfuInboundRtpPad.clockRate = this.randomGenerator.getRandomClockRate();
            sfuInboundRtpPad.sdpFmtpLine = this.randomGenerator.getRandomString();
            sfuInboundRtpPad.rid = this.randomGenerator.getRandomString();
            sfuInboundRtpPad.rtxSsrc = this.randomGenerator.getRandomSSRC();
            sfuInboundRtpPad.targetBitrate = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.voiceActivityFlag = this.randomGenerator.getRandomBoolean();
            sfuInboundRtpPad.firCount = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.pliCount = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.nackCount = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.sliCount = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.packetsLost = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.packetsReceived = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.packetsDiscarded = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.packetsRepaired = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.packetsFailedDecryption = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.packetsDuplicated = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.fecPacketsReceived = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.fecPacketsDiscarded = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.bytesReceived = this.randomGenerator.getRandomPositiveLong();
            sfuInboundRtpPad.rtcpSrReceived = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.rtcpRrSent = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.rtxPacketsReceived = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.rtxPacketsDiscarded = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.framesReceived = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.framesDecoded = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.keyFramesDecoded = this.randomGenerator.getRandomPositiveInteger();
            sfuInboundRtpPad.fractionLost = this.randomGenerator.getRandomPositiveDouble();
            sfuInboundRtpPad.jitter = this.randomGenerator.getRandomPositiveDouble();
            sfuInboundRtpPad.roundTripTime = this.randomGenerator.getRandomPositiveDouble();
            return sfuInboundRtpPad;
        }).collect(Collectors.toList());

        var outboundRtpPads = this.outboundRtpPads.entrySet().stream().map(entry -> {
            var rtpPadId = entry.getKey();
            var session = entry.getValue();
            var sfuOutboundRtpPad = new Samples.SfuSample.SfuOutboundRtpPad();
            sfuOutboundRtpPad.noReport = false;
            sfuOutboundRtpPad.transportId = session.transportId;
            sfuOutboundRtpPad.internal = session.internal;
            sfuOutboundRtpPad.streamId = session.sfuStreamId;
            sfuOutboundRtpPad.sinkId = session.sfuSinkId;
            sfuOutboundRtpPad.padId = rtpPadId;
            sfuOutboundRtpPad.ssrc = this.randomGenerator.getRandomSSRC();
            sfuOutboundRtpPad.callId = this.callId;
            sfuOutboundRtpPad.clientId = session.remoteClientId;
            sfuOutboundRtpPad.trackId = session.remoteTrackId;
            sfuOutboundRtpPad.mediaType = this.randomGenerator.getRandomMediaKind();
            sfuOutboundRtpPad.payloadType = this.randomGenerator.getRandomPayloadType();
            sfuOutboundRtpPad.mimeType = this.randomGenerator.getRandomString();
            sfuOutboundRtpPad.clockRate = this.randomGenerator.getRandomClockRate();
            sfuOutboundRtpPad.sdpFmtpLine = this.randomGenerator.getRandomString();
            sfuOutboundRtpPad.rid = this.randomGenerator.getRandomString();
            sfuOutboundRtpPad.rtxSsrc = this.randomGenerator.getRandomSSRC();
            sfuOutboundRtpPad.targetBitrate = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.voiceActivityFlag = this.randomGenerator.getRandomBoolean();
            sfuOutboundRtpPad.firCount = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.pliCount = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.nackCount = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.sliCount = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.packetsLost = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.packetsSent = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.packetsDiscarded = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.packetsRetransmitted = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.packetsFailedEncryption = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.packetsDuplicated = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.fecPacketsSent = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.fecPacketsDiscarded = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.bytesSent = this.randomGenerator.getRandomPositiveLong();
            sfuOutboundRtpPad.rtcpSrSent = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.rtcpRrReceived = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.rtxPacketsSent = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.rtxPacketsDiscarded = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.framesSent = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.framesEncoded = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.keyFramesEncoded = this.randomGenerator.getRandomPositiveInteger();
            sfuOutboundRtpPad.fractionLost = this.randomGenerator.getRandomPositiveDouble();
            sfuOutboundRtpPad.jitter = this.randomGenerator.getRandomPositiveDouble();
            sfuOutboundRtpPad.roundTripTime = this.randomGenerator.getRandomPositiveDouble();
            return sfuOutboundRtpPad;
        }).collect(Collectors.toList());

        var sctpChannels = this.sctpChannels.entrySet().stream().map(entry -> {
            var channelId = entry.getKey();
            var session = entry.getValue();
            var sfuSctpChannel = new Samples.SfuSample.SfuSctpChannel();
            sfuSctpChannel.noReport = false;
            sfuSctpChannel.transportId = session.transportId;
            sfuSctpChannel.streamId = session.streamId;
            sfuSctpChannel.channelId = channelId;
            sfuSctpChannel.label = this.randomGenerator.getRandomString();
            sfuSctpChannel.protocol = this.randomGenerator.getRandomNetworkTransportProtocols();
            sfuSctpChannel.sctpSmoothedRoundTripTime = this.randomGenerator.getRandomPositiveDouble();
            sfuSctpChannel.sctpCongestionWindow = this.randomGenerator.getRandomPositiveDouble();
            sfuSctpChannel.sctpReceiverWindow = this.randomGenerator.getRandomPositiveDouble();
            sfuSctpChannel.sctpMtu = this.randomGenerator.getRandomPositiveInteger();
            sfuSctpChannel.sctpUnackData = this.randomGenerator.getRandomPositiveInteger();
            sfuSctpChannel.messageReceived = this.randomGenerator.getRandomPositiveInteger();
            sfuSctpChannel.messageSent = this.randomGenerator.getRandomPositiveInteger();
            sfuSctpChannel.bytesReceived = this.randomGenerator.getRandomPositiveLong();
            sfuSctpChannel.bytesSent = this.randomGenerator.getRandomPositiveLong();
            return sfuSctpChannel;
        }).collect(Collectors.toList());

        var sfuSample = new Samples.SfuSample();
        sfuSample.sfuId = this.sfuId;
        sfuSample.timestamp = Instant.now().toEpochMilli();
        sfuSample.timeZoneOffsetInHours = this.timeZoneOffsetInHours;
        sfuSample.marker = this.marker;
        sfuSample.transports = arrayOrNullFromList(Samples.SfuSample.SfuTransport.class, transports);
        sfuSample.inboundRtpPads = arrayOrNullFromList(Samples.SfuSample.SfuInboundRtpPad.class, inboundRtpPads);
        sfuSample.outboundRtpPads = arrayOrNullFromList(Samples.SfuSample.SfuOutboundRtpPad.class, outboundRtpPads);
        sfuSample.sctpChannels = arrayOrNullFromList(Samples.SfuSample.SfuSctpChannel.class, sctpChannels);
        sfuSample.extensionStats = arrayOrNullFromQueue(Samples.SfuSample.SfuExtensionStats.class, addedExtensionStats);

        var controls = new Samples.Controls();
        controls.close = false;

        var samples = new Samples();
        samples.controls = controls;
        samples.sfuSamples = new Samples.SfuSample[]{ sfuSample };
        samples.clientSamples = null;
        return samples;
    }

    private class RtpSession {
        final String sfuStreamId;
        final String sfuSinkId;
        final Long SSRC;
        final String transportId;
        final String rtpPadId;
        final boolean internal;
        final String remoteClientId;
        final String remoteTrackId;

        private RtpSession(String sfuStreamId, String sfuSinkId, Long ssrc, String transportId, String rtpPadId, boolean internal, String remoteClientId, String remoteTrackId) {
            this.sfuStreamId = sfuStreamId;
            this.sfuSinkId = sfuSinkId;
            SSRC = ssrc;
            this.transportId = transportId;
            this.rtpPadId = rtpPadId;
            this.internal = internal;
            this.remoteClientId = remoteClientId;
            this.remoteTrackId = remoteTrackId;
        }
    }

    private class DataChannelSession {

        public final String transportId;
        public final String streamId;

        private DataChannelSession(String transportId, String streamId) {
            this.transportId = transportId;
            this.streamId = streamId;
        }
    }

    private class TransportSession {
        public final boolean internal;

        private TransportSession(boolean internal) {
            this.internal = internal;
        }
    }
}
