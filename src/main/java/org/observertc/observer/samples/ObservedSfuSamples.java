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

    Set<String> getSfuIds();

    Set<String> getInboundRtpPadIds();

    Set<String> getOutboundRtpPadIds();

    Set<String> getTransportIds();

    Set<String> getSctpStreamIds();

    class Builder {
        private Set<String> sfuIds = new HashSet<>();
        private Set<String> inboundRtpPadIds = new HashSet<>();
        private Set<String> outboundRtpPadIds = new HashSet<>();
        private Set<String> transportIds = new HashSet<>();
        private Set<String> sctpStreamIds = new HashSet<>();
        private List<ObservedSfuSample> sfuSamples = new LinkedList<>();

        public Builder addObservedSfuSample(ObservedSfuSample value) {
            var sfuSample = value.getSfuSample();
            SfuSampleVisitor.streamTransports(sfuSample)
                    .map(transport -> transport.transportId)
                    .forEach(transportIds::add);
            SfuSampleVisitor.streamInboundRtpPads(sfuSample)
                    .map(rtpPad -> rtpPad.padId)
                    .forEach(inboundRtpPadIds::add);
            SfuSampleVisitor.streamOutboundRtpPads(sfuSample)
                    .map(rtpPad -> rtpPad.padId)
                    .forEach(outboundRtpPadIds::add);
            SfuSampleVisitor.streamSctpStreams(sfuSample)
                    .map(channel -> channel.channelId)
                    .forEach(sctpStreamIds::add);
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
                public Set<String> getSfuIds() {
                    return sfuIds;
                }

                @Override
                public Set<String> getInboundRtpPadIds() {
                    return inboundRtpPadIds;
                }

                @Override
                public Set<String> getOutboundRtpPadIds() {
                    return outboundRtpPadIds;
                }

                @Override
                public Set<String> getTransportIds() {
                    return transportIds;
                }

                @Override
                public Set<String> getSctpStreamIds() {
                    return sctpStreamIds;
                }
            };
        }

    }
}
