package org.observertc.webrtc.observer.samples;

import java.util.*;
import java.util.stream.Stream;

/**
 * Collected SFU Samples from different SFus
 */
public class CollectedSfuSamples implements Iterable<SfuSamples>{

    private final Map<UUID, SfuSamples> samples = new HashMap<>();
    private final Set<UUID> transportIds = new HashSet<>();
    private final Set<UUID> rtpSinkIds = new HashSet<>();
    private final Set<UUID> rtpSourceIds = new HashSet<>();

    public static Builder builder() {
        return new Builder();
    }

    private CollectedSfuSamples() {

    }

    @Override
    public Iterator<SfuSamples> iterator() {
        return this.samples.values().iterator();
    }

    public Stream<SfuSamples> stream() {
        return this.samples.values().stream();
    }

    public Set<UUID> getSfuIds() {
        return this.samples.keySet();
    }

    public Set<UUID> getTransportIds() {
        return Collections.unmodifiableSet(this.transportIds);
    }

    public Set<UUID> getRtpSourceIds() {
        return Collections.unmodifiableSet(this.rtpSourceIds);
    }

    public Set<UUID> getRtpSinkIds() {
        return Collections.unmodifiableSet(this.rtpSourceIds);
    }

    public static class Builder {
        private final CollectedSfuSamples result = new CollectedSfuSamples();

        public CollectedSfuSamples.Builder withSfuSamples(SfuSamples sfuSamples) {
            this.result.samples.put(sfuSamples.getSfuId(), sfuSamples);
            sfuSamples.stream().flatMap(sample -> SfuSampleVisitor.streamTransports(sample.getSfuSample()))
                    .map(sfuTransport -> UUID.fromString(sfuTransport.transportId))
                    .forEach(this.result.transportIds::add);
            sfuSamples.stream().flatMap(sample -> SfuSampleVisitor.streamRtpSources(sample.getSfuSample()))
                    .map(rtpStream -> UUID.fromString(rtpStream.sourceId))
                    .forEach(this.result.rtpSourceIds::add);
            sfuSamples.stream().flatMap(sample -> SfuSampleVisitor.streamRtpSinks(sample.getSfuSample()))
                    .map(rtpStream -> UUID.fromString(rtpStream.sinkId))
                    .forEach(this.result.rtpSinkIds::add);
            return this;
        }

        public CollectedSfuSamples build() {
            return this.result;
        }
    }
}
