package org.observertc.observer.simulator;

import org.observertc.schemas.samples.Samples;
import org.observertc.schemas.samples.Samples.SfuSample;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class SfuSurrogate implements NetworkLinkProvider {
    private final UUID sfuId = UUID.randomUUID();
    private Map<UUID, NetworkLink> links = new HashMap<>();
    private Map<UUID, SfuTransport> transports = new HashMap<>();
    private Map<UUID, RtpPadSurrogate> inboundRtpPads = new HashMap<>();
    private Map<UUID, RtpPadSurrogate> outboundRtpPads = new HashMap<>();
    private String marker = null;
    private int timeZoneOffsetInHour = 0;

    public NetworkLink provideNetworkLink() {
        return this.provideNetworkLink(false);
    }

    public NetworkLink provideNetworkLink(boolean internal) {
        var link = new NetworkLink();
        this.links.put(link.getId(), link);
        var transport = this.createTransport(link);
        transport.internal = internal;
        this.transports.put(transport.getId(), transport);
        return link;
    }

    public void pipe(SfuSurrogate peer) {
        var link = peer.provideNetworkLink(true);
        var transport = this.createTransport(link);
        transport.internal = true;
        this.transports.put(transport.getId(), transport);
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public void setTimeZoneOffsetInHour(int value) {
        this.timeZoneOffsetInHour = value;
    }

    public Samples getSamples() {
        var result = new Samples();
        var meta = new Samples.SamplesMeta();
        meta.schemaVersion = Samples.VERSION;
        result.meta = meta;
        result.sfuSamples = new SfuSample[]{ this.generateSfuSample() };
        return result;
    }

    private SfuSample generateSfuSample() {
        var result = new SfuSample();
        result.inboundRtpPads = this.generateInboundRtpPadStats();
        result.outboundRtpPads = this.generateOutboundRtpPadStats();
        result.transports = this.generateTransportStats();
//        result.sctpChannels = this.generateSctpChannels();
        result.sfuId = this.sfuId;
        result.timestamp = Instant.now().toEpochMilli();
        result.marker = this.marker;
        result.timeZoneOffsetInHours = this.timeZoneOffsetInHour;
        return result;
    }

    private SfuTransport createTransport(NetworkLink link) {
        var sfuTransport = new SfuTransport();
        var peerConnection = link.createPeerConnection(new NetworkLinkEvents() {
            @Override
            public void onRtpSessionAdded(RtpSessionSurrogate inboundSession) {
                UUID streamId;
                if (Objects.isNull(inboundSession.sfuStreamId)) {
                    streamId = UUID.randomUUID();
                    inboundSession.sfuStreamId = streamId;
                } else {
                    streamId = inboundSession.sfuStreamId;
                }
                var mediaKind = inboundSession.kind;
                var inboundRtpPad = sfuTransport.createRtpPad(mediaKind, streamId, null);
                inboundRtpPads.put(inboundRtpPad.padId, inboundRtpPad);

                transports.values().stream()
                        .filter(addedTransport -> addedTransport.getId() != sfuTransport.getId())
                        .forEach(otherTransport -> {
                            var sinkId = UUID.randomUUID();
                            var outboundRtpPad = otherTransport.createRtpPad(mediaKind, streamId, sinkId);
                            outboundRtpPads.put(outboundRtpPad.padId, outboundRtpPad);
                        });
            }

            @Override
            public void onRtpSessionRemoved(RtpSessionSurrogate session) {
                var streamId = session.sfuStreamId;
                var inboundRtpPadIds = inboundRtpPads.values().stream()
                        .filter(rtpPad -> rtpPad.streamId == streamId)
                        .map(rtpPad -> rtpPad.padId)
                        .collect(Collectors.toList());
                inboundRtpPadIds.forEach(inboundRtpPads::remove);
                var outboundRtpPadIds = outboundRtpPads.values().stream()
                        .filter(rtpPad -> rtpPad.streamId == streamId)
                        .map(rtpPad -> rtpPad.padId)
                        .collect(Collectors.toList());
                outboundRtpPadIds.stream()
                        .map(outboundRtpPads::remove)
                        .filter(Objects::nonNull)
                        .forEach(rtpPad -> {
                            var transport = transports.get(rtpPad.transportId);
                            if (Objects.isNull(transport)) return;
                            transport.closeRtpPad(rtpPad.SSRC);
                        });
            }
        });
        sfuTransport.peerConnection = peerConnection;
        return sfuTransport;
    }


    private class SfuTransport {
        boolean internal = false;
        PeerConnection peerConnection;

        private SfuTransport() {

        }

        public UUID getId() {
            return this.peerConnection.getId();
        }

        public RtpPadSurrogate createRtpPad(String mediaKind, UUID streamId, UUID sinkId) {
            var session = RtpSessionSurrogate.create(mediaKind);
            session.sfuStreamId = streamId;
            session.sfuSinkId = sinkId;
            this.peerConnection.addRtpSession(session);
            var transportId = this.getId();
            var rtpPad = RtpPadSurrogate.createRtpPad(transportId, session);
            return rtpPad;
        }

        public void closeRtpPad(Long SSRC) {
            this.peerConnection.closeRtpSession(SSRC);
        }
    }
}
