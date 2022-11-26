package org.observertc.observer.samples;

import org.observertc.observer.common.FlatIterator;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public interface ObservedSfuSctpStream extends Iterable<Samples.SfuSample.SfuSctpChannel> {

    String getSfuId();
    String getSfuTransportId();
    String getSfuSctpStreamId();

    ObservedSfuTransport getObservedTransport();
    Long getMinTimestamp();
    Long getMaxTimestamp();
    String getMarker();

    class Builder implements ObservedSfuSctpStream {

        private static final Logger logger = LoggerFactory.getLogger(ObservedSfuSctpStream.Builder.class);

        private String sctpStreamId = null;
        final ObservedSfuTransport.Builder observedSfuTransport;
        private Map<String, ObservedSfuSctpChannel> observedSfuSctpChannels = new HashMap<>();

        public Builder(ObservedSfuTransport.Builder observedSfuTransport, String sctpStreamId) {
            this.observedSfuTransport = observedSfuTransport;
            this.sctpStreamId = sctpStreamId;

            ObservedSfuSamples.Builder root = this.observedSfuTransport.observedSfu.observedSfuSamples;
            root.sctpStreamIds.add(sctpStreamId);
        }

        public void addSctpChannel(Samples.SfuSample.SfuSctpChannel sfuSctpChannel) {
            if (sfuSctpChannel.channelId == null) {
                logger.warn("No channelId for sfu sctp stream {}, on transport {} at sfu {} mediaUnitId {}",
                        this.sctpStreamId,
                        this.observedSfuTransport.getSfuTransportId(),
                        this.observedSfuTransport.getSfuId(),
                        this.observedSfuTransport.getMediaUnitId()
                );
                return;
            }
            ObservedSfuSctpChannel.Builder observedSctpChannel = (ObservedSfuSctpChannel.Builder) this.observedSfuSctpChannels.get(sfuSctpChannel.channelId);
            if (observedSctpChannel == null) {
                observedSctpChannel = new ObservedSfuSctpChannel.Builder(this, sfuSctpChannel.channelId);
                this.observedSfuSctpChannels.put(sfuSctpChannel.channelId, observedSctpChannel);
            }
            observedSctpChannel.addSctpChannelSample(sfuSctpChannel);
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
            return sctpStreamId;
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
            return new FlatIterator<Samples.SfuSample.SfuSctpChannel>(
                    this.observedSfuSctpChannels.values().iterator(),
                    observedSfuSctpChannel -> observedSfuSctpChannel.iterator()
            );
        }

    }
}
