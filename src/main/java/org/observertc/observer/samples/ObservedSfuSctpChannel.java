package org.observertc.observer.samples;

import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public interface ObservedSfuSctpChannel extends Iterable<Samples.SfuSample.SfuSctpChannel> {

    String getSfuId();
    String getSfuTransportId();
    String getSfuSctpStreamId();
    String getSfuSctpChannelId();

    ObservedSfuTransport getObservedTransport();
    Long getMinTimestamp();
    Long getMaxTimestamp();
    String getMarker();

    class Builder implements ObservedSfuSctpChannel {

        private static final Logger logger = LoggerFactory.getLogger(Builder.class);

        private String sctpChannelId = null;
        final ObservedSfuSctpStream.Builder observedSfuSctpStream;
        private List<Samples.SfuSample.SfuSctpChannel> sfuSctpChannelSamples = new LinkedList<>();

        public Builder(ObservedSfuSctpStream.Builder observedSfuSctpStream, String sctpChannelId) {
            this.observedSfuSctpStream = observedSfuSctpStream;
            this.sctpChannelId = sctpChannelId;

            ObservedSfuSamples.Builder root = this.observedSfuSctpStream.observedSfuTransport.observedSfu.observedSfuSamples;
            root.sctpChannelIds.add(sctpChannelId);
        }

        public void addSctpChannelSample(Samples.SfuSample.SfuSctpChannel sfuSctpChannel) {
            this.sfuSctpChannelSamples.add(sfuSctpChannel);
        }

        @Override
        public String getSfuId() {
            return observedSfuSctpStream.getSfuId();
        }

        @Override
        public String getSfuTransportId() {
            return observedSfuSctpStream.getSfuTransportId();
        }

        @Override
        public String getSfuSctpStreamId() {
            return observedSfuSctpStream.getSfuSctpStreamId();
        }

        @Override
        public String getSfuSctpChannelId() {
            return sctpChannelId;
        }

        @Override
        public ObservedSfuTransport getObservedTransport() {
            return observedSfuSctpStream.getObservedTransport();
        }

        @Override
        public Long getMinTimestamp() {
            return observedSfuSctpStream.getMinTimestamp();
        }

        @Override
        public Long getMaxTimestamp() {
            return observedSfuSctpStream.getMaxTimestamp();
        }

        @Override
        public String getMarker() {
            return observedSfuSctpStream.getMarker();
        }

        @Override
        public Iterator<Samples.SfuSample.SfuSctpChannel> iterator() {
            return this.sfuSctpChannelSamples.iterator();
        }

    }
}
