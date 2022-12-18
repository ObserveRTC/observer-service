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

        private String sfuSctpStreamId;
        private String sctpChannelId;
        final ObservedSfuTransport.Builder observedSfuTransport;
        private List<Samples.SfuSample.SfuSctpChannel> sfuSctpChannelSamples = new LinkedList<>();

        public Builder(ObservedSfuTransport.Builder observedSfuTransport, String sfuSctpStreamId, String sctpChannelId) {
            this.observedSfuTransport = observedSfuTransport;
            this.sctpChannelId = sctpChannelId;
            this.sfuSctpStreamId = sfuSctpStreamId;

            ObservedSfuSamples.Builder root = this.observedSfuTransport.observedSfu.observedSfuSamples;
            root.sctpStreamIds.add(sfuSctpStreamId);
            root.sctpChannelIds.add(sctpChannelId);
        }

        public void add(Samples.SfuSample.SfuSctpChannel sfuSctpChannel) {
            this.sfuSctpChannelSamples.add(sfuSctpChannel);
        }

        @Override
        public String getSfuId() {
            return observedSfuTransport.getSfuId();
        }

        @Override
        public String getSfuTransportId() {
            return observedSfuTransport.getSfuTransportId();
        }

        @Override
        public String getSfuSctpStreamId() {
            return sfuSctpStreamId;
        }

        @Override
        public String getSfuSctpChannelId() {
            return sctpChannelId;
        }

        @Override
        public ObservedSfuTransport getObservedTransport() {
            return observedSfuTransport;
        }

        @Override
        public Long getMinTimestamp() {
            return observedSfuTransport.getMinTimestamp();
        }

        @Override
        public Long getMaxTimestamp() {
            return observedSfuTransport.getMaxTimestamp();
        }

        @Override
        public String getMarker() {
            return observedSfuTransport.getMarker();
        }

        @Override
        public Iterator<Samples.SfuSample.SfuSctpChannel> iterator() {
            return this.sfuSctpChannelSamples.iterator();
        }

    }
}
