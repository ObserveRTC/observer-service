package org.observertc.observer.samples;

import org.observertc.observer.common.FlatIterator;
import org.observertc.observer.common.MinuteToTimeZoneOffsetConverter;
import org.observertc.schemas.samples.Samples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface ObservedSfuSamples extends Iterable<ObservedSfuSample> {

    static ObservedSfuSamples EMPTY_SAMPLES = ObservedSfuSamples.builder().build();

    static Builder builder() {
        return new Builder();
    }


    boolean isEmpty();
    int size();
    Stream<ObservedSfuSample> stream();
    Iterable<ObservedSfu> observedSfus();
    ObservedSfu getSfu(String sfuId);

    Set<String> getSfuIds();
    Set<String> getTransportIds();
    Set<String> getSfuStreamIds();
    Set<String> getSfuSinkIds();
    Set<String> getSctpStreamIds();
    Set<String> getSctpChannelIds();
    Set<String> getInboundRtpPadIds();
    Set<String> getOutboundRtpPadIds();


    class Builder {

        private static final Logger logger = LoggerFactory.getLogger(ObservedSfuSamples.class);

        Set<String> sfuIds = new HashSet<>();
        Set<String> streamIds = new HashSet<>();
        Set<String> sinkIds = new HashSet<>();
        Set<String> inboundRtpPadIds = new HashSet<>();
        Set<String> outboundRtpPadIds = new HashSet<>();
        Set<String> transportIds = new HashSet<>();
        Set<String> sctpStreamIds = new HashSet<>();
        Set<String> sctpChannelIds = new HashSet<>();
        final MinuteToTimeZoneOffsetConverter minuteToTimeZoneOffsetConverter = new MinuteToTimeZoneOffsetConverter();

        private Map<String, ObservedSfu> observedSfus = new HashMap<>();
        private int size = 0;

//        List<ObservedSfuSample> sfuSamples = new LinkedList<>();

        @Deprecated
        public Builder addObservedSfuSample(ObservedSfuSample value) {
            return this.add(value.getServiceId(), value.getMediaUnitId(), value.getSfuSample());
        }

        public Builder add(String serviceId, String mediaUnitId, Samples.SfuSample sfuSample) {
            if (sfuSample == null || sfuSample.sfuId == null) {
                logger.warn("SfuSample is null or does not have an sfuId. mediaUnit: {}, service: {}", serviceId, mediaUnitId);
                return this;
            }
            ObservedSfu.Builder observedSfu = (ObservedSfu.Builder) observedSfus.get(sfuSample.sfuId);
            if (observedSfu == null) {
                observedSfu = new ObservedSfu.Builder(this, sfuSample.sfuId, serviceId, mediaUnitId);
                observedSfus.put(sfuSample.sfuId, observedSfu);
            }
            observedSfu.add(sfuSample);
            ++this.size;
            return this;
        }

        public ObservedSfuSamples build() {
            return new ObservedSfuSamples() {

                @Override
                public Iterator<ObservedSfuSample> iterator() {
                    return new FlatIterator<ObservedSfuSample>(
                            observedSfus.values().iterator(),
                            observedSfu -> observedSfu.observedSfuSamples().iterator()
                    );
                }

                @Override
                public boolean isEmpty() {
                    return observedSfus.isEmpty();
                }

                @Override
                public int size() {
                    return size;
                }

                @Override
                public Stream<ObservedSfuSample> stream() {
                    return StreamSupport.stream(
                            Spliterators.spliteratorUnknownSize(this.iterator(), Spliterator.ORDERED),
                            false);
                }

                @Override
                public Iterable<ObservedSfu> observedSfus() {
                    return () -> observedSfus.values().iterator();
                }

                @Override
                public ObservedSfu getSfu(String sfuId) {
                    return observedSfus.get(sfuId);
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
                public Set<String> getSfuStreamIds() {
                    return streamIds;
                }

                @Override
                public Set<String> getSfuSinkIds() {
                    return sinkIds;
                }

                @Override
                public Set<String> getSctpStreamIds() {
                    return sctpStreamIds;
                }

                @Override
                public Set<String> getSctpChannelIds() { return sctpChannelIds; }
            };
        }

    }
}
