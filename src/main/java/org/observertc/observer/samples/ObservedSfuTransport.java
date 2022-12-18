package org.observertc.observer.samples;

import org.jetbrains.annotations.NotNull;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public interface ObservedSfuTransport extends Iterable<Samples.SfuSample.SfuTransport> {

    static final Logger logger = LoggerFactory.getLogger(ObservedSfuTransport.class);

    String getSfuId();
    String getSfuTransportId();

    Long getMinTimestamp();
    Long getMaxTimestamp();
    String getMarker();
    String getMediaUnitId();

    Iterable<ObservedSfuInboundRtpPad> observedSfuInboundRtpPads();
    Iterable<ObservedSfuOutboundRtpPad> observedSfuOutboundRtpPads();
    Iterable<ObservedSfuSctpChannel> observedSfuSctpChannels();

    boolean getInternal();

    class Builder implements ObservedSfuTransport {
        private final String transportId;
        final ObservedSfu.Builder observedSfu;
        private final boolean internal;
        private Map<String, ObservedSfuInboundRtpPad> observedSfuInboundRtpPads = new HashMap<>();
        private Map<String, ObservedSfuOutboundRtpPad> observedSfuOutboundRtpPads = new HashMap<>();
        private Map<String, ObservedSfuSctpChannel> observedSfuSctpChannels = new HashMap<>();

        private List<Samples.SfuSample.SfuTransport> sfuTransportSamples = new LinkedList<>();

        Builder(ObservedSfu.Builder observedSfu, String transportId, boolean internal) {
            this.observedSfu = observedSfu;
            this.transportId = transportId;
            this.internal = internal;
            observedSfu.observedSfuSamples.transportIds.add(transportId);
        }

        public void add(Samples.SfuSample.SfuTransport sfuTransportSample) {
            this.sfuTransportSamples.add(sfuTransportSample);
        }

        public void addSfuInboundRtpPad(Samples.SfuSample.SfuInboundRtpPad sfuInboundRtpPadSample) {
            if (sfuInboundRtpPadSample.streamId == null) {
                logger.warn("No streamId for Inbound Rtp Pad occurred on transport {}, sfu: {}, mediaUnitId: {}",
                        this.transportId,
                        this.observedSfu.getSfuId(),
                        this.observedSfu.getMediaUnitId()
                );
                return;
            }
            ObservedSfuInboundRtpPad.Builder observedSfuInboundRtpPad = (ObservedSfuInboundRtpPad.Builder) this.observedSfuInboundRtpPads.get(sfuInboundRtpPadSample.padId);
            if (observedSfuInboundRtpPad == null) {
                observedSfuInboundRtpPad = new ObservedSfuInboundRtpPad.Builder(this, sfuInboundRtpPadSample.streamId, sfuInboundRtpPadSample.padId, sfuInboundRtpPadSample.ssrc);
                this.observedSfuInboundRtpPads.put(sfuInboundRtpPadSample.padId, observedSfuInboundRtpPad);
            }
            observedSfuInboundRtpPad.add(sfuInboundRtpPadSample);
        }

        public void addSfuOutboundRtpPad(Samples.SfuSample.SfuOutboundRtpPad sfuOutboundRtpPadSample) {
            if (sfuOutboundRtpPadSample.sinkId == null) {
                logger.warn("No sinkId for Outbound Rtp Pad occurred on transport {}, sfu: {}, mediaUnitId: {}",
                        this.transportId,
                        this.observedSfu.getSfuId(),
                        this.observedSfu.getMediaUnitId()
                );
                return;
            }
            ObservedSfuOutboundRtpPad.Builder observedSfuOutboundRtpPad = (ObservedSfuOutboundRtpPad.Builder) this.observedSfuOutboundRtpPads.get(sfuOutboundRtpPadSample.padId);
            if (observedSfuOutboundRtpPad == null) {
                observedSfuOutboundRtpPad = new ObservedSfuOutboundRtpPad.Builder(
                        this,
                        sfuOutboundRtpPadSample.streamId,
                        sfuOutboundRtpPadSample.sinkId,
                        sfuOutboundRtpPadSample.padId,
                        sfuOutboundRtpPadSample.ssrc
                );
                this.observedSfuOutboundRtpPads.put(sfuOutboundRtpPadSample.padId, observedSfuOutboundRtpPad);
            }
            observedSfuOutboundRtpPad.add(sfuOutboundRtpPadSample);
        }

        public void addSfuSctpChannel(Samples.SfuSample.SfuSctpChannel sfuSctpChannelSample) {
            if (sfuSctpChannelSample.streamId == null) {
                logger.warn("No streamId for Sctp Stream occurred on transport {}, sfu: {}, mediaUnitId: {}",
                        this.transportId,
                        this.observedSfu.getSfuId(),
                        this.observedSfu.getMediaUnitId()
                );
                return;
            }
            ObservedSfuSctpChannel.Builder observedSfuSctpChannel = (ObservedSfuSctpChannel.Builder) this.observedSfuSctpChannels.get(sfuSctpChannelSample.streamId);
            if (observedSfuSctpChannel == null) {
                observedSfuSctpChannel = new ObservedSfuSctpChannel.Builder(this, sfuSctpChannelSample.streamId, sfuSctpChannelSample.channelId);
                this.observedSfuSctpChannels.put(sfuSctpChannelSample.streamId, observedSfuSctpChannel);
            }
            observedSfuSctpChannel.add(sfuSctpChannelSample);
        }

        @Override
        public String getSfuId() {
            return observedSfu.getSfuId();
        }

        @Override
        public String getSfuTransportId() {
            return transportId;
        }

        @Override
        public Long getMinTimestamp() {
            return observedSfu.getMinTimestamp();
        }

        @Override
        public Long getMaxTimestamp() {
            return observedSfu.getMaxTimestamp();
        }

        @Override
        public String getMarker() {
            return observedSfu.getMarker();
        }

        @Override
        public String getMediaUnitId() {
            return observedSfu.getMediaUnitId();
        }

        @Override
        public Iterable<ObservedSfuInboundRtpPad> observedSfuInboundRtpPads() {
            return () -> this.observedSfuInboundRtpPads.values().iterator();
        }

        @Override
        public Iterable<ObservedSfuOutboundRtpPad> observedSfuOutboundRtpPads() {
            return () -> this.observedSfuOutboundRtpPads.values().iterator();
        }

        @Override
        public Iterable<ObservedSfuSctpChannel> observedSfuSctpChannels() {
            return () -> this.observedSfuSctpChannels.values().iterator();
        }

        @Override
        public boolean getInternal() {
            return internal;
        }

        @NotNull
        @Override
        public Iterator<Samples.SfuSample.SfuTransport> iterator() {
            return sfuTransportSamples.iterator();
        }
    }
}
