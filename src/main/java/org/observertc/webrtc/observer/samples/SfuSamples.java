package org.observertc.webrtc.observer.samples;

import java.util.*;
import java.util.stream.Stream;

/**
 * Samples belong to one specific SFU
 */
public class SfuSamples implements Iterable<ObservedSfuSample>{
    public static SfuSamples.Builder builderFrom(ObservedSfuSample observedSample) {
        var result = new SfuSamples.Builder()
                .withMetaInfo(observedSample);
        return result;
    }

    private ObservedSfuSample observedSfuSample;
    private List<ObservedSfuSample> samples = new LinkedList<>();

    public String getMediaUnitId() {
        return this.observedSfuSample.getMediaUnitId();
    }

    public String getTimeZoneId() {
        return this.observedSfuSample.getTimeZoneId();
    }

    public UUID getSfuId() {
        return this.observedSfuSample.getSfuId();
    }

    @Override
    public Iterator<ObservedSfuSample> iterator() {
        return this.samples.iterator();
    }

    public Stream<ObservedSfuSample> stream() {
        return this.samples.stream();
    }

    public static class Builder {
        private final SfuSamples result = new SfuSamples();

        public SfuSamples.Builder withObservedSfuSample(ObservedSfuSample sfuSample) {
            Objects.requireNonNull(sfuSample);
            this.result.samples.add(sfuSample);
            return this;
        }

        SfuSamples.Builder withMetaInfo(ObservedSfuSample observedSfuSample) {
            this.result.observedSfuSample = observedSfuSample;
            return this;
        }

        public SfuSamples build() {
            Objects.requireNonNull(this.result.observedSfuSample);
            return this.result;
        }
    }

}
