package org.observertc.observer.samples;

import java.util.*;
import java.util.stream.Stream;

public interface ObservedSfuSamples extends Iterable<ObservedSfuSample> {

    static Builder builder() {
        return new Builder();
    }

    boolean isEmpty();

    Stream<ObservedSfuSample> stream();

    int size();

    Set<UUID> getSfuIds();

    Set<UUID> getRtpPadIds();

    Set<UUID> getTransportIds();

    Set<UUID> getChannelIds();

    class Builder {
        private Set<UUID> sfuIds = new HashSet<>();
        private Set<UUID> rtpPadIds = new HashSet<>();
        private Set<UUID> transportIds = new HashSet<>();
        private Set<UUID> channelIds = new HashSet<>();
        private List<ObservedSfuSample> sfuSamples = new LinkedList<>();

        public Builder addObservedSfuSample(ObservedSfuSample value) {
            var sfuSample = value.getSfuSample();
            SfuSampleVisitor.streamTransports(sfuSample)
                    .map(transport -> transport.transportId)
                    .forEach(transportIds::add);
            SfuSampleVisitor.streamInboundRtpPads(sfuSample)
                    .map(rtpPad -> rtpPad.padId)
                    .forEach(rtpPadIds::add);
            SfuSampleVisitor.streamOutboundRtpPads(sfuSample)
                    .map(rtpPad -> rtpPad.padId)
                    .forEach(rtpPadIds::add);
            SfuSampleVisitor.streamSctpStreams(sfuSample)
                    .map(channel -> channel.channelId)
                    .forEach(channelIds::add);
            sfuIds.add(sfuSample.sfuId);
            sfuSamples.add(value);
            return this;
        }

        public ObservedSfuSamples build() {
            return new ObservedSfuSamples() {

                @Override
                public Iterator<ObservedSfuSample> iterator() {
                    return sfuSamples.iterator();
                }

                @Override
                public boolean isEmpty() {
                    return sfuSamples.isEmpty();
                }

                @Override
                public int size() {
                    return sfuSamples.size();
                }

                @Override
                public Stream<ObservedSfuSample> stream() {
                    return sfuSamples.stream();
                }

                @Override
                public Set<UUID> getSfuIds() {
                    return sfuIds;
                }

                @Override
                public Set<UUID> getRtpPadIds() {
                    return rtpPadIds;
                }

                @Override
                public Set<UUID> getTransportIds() {
                    return transportIds;
                }

                @Override
                public Set<UUID> getChannelIds() {
                    return channelIds;
                }
            };
        }

    }
}
