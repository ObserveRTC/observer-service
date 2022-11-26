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

    Iterable<ObservedSfuStream> observedSfuStreams();
    Iterable<ObservedSfuSink> observedSfuSinks();
    Iterable<ObservedSfuSctpStream> observedSfuSctpStream();

    class Builder implements ObservedSfuTransport {
        private final String transportId;
        final ObservedSfu.Builder observedSfu;
        private Map<String, ObservedSfuStream> observedSfuStreams = new HashMap<>();
        private Map<String, ObservedSfuSink> observedSfuSinks = new HashMap<>();
        private Map<String, ObservedSfuSctpStream> observedSfuSctpStreams = new HashMap<>();

        private List<Samples.SfuSample.SfuTransport> sfuTransportSamples = new LinkedList<>();

        Builder(ObservedSfu.Builder observedSfu, String transportId) {
            this.observedSfu = observedSfu;
            this.transportId = transportId;
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
            ObservedSfuStream.Builder observedSfuStream = (ObservedSfuStream.Builder) this.observedSfuStreams.get(sfuInboundRtpPadSample.streamId);
            if (observedSfuStream == null) {
                observedSfuStream = new ObservedSfuStream.Builder(this, sfuInboundRtpPadSample.streamId);
                this.observedSfuStreams.put(observedSfuStream.getSfuStreamId(), observedSfuStream);
            }
            observedSfuStream.addInboundRtpPad(sfuInboundRtpPadSample);
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
            ObservedSfuSink.Builder observedSfuSink = (ObservedSfuSink.Builder) this.observedSfuSinks.get(sfuOutboundRtpPadSample.sinkId);
            if (observedSfuSink == null) {
                observedSfuSink = new ObservedSfuSink.Builder(this, sfuOutboundRtpPadSample.streamId, sfuOutboundRtpPadSample.sinkId);
                this.observedSfuSinks.put(observedSfuSink.getSfuSinkId(), observedSfuSink);
            }
            observedSfuSink.addOutboundRtpPad(sfuOutboundRtpPadSample);
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
            ObservedSfuSctpStream.Builder observedSfuSctpStream = (ObservedSfuSctpStream.Builder) this.observedSfuSctpStreams.get(sfuSctpChannelSample.streamId);
            if (observedSfuSctpStream == null) {
                observedSfuSctpStream = new ObservedSfuSctpStream.Builder(this, sfuSctpChannelSample.streamId);
                this.observedSfuSctpStreams.put(sfuSctpChannelSample.streamId, observedSfuSctpStream);
            }
            observedSfuSctpStream.addSctpChannel(sfuSctpChannelSample);
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
        public Iterable<ObservedSfuStream> observedSfuStreams() {
            return () -> observedSfuStreams.values().iterator();
        }

        @Override
        public Iterable<ObservedSfuSink> observedSfuSinks() {
            return () -> observedSfuSinks.values().iterator();
        }

        @Override
        public Iterable<ObservedSfuSctpStream> observedSfuSctpStream() {
            return () -> observedSfuSctpStreams.values().iterator();
        }

        @NotNull
        @Override
        public Iterator<Samples.SfuSample.SfuTransport> iterator() {
            return sfuTransportSamples.iterator();
        }
    }
}
